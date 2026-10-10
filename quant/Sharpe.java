// 퀀트 평가지표 ③ 샤프지수 (Sharpe Ratio)
//
//   샤프지수 = 평균수익 / 표준편차
//              (무위험수익률은 0으로 단순화)
//
// 손으로 계산했던 값:
//   A = {3, 1, 2, 2}        평균 2,  표준편차 0.71,  샤프 2.8
//   B = {20, -15, 18, -15}  평균 2,  표준편차 17.0,  샤프 0.12
public class Sharpe {

    // ───────────────────────────────────────────────
    // 평균 구하기
    // ───────────────────────────────────────────────
    public static double mean(double[] returns) {
        double sum = 0;

        for (double r : returns) {
            // 힌트: sum에 r을 더하기.  sum += ___;
            sum += r;
        }

        // 힌트: 합을 개수로 나눈다. 개수는 returns.length
        return sum / returns.length;
    }

    // ───────────────────────────────────────────────
    // 표준편차 구하기 (손계산 5단계 그대로)
    // ───────────────────────────────────────────────
    public static double stdev(double[] returns) {

        // ① 평균 — 위에서 만든 mean() 재사용
        double m = mean(returns);

        // ②③ 편차 구하고 제곱해서 모으기
        double sqSum = 0;
        for (double r : returns) {
            // 힌트: 편차 = 값 - 평균
            // double diff = ___ - ___;
            double diff = r - m;

            // 힌트: 제곱해서 sqSum에 더하기.  diff * diff 로 제곱하면 됨
            // sqSum += ___;
            sqSum += diff * diff;
        }

        // ④ 제곱들의 평균 (분산)
        // 힌트: sqSum을 개수로 나눈다
        double variance = sqSum / returns.length;

        // ⑤ 제곱근
        // 힌트: Math.sqrt(변수)
        return Math.sqrt(variance);
    }

    // ───────────────────────────────────────────────
    // 샤프지수
    // ───────────────────────────────────────────────
    public static double sharpe(double[] returns) {
        // 힌트: 위에서 만든 두 메서드를 나누기만 하면 됨
        return mean(returns) / stdev(returns);
    }

    public static void main(String[] args) {
        double[] a = {3, 1, 2, 2};
        double[] b = {20, -15, 18, -15};

        System.out.println("A 평균   = " + mean(a));      // 기대값 2.0
        System.out.println("A 표준편차 = " + stdev(a));     // 기대값 0.707...
        System.out.println("A 샤프   = " + sharpe(a));    // 기대값 2.82...

        System.out.println();
        System.out.println("B 평균   = " + mean(b));      // 기대값 2.0
        System.out.println("B 표준편차 = " + stdev(b));     // 기대값 17.01...
        System.out.println("B 샤프   = " + sharpe(b));    // 기대값 0.117...
    }
}
