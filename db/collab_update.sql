-- 협업 기능 추가용 SQL (feature/collab-tools 브랜치)
-- 프로그램을 켜면 CollabSchema 클래스가 아래 작업을 자동으로 하므로 직접 실행하지 않아도 됩니다.
-- MySQL Workbench에서 직접 반영하고 싶을 때만 실행하세요. 여러 번 실행해도 안전합니다
-- (단, 2번의 ALTER ... ADD COLUMN은 url 칸이 이미 있으면 "Duplicate column" 오류가 나는데 무시해도 됩니다).

USE team_project;

-- 1. 새 표 3개 (이미 있으면 건너뜀)
CREATE TABLE IF NOT EXISTS team_memo (
    team_id    INT PRIMARY KEY,
    content    TEXT,
    version    INT NOT NULL DEFAULT 0,
    updated_by INT NULL,
    updated_at DATETIME NULL,
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS board_posts (
    post_id    INT AUTO_INCREMENT PRIMARY KEY,
    team_id    INT NOT NULL,
    user_id    INT NOT NULL,
    content    VARCHAR(500) NOT NULL,
    color      VARCHAR(20) NOT NULL DEFAULT '노랑',
    likes      INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS pages (
    page_id    INT AUTO_INCREMENT PRIMARY KEY,
    team_id    INT NOT NULL,
    title      VARCHAR(100) NOT NULL,
    content    TEXT,
    updated_by INT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

-- 2. 자료 표를 자료 관리 코드(MaterialDAO)에 맞춤
ALTER TABLE materials ADD COLUMN url VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE materials MODIFY uploader_id INT NULL;
SET SQL_SAFE_UPDATES = 0; -- Workbench의 안전 모드(키 없는 UPDATE 막기)를 잠시 끔
UPDATE materials SET url = link_url
 WHERE (url IS NULL OR url = '') AND link_url IS NOT NULL;
SET SQL_SAFE_UPDATES = 1;

-- 확인
SHOW TABLES;
DESCRIBE materials;
