package tree;

public class MyBST {

    // TODO Step 1: 필드 채우기
    // 힌트: 트리는 "루트 노드 하나"만 기억하면 나머지는 노드끼리 연결(left/right)로 다 따라갈 수 있음.
    //       Stack의 top, LinkedList의 first랑 같은 역할.
    private Node root;

    // TODO Step 2: Node 내부 클래스
    // 힌트: 데이터(value) + 왼쪽 자식(left) + 오른쪽 자식(right), 총 3개 필드.
    private static class Node {
        private int value;
        private Node left;
        private Node right;

        Node (int value) {
            this.value = value;
        }
    }

    // TODO Step 3: insert(value) — 새 값을 알맞은 자리에 삽입
    // 힌트: 루트가 비어있으면 거기가 자리. 아니면 루트부터 시작해서
    //       "값이 크면 오른쪽, 작으면 왼쪽"으로 계속 내려가다가 빈 자리(null)를 만나면 거기 새 노드를 붙인다.
    //       (오늘 65 삽입할 때 손으로 했던 그 과정 그대로)
    public void insert(int value) {
        if(root == null) {
            root = new Node(value);
        } else {
            Node current = root;
            while (true) {
                if(current.value > value) {
                    if(current.left == null) {
                        current.left = new Node(value);
                        return;
                    } else {
                        current = current.left;
                    }
                } else {
                    if (current.right == null) {
                        current.right = new Node(value);
                        return;
                    } else {
                        current = current.right;
                    }
                }
            }
        }
    }

    // TODO Step 4: contains(value) — 값이 트리 안에 있는지 탐색
    // 힌트: insert랑 내려가는 로직이 거의 똑같음. 다만 새로 붙이는 대신 "찾았다/못 찾았다"만 판단.
    public boolean contains(int value) {
        Node current = root;
        while (current != null) {
            if (current.value == value) {
                return true;
            } else {
                if(current.value > value) {
                    current = current.left;    // ← current.left가 아니라, current가 왼쪽(=바뀌는 대상)
                } else {
                    current = current.right;
                }
            }
        }

        return false;
    }

    // TODO Step 5: inOrderTraversal() — 왼쪽→나→오른쪽 순서로 전부 출력
    // 힌트: 이게 진짜 재귀가 필요한 부분. "이 노드를 처리해라" =
    //       "왼쪽 자식을 처리해라(재귀 호출)" → "나 출력" → "오른쪽 자식을 처리해라(재귀 호출)".
    //       public 메서드는 인자가 없는데 재귀는 "지금 어느 노드 차례인지" 알아야 하니까,
    //       보통 private helper 메서드를 하나 더 만들어서(예: inOrder(Node node)) 그 안에서 재귀를 돌리고,
    //       public inOrderTraversal()은 그 helper를 root로 시작만 시켜주는 역할을 함.
    public void inOrderTraversal() {
        inOrder(root);
    }

    private void inOrder(Node node) {
        if(node == null) {
            return;
        }

        inOrder(node.left);
        System.out.println(node.value);
        inOrder(node.right);
    }

    public static void main(String[] args) {
        // TODO: 50, 30, 70, 20, 40, 60, 80 순서로 insert
        // TODO: inOrderTraversal() 호출해서 20,30,40,50,60,70,80 순서(정렬됨)로 나오는지 확인
        // TODO: contains(60), contains(65) 각각 호출해서 true/false 나오는지 확인
        MyBST tree = new MyBST();

        tree.insert(50);
        tree.insert(30);
        tree.insert(70);
        tree.insert(20);
        tree.insert(40);
        tree.insert(60);
        tree.insert(80);

        tree.inOrderTraversal();

        System.out.println(tree.contains(60));
        System.out.println(tree.contains(65));
    }
}
