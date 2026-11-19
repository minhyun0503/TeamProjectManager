package teamproject.common;

// 브리핑 담당: 장*민 | 파일별 설명: docs/02_common.md

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import javax.swing.*;
import teamproject.member.LoginFrame;

public class Main {
  public static void main(String[] args) {
    // Swing 화면 생성은 이벤트 처리 스레드(EDT)에서 시작합니다.
    SwingUtilities.invokeLater(Main::start);
  }

  private static void start() {
    String[] modes = {"로컬 데모", "MySQL 연결"};
    int mode =
        JOptionPane.showOptionDialog(
            null,
            "실행 방식을 선택하세요.",
            "피드백 반영 버전",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            modes,
            modes[0]);
    if (mode < 0) return;
    try {
      if (mode == 0) {
        Path dir = Path.of(System.getProperty("user.home"), ".teamproject-feedback");
        Files.createDirectories(dir);
        DB_connection.configure(
            "jdbc:h2:file:"
                + dir.resolve("demo").toAbsolutePath()
                + ";MODE=MySQL;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE",
            "sa",
            "");
      } else {
        JTextField url =
            new JTextField("jdbc:mysql://localhost:3306/team_project_feedback?sslMode=PREFERRED");
        JTextField user = new JTextField("root");
        JPasswordField password = new JPasswordField();
        JPanel form = new JPanel(new java.awt.GridLayout(0, 1));
        form.add(new JLabel("새 버전 전용 DB 주소"));
        form.add(url);
        form.add(new JLabel("DB 사용자"));
        form.add(user);
        form.add(new JLabel("DB 비밀번호"));
        form.add(password);
        if (JOptionPane.showConfirmDialog(null, form, "MySQL 연결", JOptionPane.OK_CANCEL_OPTION)
            != JOptionPane.OK_OPTION) return;
        if (url.getText().matches(".*[/]team_project(?:[?].*)?$")) {
          throw new IllegalArgumentException("원본 DB 대신 team_project_feedback 같은 별도 DB를 사용하세요.");
        }
        DB_connection.configure(
            url.getText().trim(), user.getText().trim(), new String(password.getPassword()));
      }
      // 연결과 스키마 준비 동안 화면이 멈추지 않게 백그라운드에서 실행합니다.
      JLabel status = new JLabel("데이터베이스를 준비하고 있습니다...");
      JDialog waiting = new JDialog();
      waiting.setTitle("시작 준비");
      waiting.add(status);
      waiting.setSize(340, 100);
      waiting.setLocationRelativeTo(null);
      waiting.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      waiting.setVisible(true);
      final boolean demo = mode == 0;
      new SwingWorker<Connection, Void>() {
        protected Connection doInBackground() throws Exception {
          Connection conn = DB_connection.open();
          try {
            SchemaSetup.initialize(conn, demo);
            return conn;
          } catch (Exception e) {
            conn.close();
            throw e;
          }
        }

        protected void done() {
          waiting.dispose();
          try {
            new LoginFrame(get()).setVisible(true);
          } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "실행 준비 실패: " + e.getCause());
          }
        }
      }.execute();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "시작 실패: " + e.getMessage());
    }
  }
}
