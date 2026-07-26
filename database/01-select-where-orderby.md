# Day 3 — SELECT · WHERE · ORDER BY (쿼리 처리 순서)

> 2026-07-26. ⚠️ AI 자동완성 끄고 내 머리로. 강의·검색 없이 아는 만큼. 모르면 ❓.
> 서브젝트: "SQL은 쿼리를 어떤 순서로 처리하는가"

## 핵심 (내 말로)

### Q1. 쿼리 처리 순서는? (우리가 쓰는 순서 vs DB가 실행하는 순서)
from -> where -> select -> order by


### Q2. SELECT 안 한 컬럼(age)을 WHERE에서 쓸 수 있는 이유?
where 는 비록 select 전에 실행되지만 , from 에서 전체 컬럼에대해서 가져오기때문에 where 에서도 사용할 수 있다


### Q3. 별칭(AS)이 ORDER BY에선 되고 WHERE에선 안 되는 이유?
where 는 select 전에 실행되기 때문에 이후에 실행되는 select 구문에서의 별칭은 사용 할 수 없지만  order by는 본테이블 뿐 아니라 select 에서 가져올 수 도 있다


## 실습에서 확인한 것
ORDER BY 나이 → 됨 (별칭으로 정렬)
WHERE 나이 >= 30 → 에러 (column "나이" does not exist)
## 경계 조건 (이상/초과/이하/미만)
<!-- >= / > / <= / < 매핑 -->
이상 >= / 초과 > / 이하 <= / 미만 <

## 오늘 배운 것 3줄
처리 순서 FROM→WHERE→SELECT→ORDER BY
WHERE는 별칭 X, ORDER BY는 별칭 O (실행 시점 차이)
이상/초과 경계 (>= vs >) 조심