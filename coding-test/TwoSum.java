import java.util.*;

// 프로그래머스 Lv1 - 두 개 뽑아서 더하기
// ⏸️ 보류: 깔끔한 풀이에 Set(중복 제거)이 필요함 → Day 5(Hash/Set) 배운 뒤에 풀 것.
// numbers에서 서로 다른 인덱스의 두 수를 뽑아 더한 값들을
// 중복 없이 오름차순으로 반환한다.
public class TwoSum {

    public int[] solution(int[] numbers) {
        Set<Integer> sums = new TreeSet<>();

        for (int i = 0; i < numbers.length; i++) {
            for (int j = i+1; j < numbers.length; j++) {
                int sum = numbers[i] + numbers[j];
                sums.add(sum);
            }
        }

        int[] result = new int[sums.size()];
        int index = 0;
        for (int sum : sums) {
            result[index] = sum;
            index++;
        }

        return result;
    }
}
