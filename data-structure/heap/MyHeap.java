package heap;

import java.util.Arrays;

public class MyHeap {

    // TODO Step 1: 필드
    // 힌트: 값들을 담을 배열(int[]) + 지금 몇 개 들어있는지 세는 size(int).
    //       오늘은 간단하게 충분히 큰 고정 크기(예: new int[20])로 시작해도 됨(grow는 나중 주제).
    private int[] arrayInt;

    private int size;

    // TODO Step 2: 생성자
    // 힌트: 배열 초기화(new int[20]), size = 0
    public MyHeap() {
        arrayInt = new int[20];
    }


    // TODO Step 3: 인덱스 계산 헬퍼 3개 — 한 줄씩이라 여기는 쉬울 거예요
    // 힌트: 부모 인덱스 = (자식-1)/2, 왼쪽자식 = 부모*2+1, 오른쪽자식 = 부모*2+2
    private int parentIndex(int i) {
        return (i - 1) / 2;
    }

    private int leftChildIndex(int i) {
        return (i * 2) + 1;
    }

    private int rightChildIndex(int i) {
        return (i * 2) + 2;
    }

    // TODO Step 4: swap(i, j) — 배열의 두 자리 값을 맞바꾸는 헬퍼
    // 힌트: 셋 중 하나가 잠깐 값을 담아둬야 함(temp 변수 필요) — 안 그러면 값을 덮어쓰면서 잃어버림.
    //       예: temp에 배열[i] 저장 → 배열[i]에 배열[j] 넣기 → 배열[j]에 temp 넣기
    private void swap(int i, int j) {
        int temp = arrayInt[i];
        arrayInt[i] = arrayInt[j];
        arrayInt[j] = temp;
    }

    // TODO Step 5: insert(value) — sift-up(부상)
    // 힌트 순서 (오늘 손으로 90 넣을 때 했던 과정 그대로):
    //   1. 배열의 size번째 자리에 value를 넣고, size를 1 증가시킴
    //   2. "지금 위치"라는 변수를 만들어서, 방금 넣은 자리(size-1)로 시작
    //   3. 반복(while):
    //        - "지금 위치"가 루트(0)면 → 더 올라갈 데 없음, 반복 종료
    //        - 부모 인덱스를 구해서, 부모 값이 "지금 위치" 값보다 작으면(Max-Heap 규칙 위반)
    //             → swap(지금위치, 부모위치)
    //             → "지금 위치"를 부모 위치로 갱신 (계속 올라갈 수도 있으니)
    //        - 부모 값이 더 크거나 같으면 → 규칙 만족, 반복 종료
    public void insert(int value) {
        arrayInt[size] = value;
        size ++;
        int current = size -1;
        while( current !=0 && arrayInt[parentIndex(current)] < arrayInt[current]) {
            swap(parentIndex(current), current);
            current = parentIndex(current);
        }
    }

    // TODO Step 6: largerChildIndex(i) — i의 두 자식 중 "더 큰 쪽"의 인덱스를 반환.
    //              자식이 하나도 없으면 -1 반환.
    // 힌트 순서:
    //   1. 왼쪽 자식 인덱스(leftChildIndex(i))가 size보다 크거나 같으면
    //      → 왼쪽 자식조차 없다는 뜻(자식이 아예 없음) → -1 반환
    //   2. 오른쪽 자식 인덱스(rightChildIndex(i))가 size보다 크거나 같으면
    //      → 오른쪽은 없고 왼쪽만 있다는 뜻 → 왼쪽 자식 인덱스 반환
    //   3. 여기까지 안 걸렸으면 둘 다 있다는 뜻
    //      → arrayInt[왼쪽자식인덱스]랑 arrayInt[오른쪽자식인덱스] 비교해서
    //        더 큰 값 쪽의 "인덱스"를 반환 (값이 아니라 인덱스를 반환하는 거 주의!)
    private int largerChildIndex(int i) {
        if (leftChildIndex(i) >= size) {
            return -1;
        }

        if (rightChildIndex(i) >= size) {
            return leftChildIndex(i);   // -1이 아니라 이거!
        }

        if (arrayInt[leftChildIndex(i)] > arrayInt[rightChildIndex(i)]) {
            return leftChildIndex(i);
        } else {
            return rightChildIndex(i);
        }
    }

    // TODO Step 7: extractMax() — 루트(최댓값)를 꺼내고, 배열을 다시 Max-Heap으로 복구
    // 힌트 순서 (오늘 손으로 90 꺼낼 때 했던 과정 그대로):
    //   1. arrayInt[0](루트값)을 max라는 변수에 저장해둠 (이게 나중에 반환할 값)
    //   2. arrayInt[size-1](배열의 맨 끝 값)을 arrayInt[0]에 옮겨 넣음
    //   3. size--
    //   4. "지금 위치" current = 0으로 시작
    //   5. 반복(while(true)):
    //        - largerChildIndex(current) 호출 → 결과를 변수(예: biggerChild)에 저장
    //        - biggerChild가 -1이면(자식 없음) → 반복 종료
    //        - arrayInt[current]가 arrayInt[biggerChild]보다 크거나 같으면(규칙 만족) → 반복 종료
    //        - 그게 아니면(규칙 위반) → swap(current, biggerChild), current = biggerChild로 갱신
    //   6. 저장해둔 max를 반환(return)
    public int extractMax() {
        int max = arrayInt[0];
        arrayInt[0] = arrayInt[size - 1];
        size--;
        int current = 0;
        while(true) {
            int biggerChild = largerChildIndex(current);
            if(biggerChild == -1) {
                break;
            }

            if(arrayInt[current] >= arrayInt[biggerChild]) {
                break;
            }

            swap(current, biggerChild);
            current = biggerChild;
        }

        return max;
    }

    public static void main(String[] args) {
        MyHeap heap = new MyHeap();
        // TODO: 50, 30, 40, 10, 20, 35, 90 순서로 insert() 호출
        // TODO: 배열을 출력해서 확인 (Arrays.toString(배열변수) 쓰면 편함)
        //       기대값: 루트(인덱스0)에 90이 와야 하고, 모든 부모>=자식이어야 함
        heap.insert(50);
        heap.insert(30);
        heap.insert(40);
        heap.insert(10);
        heap.insert(20);
        heap.insert(35);
        heap.insert(90);

        System.out.println(Arrays.toString(heap.arrayInt));

        System.out.println(heap.extractMax());
        System.out.println(Arrays.toString(heap.arrayInt));

        System.out.println(heap.extractMax());
        System.out.println(Arrays.toString(heap.arrayInt));

        while (heap.size > 0) {
            System.out.println(heap.extractMax());
        }
    }
}
