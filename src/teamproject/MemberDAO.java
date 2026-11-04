	package teamproject;

	// 데이터베이스 관련 클래스
	import java.sql.Connection;
	import java.sql.PreparedStatement;
	import java.sql.ResultSet;
	import java.sql.SQLException;

	// GUI 표 데이터 관리 클래스
	import javax.swing.table.DefaultTableModel;

	public class MemberDAO {// 팀원 데이터 조회 및 관리 클래스

	    // 데이터베이스에서 팀원 목록을 가져와 GUI 표에 표시하는 메서드
	    public static void loadMembers(
	            Connection conn,
	            DefaultTableModel tableModel,
	            int teamId) throws SQLException {

	        // users와 team_members 테이블을 user_id로 연결
	        String sql = """
	                SELECT
	                    users.user_id,
	                    users.login_id,
	                    users.name,
	                    users.student_no,
	                    team_members.role,
	                    team_members.joined_at
	                FROM team_members
	                JOIN users
	                    ON team_members.user_id = users.user_id
	                WHERE team_members.team_id = ?
	                ORDER BY users.user_id
	                """;

	        // 기존 표에 들어 있는 데이터 삭제
	        tableModel.setRowCount(0);

	        // SQL 준비 및 실행
	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

	            // ? 자리에 조회할 팀 번호 입력
	            pstmt.setInt(1, teamId); //1번으로 고정(수정필요!!)

	            // SQL 실행 후 조회 결과 저장
	            try (ResultSet rs = pstmt.executeQuery()) {

	                // 조회 결과가 있는 동안 반복
	                while (rs.next()) {

	                    // 데이터베이스에서 가져온 정보를 한 행으로 구성
	                    Object[] row = {
	                        rs.getInt("user_id"),
	                        rs.getString("login_id"),
	                        rs.getString("name"),
	                        rs.getString("student_no"),
	                        rs.getString("role"),
	                        rs.getTimestamp("joined_at")
	                    };

	                    // GUI 표에 한 행 추가
	                    tableModel.addRow(row);
	                }
	            }
	        }
	    }

    // 새로운 팀원을 데이터베이스에 등록하는 메서드
    public static void insertMember(
            Connection conn,
            int teamId,
            String loginId,
            String password,
            String name,
            String role) throws SQLException {

        // users 테이블에 사용자 정보를 저장하는 SQL
        String userSql = """
                INSERT INTO users (login_id, password, name, student_no)
                VALUES (?, ?, ?, ?)
                """;

        // team_members 테이블에 팀 소속 정보를 저장하는 SQL
        String memberSql = """
                INSERT INTO team_members (team_id, user_id, role)
                VALUES (?, ?, ?)
                """;

        // 기존 자동 커밋 설정 저장
        boolean originalAutoCommit = conn.getAutoCommit();

        try {
            // 두 번의 INSERT를 하나의 작업으로 처리
            conn.setAutoCommit(false);

            int userId = 0;

            // 먼저 users 테이블에 사용자 등록
            try (PreparedStatement pstmt =
                    conn.prepareStatement(userSql,
                            PreparedStatement.RETURN_GENERATED_KEYS)) {

                // ? 자리에 사용자 정보 입력
                pstmt.setString(1, loginId);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.setString(4, loginId);

                // 사용자 정보 등록 실행
                pstmt.executeUpdate();

                // 자동 생성된 user_id 가져오기
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    } else {
                        throw new SQLException("회원 번호를 가져오지 못했습니다.");
                    }
                }
                
            }

            // team_members 테이블에 팀원 정보 등록
            try (PreparedStatement pstmt = conn.prepareStatement(memberSql)) {

                // 팀 번호, 사용자 번호, 역할 입력
                pstmt.setInt(1, teamId);
                pstmt.setInt(2, userId);
                pstmt.setString(3, role);

                // 팀원 등록 실행
                pstmt.executeUpdate();
            }

            // 두 등록 작업 모두 성공하면 저장
            conn.commit();

        } catch (SQLException e) {

            // 오류가 발생하면 등록 작업 전체 취소
            conn.rollback();

            // 오류를 호출한 곳으로 전달
            throw e;

        } finally {

            // 기존 자동 커밋 설정으로 복원
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    // 기존 팀원의 정보를 수정하는 메서드
    public static void updateMember(
            Connection conn,
            int teamId,
            int userId,
            String name,
            String studentNo,
            String role) throws SQLException {

        // users 테이블의 이름과 학번을 수정하는 SQL
        String userSql = """
                UPDATE users
                SET name = ?, student_no = ?
                WHERE user_id = ?
                """;

        // team_members 테이블의 역할을 수정하는 SQL
        String memberSql = """
                UPDATE team_members
                SET role = ?
                WHERE team_id = ? AND user_id = ?
                """;

        // 기존 자동 커밋 설정 저장
        boolean originalAutoCommit = conn.getAutoCommit();

        try {

            // 두 테이블의 수정을 하나의 작업으로 처리
            conn.setAutoCommit(false);

            // users 테이블 정보 수정
            try (PreparedStatement pstmt = conn.prepareStatement(userSql)) {

                // 수정할 이름, 학번, 회원 번호 입력
                pstmt.setString(1, name);
                pstmt.setString(2, studentNo);
                pstmt.setInt(3, userId);

                // SQL 실행
                pstmt.executeUpdate();
            }

            // team_members 테이블 역할 수정
            try (PreparedStatement pstmt = conn.prepareStatement(memberSql)) {

                // 수정할 역할, 팀 번호, 회원 번호 입력
                pstmt.setString(1, role);
                pstmt.setInt(2, teamId);
                pstmt.setInt(3, userId);

                // SQL 실행
                pstmt.executeUpdate();
            }

            // 두 수정 작업 모두 성공하면 저장
            conn.commit();

        } catch (SQLException e) {

            // 오류가 발생하면 수정 작업 취소
            conn.rollback();

            // 오류를 호출한 곳으로 전달
            throw e;

        } finally {

            // 기존 자동 커밋 설정으로 복원
            conn.setAutoCommit(originalAutoCommit);
        }
    }

 // 팀에서 팀원 삭제
 public static void deleteMember(Connection conn, int teamId, int userId) throws SQLException {

     // 팀원 정보를 삭제하는 SQL문
     String sql = """
         DELETE FROM team_members
         WHERE team_id = ? AND user_id = ?
         """;

     // SQL문 실행 준비
     try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

         // 첫 번째 물음표에 팀 번호 설정
         pstmt.setInt(1, teamId);

         // 두 번째 물음표에 회원 번호 설정
         pstmt.setInt(2, userId);

         // 삭제 SQL 실행
         pstmt.executeUpdate();
     }
 }
	}
