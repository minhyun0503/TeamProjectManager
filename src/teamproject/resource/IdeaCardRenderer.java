package teamproject.resource;

// 브리핑 담당: 신*준 | 파일별 설명: docs/06_resource.md

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.*;

// JList의 데이터는 그대로 두고, 한 항목을 작은 카드 모양으로 그립니다.
public class IdeaCardRenderer extends JPanel implements ListCellRenderer<IdeaDAO.Idea> {
  private final JLabel title = new JLabel(), author = new JLabel(), counts = new JLabel();

  public IdeaCardRenderer() {
    setLayout(new GridLayout(3, 1, 0, 4));
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    add(title);
    add(author);
    add(counts);
  }

  @Override
  public Component getListCellRendererComponent(
      JList<? extends IdeaDAO.Idea> list,
      IdeaDAO.Idea idea,
      int index,
      boolean selected,
      boolean focus) {
    title.setText(idea.title);
    author.setText("작성자: " + idea.author);
    counts.setText("댓글 " + idea.comments + " · 공감 " + idea.likes);
    setBackground(selected ? list.getSelectionBackground() : list.getBackground());
    Color text = selected ? list.getSelectionForeground() : list.getForeground();
    title.setForeground(text);
    author.setForeground(text);
    counts.setForeground(text);
    return this;
  }
}
