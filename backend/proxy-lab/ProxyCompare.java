import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

// ══════════════════════════════════════════════════════════════
// [A] 인터페이스가 있는 경우 → JDK 동적 프록시 가능
// ══════════════════════════════════════════════════════════════
interface OrderService {
    void order();
}

class OrderServiceImpl implements OrderService {
    public void order() { System.out.println("   진짜 주문 처리"); }
}

// JDK 동적 프록시용 핸들러
class JdkLogHandler implements InvocationHandler {
    private Object target;
    public JdkLogHandler(Object target) { this.target = target; }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("   [JDK] 로그 시작");
        Object result = method.invoke(target, args);
        System.out.println("   [JDK] 로그 끝");
        return result;
    }
}

// ══════════════════════════════════════════════════════════════
// [B] 인터페이스가 없는 경우 → CGLIB만 가능
// ══════════════════════════════════════════════════════════════
class PaymentService {                    // 인터페이스 없음!
    public void pay() { System.out.println("   진짜 결제 처리"); }
}

// CGLIB용 인터셉터 (JDK의 InvocationHandler에 해당)
class CglibLogInterceptor implements MethodInterceptor {
    private Object target;
    public CglibLogInterceptor(Object target) { this.target = target; }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
        System.out.println("   [CGLIB] 로그 시작");
        Object result = method.invoke(target, args);
        System.out.println("   [CGLIB] 로그 끝");
        return result;
    }
}

public class ProxyCompare {
    public static void main(String[] args) {

        System.out.println("=== [A] JDK 동적 프록시 (인터페이스 필요) ===");
        OrderService realOrder = new OrderServiceImpl();
        OrderService jdkProxy = (OrderService) Proxy.newProxyInstance(
            OrderService.class.getClassLoader(),
            new Class[]{OrderService.class},
            new JdkLogHandler(realOrder)
        );
        jdkProxy.order();
        System.out.println("   생성된 클래스: " + jdkProxy.getClass().getName());
        System.out.println("   OrderServiceImpl의 자손인가? " + (jdkProxy instanceof OrderServiceImpl));

        System.out.println();
        System.out.println("=== [B] CGLIB (인터페이스 없어도 됨) ===");
        PaymentService realPay = new PaymentService();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(PaymentService.class);
        enhancer.setCallback(new CglibLogInterceptor(realPay));
        PaymentService cglibProxy = (PaymentService) enhancer.create();
        cglibProxy.pay();
        System.out.println("   생성된 클래스: " + cglibProxy.getClass().getName());
        System.out.println("   PaymentService의 자손인가? " + (cglibProxy instanceof PaymentService));

        System.out.println();
        System.out.println("=== [C] 인터페이스 없는 클래스에 JDK 프록시를 시도하면? ===");
        try {
            Proxy.newProxyInstance(
                PaymentService.class.getClassLoader(),
                new Class[]{PaymentService.class},
                new JdkLogHandler(realPay)
            );
        } catch (IllegalArgumentException e) {
            System.out.println("   실패: " + e.getMessage());
        }
    }
}
