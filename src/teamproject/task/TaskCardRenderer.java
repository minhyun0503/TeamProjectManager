package teamproject.task;

// 브리핑 담당: 윤*훈 | 파일별 설명: docs/04_task.md

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.*;

public class TaskCardRenderer extends JPanel implements ListCellRenderer<TaskDAO.Card> {
  private final JLabel title = new JLabel(), person = new JLabel(), due = new JLabel();

  public TaskCardRenderer() {
    setLayout(new GridLayout(3, 1, 0, 4));
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    add(title);
    add(person);
    add(due);
  }

  @Override
  public Component getListCellRendererComponent(
      JList<? extends TaskDAO.Card> list,
      TaskDAO.Card card,
      int index,
      boolean selected,
      boolean focus) {
    title.setText(card.title);
    person.setText("담당: " + card.name);
    due.setText("마감: " + card.due);
    setBackground(selected ? list.getSelectionBackground() : list.getBackground());
    Color text = selected ? list.getSelectionForeground() : list.getForeground();
    title.setForeground(text);
    person.setForeground(text);
    due.setForeground(text);
    return this;
  }
}
