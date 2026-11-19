package teamproject.task;

// 브리핑 담당: 윤*훈 | 파일별 설명: docs/04_task.md

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate; // [업무 관리 추가] 마감일(날짜) 처리
import java.util.ArrayList; // [업무 관리 추가] 담당자 목록 저장
import java.util.List; // [업무 관리 추가] 담당자 목록 저장
import javax.swing.table.DefaultTableModel;

public class TaskDAO { // 데이터베이스 조회 및 관리 클래스

  // 선택한 팀의 업무만 조회합니다.
  public static void showTasks(Connection conn, int teamId) throws SQLException {
    String sql =
        """
        SELECT t.task_id, t.title, u.name, t.due_date, t.status
        FROM tasks t
        JOIN users u ON t.assignee_id = u.user_id
        WHERE t.team_id = ?
        ORDER BY t.task_id
        """;
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, teamId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          System.out.println(
              rs.getInt("task_id")
                  + " | "
                  + rs.getString("title")
                  + " | "
                  + rs.getString("name")
                  + " | "
                  + rs.getDate("due_date")
                  + " | "
                  + rs.getString("status"));
        }
      }
    }
  }

  // 조회한 업무 데이터를 GUI 표에 추가합니다. 진행률 패널의 마감 안내 열은 유지합니다.
  public static void loadTasks(Connection conn, DefaultTableModel tableModel, int teamId)
      throws SQLException {
    String sql =
        """
        SELECT t.task_id, t.title, u.name, t.due_date, t.status
        FROM tasks t
        JOIN users u ON t.assignee_id = u.user_id
        WHERE t.team_id = ?
        ORDER BY t.task_id
        """;
    tableModel.setRowCount(0);
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, teamId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          Object[] row = {
            rs.getInt("task_id"),
            rs.getString("title"),
            rs.getString("name"),
            rs.getDate("due_date"),
            rs.getString("status")
          };
          tableModel.addRow(row);
        }
      }
    }
  }

  // ================================================================
  // [업무 관리 추가] 업무 등록 · 수정 · 삭제 · 완료 처리
  //  - MemberDAO와 같은 방식(static 메서드 + Connection을 매개변수로 전달)으로 작성
  //  - 모든 SQL은 PreparedStatement(?)를 사용해서 SQL 인젝션을 막는다.
  // ================================================================

  // 업무 상태로 사용하는 값 (tasks.status 컬럼에 저장되는 문자열)
  // schema.sql의 기본값 '할 일', 샘플 데이터의 '완료'와 같은 표기를 사용한다.
  // 진행률 담당(완료 개수 / 전체 개수 계산)도 STATUS_DONE을 가져다 쓰면 오타를 막을 수 있다.
  public static final String STATUS_TODO = "할 일";
  public static final String STATUS_IN_PROGRESS = "진행 중";
  public static final String STATUS_DONE = "완료";
  public static final String[] STATUS_LIST = {STATUS_TODO, STATUS_IN_PROGRESS, STATUS_DONE};

  // 담당자 선택 상자(JComboBox)에 넣을 팀원 한 명의 정보 (회원 번호 + 이름)
  public static class Assignee {

    private final int userId; // users.user_id (DB에 저장할 때 사용)
    private final String name; // users.name (화면에 보여줄 이름)

    public Assignee(int userId, String name) {
      this.userId = userId;
      this.name = name;
    }

    public int getUserId() {
      return userId;
    }

    public String getName() {
      return name;
    }

    // 콤보박스에 번호가 아니라 이름이 보이도록 한다
    @Override
    public String toString() {
      return name;
    }
  }

  // 업무 한 건의 상세 정보 (수정 화면에 기존 내용을 채우거나, 입력한 내용을 전달하는 용도)
  public static class TaskInfo {

    private final int taskId; // 업무 번호 (등록 전에는 0)
    private final String title; // 업무명
    private final String description; // 상세 설명
    private final int assigneeId; // 담당자 회원 번호
    private final String status; // 상태 ("할 일" / "진행 중" / "완료")
    private final LocalDate dueDate; // 마감일

    public TaskInfo(
        int taskId,
        String title,
        String description,
        int assigneeId,
        String status,
        LocalDate dueDate) {
      this.taskId = taskId;
      this.title = title;
      this.description = description;
      this.assigneeId = assigneeId;
      this.status = status;
      this.dueDate = dueDate;
    }

    public int getTaskId() {
      return taskId;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public int getAssigneeId() {
      return assigneeId;
    }

    public String getStatus() {
      return status;
    }

    public LocalDate getDueDate() {
      return dueDate;
    }
  }

  // 담당자 선택 목록용: 해당 팀에 소속된 팀원 목록 조회
  public static List<Assignee> getTeamMembers(Connection conn, int teamId) throws SQLException {

    // team_members(팀 소속)와 users(사용자)를 user_id로 연결해서 이름을 가져온다
    String sql =
        """
        SELECT
            users.user_id,
            users.name
        FROM team_members
        JOIN users
            ON team_members.user_id = users.user_id
        WHERE team_members.team_id = ?
        ORDER BY users.user_id
        """;

    List<Assignee> list = new ArrayList<>();

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // ? 자리에 조회할 팀 번호 입력
      pstmt.setInt(1, teamId);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          // 조회된 팀원 한 명을 목록에 추가
          list.add(new Assignee(rs.getInt("user_id"), rs.getString("name")));
        }
      }
    }
    return list;
  }

  // 업무 한 건 조회 (수정 화면에 기존 내용을 채울 때 사용). 없으면 null 반환
  public static TaskInfo findTask(Connection conn, int teamId, int taskId) throws SQLException {

    String sql =
        """
        SELECT
            title,
            description,
            assignee_id,
            status,
            due_date
        FROM tasks
        WHERE task_id = ? AND team_id = ?
        """;

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, taskId);
      pstmt.setInt(2, teamId);

      try (ResultSet rs = pstmt.executeQuery()) {

        // 조회된 줄이 없으면(이미 삭제된 업무) null 반환
        if (!rs.next()) {
          return null;
        }

        return new TaskInfo(
            taskId,
            rs.getString("title"),
            rs.getString("description"), // TEXT 컬럼은 null일 수 있다
            rs.getInt("assignee_id"),
            rs.getString("status"),
            rs.getDate("due_date").toLocalDate() // java.sql.Date -> LocalDate 변환
            );
      }
    }
  }

  // 새 업무를 데이터베이스에 등록하는 메서드
  public static void insertTask(
      Connection conn,
      int teamId,
      String title,
      String description,
      int assigneeId,
      String status,
      LocalDate dueDate)
      throws SQLException {

    requireTeamMember(conn, teamId, assigneeId);

    // tasks 테이블에 업무를 저장하는 SQL
    String sql =
        """
        INSERT INTO tasks (team_id, title, description, assignee_id, status, due_date)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // ? 자리에 순서대로 값 입력
      pstmt.setInt(1, teamId);
      pstmt.setString(2, title);
      pstmt.setString(3, description);
      pstmt.setInt(4, assigneeId);
      pstmt.setString(5, status);
      pstmt.setDate(6, java.sql.Date.valueOf(dueDate)); // LocalDate -> java.sql.Date 변환

      // INSERT / UPDATE / DELETE는 executeUpdate()로 실행
      pstmt.executeUpdate();
    }
  }

  // 기존 업무의 내용을 수정하는 메서드 (업무명, 설명, 담당자, 상태, 마감일)
  // 반환값: 수정된 줄이 있으면 true, 이미 삭제된 업무라 수정할 줄이 없으면 false
  public static boolean updateTask(
      Connection conn,
      int teamId,
      int taskId,
      String title,
      String description,
      int assigneeId,
      String status,
      LocalDate dueDate)
      throws SQLException {

    requireTeamMember(conn, teamId, assigneeId);

    String sql =
        """
        UPDATE tasks
        SET title = ?, description = ?, assignee_id = ?, status = ?, due_date = ?
        WHERE task_id = ? AND team_id = ?
        """;

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, title);
      pstmt.setString(2, description);
      pstmt.setInt(3, assigneeId);
      pstmt.setString(4, status);
      pstmt.setDate(5, java.sql.Date.valueOf(dueDate));
      pstmt.setInt(6, taskId); // WHERE 조건: 어떤 업무를 수정할지
      pstmt.setInt(7, teamId);

      // executeUpdate()는 영향받은 줄 수를 돌려준다
      return pstmt.executeUpdate() > 0;
    }
  }

  // 업무 상태만 바꾸는 메서드 (예: 완료 처리 버튼)
  public static boolean updateTaskStatus(Connection conn, int teamId, int taskId, String status)
      throws SQLException {

    String sql = "UPDATE tasks SET status = ? WHERE task_id = ? AND team_id = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, status);
      pstmt.setInt(2, taskId);
      pstmt.setInt(3, teamId);
      return pstmt.executeUpdate() > 0;
    }
  }

  // 업무 삭제 메서드. 삭제된 줄이 있으면 true
  public static boolean deleteTask(Connection conn, int teamId, int taskId) throws SQLException {

    String sql = "DELETE FROM tasks WHERE task_id = ? AND team_id = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, taskId);
      pstmt.setInt(2, teamId);
      return pstmt.executeUpdate() > 0;
    }
  }

  private static void requireTeamMember(Connection conn, int teamId, int userId)
      throws SQLException {
    String sql = "SELECT 1 FROM team_members WHERE team_id = ? AND user_id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, teamId);
      pstmt.setInt(2, userId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (!rs.next()) throw new SQLException("선택한 담당자가 현재 팀에 소속되어 있지 않습니다.");
      }
    }
  }

  // [피드백 추가] 보드 카드 한 장에 필요한 값만 담습니다.
  public static class Card {
    public final int id, assigneeId;
    public final String title, name, status;
    public final LocalDate due;

    public Card(int id, int assigneeId, String title, String name, String status, LocalDate due) {
      this.id = id;
      this.assigneeId = assigneeId;
      this.title = title;
      this.name = name;
      this.status = status;
      this.due = due;
    }

    public String toString() {
      return title + " / " + name + " / " + due;
    }
  }

  public static List<Card> board(Connection conn, int teamId, int userId, boolean mine)
      throws SQLException {
    List<Card> rows = new ArrayList<>();
    String sql =
        "SELECT t.*,u.name FROM tasks t JOIN users u ON t.assignee_id=u.user_id "
            + "WHERE t.team_id=? AND (?=0 OR t.assignee_id=?) ORDER BY t.due_date,t.task_id";
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, teamId);
      p.setInt(2, mine ? 1 : 0);
      p.setInt(3, userId);
      try (ResultSet r = p.executeQuery()) {
        while (r.next())
          rows.add(
              new Card(
                  r.getInt("task_id"),
                  r.getInt("assignee_id"),
                  r.getString("title"),
                  r.getString("name"),
                  r.getString("status"),
                  r.getDate("due_date").toLocalDate()));
      }
    }
    return rows;
  }

  // 업무와 연결 행이 모두 저장돼야 성공입니다. 하나라도 실패하면 전부 취소합니다.
  public static void insertFromNote(
      Connection conn,
      int teamId,
      int noteId,
      String title,
      String description,
      int assigneeId,
      LocalDate due)
      throws SQLException {
    requireTeamMember(conn, teamId, assigneeId);
    conn.setAutoCommit(false);
    try {
      try (PreparedStatement p =
          conn.prepareStatement("SELECT 1 FROM notes WHERE note_id=? AND team_id=?")) {
        p.setInt(1, noteId);
        p.setInt(2, teamId);
        try (ResultSet r = p.executeQuery()) {
          if (!r.next()) throw new SQLException("현재 팀의 메모가 아닙니다.");
        }
      }
      int taskId;
      try (PreparedStatement p =
          conn.prepareStatement(
              "INSERT INTO tasks (team_id,title,description,assignee_id,status,due_date) VALUES"
                  + " (?,?,?,?,?,?)",
              Statement.RETURN_GENERATED_KEYS)) {
        p.setInt(1, teamId);
        p.setString(2, title);
        p.setString(3, description);
        p.setInt(4, assigneeId);
        p.setString(5, STATUS_TODO);
        p.setDate(6, java.sql.Date.valueOf(due));
        p.executeUpdate();
        try (ResultSet r = p.getGeneratedKeys()) {
          r.next();
          taskId = r.getInt(1);
        }
      }
      try (PreparedStatement p =
          conn.prepareStatement("INSERT INTO task_notes (task_id,note_id) VALUES (?,?)")) {
        p.setInt(1, taskId);
        p.setInt(2, noteId);
        p.executeUpdate();
      }
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    } finally {
      conn.setAutoCommit(true);
    }
  }

  public static String sourceNote(Connection conn, int teamId, int taskId) throws SQLException {
    String sql =
        "SELECT n.title,n.content FROM task_notes x JOIN tasks t ON x.task_id=t.task_id JOIN notes"
            + " n ON x.note_id=n.note_id WHERE t.task_id=? AND t.team_id=? AND n.team_id=?";
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, taskId);
      p.setInt(2, teamId);
      p.setInt(3, teamId);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? r.getString(1) + "\n\n" + r.getString(2) : "연결된 메모가 없습니다.";
      }
    }
  }
}
