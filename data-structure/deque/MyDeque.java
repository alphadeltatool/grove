package deque;

public class MyDeque {

    // TODO Step 1: 필드
    // 힌트: MyQueue랑 똑같음 — 배열 + front(맨앞 위치) + rear(다음에 뒤로 넣을 위치) + count(현재 개수)
    private Object[] objects;
    private int front;
    private int rear;
    private int count;

    public MyDeque() {
        objects = new Object[5];
    }

    // TODO Step 2: addLast (뒤에 넣기) — MyQueue의 enqueue랑 완전히 동일한 로직
    // rear 자리에 넣고, rear를 (rear + 1) % capacity 로 이동. 꽉 찼을 때 처리도.
    public void addLast(Object value) {
        if (count == objects.length) {
            System.out.println("큐가꽉찼습니다");
            return;
        }

        objects[rear] = value;
        rear = (rear + 1) % objects.length;
        count++;
    }

    // TODO Step 3: removeFirst (앞에서 빼기) — MyQueue의 dequeue랑 완전히 동일한 로직
    // front 자리 값 꺼내고, front를 (front + 1) % capacity 로 이동. 비어있을 때 처리도.
    public Object removeFirst() {
        if(count == 0) {
            System.out.println("비었습니다");
            return null;
        }

        Object dequeued = objects[front];
        front = (front + 1) % objects.length;
        count--;
        return dequeued;
    }

    // TODO Step 4: addFirst (앞에 넣기) — 새로운 부분!
    // 지금까지는 항상 "앞으로 이동"만 했는데, 이번엔 "뒤로(앞쪽으로) 이동"해야 함.
    // 힌트: front를 먼저 한 칸 앞으로 옮긴 다음(= 인덱스는 작아지는 방향) 그 자리에 값을 넣어야 함.
    //   front - 1 을 그냥 쓰면 front가 0일 때 -1이 되어버림 (배열 인덱스 음수 불가, 에러).
    //   그래서 (front - 1 + capacity) % capacity 로 계산해야 함.
    //   왜 "+ capacity"가 필요한지: 자바의 % 연산은 음수를 넣으면 음수가 나올 수 있음(-1 % 5 == -1, 0이 아님).
    //   capacity를 더해주면 항상 0 이상의 값으로 wrap-around됨.
    // 순서 주의: front를 먼저 이동시키고 나서 그 자리에 값을 넣어야 함 (addLast/removeFirst와 순서가 반대!)
    public void addFirst(Object value) {
        if (count == objects.length) {
            System.out.println("큐가꽉찼습니다");
            return;
        }
        front = (front - 1 + objects.length) % objects.length;
        objects[front] = value;
        count++;
    }

    // TODO Step 5: removeLast (뒤에서 빼기) — 이것도 새로운 부분
    // 힌트: rear는 "다음에 넣을 자리"를 가리키고 있으므로, 실제 마지막 값은 rear 바로 이전 칸에 있음.
    //   그래서 먼저 rear를 한 칸 뒤로(왼쪽으로) 옮긴 다음, 그 자리의 값을 꺼내야 함.
    //   여기도 (rear - 1 + capacity) % capacity 로 계산.
    //   순서 주의: rear를 먼저 이동시키고 나서 그 자리 값을 꺼내야 함.
    public Object removeLast() {
        if(count == 0) {
            System.out.println("큐가 없음");
            return null;
        }

        rear = (rear - 1 + objects.length) % objects.length;
        Object newRear = objects[rear];
        count--;
        return newRear;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    // TODO Step 6: main — 회문(palindrome) 판별로 검증
    // "level" 같은 문자열을 한 글자씩 addLast로 다 넣은 다음,
    // removeFirst()와 removeLast()를 동시에 꺼내면서 서로 같은지 비교 → 다르면 회문 아님, 다 같으면 회문
    public static void main(String[] args) {
        MyDeque myDeque = new MyDeque();
        String word = "level";

        for (int i = 0; i < word.length(); i++) {
            myDeque.addLast(word.charAt(i));
        }

        boolean isPalindrome = true;
        int firstLength = word.length() / 2;

        for (int i = 0; i < firstLength; i++) {
            Object left = myDeque.removeFirst();
            Object right = myDeque.removeLast();

            if (!left.equals(right)) {
                isPalindrome = false;
            }
        }

        System.out.println(isPalindrome);
    }
}
