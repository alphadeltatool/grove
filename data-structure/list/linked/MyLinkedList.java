package list.linked;

public class MyLinkedList {

    // TODO Step 1: 필드 2개 채우기
    private Node first;    // 첫 노드(head) - 순회 시작점  → first
    private int count;     // 개수 카운터                  → size

    // 노드 하나 = 데이터 + 다음 노드를 가리키는 참조 (Q1에서 말한 그것)
    private static class Node {
        Object data;      // 이 노드의 값        → data
        Node next;        // 다음 노드(없으면 null) → next
        Node(Object data) {
            this.data = data;
        }
    }

    // TODO Step 2: add() 는 뼈대 채운 뒤 이어서 작성
    public void add(Object value) {
        Node newNode = new Node(value);
        if (first == null) {
            first = newNode;
        } else {
            Node last = first;
            while(last.next != null) {
                last = last.next;
            }

            last.next = newNode;
        }

        count++;
     }

     public void printAll() {
        Node node = first;
        while(node != null) {
            System.out.println(node.data);
            node = node.next;
        }
     }

     public boolean contains(Object value) {
        Node node = first;
        while (node != null) {
            if (node.data.equals(value)) {
                return true;
            }

            node = node.next;
        }

        return false;
     }

     public static void main(String[] args) {
        MyLinkedList list = new MyLinkedList();
        list.add("10");
        list.add("20");
        list.add("30");
        list.printAll();
        System.out.println("Count: " + list.count);
     }
}
