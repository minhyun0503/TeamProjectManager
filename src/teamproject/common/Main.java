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
    if (mode < 0) {
      return;
    }

    try {
      boolean demo = mode == 0;
      if (demo) {
        configureDemoDatabase();
      } else if (!configureMySqlDatabase()) {
        return;
      }
      JDialog waiting = showWaitingDialog();
      new DatabaseStartupWorker(demo, waiting).execute();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "시작 실패: " + e.getMessage());
    }
  }

  private static void configureDemoDatabase() throws java.io.IOException {
    Path folder = Path.of(System.getProperty("user.home"), ".teamproject-feedback");
    Files.createDirectories(folder);
    String address =
        "jdbc:h2:file:"
            + folder.resolve("demo").toAbsolutePath()
            + ";MODE=MySQL;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE";
    DB_connection.configure(address, "sa", "");
  }

  private static boolean configureMySqlDatabase() {
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
    int answer =
        JOptionPane.showConfirmDialog(null, form, "MySQL 연결", JOptionPane.OK_CANCEL_OPTION);
    if (answer != JOptionPane.OK_OPTION) {
      return false;
    }
    if (url.getText().matches(".*[/]team_project(?:[?].*)?$")) {
      throw new IllegalArgumentException("원본 DB 대신 team_project_feedback 같은 별도 DB를 사용하세요.");
    }
    DB_connection.configure(
        url.getText().trim(), user.getText().trim(), new String(password.getPassword()));
    return true;
  }

  private static JDialog showWaitingDialog() {
    JDialog waiting = new JDialog();
    waiting.setTitle("시작 준비");
    waiting.add(new JLabel("데이터베이스를 준비하고 있습니다..."));
    waiting.setSize(340, 100);
    waiting.setLocationRelativeTo(null);
    waiting.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    waiting.setVisible(true);
    return waiting;
  }

  // DB 준비는 별도 실행 흐름에서, 로그인 창 표시는 화면을 담당하는 실행 흐름에서 처리합니다.
  private static class DatabaseStartupWorker extends SwingWorker<Connection, Void> {
    private final boolean demo;
    private final JDialog waiting;

    private DatabaseStartupWorker(boolean demo, JDialog waiting) {
      this.demo = demo;
      this.waiting = waiting;
    }

    @Override
    protected Connection doInBackground() throws Exception {
      Connection connection = DB_connection.open();
      try {
        SchemaSetup.initialize(connection, demo);
        return connection;
      } catch (Exception e) {
        connection.close();
        throw e;
      }
    }

    @Override
    protected void done() {
      waiting.dispose();
      try {
        Connection connection = get();
        LoginFrame loginFrame = new LoginFrame(connection);
        loginFrame.setVisible(true);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "실행 준비 실패: " + e.getCause());
      }
    }
  }
}
