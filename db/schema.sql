CREATE DATABASE IF NOT EXISTS team_project DEFAULT CHARACTER SET utf8mb4;
USE team_project;

USE team_project;

-- ① 사용자 (로그인용)
CREATE TABLE users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    login_id   VARCHAR(30) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    name       VARCHAR(30) NOT NULL,
    student_no VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ② 팀
CREATE TABLE teams (
    team_id     INT AUTO_INCREMENT PRIMARY KEY,
    team_name   VARCHAR(50) NOT NULL,
    description TEXT,
    leader_id   INT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (leader_id) REFERENCES users(user_id)
);

-- ③ 팀 소속
CREATE TABLE team_members (
    team_id   INT NOT NULL,
    user_id   INT NOT NULL,
    role      VARCHAR(20) NOT NULL DEFAULT '조원',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, user_id),
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ④ 업무
CREATE TABLE tasks (
    task_id     INT AUTO_INCREMENT PRIMARY KEY,
    team_id     INT NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    assignee_id INT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT '할 일',
    due_date    DATE NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (assignee_id) REFERENCES users(user_id)
);

-- ⑤ 일정
CREATE TABLE schedules (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    team_id     INT NOT NULL,
    title       VARCHAR(100) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    memo        TEXT,
    FOREIGN KEY (team_id) REFERENCES teams(team_id)
);

-- ⑥ 자료
CREATE TABLE materials (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    team_id     INT NOT NULL,
    title       VARCHAR(100) NOT NULL,
    link_url    VARCHAR(255),
    description TEXT,
    uploader_id INT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (uploader_id) REFERENCES users(user_id)
);

-- 샘플 데이터: 사용자 5명 (아이디 = 학번, 비밀번호 = 1234)
INSERT INTO users (login_id, password, name, student_no) VALUES
('20253289', '1234', '장현민', '20253289'),
('20253293', '1234', '이해찬', '20253293'),
('20253307', '1234', '이동주', '20253307'),
('20253364', '1234', '윤정훈', '20253364'),
('20253584', '1234', '신예준', '20253584');

-- 샘플 데이터: 팀 1개 (팀장 = 1번)
INSERT INTO teams (team_name, description, leader_id)
VALUES ('자바프로그래밍 3조', '대학생 팀프로젝트 관리 프로그램', 1);

-- 샘플 데이터: 팀 소속
INSERT INTO team_members (team_id, user_id, role) VALUES
(1, 1, '팀장'),
(1, 2, '부조장'),
(1, 3, '조원'),
(1, 4, '조원'),
(1, 5, '조원');

-- 샘플 데이터: 업무·일정·자료 각 1건
INSERT INTO tasks (team_id, title, assignee_id, status, due_date)
VALUES (1, 'Java와 MySQL 연결 구현', 1, '완료', '2026-11-04');

INSERT INTO schedules (team_id, title, start_date, end_date)
VALUES (1, '중간발표', '2026-11-05', '2026-11-05');

INSERT INTO materials (team_id, title, link_url, uploader_id)
VALUES (1, '1차 발표자료', 'https://example.com', 1);

-- 확인
SHOW TABLES;

SELECT t.task_id, t.title, u.name AS 담당자, t.due_date, t.status
FROM tasks t
JOIN users u ON t.assignee_id = u.user_id;