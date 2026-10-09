import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MyTransactional { }

// ───── 서비스 2개 ─────
class OrderService {
    public void placeOrder() {
        System.out.println("      주문 저장");
        this.sendEmail();
    }
    @MyTransactional
    public void sendEmail() {
        System.out.println("      이메일 발송");
    }
}

class SimpleService {
    public void doSomething() { System.out.println("      그냥 일함"); }
}

// ───── 인터셉터 ─────
class TxInterceptor implements MethodInterceptor {
    private Object target;
    public TxInterceptor(Object target) { this.target = target; }

    public Object intercept(Object obj, Method m, Object[] args, MethodProxy mp) throws Throwable {
        Method real = target.getClass().getMethod(m.getName(), m.getParameterTypes());
        if (!real.isAnnotationPresent(MyTransactional.class)) return m.invoke(target, args);

        System.out.println("   >> 트랜잭션 시작");
        Object r = m.invoke(target, args);
        System.out.println("   >> 트랜잭션 커밋");
        return r;
    }
}

// ───── ★ 빈 후처리기 ─────
class ProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        boolean needsProxy = Arrays.stream(bean.getClass().getDeclaredMethods())
                .anyMatch(m -> m.isAnnotationPresent(MyTransactional.class));

        if (!needsProxy) {
            System.out.println("[후처리기] " + beanName + " → 그대로 통과");
            return bean;
        }

        System.out.println("[후처리기] " + beanName + " → 프록시로 바꿔치기!");
        Enhancer e = new Enhancer();
        e.setSuperclass(bean.getClass());
        e.setCallback(new TxInterceptor(bean));
        return e.create();
    }
}

@Configuration
class AppConfig {
    @Bean public OrderService  orderService()  { return new OrderService();  }
    @Bean public SimpleService simpleService() { return new SimpleService(); }
    @Bean public ProxyBeanPostProcessor bpp()  { return new ProxyBeanPostProcessor(); }
}

public class RealSpringBpp {
    public static void main(String[] args) {

        System.out.println("────── 스프링 컨테이너 기동 ──────");
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(AppConfig.class);

        OrderService  orderService  = ctx.getBean(OrderService.class);
        SimpleService simpleService = ctx.getBean(SimpleService.class);

        System.out.println();
        System.out.println("getBean 결과 orderService  = " + orderService.getClass().getSimpleName());
        System.out.println("getBean 결과 simpleService = " + simpleService.getClass().getSimpleName());

        System.out.println();
        System.out.println("[A] 바깥에서 sendEmail()");
        orderService.sendEmail();

        System.out.println();
        System.out.println("[B] 바깥에서 placeOrder()  (안에서 this.sendEmail())");
        orderService.placeOrder();

        ctx.close();
    }
}
