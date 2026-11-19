package teamproject.task;

// 브리핑 담당: 윤*훈 | 파일별 설명: docs/04_task.md

import java.awt.Component;
import java.awt.GridLayout;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import teamproject.member.NoteDAO;

// 메모 원문은 남기고, 결정된 일을 기존 tasks 테이블의 업무로 추가합니다.
public class TaskFromNoteDialog {
  public static void open(Component parent, Connection conn, int teamId, NoteDAO.Note note) {
    try {
      List<TaskDAO.Assignee> members = TaskDAO.getTeamMembers(conn, teamId);
      if (members.isEmpty()) {
        JOptionPane.showMessageDialog(parent, "담당할 팀원이 없습니다.");
        return;
      }
      JTextField title = new JTextField(note.title),
          due = new JTextField(LocalDate.now().plusDays(1).toString());
      JComboBox<TaskDAO.Assignee> person =
          new JComboBox<>(members.toArray(new TaskDAO.Assignee[0]));
      JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
      form.add(new JLabel("업무명"));
      form.add(title);
      form.add(new JLabel("담당자"));
      form.add(person);
      form.add(new JLabel("마감일 yyyy-MM-dd"));
      form.add(due);
      while (JOptionPane.showConfirmDialog(parent, form, "메모에서 업무 등록", JOptionPane.OK_CANCEL_OPTION)
          == JOptionPane.OK_OPTION) {
        try {
          LocalDate date = LocalDate.parse(due.getText().trim());
          if (title.getText().isBlank()
              || title.getText().trim().length() > 100
              || date.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(parent, "업무명은 1~100자, 마감일은 오늘 이후로 입력하세요.");
            continue;
          }
          TaskDAO.Assignee who = (TaskDAO.Assignee) person.getSelectedItem();
          TaskDAO.insertFromNote(
              conn, teamId, note.id, title.getText().trim(), note.content, who.getUserId(), date);
          JOptionPane.showMessageDialog(parent, "업무가 등록됐습니다. 업무 화면에서 원본 메모를 확인할 수 있습니다.");
          return;
        } catch (Exception e) {
          JOptionPane.showMessageDialog(parent, "등록 실패: " + e.getMessage());
        }
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(parent, e.getMessage());
    }
  }
}
