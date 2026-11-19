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
