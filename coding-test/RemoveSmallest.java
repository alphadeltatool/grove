import java.util.Arrays;

// 프로그래머스 Lv1 - 제일 작은 수 제거하기
// 정수 배열 arr에서 가장 작은 수를 제거한 배열을 반환한다.
// (배열이 1개짜리면 [-1] 을 반환)
// * 배열 + 반복문만으로 풀 수 있음 (Set/정렬 불필요)
public class RemoveSmallest {

    public int[] solution(int[] arr) {
        // TODO: 여기 로직 채우기
        // 힌트:
        //  1) arr 길이가 1이면 return new int[]{-1};
        if (arr.length == 1) {
            return new int[]{-1};
        }
        //  2) 반복문으로 "가장 작은 값"을 찾는다
        int minIdx = 0;
        for (int i =0; i < arr.length; i++) {
            if (arr[i] < arr[minIdx]) {
                minIdx = i;
            }
        }
        //  3) 크기 (arr.length - 1) 짜리 새 배열을 만들고,
        //     가장 작은 값 하나만 빼고 나머지를 새 배열에 옮긴다
        int[] result = new int[arr.length -1];
        int k = 0;
        for (int i = 0; i < arr.length; i++) {
            if (i != minIdx) {
                result[k] = arr[i];
                k++;
            }
        }
        return result;
    }

    // ===== 테스트 하니스 (건드리지 마세요) =====
    public static void main(String[] args) {
        RemoveSmallest sol = new RemoveSmallest();
        check(sol.solution(new int[]{4, 3, 2, 1}), new int[]{4, 3, 2}, "테스트1");
        check(sol.solution(new int[]{10}),         new int[]{-1},      "테스트2");
        check(sol.solution(new int[]{1, 2, 3, 4}), new int[]{2, 3, 4}, "테스트3");
    }

    static void check(int[] actual, int[] expected, String name) {
        boolean ok = Arrays.equals(actual, expected);
        System.out.println(name + ": " + (ok ? "통과 ✅" : "실패 ❌")
                + " | 내 답=" + Arrays.toString(actual)
                + " | 정답=" + Arrays.toString(expected));
    }
}
