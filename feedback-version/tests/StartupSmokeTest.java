import java.awt.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import javax.swing.*;
import teamproject.common.Main;
import teamproject.member.LoginFrame;
import teamproject.resource.MainFrame;

// 실제 Main 진입점에서 모드 선택→데모 DB 준비→로그인→팀 선택까지 검사합니다.
public class StartupSmokeTest {
  private static JButton button(Container root, String text) {
    for (Component c : root.getComponents()) {
      if (c instanceof JButton && text.equals(((JButton) c).getText())) return (JButton) c;
      if (c instanceof Container) {
        JButton result = button((Container) c, text);
        if (result != null) return result;
      }
    }
    return null;
  }

  private static void click(String text) throws Exception {
    long end = System.currentTimeMillis() + 12000;
    while (System.currentTimeMillis() < end) {
      final boolean[] clicked = {false};
      SwingUtilities.invokeAndWait(
          () -> {
            for (Window w : Window.getWindows())
              if (w.isShowing()) {
                JButton b = button(w, text);
                if (b != null) {
                  clicked[0] = true;
                  SwingUtilities.invokeLater(b::doClick);
                  break;
                }
              }
          });
      if (clicked[0]) {
        Thread.sleep(350);
        return;
      }
      Thread.sleep(100);
    }
    throw new AssertionError("Button not found: " + text);
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("user.home", Files.createTempDirectory("feedback-startup-").toString());
    Main.main(new String[0]);
    click("로컬 데모");
    long end = System.currentTimeMillis() + 12000;
    LoginFrame login = null;
    while (System.currentTimeMillis() < end && login == null) {
      for (Window w : Window.getWindows())
        if (w instanceof LoginFrame && w.isShowing()) login = (LoginFrame) w;
      if (login == null) Thread.sleep(100);
    }
    if (login == null) throw new AssertionError("Login window not opened");
    final LoginFrame found = login;
    SwingUtilities.invokeAndWait(
        () -> {
          try {
            Field id = LoginFrame.class.getDeclaredField("idField"),
                pw = LoginFrame.class.getDeclaredField("pwField");
            id.setAccessible(true);
            pw.setAccessible(true);
            ((JTextField) id.get(found)).setText("demo1");
            ((JPasswordField) pw.get(found)).setText("demo1234");
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    click("로그인");
    click(UIManager.getString("OptionPane.okButtonText"));
    click(UIManager.getString("OptionPane.okButtonText"));
    boolean opened = false;
    for (Window w : Window.getWindows()) if (w instanceof MainFrame && w.isShowing()) opened = true;
    if (!opened) throw new AssertionError("Main window not opened");
    SwingUtilities.invokeAndWait(
        () -> {
          for (Window w : Window.getWindows()) w.dispose();
        });
    System.out.println("PASS: Main startup, file demo DB, login and team selection");
    System.exit(0);
  }
}
