package teamproject;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.swing.table.DefaultTableModel;

// 팀 일정(schedules 표)을 다루는 클래스
// schedules 표는 처음 DB 설계 때 만들어 두었고, 이 클래스로 화면과 연결한다.
public class ScheduleDAO {

    // 일정 하나의 정보를 담는 상자
    public static class ScheduleInfo {
        private final int scheduleId;
        private final String title;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String memo;

        public ScheduleInfo(int scheduleId, String title, LocalDate startDate,
                            LocalDate endDate, String memo) {
            this.scheduleId = scheduleId;
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.memo = memo;
        }

        public int getScheduleId() { return scheduleId; }
        public String getTitle() { return title; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getMemo() { return memo; }
    }

    // 팀 일정을 표(tableModel)에 채운다. 시작일이 빠른 순서
    public static void loadSchedules(Connection conn, DefaultTableModel tableModel, int teamId)
            throws SQLException {

        String sql = "SELECT schedule_id, title, start_date, end_date, memo"
                + " FROM schedules WHERE team_id = ? ORDER BY start_date, schedule_id";

        tableModel.setRowCount(0); // 기존 줄 모두 지우기

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate start = rs.getDate("start_date").toLocalDate();
                    LocalDate end = rs.getDate("end_date").toLocalDate();
                    String memo = rs.getString("memo");

                    tableModel.addRow(new Object[]{
                            rs.getInt("schedule_id"),
                            rs.getString("title"),
                            start.toString(),
                            end.toString(),
                            dDayText(start, end),
                            memo == null ? "" : memo
                    });
                }
            }
        }
    }

    // 오늘 기준 남은 날짜 안내 글자
    //  시작 전: D-3 / 시작일 당일: D-DAY / 기간 안: 진행 중 / 끝난 뒤: 종료
    public static String dDayText(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(start)) {
            long days = ChronoUnit.DAYS.between(today, start); // 오늘부터 시작일까지 며칠
            return "D-" + days;
        }
        if (today.isEqual(start)) {
            return "D-DAY";
        }
        if (!today.isAfter(end)) {
            return "진행 중";
        }
        return "종료";
    }

    // 일정 하나 읽기 (없으면 null)
    public static ScheduleInfo findSchedule(Connection conn, int scheduleId) throws SQLException {

        String sql = "SELECT schedule_id, title, start_date, end_date, memo FROM schedules WHERE schedule_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ScheduleInfo(
                            rs.getInt("schedule_id"),
                            rs.getString("title"),
                            rs.getDate("start_date").toLocalDate(),
                            rs.getDate("end_date").toLocalDate(),
                            rs.getString("memo"));
                }
                return null;
            }
        }
    }

    // 일정 등록
    public static void insertSchedule(Connection conn, int teamId, String title,
                                      LocalDate start, LocalDate end, String memo) throws SQLException {

        String sql = "INSERT INTO schedules (team_id, title, start_date, end_date, memo) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);
            pstmt.setString(2, title);
            pstmt.setDate(3, Date.valueOf(start)); // LocalDate → SQL 날짜
            pstmt.setDate(4, Date.valueOf(end));
            pstmt.setString(5, memo);
            pstmt.executeUpdate();
        }
    }

    // 일정 수정
    public static boolean updateSchedule(Connection conn, int scheduleId, String title,
                                         LocalDate start, LocalDate end, String memo) throws SQLException {

        String sql = "UPDATE schedules SET title = ?, start_date = ?, end_date = ?, memo = ? WHERE schedule_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setDate(2, Date.valueOf(start));
            pstmt.setDate(3, Date.valueOf(end));
            pstmt.setString(4, memo);
            pstmt.setInt(5, scheduleId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 일정 삭제
    public static boolean deleteSchedule(Connection conn, int scheduleId) throws SQLException {

        String sql = "DELETE FROM schedules WHERE schedule_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            return pstmt.executeUpdate() > 0;
        }
    }
}
