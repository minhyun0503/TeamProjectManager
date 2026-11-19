-- 기존 DB에서 한 번 적용합니다. 테이블이나 기존 자료는 삭제하지 않습니다.
-- schema.sql 전체를 기존 DB에 재실행하지 마세요.
USE team_project;

-- 이전 자료 화면 실행으로 url이 이미 추가된 경우에도 재실행할 수 있습니다.
SET @has_url = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'materials' AND column_name = 'url'
);
SET @add_url_sql = IF(@has_url = 0,
    'ALTER TABLE materials ADD COLUMN url VARCHAR(500) NULL',
    'SELECT 1');
PREPARE add_url_stmt FROM @add_url_sql;
EXECUTE add_url_stmt;
DEALLOCATE PREPARE add_url_stmt;

-- 새 url 값이 있는 자료는 그대로 두고, 비어 있는 자료만 기존 링크를 복사합니다.
UPDATE materials SET url = link_url
WHERE (url IS NULL OR TRIM(url) = '') AND link_url IS NOT NULL;

-- uploader_id의 NOT NULL과 외래키는 유지합니다.
-- 새 프로그램은 로그인한 user_id를 uploader_id에 저장합니다.
SELECT material_id, team_id, title, url, uploader_id FROM materials;
