import java.util.ArrayList;
import java.util.List;

// 프로그래머스 Lv1 - 자릿수 더하기
//
// 자연수 N이 주어지면, N의 각 자릿수를 더한 값을 리턴하세요.
//
//   N = 123   →   1 + 2 + 3  =  6
//   N = 987   →   9 + 8 + 7  =  24
//   N = 5     →   5          =  5
//
// 제한: N은 1 이상 10,000,000 이하의 자연수
public class DigitSum {

    public int solution(int n) {
        String str = String.valueOf(n);
        int solution = 0;

        for (int i = 0; i < str.length(); i++) {
            solution += str.charAt(i) - '0';
        }

        return solution;
    }

    public static void main(String[] args) {
        DigitSum s = new DigitSum();
        System.out.println(s.solution(123));   // 기대값 6
        System.out.println(s.solution(987));   // 기대값 24
        System.out.println(s.solution(5));     // 기대값 5
    }
}
