package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MyGraph {

    // TODO Step 1: 필드
    // 힌트: "노드 이름 → 이웃 목록"을 저장할 자료구조 하나면 충분함.
    //       어제 인출한 그대로: Map<String, List<String>>
    //       타입은 인터페이스(Map, List)로 선언하고, 실제 구현체는 생성자에서 정함.
    private Map<String, List<String>> graph;


    // TODO Step 2: 생성자
    // 힌트: 필드를 실제 구현체로 초기화. Map은 뭘 썼었죠? (Day5 HashSet 만들 때 내부적으로 뭘 썼는지 떠올려보기)
    //       new HashMap<>() 로 초기화하면 됨.
    public MyGraph() {
        this.graph = new HashMap<>();
    }


    // TODO Step 3: addEdge(String a, String b) — 양방향 간선 추가
    // 힌트: "A-B가 연결됐다" = A의 이웃목록에 B 추가 + B의 이웃목록에 A 추가 (양방향이라 두 번)
    // 막힐 포인트: graph.get("A")가 처음엔 null임(아직 아무것도 안 넣었으니까).
    //   null인 상태에서 바로 .add()하면 NullPointerException 남.
    //   방법 두 가지 중 하나 선택:
    //     (1) if (!graph.containsKey(a)) graph.put(a, new ArrayList<>()); 로 먼저 빈 리스트 만들어주기
    //     (2) Map의 computeIfAbsent(key, k -> new ArrayList<>()) 메서드 — "키 없으면 만들어서 넣고, 있으면 그대로 반환"
    //   이번엔 (1)번 방식으로 먼저 손으로 짜보고, 되면 (2)번도 찾아서 리팩터링 해보세요.
    public void addEdge(String a, String b) {
        graph.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        graph.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }


    // TODO Step 4: bfs(String start) — 너비 우선 탐색, 방문 순서를 출력
    // 준비물 3개:
    //   Queue<String> queue = new LinkedList<>();   // 다음에 방문할 후보들
    //   Set<String> visited = new HashSet<>();       // 이미 방문한 노드 (Q3에서 왜 HashSet인지 정리함)
    //   시작 노드를 queue에 넣고 visited에도 추가
    //
    // 반복 로직 (큐가 빌 때까지):
    //   1. queue.poll()로 하나 꺼내기 (= 지금 처리할 노드)
    //   2. 꺼낸 노드 출력(System.out.print)
    //   3. 그 노드의 이웃 목록(graph.get(현재노드))을 순회하면서:
    //      - 아직 visited에 없는 이웃이면 → visited에 추가 + queue에 추가
    //      - 이미 visited에 있으면 → 스킵
    //
    // 막힐 포인트: "이웃을 발견했을 때(큐에 넣을 때) visited 체크" vs "큐에서 꺼냈을 때 visited 체크" —
    //   둘 다 동작은 하지만 방식이 다름. 위 로직은 "발견 시점에 체크"하는 방식으로 안내함.
    //   왜 이 방식이 더 나은지: 같은 노드가 큐에 중복으로 쌓이는 걸 미리 막아줌.
    public void bfs(String start) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while(!queue.isEmpty()) {
            String pollQueue = queue.poll();
            System.out.println(pollQueue);
            List<String> listValue = graph.get(pollQueue);

            for(String neighbor : listValue) {
                if(!visited.contains(neighbor)) {
                    queue.offer(neighbor);
                    visited.add(neighbor);
                }
            }
        }
    }


    // TODO Step 5: dfs(String start) — 깊이 우선 탐색, 방문 순서를 출력
    // 두 가지 방식 중 선택 (재귀 추천 — 어제 Tree in-order에서 재귀 콜스택 이미 체득했으니 여기서 재사용해보기):
    //
    // [재귀 버전] dfs 안에서 visited를 계속 들고 다녀야 하므로, private 헬퍼를 하나 더 만드는 게 편함:
    //   public void dfs(String start) {
    //       Set<String> visited = new HashSet<>();
    //       dfsHelper(start, visited);
    //   }
    //   private void dfsHelper(String node, Set<String> visited) {
    //       // 1. visited에 node 추가 + node 출력
    //       // 2. node의 이웃들을 순회하면서, 아직 방문 안 한 이웃이면 dfsHelper(이웃, visited) 재귀 호출
    //   }
    //
    // [스택 버전을 원하면] Deque<String> stack = new ArrayDeque<>() 로 스택처럼 사용(push/pop).
    //   단, 스택 버전은 "꺼낼 때 visited 체크"라 BFS와 체크 시점이 달라짐 — 헷갈리면 일단 재귀로 먼저 완성하고 나중에 비교해보세요.
    public void dfs(String start) {

    }


    // TODO Step 6: main — 어제 백지인출에 썼던 그래프로 검증
    // A-B, A-C, B-D, C-E 관계 (Q2 예시 그래프 그대로)
    // 기대 결과: bfs("A") → A,B,C,D,E / dfs("A") → A,B,D,C,E
    public static void main(String[] args) {

    }
}
