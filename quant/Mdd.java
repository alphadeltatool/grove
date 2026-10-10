// 퀀트 평가지표 ② MDD (Maximum Drawdown, 최대 낙폭)
//
// 자산 곡선 배열을 받아서 MDD를 %로 리턴하세요.
//
// 예) {1000, 1200, 900, 1500, 600, 2000}
//
//   자산    그때까지 최고점   낙폭
//   ──────────────────────────────────────────────
//   1000    1000            0%
//   1200    1200            0%
//   900     1200            (1200-900)/1200  = 25%
//   1500    1500            0%
//   600     1500            (1500-600)/1500  = 60%   ← 제일 깊음
//   2000    2000            0%
//
//   → MDD = 60.0
//
// 힌트: 변수 2개가 필요합니다.
//        peak  = 지금까지의 최고점
//        maxDd = 지금까지 본 낙폭 중 제일 큰 값
public class Mdd {

    public static double mdd(double[] equity) {
        double peak = equity[0];
        double maxDd = 0;

        for (double v: equity) {
            if(v > peak) {
                peak = v;
            } else {
                double dd = (peak - v) / peak * 100;
                if(dd > maxDd) {
                    maxDd = dd;
                }
            }
        }

        return maxDd;
    }

    public static void main(String[] args) {
        double[] a = {1000, 1200, 900, 1500, 600, 2000};
        System.out.println(mdd(a));        // 기대값 60.0

        double[] b = {1000, 900, 1100, 1000};
        System.out.println(mdd(b));        // 기대값 10.0  (1000 → 900)

        double[] c = {1000, 2000, 3000};
        System.out.println(mdd(c));        // 기대값 0.0   (계속 오르기만 함)
    }
}
