package stack;

public class MyStack {

    // TODO Step 1: 필드 뭐가 필요할까? (힌트: MyArrayList 만들 때 배열 + 뭘 같이 썼었죠?)
    private Object[] stackRoom;
    private int top;

    //private int count++;

    public MyStack() {
        // TODO: 초기화
        stackRoom = new Object[5];
        top = -1;
    }

    // TODO Step 2: push (넣기) - 꽉 찼을 때는 어떻게 할지도 생각해보기
    public void push(Object value) {
        if (top == stackRoom.length -1) {
            System.out.println("Stack이 꽉찼습니다");
            return;
        }
        top = top + 1;
        stackRoom[top] = value;
    }

    // TODO Step 3: pop (빼면서 반환) - 비어있으면?
    public Object pop() {
        if (isEmpty()) {
            System.out.println("stack이 비어있습니다");
            return null;
        }
        Object value = stackRoom[top];
        top = top -1;
        return value;
    }

    // TODO Step 4: peek (안 빼고 맨 위만 보기)
    public Object peek() {
        if (isEmpty()) return null;
        return stackRoom[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public static void main(String[] args) {
        MyStack stack = new MyStack();
        stack.push(10);
        stack.push(20);
        stack.push(30);
        System.out.println(stack.pop());
        System.out.println(stack.pop());
        System.out.println(stack.pop());
    }
}
