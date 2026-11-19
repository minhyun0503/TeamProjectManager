package teamproject.common;

// 브리핑 담당: 장*민 | 파일별 설명: docs/02_common.md

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

// 새 버전 전용 DB를 준비합니다. 원본 프로그램의 DB에는 연결하지 않습니다.
public class SchemaSetup {
  public static void initialize(Connection conn, boolean demo) throws Exception {
    try (InputStream in = SchemaSetup.class.getResourceAsStream("schema.sql")) {
      if (in == null) throw new IllegalStateException("schema.sql 리소스가 없습니다. 빌드 안내를 확인하세요.");
      String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      sql = sql.replaceAll("(?m)--.*$", "");
      try (Statement stmt = conn.createStatement()) {
        for (String command : sql.split(";")) {
          if (!command.isBlank()) stmt.execute(command);
        }
      }
    }
    if (demo) seedDemo(conn);
  }

  private static void seedDemo(Connection conn) throws Exception {
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
      rs.next();
      if (rs.getInt(1) != 0) return; // 이미 저장한 데모 데이터는 덮어쓰지 않습니다.
    }
    conn.setAutoCommit(false);
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(
          "INSERT INTO users (user_id,login_id,password,name,student_no) VALUES "
              + "(10,'demo1','demo1234','데모팀원1','20990001'),"
              + "(20,'demo2','demo1234','데모팀원2','20990002'),"
              + "(30,'demo3','demo1234','데모팀원3','20990003'),"
              + "(40,'demo4','demo1234','데모팀원4','20990004'),"
              + "(50,'demo5','demo1234','데모팀원5','20990005')");
      stmt.executeUpdate(
          "INSERT INTO teams (team_id,team_name,leader_id) VALUES (7,'시연 프로젝트',10),(42,'두 번째"
              + " 프로젝트',20)");
      stmt.executeUpdate(
          "INSERT INTO team_members (team_id,user_id,role) VALUES "
              + "(7,10,'팀장'),(7,20,'부조장'),(7,30,'조원'),(7,40,'조원'),(7,50,'조원'),(42,10,'조원'),(42,20,'팀장')");
      stmt.executeUpdate(
          "INSERT INTO tasks (team_id,title,assignee_id,status,due_date) VALUES "
              + "(7,'프로젝트 코드 브리핑 준비',10,'할 일',CURRENT_DATE),"
              + "(7,'업무 화면 확인',30,'진행 중',CURRENT_DATE),(42,'다른 팀 업무',20,'완료',CURRENT_DATE)");
      stmt.executeUpdate(
          "INSERT INTO notes (team_id,title,content,author_id,updated_by) VALUES (7,'첫 회의','각자 담당"
              + " 코드를 설명하고 개선 의견을 남겨 주세요.',10,10)");
      stmt.executeUpdate(
          "INSERT INTO note_history (note_id,version,content,editor_id) SELECT"
              + " note_id,version,content,updated_by FROM notes");
      stmt.executeUpdate(
          "INSERT INTO schedules (team_id,title,start_date,end_date,memo) VALUES (7,'팀원 코드"
              + " 브리핑',CURRENT_DATE,CURRENT_DATE,'설명서를 보고 담당 흐름을 설명합니다.')");
      stmt.executeUpdate(
          "INSERT INTO ideas (team_id,author_id,title,content,link_url) VALUES (7,20,'브리핑 순서를"
              + " 정해볼까요?','공통 실행 흐름부터 시작하고 각 담당 기능으로 이어가면 좋겠습니다.','https://example.com')");
      conn.commit();
    } catch (Exception e) {
      conn.rollback();
      throw e;
    } finally {
      conn.setAutoCommit(true);
    }
  }
}
