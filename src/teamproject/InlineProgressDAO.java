package teamproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;

// [이동주 담당 2] 기존 DB에서 데이터를 읽습니다. DB 내용을 바꾸지는 않습니다.
public class InlineProgressDAO {

    public static InlineProgressData load(Connection conn, int teamId) throws SQLException {
        InlineProgressData data = new InlineProgressData();

        // 1. 이 팀에 소속된 사람을 가져옵니다.
        String memberSql = "SELECT u.user_id, u.name "
                + "FROM team_members m JOIN users u ON m.user_id = u.user_id "
                + "WHERE m.team_id = ? ORDER BY u.user_id";

        // try 괄호 안에서 만든 조회 도구는 사용이 끝나면 자동으로 닫힙니다.
        // 전달받은 conn(DB 연결)은 다른 화면도 사용하므로 여기서 닫지 않습니다.
        try (PreparedStatement pstmt = conn.prepareStatement(memberSql)) {
            pstmt.setInt(1, teamId); // 첫 번째 ?에 팀 번호를 넣습니다.
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String name = rs.getString("name");
                    data.getPeople().add(new InlineProgressData.Person(userId, name));
                }
            }
        }

        // 2. 이 팀의 업무를 가져옵니다. 다른 팀의 업무가 계산에 섞이지 않게 합니다.
        String taskSql = "SELECT t.assignee_id, t.status, t.due_date, u.name "
                + "FROM tasks t LEFT JOIN users u ON t.assignee_id = u.user_id "
                + "WHERE t.team_id = ? ORDER BY t.task_id";

        try (PreparedStatement pstmt = conn.prepareStatement(taskSql)) {
            pstmt.setInt(1, teamId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int assigneeId = rs.getInt("assignee_id");
                    String status = rs.getString("status");
                    Date dbDate = rs.getDate("due_date");
                    LocalDate dueDate = null;
                    if (dbDate != null) {
                        dueDate = dbDate.toLocalDate();
                    }
                    data.getTasks().add(new InlineProgressData.Task(assigneeId, status, dueDate));

                    // 팀에서 나갔지만 업무 담당자로 남은 사람도 선택할 수 있게 합니다.
                    boolean found = false;
                    for (int i = 0; i < data.getPeople().size(); i++) {
                        if (data.getPeople().get(i).getUserId() == assigneeId) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        String name = rs.getString("name");
                        if (name == null) {
                            name = "담당자 정보 없음";
                        }
                        data.getPeople().add(new InlineProgressData.Person(assigneeId, name));
                    }
                }
            }
        }
        return data;
    }
}
