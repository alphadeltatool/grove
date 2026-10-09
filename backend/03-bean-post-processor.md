# 빈 후처리기 (BeanPostProcessor)

> 2026-10-09 학습. 실행 코드는 `backend/proxy-lab/` 에 전부 있음 (진짜 스프링 컨테이너로 돌림).

## 한 문장

스프링이 빈을 만든 직후, 컨테이너에 넣기 **직전에** "이거 어떻게 할래?" 하고 물어보는 지점.
거기서 **다른 객체를 돌려주면 그게 대신 등록**된다.

```java
public Object postProcessAfterInitialization(Object bean, String beanName) {
    //                                        ↑ 스프링이 만든 진짜 빈을 건네줌

    return bean;        // 그대로 돌려주면 → 진짜 빈이 등록됨
    return 다른객체;     // 다른 걸 돌려주면 → 그게 등록됨   ★ 바꿔치기 지점
}
```

**이 메서드 하나가 빈 후처리기의 전부.** `@Transactional`이 "알아서 걸리는" 마법의 정체가 여기.

## 실행으로 확인한 것 (`proxy-lab/RealSpringBpp.java`)

진짜 스프링 컨테이너(`AnnotationConfigApplicationContext`)에 후처리기를 등록하고 돌린 결과:

```
[후처리기] orderService  → 프록시로 바꿔치기!     ← @MyTransactional 있음
[후처리기] simpleService → 그대로 통과           ← 없음

getBean 결과 orderService  = OrderService$$EnhancerByCGLIB$$6011210e
getBean 결과 simpleService = SimpleService

[A] 바깥에서 sendEmail()
   >> 트랜잭션 시작
      이메일 발송
   >> 트랜잭션 커밋

[B] placeOrder() (안에서 this.sendEmail())
      주문 저장
      이메일 발송                                ← 트랜잭션 없음 (self-invocation)
```

- **필요한 빈만** 프록시로 바뀜 (어노테이션 없는 `simpleService`는 원본 그대로)
- 클래스 이름이 바뀐 게 바꿔치기의 증거

## proxy-lab 파일들

| 파일 | 뭘 증명하는가 |
|---|---|
| `ProxyCompare.java` | JDK 동적 프록시 vs **진짜 CGLIB** 비교. `instanceof` 결과가 false/true로 갈림. 인터페이스 없는 클래스에 JDK 프록시 시도 → `is not an interface` 예외 |
| `WhoIsThis.java` | **self-invocation의 결정적 증거.** 바깥에서 들고 있는 객체는 `OrderService$$EnhancerByCGLIB$$...`인데, 메서드 **안의 `this`는 그냥 `OrderService`** — 이름이 다름 = 다른 객체. 그래서 `this.`로는 프록시에 영원히 닿을 수 없음 |
| `MiniSpring.java` | 스프링 없이 직접 만든 미니 컨테이너 (어노테이션 스캔 → 프록시 바꿔치기) |
| `RealSpringBpp.java` | 진짜 스프링 컨테이너 + 진짜 `BeanPostProcessor` |

**실행 방법** (spring jar는 로컬 .m2에 있음):
```bash
cd backend/proxy-lab
M="C:/Users/ys.nam/.m2/repository/org/springframework"
CP="$M/spring-core/6.2.3/spring-core-6.2.3.jar;$M/spring-beans/6.2.3/spring-beans-6.2.3.jar;$M/spring-context/6.2.3/spring-context-6.2.3.jar;$M/spring-aop/6.2.3/spring-aop-6.2.3.jar;$M/spring-jcl/6.2.3/spring-jcl-6.2.3.jar;$M/spring-expression/6.2.3/spring-expression-6.2.3.jar"
javac -encoding UTF-8 -cp "$CP" RealSpringBpp.java
java -cp "$CP;." RealSpringBpp
```

## 어노테이션마다 "읽어주는 놈"이 다르다

```
@Transactional, @Cacheable, @Async   →  프록시가 읽음      ★ 프록시 안 거치면 무시됨
@Entity, @Column                     →  하이버네이트가 읽음  (self-invocation 무관)
@Autowired                           →  컨테이너가 읽음     (무관)
@Override                            →  컴파일러가 읽음     (무관)
@Getter (롬복)                        →  컴파일러가 코드 생성 (무관)
```

self-invocation이 문제되는 건 **위 첫 줄(AOP 계열)뿐**. 나머지는 상관없음.

(예외: AspectJ 위빙은 프록시가 아니라 바이트코드에 직접 심어서 self-invocation도 잡힘. 설정이 복잡해 실무에서 잘 안 씀.)

## 다음

1. Advice / Pointcut / Advisor — 용어 3개 (`@Aspect` 배우기 전 필수 어휘)
2. `@Aspect` 실전 AOP
