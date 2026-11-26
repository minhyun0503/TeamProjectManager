package teamproject.collab;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 칸반 보드에 보여줄 업무 카드를 읽어 오는 클래스
// 업무 등록 · 수정 · 상태 변경은 기존 TaskDAO를 그대로 사용한다.
public class KanbanDAO {

    // 칸반 카드 한 장 = 업무 한 개
    public static class Card {
        private final int taskId;
        private final String title;
        private final String assigneeName;
        private final String dueDate;
        private final String status;

        public Card(int taskId, String title, String assigneeName, String dueDate, String status) {
            this.taskId = taskId;
            this.title = title;
            this.assigneeName = assigneeName;
            this.dueDate = dueDate;
            this.status = status;
        }

        public int getTaskId() { return taskId; }
        public String getStatus() { return status; }

        // JList가 목록에 보여줄 글자: [마감일] 업무명 - 담당자
        @Override
        public String toString() {
            return "[" + dueDate + "] " + title + " - " + assigneeName;
        }
    }

    // 팀의 업무를 마감일 순서로 읽어 온다
    public static List<Card> loadCards(Connection conn, int teamId) throws SQLException {

        String sql = "SELECT t.task_id, t.title, u.name, t.due_date, t.status"
                + " FROM tasks t"
                + " JOIN users u ON t.assignee_id = u.user_id"
                + " WHERE t.team_id = ?"
                + " ORDER BY t.due_date, t.task_id";

        List<Card> cards = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(new Card(
                            rs.getInt("task_id"),
                            rs.getString("title"),
                            rs.getString("name"),
                            rs.getDate("due_date").toString(),
                            rs.getString("status")));
                }
            }
        }
        return cards;
    }
}
