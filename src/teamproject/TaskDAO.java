package teamproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class TaskDAO {//데이터베이스 조회 및 관리 클래스

    // 데이터베이스에 저장된 업무 목록 조회 메서드
    public static void showTasks(Connection conn)//Connection conn 데이터베이스 연결 객체를 전달받는 매개변수
            throws SQLException {

        String sql = """
                SELECT
                    tasks.task_id,
                    tasks.title,
                    users.name,
                    tasks.due_date,
                    tasks.status
                FROM tasks
                JOIN users
                    ON tasks.assignee_id = users.user_id
                ORDER BY tasks.task_id
                """;

        // SQL 실행 및 결과 가져오기
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
        	//PreparedStatement : SQL을 준비하고 실행하는 객체의 자료형, conn: main문에서 전달받은 연결객체
    		//prepareStatement(sql): 실행할 SQL을 준비하는 메서드
        	//ResultSet: SQL 조회 결과를 보관하고 읽는 인터페이스
        	//executeQuery() : SQL조회 실행 후 결과반환 하는 메서드

            // 조회된 업무를 한 줄씩 출력
            while (rs.next()) {
                System.out.println(
                        rs.getInt("task_id") + " | " +
                        rs.getString("title") + " | " +
                        rs.getString("name") + " | " +
                        rs.getDate("due_date") + " | " +
                        rs.getString("status")
                );
            }
        }
    }

    // 조회한 업무 데이터를 GUI 표에 추가
    public static void loadTasks(
            Connection conn,
            DefaultTableModel tableModel) throws SQLException {

        String sql = """
        SELECT
            tasks.task_id,
            tasks.title,
            users.name,
            tasks.due_date,
            tasks.status
        FROM tasks
        JOIN users
            ON tasks.assignee_id = users.user_id
        ORDER BY tasks.task_id
        """;

        // 기존 표 데이터 초기화
        tableModel.setRowCount(0);

        // SQL 실행 및 업무 데이터 조회
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                Object[] row = {
                    rs.getInt("task_id"),
                    rs.getString("title"),
                    rs.getString("name"),
                    rs.getDate("due_date"),
                    rs.getString("status")
                };

                // 조회한 업무를 표에 추가
                tableModel.addRow(row);
            }
        }
    }
}
