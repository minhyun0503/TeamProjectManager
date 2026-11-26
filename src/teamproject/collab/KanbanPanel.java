package teamproject.collab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import teamproject.task.TaskDAO;

// 칸반 보드 화면 (노션 · 트렐로의 보드 보기처럼 업무를 상태별 세 칸으로 나눠 보여줌)
// - 칸: 할 일 → 진행 중 → 완료
// - 카드를 고르고 [다음 단계 →] 를 누르면 오른쪽 칸으로 옮겨진다 (DB 상태도 바뀜)
// - 업무 등록 · 수정은 "업무 관리" 탭에서 한다.
public class KanbanPanel extends JPanel {

    private final Connection conn;
    private final int teamId;

    // 세 칸의 목록. 0번 칸 = 할 일, 1번 칸 = 진행 중, 2번 칸 = 완료 (TaskDAO.STATUS_LIST 순서)
    private final List<DefaultListModel<KanbanDAO.Card>> models = new ArrayList<>(); // 칸별 데이터
    private final List<JList<KanbanDAO.Card>> lists = new ArrayList<>();             // 칸별 목록 화면
    private final JLabel[] countLabels = new JLabel[3];                              // 칸 제목

    public KanbanPanel(Connection conn, int teamId) {
        this.conn = conn;
        this.teamId = teamId;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("칸반 보드  ·  업무를 상태별로 한눈에 보고 단계를 옮깁니다");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        add(titleLabel, BorderLayout.NORTH);

        // 칸 3개를 가로로 나란히 (1줄 3칸)
        JPanel columnsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        Color[] headerColors = {
                new Color(255, 224, 178),  // 할 일: 주황빛
                new Color(187, 222, 251),  // 진행 중: 파랑빛
                new Color(200, 230, 201)   // 완료: 초록빛
        };

        for (int i = 0; i < 3; i++) {
            DefaultListModel<KanbanDAO.Card> model = new DefaultListModel<>();
            JList<KanbanDAO.Card> list = new JList<>(model);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            list.setFixedCellHeight(28); // 카드 한 줄 높이
            models.add(model);
            lists.add(list);

            // 한 칸에서 카드를 고르면 다른 칸의 선택은 지운다 (선택은 항상 하나만)
            final int columnIndex = i; // 람다 안에서 쓰려면 바뀌지 않는 변수여야 함
            list.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && list.getSelectedIndex() != -1) {
                    clearOtherSelections(columnIndex);
                }
            });

            countLabels[i] = new JLabel(TaskDAO.STATUS_LIST[i], JLabel.CENTER);
            countLabels[i].setOpaque(true); // 배경색이 보이도록
            countLabels[i].setBackground(headerColors[i]);
            countLabels[i].setFont(new Font("맑은 고딕", Font.BOLD, 14));
            countLabels[i].setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            JPanel columnPanel = new JPanel(new BorderLayout());
            columnPanel.setBorder(BorderFactory.createLineBorder(headerColors[i].darker()));
            columnPanel.add(countLabels[i], BorderLayout.NORTH);
            columnPanel.add(new JScrollPane(list), BorderLayout.CENTER);
            columnsPanel.add(columnPanel);
        }
        add(columnsPanel, BorderLayout.CENTER);

        // 아래: 단계 이동 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevButton = new JButton("← 이전 단계");
        JButton nextButton = new JButton("다음 단계 →");
        JButton refreshButton = new JButton("새로고침");
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        prevButton.addActionListener(e -> moveSelected(-1)); // 왼쪽 칸으로
        nextButton.addActionListener(e -> moveSelected(1));  // 오른쪽 칸으로
        refreshButton.addActionListener(e -> reload());

        reload();
    }

    // 업무를 다시 읽어서 상태에 맞는 칸에 넣는다 (탭을 열 때 MainFrame에서도 호출)
    public void reload() {
        try {
            List<KanbanDAO.Card> cards = KanbanDAO.loadCards(conn, teamId);

            for (int i = 0; i < 3; i++) {
                models.get(i).clear();
            }

            for (KanbanDAO.Card card : cards) {
                int column = columnOf(card.getStatus());
                models.get(column).addElement(card);
            }

            // 칸 제목에 개수 표시: "진행 중 (2)"
            for (int i = 0; i < 3; i++) {
                countLabels[i].setText(TaskDAO.STATUS_LIST[i] + " (" + models.get(i).size() + ")");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "업무 불러오기 실패: " + e.getMessage());
        }
    }

    // 상태 글자 → 칸 번호 (0: 할 일, 1: 진행 중, 2: 완료)
    private int columnOf(String status) {
        for (int i = 0; i < TaskDAO.STATUS_LIST.length; i++) {
            if (TaskDAO.STATUS_LIST[i].equals(status)) {
                return i;
            }
        }
        return 0; // 모르는 상태 값은 "할 일" 칸에 둔다
    }

    // 선택한 카드를 왼쪽(-1) 또는 오른쪽(+1) 칸으로 옮긴다
    private void moveSelected(int direction) {
        for (int i = 0; i < 3; i++) {
            KanbanDAO.Card card = lists.get(i).getSelectedValue();
            if (card == null) {
                continue; // 이 칸에는 선택된 카드가 없음
            }

            int target = i + direction;
            if (target < 0 || target > 2) {
                JOptionPane.showMessageDialog(this, "더 옮길 수 있는 단계가 없습니다.");
                return;
            }

            try {
                TaskDAO.updateTaskStatus(conn, card.getTaskId(), TaskDAO.STATUS_LIST[target]);
                reload();
                selectCard(target, card.getTaskId()); // 옮긴 카드를 계속 선택해 둔다
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "상태 변경 실패: " + e.getMessage());
            }
            return;
        }

        JOptionPane.showMessageDialog(this, "옮길 업무 카드를 먼저 선택하세요.");
    }

    // 다른 칸들의 선택 지우기
    private void clearOtherSelections(int keepColumn) {
        for (int i = 0; i < 3; i++) {
            if (i != keepColumn) {
                lists.get(i).clearSelection();
            }
        }
    }

    // 지정한 칸에서 업무 번호가 같은 카드를 선택
    private void selectCard(int column, int taskId) {
        DefaultListModel<KanbanDAO.Card> model = models.get(column);
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i).getTaskId() == taskId) {
                lists.get(column).setSelectedIndex(i);
                return;
            }
        }
    }
}
