# 프록시/AOP 복습 — 내가 실제로 막혔던 질문들 (Q&A)

> 2026-10-07 작성. 9/6·9/13 세션에서 **내가 직접 던졌던 질문**만 모아서, 그때 나온 답을 정리한 복습용 문서.
> 개념 요약은 `01-proxy-aop.md`, 이 문서는 "내가 어디서 막혔나" 기준.

---

## Part 1. 프록시가 왜 필요한가

### Q1. 왜 굳이 비서(프록시)를 거치게 만들어? 담당자가 직접 "누구세요" 물어보면 안 되나?

**내 답(정답이었음)**: "서비스는 서비스대로 하게 하고, 부가적인 일을 시키려고 프록시를 만드는 거 아냐?"

정확히 맞음. 정식 용어로 **관심사의 분리**.

프록시 없이 부가기능을 직접 넣으면:
```java
class OrderService {
    public void order() {
        System.out.println("로그 시작");  // 부가기능이 섞임
        // 진짜 주문 로직
        System.out.println("로그 끝");    // 부가기능이 섞임
    }
}
```
핵심 로직과 부가기능이 뒤섞임. 트랜잭션·권한체크까지 늘어나면 메서드마다 도배됨.

프록시를 쓰면:
```java
class OrderService {
    public void order() { /* 진짜 로직만, 깨끗함 */ }
}
class OrderServiceProxy {
    OrderService real;
    public void order() {
        System.out.println("로그 시작");
        real.order();              // 진짜 객체한테 넘김
        System.out.println("로그 끝");
    }
}
```

**이어진 문제**: 서비스가 100개면 프록시도 100개 손으로 만들어야 함 → 동적 프록시가 이걸 해결.

---

## Part 2. 동적 프록시 코드가 안 읽힘

### Q2. `Proxy.newProxyInstance(...)` 이게 도대체 뭘 하는 거야?

**한 줄**: "OrderService인 척하는 가짜 객체를 하나 만들어줘"라는 요청. 그 가짜 클래스를 **우리가 직접 안 만들어도** 자바가 실행 중에 자동 생성해줌.

넣는 재료 3가지:
```java
Proxy.newProxyInstance(
    클래스로더,                          // "어디 소속인지" 정보, 지금은 무시해도 됨
    new Class[]{OrderService.class},    // "무슨 인터페이스인 척할지"
    new LogHandler(new OrderServiceImpl())  // "호출 오면 누구한테 넘길지"
)
```

실행 결과로 **`OrderService` 타입인데 우리가 클래스로 작성한 적 없는 가짜 객체**가 리턴됨.

**증거**: `orderProxy.getClass().getName()`을 찍어보니 `$Proxy0`가 나옴 — `OrderServiceImpl`이 아님.

### Q3. `method`에 "order 메서드"라는 정보가 담겨서 들어온다는데, 이게 어떻게 가능해?

**답: 자바가 `$Proxy0`를 만들 때, 이미 `OrderService` 인터페이스를 다 들여다보고 만들었기 때문.**

`new Class[]{OrderService.class}`를 넘겼으니, 자바는 그 인터페이스 안에 `order()`, `cancel()`이 있다는 걸 이미 앎. 그래서 **메서드 하나하나마다 대응하는 코드를 자동으로 찍어냄**:

```java
// 우리가 안 쓴 코드. Proxy.newProxyInstance가 실행 중에 자동 생성한 것
class $Proxy0 implements OrderService {
    InvocationHandler handler;

    public void order() {
        Method m = OrderService.class.getMethod("order");  // "order다"를 미리 박아넣음
        handler.invoke(this, m, new Object[0]);
    }
    public void cancel() {
        Method m = OrderService.class.getMethod("cancel"); // "cancel이다"를 미리 박아넣음
        handler.invoke(this, m, new Object[0]);
    }
}
```

**마법처럼 알아내는 게 아니라, 인터페이스를 넘겨줬기 때문에 자바가 애초에 다 알고 만들었다**는 게 핵심.

### Q4. `orderProxy.order()`를 불렀는데 왜 `LogHandler.invoke()` 안으로 들어가?

위 `$Proxy0` 코드를 보면 답이 나옴 — `$Proxy0.order()`의 몸통이 **`handler.invoke(...)`를 부르는 것뿐**임. 그 연결이 자동 생성된 가짜 클래스 안에 이미 심어져 있음.

전체 흐름(전체 소스 → 위에서 아래로):
```java
public static void main(String[] args) {
    OrderService real = new OrderServiceImpl();          // ① 진짜 객체 생성

    OrderService orderProxy = (OrderService) Proxy.newProxyInstance(  // ② 가짜 객체 생성
        OrderService.class.getClassLoader(),
        new Class[]{OrderService.class},
        new LogHandler(real)
    );

    orderProxy.order();                                   // ③ 여기서 실행 시작
}
```
③에서 `orderProxy.order()` → 자동으로 `LogHandler.invoke()` 실행 → `"로그 시작"` 출력 → `method.invoke(target, args)`로 **진짜** `OrderServiceImpl.order()` 실행(`"진짜 주문 처리"` 출력) → 다시 `invoke()`로 복귀 → `"로그 끝"` 출력.

**실제 실행 결과**:
```
로그 시작
진짜 주문 처리
로그 끝
```

### Q5. 만약 `order()` 말고 다른 함수가 더 있으면? 서비스임플에?

**`LogHandler` 코드를 한 글자도 안 고쳐도 다 처리됨.** 인터페이스에 `cancel()`을 추가하고 실행해봤더니:
```
로그 시작
진짜 주문 처리     ← orderProxy.order()
로그 끝
---
로그 시작
진짜 취소 처리     ← orderProxy.cancel()
로그 끝
```

**왜 되냐면**: `invoke(proxy, method, args)`에서 `method`에 "지금 호출된 게 뭔지"를 자바가 매번 알려주니까, `method.invoke(target, args)` 한 줄이 그때그때 맞는 메서드를 실행해줌. 이게 **"서비스 100개면 프록시 100개?" 문제를 해결하는 방식**.

---

## Part 3. CGLIB

### Q6. 그냥 `OrderServiceImpl` 직접 쓰는 거랑 CGLIB 프록시 쓰는 거랑 차이가 뭐야?

```java
OrderServiceImpl real = new OrderServiceImpl();
real.order();     // → "진짜 주문 처리"만 실행. 로그도 트랜잭션도 없음.

OrderServiceImpl proxy = (CGLIB가 만든 자식 객체);  // 타입은 여전히 OrderServiceImpl!
proxy.order();    // → "로그 시작" → "진짜 주문 처리" → "로그 끝"
```

**`OrderServiceImpl` 코드는 한 글자도 안 바뀌었는데**, 어떤 객체를 호출하느냐(진짜 vs 프록시)에 따라 부가기능이 붙었다 안 붙었다 함.

실무에서 `@Transactional`이 정확히 이거임 — `@Autowired`로 받는 건 진짜 객체가 아니라 **프록시**라서, 코드에 트랜잭션을 한 줄도 안 썼는데 자동으로 걸림.

### Q7. 그럼 CGLIB가 훨씬 편한 방식 같은데? 왜 둘 다 있어?

**무조건 낫지는 않음.** CGLIB 제약:

1. **`final` 클래스는 아예 프록시 불가** (아래 Q8)
2. **`final` 메서드도 오버라이드 불가** → 그 메서드만 AOP가 안 걸림
3. **self-invocation 함정**은 JDK든 CGLIB든 똑같이 있음 (Part 4)

### Q8. `final`이면 왜 CGLIB가 못 만들어?

**자바 언어 규칙 자체**임. 직접 컴파일해서 확인함:
```java
final class OrderServiceImpl3 { ... }
class OrderServiceImpl3_ProxyAttempt extends OrderServiceImpl3 { }
```
```
error: cannot inherit from final OrderServiceImpl3
```

CGLIB가 프록시를 만드는 방법이 **"대상 클래스를 상속한 자식 클래스 만들기"**인데, `final`은 "상속 금지"라서 정면 충돌.

`final` 메서드도 같은 원리 — CGLIB는 "메서드를 오버라이드해서 앞뒤에 코드 끼워넣기"로 동작하는데, `final` 메서드는 오버라이드 자체가 금지라 손을 못 댐.

### Q9. 프록시 종류가 JDK/CGLIB인데, 둘의 차이는 결국 인터페이스를 구현했냐 안 했냐 차이지?

**맞음.**
- **인터페이스 있음** → JDK 동적 프록시 가능 (`implements`로 가짜 객체, 위임 방식)
- **인터페이스 없음** → CGLIB만 가능 (`extends`로 자식 클래스, 상속 방식)

**구조적 증거** (`instanceof`로 확인):
```
jdkProxy instanceof OrderServiceImpl        → false   (인터페이스만 구현, 클래스와 혈연관계 없음)
cglibStyleProxy instanceof OrderServiceImpl → true    (상속했으니 자손이 맞음)
```

**예시로 비교**:
```java
// ① 인터페이스 있는 경우
public interface OrderService { void order(); }
@Service
public class OrderServiceImpl implements OrderService { ... }

// ② 인터페이스 없는 경우
@Service
public class OrderService { ... }   // 단독 클래스
```

**단, 스프링부트 2.0+는 인터페이스가 있어도 기본값을 무조건 CGLIB로 통일**(`spring.aop.proxy-target-class=true`). 일관성 때문.

---

## Part 4. self-invocation (제일 많이 막혔던 부분)

### Q10. 아까는 `super.order()`로 했는데 왜 `target` 필드 방식으로 바꿨어?

**코치가 처음에 틀리게 짰고, 실행해보고 고쳤음.**

- 첫 예제(`super.order()`): "CGLIB는 상속으로 프록시를 만든다"는 **기본 구조만** 보여주려고 단순화한 버전. 근데 이 방식은 프록시와 진짜 로직이 **한 객체 안에** 있어서 self-invocation 버그가 재현이 안 됨.
- 수정본(`target` 필드): **실제 스프링은 원본 빈(target)을 따로 들고 있다가 호출하는 인터셉터 방식**이라, 이게 진짜 동작에 더 가까움. (JDK 방식과 원리가 사실상 같음)

### Q11. 객체가 2개 생긴다는데, 어떤 코드에서 2개가 생기는 거야?

**`main()`의 딱 한 줄이 둘 다 만듦:**
```java
OrderServiceImpl2 proxy = new OrderServiceImpl2_CglibStyle();
```

1. `new OrderServiceImpl2_CglibStyle()` → **객체 A(프록시)** 생성 시작
2. 객체를 만들 때 **필드 초기화 코드도 자동으로 같이 실행**됨:
```java
class OrderServiceImpl2_CglibStyle extends OrderServiceImpl2 {
    private OrderServiceImpl2 target = new OrderServiceImpl2();  // ← 이 줄이 객체 B 생성
}
```

`main()` 어디에도 "객체 B를 만들어라"가 없는데, 클래스 정의 안의 필드 초기화가 자동 실행되면서 딸려 나옴.

```
[객체 A — 프록시]                    [객체 B — target]
타입: CglibStyle                    타입: OrderServiceImpl2
target 필드 ─────────────────────→  (객체 B를 가리킴)
```

### Q12. 그냥 소스에서 `this.`를 안 쓰면 되는 거 아냐?

**안 됨.** `this.` 지우고 그냥 `notify_()`로만 써서 직접 실행해봤는데 **결과가 완전히 똑같았음.**

**이유**: `notify_()`라고만 써도 자바 컴파일러가 뒤에서 몰래 `this.notify_()`로 해석함. **문법만 다르고 의미는 100% 동일.**

진짜 문제는 "this를 썼냐"가 아니라 **"같은 클래스 안에서 자기 메서드를 부르면 프록시를 거칠 방법이 원천적으로 없다"**는 구조 자체.

### Q13. 그럼 자기 클래스의 함수를 호출하면 안 된다는 거야?

**아니요, 그렇게 일반화하면 안 됨.** 자기 클래스 메서드 호출은 지극히 정상적인 일.

문제는 **딱 한 가지 좁은 경우**:
```java
public void order() {
    calculateTotal();              // ← AOP 없는 평범한 메서드, self 호출 아무 문제 없음
    this.sendConfirmationEmail();  // ← @Transactional 붙어있어서 문제됨
}
```

**"이 메서드는 프록시(부가기능)를 반드시 거쳐야 의미가 있는데, self 호출이라 프록시를 못 거친다"는 경우만** 조심. 즉 `@Transactional`, `@Cacheable`, `@Async` 같은 걸 붙인 메서드를 같은 클래스 안에서 부를 때.

### Q14. `@Transactional public void notify_()` — **어노테이션 여기 있잖아!** 그런데 왜 안 먹어?

**이게 가장 여러 번 되물었던 질문.** 답은:

**어노테이션은 "그냥 붙어있는 꼬리표(메타데이터)"일 뿐, 그 자체로는 아무 동작도 안 함.**

직접 증명한 코드:
```java
@Retention(RetentionPolicy.RUNTIME)
@interface MyTransactional { }   // @Transactional을 흉내낸 커스텀 어노테이션

class OrderServiceImpl3 {
    @MyTransactional             // ← 메서드 바로 위에 직접 붙임
    public void notify_() {
        System.out.println("알림 발송");
        throw new RuntimeException("일부러 에러 발생!");   // 롤백돼야 할 상황
    }
}
// main에서 real.notify_() 직접 호출
```
**실행 결과**:
```
알림 발송
예외 잡음: 일부러 에러 발생!
프로그램은 여기까지 정상적으로 옴 — 롤백이든 뭐든 자동으로 된 거 아무것도 없음
```

**어노테이션이 거기 붙어있어도 아무 일도 안 일어남.** `javac`가 어노테이션을 보고 메서드 안에 코드를 끼워넣어주는 게 아니기 때문.

**그래서 결론**:
```java
// real 객체의 notify_() — 이 안엔 트랜잭션 코드가 물리적으로 한 줄도 없음
class OrderServiceImpl {
    @Transactional
    public void notify_() {
        System.out.println("알림 발송");   // 딱 이거뿐
    }
}

// 프록시의 notify_() — 트랜잭션 여는 코드는 오직 여기 있음
class 프록시 {
    public void notify_() {
        트랜잭션시작();
        real.notify_();
        트랜잭션커밋();
    }
}
```
`this.notify_()`로 부르면 **위쪽(트랜잭션 코드 없는 버전)**이 실행됨. **"스코프 때문에 못 탄다"가 아니라 "real 안에는 탈 코드 자체가 없다"**가 핵심.

### Q15. 그럼 애초에 스프링이 `@Transactional` 있으면 `OrderService` 전체를 프록시로 묶는 거 아냐?

**맞음.** 근데 "묶는다"는 게 **"원래 객체 바깥에 별도 객체(프록시)를 하나 더 만들어서 컨테이너에 등록한다"**는 뜻이지, **"원래 객체 자체가 변한다"**는 뜻이 아님.

```
                  ┌──────────────────┐
  외부 방문자       │  프록시(정문)       │
  (컨트롤러 등)   → │  "여기 통과할 때     │
                  │   트랜잭션 체크"     │
                  └────────┬─────────┘
                           │ (안으로 들어감)
                  ┌────────▼─────────┐
                  │  real (건물 내부)   │
                  │  order() 방       │
                  │  notify_() 방     │
                  └──────────────────┘
```
- **바깥에서 오는 사람**은 무조건 정문(프록시)을 거침
- **건물 안에서 `order()` 방 → `notify_()` 방 이동**은? 이미 안에 있으니 **정문으로 나갔다 들어올 필요가 없음** ← 이게 `this.notify_()`

**"전체가 프록시로 묶였다" = "바깥에서 접근하는 유일한 통로가 프록시다"**라는 뜻이지, "내부 호출도 전부 프록시를 거친다"가 아님.

### Q16. 클래스 전체에 `@Transactional`이 붙어있으면 어떻게 돼?

**여전히 해결 안 됨.** 클래스 레벨은 "모든 public 메서드에 일일이 붙이는 걸 한 줄로 줄여쓴 것"일 뿐, 메커니즘은 동일.

(참고: 바깥에서 부른 `placeOrder()`도 트랜잭션이 걸려있으면, 그 안에서 self 호출된 메서드도 **이미 열린 그 트랜잭션에 얹혀가서** 롤백은 "우연히" 되기도 함. 단 `REQUIRES_NEW` 같은 그 메서드만의 별도 설정은 무시됨.)

---

## Part 5. 스프링이 프록시를 꽂는 시점

### Q17. 왜 `orderService.placeOrder()`가 프록시로 가?

**컨테이너가 애초에 진짜 객체 대신 프록시를 등록해뒀기 때문.**

```
① @Service 발견 → ② 진짜 객체 생성 → ③ 후처리기가 "@Transactional 있나?" 검사
  → 있으면 ④ 프록시로 감싸고 ⑤ 컨테이너엔 "orderService"라는 이름으로 프록시를 등록
     (진짜 객체는 이름표 없이 프록시 안에 숨음)
```

그래서 컨트롤러가 `@Autowired OrderService orderService`로 받으면 **처음부터 프록시**를 받음. 특별히 뭘 한 게 아니라 스프링이 등록 단계에서 바꿔치기해둔 것.

### Q18. 컨트롤러에서 `@Autowired`로 부르면 **무조건** 프록시를 만들어?

**아니요.** `@Transactional`, `@Cacheable`, `@Async`, 커스텀 `@Aspect` 같은 **AOP가 필요한 게 붙어있을 때만** 프록시로 감쌈.

```java
@Service
public class SimpleService {       // 아무것도 안 붙음
    public void doSomething() { }
}
// → @Autowired로 받아도 진짜 SimpleService 객체. 프록시 아님.
```
이유: 필요 없는 빈까지 다 감싸면 리플렉션 호출 비용·객체 생성 오버헤드만 늘어남.

### Q19. 바깥에서 `orderService.notify_()`를 부르면 **왜 실제로** 트랜잭션이 걸려?

**프록시의 메서드 안에 트랜잭션 코드가 물리적으로 쓰여있기 때문.** 직접 만들어서 증명한 코드:

```java
class TransactionHandler implements InvocationHandler {
    private Object target;
    public TransactionHandler(Object target) { this.target = target; }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(">> 트랜잭션 시작 (DB 커넥션 얻고 begin)");
        try {
            Object result = method.invoke(target, args);   // 진짜 비즈니스 로직 실행
            System.out.println(">> 트랜잭션 커밋");
            return result;
        } catch (Exception e) {
            System.out.println(">> 예외 발생, 트랜잭션 롤백!");
            throw e;
        }
    }
}
```
**실행 결과** (진짜 메서드가 예외를 던지게 해둠):
```
>> 트랜잭션 시작 (DB 커넥션 얻고 begin)
알림 발송
>> 예외 발생, 트랜잭션 롤백!
```

`orderService.notify_()` 한 줄을 불렀을 뿐인데, 실제로 실행된 건 `invoke()`였고 그 안에 **"시작 → 실행 → 성공하면 커밋 / 실패하면 롤백"이 전부 쓰여있어서** 저 출력이 나온 것.

실제 스프링에서는 이 역할을 **`TransactionInterceptor`**가 함 (`PlatformTransactionManager`로 진짜 DB 트랜잭션 제어). 구조는 위 코드와 동일.

---

## 부록: 헷갈렸던 것

- **`notify_()`는 스레드의 `Object.notify()`와 전혀 무관.** 예제에서 "주문 후 알림 발송"이라는 비즈니스 메서드로 임의로 지은 이름. (자바 내장 `notify()`와 겹치지 않으려고 언더스코어를 붙인 것)
- **`this.메서드()`와 `메서드()`는 완전히 동일.** 자바가 자동으로 `this.`를 채워넣음.

---

## 한 줄 요약 5개

1. 프록시 = 핵심 로직과 부가기능(로그/트랜잭션)을 분리하려고 앞에 세우는 대리인
2. JDK 동적 프록시 = 인터페이스 기반(`implements`, 위임) / CGLIB = 클래스 상속 기반(`extends`), `final`엔 못 씀
3. 어노테이션은 꼬리표일 뿐 — 그걸 읽고 실제로 동작하는 코드는 **프록시 안에만** 있음
4. 바깥에서 부르면 프록시를 거쳐서 걸리고, `this.`로 자기 자신을 부르면 프록시를 건너뛰어서 안 걸림
5. 스프링은 AOP가 필요한 빈만 골라서, 등록 시점에 진짜 객체 대신 프록시를 컨테이너에 넣어둠
