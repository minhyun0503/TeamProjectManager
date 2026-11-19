-- 원본 team_project에 실행하지 않습니다. 비어 있는 새 전용 DB용입니다.
CREATE DATABASE IF NOT EXISTS team_project_feedback DEFAULT CHARACTER SET utf8mb4;
USE team_project_feedback;
-- ① 사용자 (로그인용)
CREATE TABLE IF NOT EXISTS users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    login_id   VARCHAR(30) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    name       VARCHAR(30) NOT NULL,
    student_no VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ② 팀
CREATE TABLE IF NOT EXISTS teams (
    team_id     INT AUTO_INCREMENT PRIMARY KEY,
    team_name   VARCHAR(50) NOT NULL,
    description TEXT,
    leader_id   INT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (leader_id) REFERENCES users(user_id)
);

-- ③ 팀 소속
CREATE TABLE IF NOT EXISTS team_members (
    team_id   INT NOT NULL,
    user_id   INT NOT NULL,
    role      VARCHAR(20) NOT NULL DEFAULT '조원',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, user_id),
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ④ 업무
CREATE TABLE IF NOT EXISTS tasks (
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
CREATE TABLE IF NOT EXISTS schedules (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    team_id     INT NOT NULL,
    title       VARCHAR(100) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    memo        TEXT,
    FOREIGN KEY (team_id) REFERENCES teams(team_id)
);

-- ⑥ 자료
CREATE TABLE IF NOT EXISTS materials (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    team_id     INT NOT NULL,
    title       VARCHAR(100) NOT NULL,
    link_url    VARCHAR(255), -- 이전 자료 링크 보존
    url         VARCHAR(500), -- 자료 관리 코드에서 사용하는 링크
    description TEXT,
    uploader_id INT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (uploader_id) REFERENCES users(user_id)
);


-- 공유 메모: version은 동시에 수정했는지 확인하는 정수입니다.
CREATE TABLE IF NOT EXISTS notes (
 note_id INT AUTO_INCREMENT PRIMARY KEY, team_id INT NOT NULL,
 title VARCHAR(100) NOT NULL, content TEXT NOT NULL,
 author_id INT NOT NULL, updated_by INT NOT NULL, version INT NOT NULL DEFAULT 1,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (team_id) REFERENCES teams(team_id),
 FOREIGN KEY (author_id) REFERENCES users(user_id),
 FOREIGN KEY (updated_by) REFERENCES users(user_id)
);
CREATE TABLE IF NOT EXISTS note_history (
 note_id INT NOT NULL, version INT NOT NULL, content TEXT NOT NULL,
 editor_id INT NOT NULL, edited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (note_id, version),
 FOREIGN KEY (note_id) REFERENCES notes(note_id) ON DELETE CASCADE,
 FOREIGN KEY (editor_id) REFERENCES users(user_id)
);
CREATE TABLE IF NOT EXISTS task_notes (
 task_id INT PRIMARY KEY, note_id INT NOT NULL,
 FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
 FOREIGN KEY (note_id) REFERENCES notes(note_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS ideas (
 idea_id INT AUTO_INCREMENT PRIMARY KEY, team_id INT NOT NULL,
 author_id INT NOT NULL, title VARCHAR(100) NOT NULL,
 content TEXT NOT NULL, link_url VARCHAR(500),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (team_id) REFERENCES teams(team_id),
 FOREIGN KEY (author_id) REFERENCES users(user_id)
);
CREATE TABLE IF NOT EXISTS idea_comments (
 comment_id INT AUTO_INCREMENT PRIMARY KEY, idea_id INT NOT NULL,
 author_id INT NOT NULL, content VARCHAR(1000) NOT NULL,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (idea_id) REFERENCES ideas(idea_id) ON DELETE CASCADE,
 FOREIGN KEY (author_id) REFERENCES users(user_id)
);
CREATE TABLE IF NOT EXISTS idea_reactions (
 idea_id INT NOT NULL, user_id INT NOT NULL,
 PRIMARY KEY (idea_id, user_id),
 FOREIGN KEY (idea_id) REFERENCES ideas(idea_id) ON DELETE CASCADE,
 FOREIGN KEY (user_id) REFERENCES users(user_id)
);
CREATE TABLE IF NOT EXISTS activity_log (
 activity_id INT AUTO_INCREMENT PRIMARY KEY, team_id INT NOT NULL,
 user_id INT NOT NULL, action VARCHAR(200) NOT NULL,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (team_id) REFERENCES teams(team_id),
 FOREIGN KEY (user_id) REFERENCES users(user_id)
);

START TRANSACTION;

-- 새 전용 DB에 처음 한 번 넣는 가상 시연 계정입니다.
INSERT INTO users (user_id,login_id,password,name,student_no) VALUES
(10,'demo1','demo1234','데모팀원1','20990001'),(20,'demo2','demo1234','데모팀원2','20990002'),
(30,'demo3','demo1234','데모팀원3','20990003'),(40,'demo4','demo1234','데모팀원4','20990004'),
(50,'demo5','demo1234','데모팀원5','20990005');
INSERT INTO teams (team_id,team_name,leader_id) VALUES (7,'시연 프로젝트',10),(42,'두 번째 프로젝트',20);
INSERT INTO team_members (team_id,user_id,role) VALUES
(7,10,'팀장'),(7,20,'부조장'),(7,30,'조원'),(7,40,'조원'),(7,50,'조원'),(42,10,'조원'),(42,20,'팀장');

COMMIT;
