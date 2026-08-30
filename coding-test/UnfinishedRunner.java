import java.util.*;

// 프로그래머스 Lv1 - 완주하지 못한 선수
// participant(참가자)와 completion(완주자) 배열이 주어짐. completion은 participant보다 딱 1명 적음.
// 동명이인이 있을 수 있음 → 이름만으로 단순 비교하면 안 됨.
// 완주하지 못한 선수 1명의 이름을 반환하라.
//
// 예: participant = ["leo", "kiki", "eden"], completion = ["eden", "kiki"] → "leo"
public class UnfinishedRunner {

    public String solution(String[] participant, String[] completion) {
        Map<String, Integer> count = new HashMap<>();

        for (int i = 0; i < participant.length; i++) {
            count.put(participant[i], count.getOrDefault(participant[i], 0) + 1);
        }

        for(int i = 0; i < completion.length; i++) {
            count.put(completion[i], count.getOrDefault(completion[i], 0) - 1);
        }

        for(Map.Entry<String, Integer> entry : count.entrySet()) {
            if(entry.getValue() > 0) {
                return entry.getKey();
            }
        }

        return null;
    }
}
