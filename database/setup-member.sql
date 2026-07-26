-- Day 3 실습용 member 테이블 (PostgreSQL, Docker)
-- 실행: docker exec -i study-pg psql -U postgres -d study < database/setup-member.sql

DROP TABLE IF EXISTS member;
CREATE TABLE member (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(20),
    age  INT,
    city VARCHAR(20)
);

INSERT INTO member (name, age, city) VALUES
('김철수', 25, '서울'),
('이영희', 30, '부산'),
('박민수', 19, '서울'),
('최지우', 42, '대구'),
('정해인', 28, '서울'),
('강수지', 35, '부산'),
('윤도현', 22, '인천'),
('한지민', 19, '서울'),
('오세훈', 51, '대구'),
('임나연', 27, '부산');
