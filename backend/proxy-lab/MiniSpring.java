import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

// ══════════════════════════════════════════════════════════════
// ① 어노테이션 — 스프링의 @Transactional을 흉내낸 것
//    (그냥 꼬리표일 뿐, 자체로는 아무 동작도 안 함)
// ══════════════════════════════════════════════════════════════
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MyTransactional { }

// ══════════════════════════════════════════════════════════════
// ② 서비스 2개 — 스프링의 @Service 빈을 흉내낸 것
// ══════════════════════════════════════════════════════════════
class OrderService {
    public void placeOrder() {
        System.out.println("      주문 저장");
        this.sendEmail();                 // ★ self-invocation (자기 자신 호출)
    }

    @MyTransactional                      // ★ 트랜잭션이 걸려야 하는 메서드
    public void sendEmail() {
        System.out.println("      이메일 발송");
    }
}

class SimpleService {                     // 어노테이션이 하나도 없는 서비스
    public void doSomething() {
        System.out.println("      그냥 일함");
    }
}

// ══════════════════════════════════════════════════════════════
// ③ 트랜잭션 인터셉터 — 스프링의 TransactionInterceptor 흉내
// ══════════════════════════════════════════════════════════════
class TxInterceptor implements MethodInterceptor {
    private Object target;
    public TxInterceptor(Object target) { this.target = target; }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
        Method realMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (!realMethod.isAnnotationPresent(MyTransactional.class)) {
            return method.invoke(target, args);          // 어노테이션 없으면 그냥 통과
        }

        System.out.println("   >> 트랜잭션 시작");
        Object result = method.invoke(target, args);
        System.out.println("   >> 트랜잭션 커밋");
        return result;
    }
}

// ══════════════════════════════════════════════════════════════
// ④ 미니 컨테이너 — 스프링이 빈 등록할 때 하는 일 흉내
// ══════════════════════════════════════════════════════════════
class MiniContainer {
    static Object createBean(Class<?> clazz) throws Exception {
        Object real = clazz.getDeclaredConstructor().newInstance();      // 진짜 객체 생성

        boolean needsProxy = Arrays.stream(clazz.getDeclaredMethods())   // @MyTransactional 있나 검사
                .anyMatch(m -> m.isAnnotationPresent(MyTransactional.class));

        if (!needsProxy) {
            System.out.println("[컨테이너] " + clazz.getSimpleName()
                    + " → 어노테이션 없음. 진짜 객체 그대로 등록");
            return real;
        }

        System.out.println("[컨테이너] " + clazz.getSimpleName()
                + " → @MyTransactional 발견! 프록시로 감싸서 등록");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new TxInterceptor(real));
        return enhancer.create();
    }
}

// ══════════════════════════════════════════════════════════════
// ⑤ 실행
// ══════════════════════════════════════════════════════════════
public class MiniSpring {
    public static void main(String[] args) throws Exception {

        System.out.println("────── 컨테이너 기동 ──────");
        OrderService orderService   = (OrderService)  MiniContainer.createBean(OrderService.class);
        SimpleService simpleService = (SimpleService) MiniContainer.createBean(SimpleService.class);

        System.out.println();
        System.out.println("   orderService  실제 클래스: " + orderService.getClass().getSimpleName());
        System.out.println("   simpleService 실제 클래스: " + simpleService.getClass().getSimpleName());

        System.out.println();
        System.out.println("────── [A] 바깥에서 sendEmail() 직접 호출 ──────");
        orderService.sendEmail();

        System.out.println();
        System.out.println("────── [B] 바깥에서 placeOrder() 호출 (안에서 this.sendEmail()) ──────");
        orderService.placeOrder();
    }
}
