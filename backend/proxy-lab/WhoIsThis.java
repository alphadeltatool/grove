import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MyTransactional { }

class OrderService {

    public void placeOrder() {
        System.out.println("   placeOrder 안에서  this = " + this.getClass().getSimpleName());
        this.sendEmail();
    }

    @MyTransactional
    public void sendEmail() {
        System.out.println("   sendEmail  안에서  this = " + this.getClass().getSimpleName());
    }
}

class TxInterceptor implements MethodInterceptor {
    private Object target;
    public TxInterceptor(Object target) { this.target = target; }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
        Method realMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (!realMethod.isAnnotationPresent(MyTransactional.class)) {
            return method.invoke(target, args);
        }
        System.out.println("   >> 트랜잭션 시작");
        Object r = method.invoke(target, args);
        System.out.println("   >> 트랜잭션 커밋");
        return r;
    }
}

public class WhoIsThis {
    public static void main(String[] args) throws Exception {

        OrderService real = new OrderService();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(OrderService.class);
        enhancer.setCallback(new TxInterceptor(real));
        OrderService orderService = (OrderService) enhancer.create();

        System.out.println("내가 @Autowired로 들고 있는 객체 = " + orderService.getClass().getSimpleName());
        System.out.println();

        System.out.println("[A] orderService.sendEmail()");
        orderService.sendEmail();

        System.out.println();
        System.out.println("[B] orderService.placeOrder()");
        orderService.placeOrder();
    }
}
