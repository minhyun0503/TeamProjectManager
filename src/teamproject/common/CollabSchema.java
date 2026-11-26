package teamproject.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import teamproject.material.MaterialDAO;

// 협업 기능(메모장 · 포스트잇 보드 · 문서)에 필요한 DB 표를 준비하는 클래스
// - 프로그램을 켤 때 MainFrame에서 한 번 호출한다.
// - CREATE TABLE IF NOT EXISTS 라서 표가 이미 있으면 아무것도 바꾸지 않는다.
// - 같은 SQL이 db/collab_update.sql 에도 있어서 Workbench로 직접 실행해도 된다.
public class CollabSchema {

    // ① 팀 공동 메모장: 팀마다 메모 1개
    //    version: 저장할 때마다 1씩 올라가는 번호 (다른 사람이 먼저 저장했는지 확인용)
    private static final String CREATE_TEAM_MEMO =
            "CREATE TABLE IF NOT EXISTS team_memo ("
            + " team_id    INT PRIMARY KEY,"
            + " content    TEXT,"
            + " version    INT NOT NULL DEFAULT 0,"
            + " updated_by INT NULL,"
            + " updated_at DATETIME NULL,"
            + " FOREIGN KEY (team_id) REFERENCES teams(team_id),"
            + " FOREIGN KEY (updated_by) REFERENCES users(user_id)"
            + ")";

    // ② 포스트잇 보드(패들렛 스타일): 팀원이 붙이는 짧은 메모 카드
    private static final String CREATE_BOARD_POSTS =
            "CREATE TABLE IF NOT EXISTS board_posts ("
            + " post_id    INT AUTO_INCREMENT PRIMARY KEY,"
            + " team_id    INT NOT NULL,"
            + " user_id    INT NOT NULL,"
            + " content    VARCHAR(500) NOT NULL,"
            + " color      VARCHAR(20) NOT NULL DEFAULT '노랑',"
            + " likes      INT NOT NULL DEFAULT 0,"
            + " created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + " FOREIGN KEY (team_id) REFERENCES teams(team_id),"
            + " FOREIGN KEY (user_id) REFERENCES users(user_id)"
            + ")";

    // ③ 문서 페이지(노션 스타일): 회의록 · 기획서 같은 긴 글
    private static final String CREATE_PAGES =
            "CREATE TABLE IF NOT EXISTS pages ("
            + " page_id    INT AUTO_INCREMENT PRIMARY KEY,"
            + " team_id    INT NOT NULL,"
            + " title      VARCHAR(100) NOT NULL,"
            + " content    TEXT,"
            + " updated_by INT NULL,"
            + " updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + " FOREIGN KEY (team_id) REFERENCES teams(team_id),"
            + " FOREIGN KEY (updated_by) REFERENCES users(user_id)"
            + ")";

    // 표 3개를 차례대로 만든다 (이미 있으면 건너뜀)
    public static void prepare(Connection conn) throws SQLException {
        String[] sqlList = {CREATE_TEAM_MEMO, CREATE_BOARD_POSTS, CREATE_PAGES};

        try (Statement stmt = conn.createStatement()) {
            for (String sql : sqlList) {
                stmt.executeUpdate(sql);
            }
        }

        fixMaterials(conn);
    }

    // 자료 관리 코드(MaterialDAO)는 링크를 url 칸에 저장하고 올린 사람(uploader_id)을 넣지 않는다.
    // 처음 설계한 materials 표와 달라서 등록이 실패하므로, 표를 자료 관리 코드에 맞춰 준다.
    private static void fixMaterials(Connection conn) {

        // 1. url 칸이 없으면 추가 (MaterialDAO에 원래 있던 기능)
        MaterialDAO.createTableIfNotExist(conn);

        try (Statement stmt = conn.createStatement()) {

            // 2. 올린 사람을 비워도 등록되게 변경 (이미 바뀌었으면 그대로)
            try {
                stmt.executeUpdate("ALTER TABLE materials MODIFY uploader_id INT NULL");
            } catch (SQLException e) {
                // uploader_id 칸이 없는 DB라면 할 일이 없으므로 무시
            }

            // 3. 예전 link_url 칸의 링크를 새 url 칸으로 복사 (비어 있는 것만)
            try {
                stmt.executeUpdate("UPDATE materials SET url = link_url"
                        + " WHERE (url IS NULL OR url = '') AND link_url IS NOT NULL");
            } catch (SQLException e) {
                // link_url 칸이 없는 DB라면 옮길 것이 없으므로 무시
            }

        } catch (SQLException e) {
            // 자료 표 정리에 실패해도 다른 기능은 쓸 수 있으므로 프로그램은 계속 진행
        }
    }
}
