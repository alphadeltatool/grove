package queue;

public class MyQueue {

    // TODO Step 1: 필드 채우기
    // 힌트: 배열 하나 + front(꺼낼 위치) + rear(넣을 위치) + 현재 개수(꽉 찼는지 판단용)
    private Object[] objects;
    private int front;
    private int real;
    private int count;

    public MyQueue() {
        // TODO: 배열 초기화 (크기는 자유, 예: 5)
        objects = new Object[5];
    }

    // TODO Step 2: enqueue (넣기)
    // rear 자리에 넣고, rear를 (rear + 1) % capacity 로 이동. 꽉 찼을 때 처리도 생각해보기
    public void enqueue(Object value) {
        if (count == objects.length) {
            System.out.println("큐가꽉찼습니다");
            return;
        }

        objects[real] = value;
        real = (real + 1) % objects.length;
        count ++;
    }

    // TODO Step 3: dequeue (빼면서 반환)
    // front 자리 값 꺼내고, front를 (front + 1) % capacity 로 이동. 비어있을 때 처리도 생각해보기
    public Object dequeue() {
        if(count == 0) {
            System.out.println("비었습니다");
            return null;
        }
        Object dequeued = objects[front];
        front = (front + 1) % objects.length;
        count--;
        return dequeued;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public static void main(String[] args) {
        MyQueue queue = new MyQueue();
        // TODO: enqueue 3개, dequeue 2개 하면서 FIFO(먼저 넣은 게 먼저 나옴) 확인
        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);
        queue.dequeue();
        queue.dequeue();
        queue.enqueue(40);
        queue.enqueue(50);
        queue.enqueue(60);
        // + wrap-around 확인: 채웠다 좀 빼고 다시 채워서 rear가 0으로 돌아오는지 눈으로 확인해보기
    }
}
