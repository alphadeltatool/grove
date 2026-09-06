# 스프링 핵심원리 고급편 — 프록시 / CGLIB / self-invocation

> 2026-09-06(토) 학습. 강의(79/125강까지 본 상태, 프록시/AOP 잘 기억 안 남)를 다시 안 보고, 코치와 직접 개념+코드로 재구성.
> 서브젝트: "`@Transactional` 하나 붙이면 왜 트랜잭션이 알아서 걸리는가?"

## 큰 그림
```
ThreadLocal → 템플릿메서드/콜백 → 프록시 패턴 → 동적 프록시 → 스프링 프록시 지원 → 빈 후처리기 → AOP(@Aspect)
```
전부 다 "AOP(`@Transactional`의 실체)"를 이해하기 위한 준비 단계. 오늘은 **프록시 패턴 → 동적 프록시 → CGLIB → self-invocation**까지 진행.

## 프록시 패턴
"진짜 객체" 대신 앞에 서는 대리인. 핵심 로직(비즈니스)과 부가기능(로깅/트랜잭션/권한체크)을 분리하는 게 목적 — 관심사의 분리.
```java
class OrderServiceImpl { void order() { /* 진짜 로직만 */ } }
class OrderServiceProxy { OrderService real; void order() { 로그(); real.order(); 로그(); } }
```
손으로 프록시를 매번 만들면 서비스 개수 × 부가기능 개수만큼 클래스가 폭발 → 동적 프록시로 해결.

## JDK 동적 프록시
`java.lang.reflect.Proxy.newProxyInstance(classLoader, 인터페이스[], InvocationHandler)`로 인터페이스 기반 가짜 객체를 런타임에 자동 생성.
```java
class LogHandler implements InvocationHandler {
    Object target;
    public Object invoke(Object proxy, Method method, Object[] args) {
        로그시작();
        Object result = method.invoke(target, args);  // 리플렉션으로 진짜 메서드 실행
        로그끝();
        return result;
    }
}
```
- `method`/`args`는 자바가 자동으로 채워줌 — 인터페이스에 어떤 메서드가 있는지 이미 알고 있어서, 메서드마다 대응하는 코드를 자동 생성해줌(`$Proxy0` 내부를 pseudocode로 열어봄).
- **제약**: 인터페이스가 없으면 `Proxy.newProxyInstance`는 `IllegalArgumentException("~ is not an interface")`로 즉시 실패 — 직접 컴파일해서 확인함.

## CGLIB
인터페이스 없이 **대상 클래스를 상속(extends)**해서 자식 클래스를 만들고, 메서드를 오버라이드해서 가로챔.
```java
class OrderServiceImpl$$CGLIB$$ extends OrderServiceImpl {
    // 스프링 실제 구현은 super.xxx()가 아니라 별도 target을 들고 위임하는 방식(JDK와 원리 동일)
    OrderServiceImpl target = ...;
    public void order() { 로그(); target.order(); 로그(); }
}
```
- **`final` 클래스/메서드엔 못 씀** — `final`은 자바 언어 규칙상 "상속 금지"라서, CGLIB의 "상속해서 흉내내기" 자체가 원천 차단됨(`cannot inherit from final X` 컴파일 에러로 직접 확인).
- JDK vs CGLIB 차이의 본질: `implements`(완전 남남, 위임) vs `extends`(자식, 상속받아 흡수). `instanceof OrderServiceImpl`로 확인하면 JDK프록시는 `false`, CGLIB식은 `true`.

## self-invocation 함정 (제일 중요한 실무 포인트)
같은 클래스 안에서 `this.메서드()`(또는 그냥 `메서드()` — `this.`는 자바가 자동으로 붙여주는 것이라 생략해도 완전히 동일)로 자기 자신의 다른 메서드를 부르면, **프록시를 건너뛰고 진짜 객체끼리 직접 호출**돼서 AOP(`@Transactional` 등)가 전혀 안 걸림.

```java
@Service
public class OrderService {
    public void placeOrder() {
        this.sendConfirmationEmail();  // ← 프록시 건너뜀
    }
    @Transactional
    public void sendConfirmationEmail() { ... }  // 여기 트랜잭션 안 걸림
}
```
- 컴파일/런타임 에러 없이 **조용히** 부가기능만 빠짐 — 실무에서 제일 놓치기 쉬운 버그.
- **해결법**: `sendConfirmationEmail()`을 별도 빈(다른 클래스)으로 분리해서 주입받아 호출 — 다른 빈 호출은 항상 프록시를 거침.
- **클래스 레벨 `@Transactional`을 붙여도 이 문제는 그대로**(메서드 단위 vs 클래스 단위는 애노테이션 편의 문법 차이일 뿐, 메커니즘은 동일). 단, 바깥 호출(`placeOrder()`) 자체도 트랜잭션이 걸려있으면 self-invocation된 메서드도 그 트랜잭션에 얹혀가서 "우연히" 롤백은 되지만, `REQUIRES_NEW` 같은 그 메서드만의 별도 설정은 무시됨.

## 스프링이 프록시를 만드는 시점 (빈 등록 흐름)
```
① @Service 발견 → ② 진짜 객체 생성 → ③ 후처리기가 "@Transactional 있나?" 검사
  → 있으면 ④ 프록시로 감싸서 ⑤ 컨테이너에 "그 이름"으로는 프록시를 등록 (진짜 객체는 이름 없이 프록시 안에 숨음)
  → 없으면 진짜 객체 그대로 등록 (프록시 안 만듦 — 불필요한 오버헤드 방지)
```
`@Autowired`로 받는 건 항상 컨테이너에 등록된 것(AOP 필요하면 프록시, 아니면 진짜) — self-invocation이 문제되는 이유는 "진짜 객체 자신은 프록시로 바꿔치기 당한 걸 모른다"는 것.

## JDK vs CGLIB 선택 기준
- 순수 스프링: 인터페이스 있으면 JDK, 없으면 CGLIB (자동 선택)
- **스프링부트 2.0+**: 인터페이스 있어도 기본값을 무조건 CGLIB로 통일(`spring.aop.proxy-target-class=true`가 기본) — 일관성 때문. 실무에서 잘 안 챙기는 디테일.

## 다음 세션 시작점
빈 후처리기(BeanPostProcessor)가 ③④⑤ 단계를 정확히 어떻게 구현하는지 → `@Aspect`/포인트컷으로 실제 AOP 어노테이션 작성까지.
