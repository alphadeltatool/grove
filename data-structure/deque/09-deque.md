# Day 10 — Deque (덱)

> 2026-08-30 학습. `ArrayDeque`가 Day4 Queue 노트에 "실무에 뭘 쓰나"로 스치듯 언급된 적은 있었지만, Deque 자체(양쪽 끝 조작)는 완전 신규 개념.

## 큰 그림
```
Stack: 한쪽 끝(top)에서만 넣고 뺌            → 넣는 곳 = 빼는 곳 → LIFO
Queue: 한쪽 끝(rear)에서 넣고 반대쪽(front)에서 뺌 → 넣는 곳 ≠ 빼는 곳(고정) → FIFO
Deque: 양쪽 끝 어디서든 넣고 뺄 수 있음            → 매번 선택 가능
```
Deque는 Stack·Queue의 상위호환. `addFirst`+`removeFirst`(같은 쪽)로 쓰면 Stack(LIFO), `addLast`+`removeFirst`(반대쪽)로 쓰면 Queue(FIFO)가 됨 — "어느 쪽 메서드를 조합해 쓰느냐"가 동작을 결정.

**왜 양쪽이 필요한가 — 회문(palindrome) 판별**: `"level"`처럼 앞뒤 대칭을 확인하려면 앞(`removeFirst`)과 뒤(`removeLast`)를 동시에 꺼내 비교해야 함. Stack이나 Queue 하나로는 어색하고, Deque가 자연스럽게 맞음.

## 구현 (`MyDeque.java`)
`MyQueue`(원형 배열, front/rear/count)를 그대로 재사용 + 새 연산 2개 추가.

- **`addLast`/`removeFirst`**: `MyQueue`의 `enqueue`/`dequeue`와 완전히 동일한 로직 — 힌트만 보고 정확히 작성.
- **`addFirst`(신규)**: front를 반대 방향(감소)으로 옮겨야 함. `front - 1`을 그냥 쓰면 `front=0`일 때 `-1`이 되어 배열 인덱스 에러. 자바의 `%`는 음수를 넣으면 음수를 그대로 리턴(`-1 % 5 == -1`)하므로 `% capacity`만으론 해결 안 됨 → `(front - 1 + capacity) % capacity`로 먼저 양수를 보장한 뒤 wrap-around.
  - **순서 버그**: 처음엔 "값 먼저 넣고 → front 이동"으로 잘못 씀 → 이미 있던 값을 덮어쓰는 버그. `front`는 "현재 첫 번째가 있는 자리"를 가리키므로, 새 첫 번째를 넣으려면 **먼저 빈 자리로 이동한 뒤에** 넣어야 함(순서: 이동 → 삽입). `addLast`는 반대로 `rear`가 "다음에 채울 빈자리"를 가리키고 있어서 바로 채우면 됨(순서: 삽입 → 이동) — 이 순서 차이가 왜 나는지 직접 트레이스로 확인.
- **`removeLast`(신규)**: `addFirst`와 대칭. `rear`를 먼저 뒤로 이동시킨 뒤 그 자리 값을 꺼냄. 빈 상태 체크(`count==0`)에서 `return null;` 빠뜨린 버그 발견 후 직접 수정(안 넣으면 `count`가 `-1`까지 내려가고 쓰레기값 반환).

## 원형 배열 시각화 (핵심 감각)
인덱스를 일자가 아니라 원으로 그리면 "0 앞이 왜 4인지"가 자연스러움 — 시계에서 12시 반시계로 한 칸 가면 11시가 되는 것과 같은 이치. `front`/`rear`는 "물리적 배열 위치"가 아니라 "지금 논리적으로 어디가 첫/마지막인지"를 가리키는 변수 — 이 변수가 클래스 필드(`private int front`)로 한 번만 선언되어 모든 메서드가 공유하기 때문에, `addFirst`가 바꿔놓은 값을 `removeFirst`가 그대로 이어받아 씀.

**`count`가 별도로 필요한 이유(Day4 Queue 노트 재확인)**: `front==rear`인 상태가 "완전히 빔"과 "완전히 가득 참" 둘 다에서 나타날 수 있어서, `front`/`rear`만으론 구분 불가 → 매 삽입/삭제마다 `count`를 명시적으로 증감시켜야 함. `front` 이동과 `count` 증감은 인과관계가 아니라 "넣기/빼기"라는 한 행위가 일으키는 두 가지 별개 결과라는 것 확인.

## 검증
`main()`에서 `"level"`을 `addLast`로 다 넣고, `word.length()/2`번만 `removeFirst()`/`removeLast()`로 동시에 꺼내 `.equals()` 비교 → `System.out.println(isPalindrome)` → **`true`** 출력 확인.
- 버그: 비교 반복문 조건을 `firstLength` 대신 `word.length()`로 잘못 써서 deque가 다 빌 때까지 도는 바람에 `NullPointerException` 발생 → 원인(가운데 글자 이후 `removeLast()`가 `null` 반환, 그다음 `null.equals()` 호출) 트레이스로 확인 후 수정.

## 오늘 배운 것 3줄
Deque = Stack+Queue 상위호환, `addFirst`+`removeFirst`(같은쪽)=LIFO / `addLast`+`removeFirst`(반대쪽)=FIFO. `front`/`rear`는 원형 배열 위의 "논리적 위치"를 가리키는 공유 필드 — `(idx - 1 + capacity) % capacity`로 음수 없이 반시계 wrap-around. `count`는 `front`/`rear`만으론 구분 안 되는 "빔 vs 가득참"을 판정하기 위한 별도 카운터.
