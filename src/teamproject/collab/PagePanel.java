package teamproject.collab;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import teamproject.common.LoginUser;

// 문서 페이지 화면 (노션처럼 왼쪽 페이지 목록 + 오른쪽 편집 영역)
// - 새 페이지를 만들 때 회의록 · 발표 준비 같은 서식(템플릿)을 고를 수 있다.
// - 저장하지 않고 다른 페이지로 넘어가면 저장할지 물어본다.
public class PagePanel extends JPanel {

    // 새 페이지 서식 이름과 내용 (순서를 맞춰서 사용)
    private static final String[] TEMPLATE_NAMES = {"빈 페이지", "회의록", "발표 준비", "할 일 체크리스트"};

    private final Connection conn;
    private final int teamId;

    private DefaultListModel<PageDAO.PageItem> listModel; // 왼쪽 목록의 데이터
    private JList<PageDAO.PageItem> pageList;             // 왼쪽 목록
    private JTextField titleField;                        // 제목 입력칸
    private JTextArea contentArea;                        // 본문 입력칸
    private JLabel infoLabel;                             // 마지막 수정 안내

    private int currentPageId = 0;   // 지금 열려 있는 페이지 번호 (0이면 없음)
    private boolean changed = false; // 저장하지 않은 수정이 있는지
    private boolean loading = false; // 프로그램이 글을 채우는 중인지
    private boolean selecting = false; // 목록 선택을 코드로 바꾸는 중인지

    public PagePanel(Connection conn, int teamId) {
        this.conn = conn;
        this.teamId = teamId;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── 왼쪽: 페이지 목록 + 버튼 ──
        listModel = new DefaultListModel<>();
        pageList = new JList<>(listModel);
        pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 한 개만 선택
        pageList.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newButton = new JButton("+ 새 페이지");
        JButton deleteButton = new JButton("삭제");
        leftButtons.add(newButton);
        leftButtons.add(deleteButton);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(new JLabel("페이지 목록"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(pageList), BorderLayout.CENTER);
        leftPanel.add(leftButtons, BorderLayout.SOUTH);

        // ── 오른쪽: 제목 · 본문 · 저장 ──
        titleField = new JTextField();
        titleField.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        contentArea = new JTextArea();
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        JButton saveButton = new JButton("저장");
        infoLabel = new JLabel("왼쪽에서 페이지를 고르거나 새 페이지를 만드세요.");

        JPanel rightBottom = new JPanel(new BorderLayout());
        rightBottom.add(infoLabel, BorderLayout.WEST);
        rightBottom.add(saveButton, BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(titleField, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        rightPanel.add(rightBottom, BorderLayout.SOUTH);

        // JSplitPane: 왼쪽 · 오른쪽 사이 경계선을 마우스로 옮길 수 있는 패널
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(220);
        add(splitPane, BorderLayout.CENTER);

        // ── 이벤트 연결 ──
        DocumentListener changeListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { markChanged(); }
            public void removeUpdate(DocumentEvent e) { markChanged(); }
            public void changedUpdate(DocumentEvent e) { markChanged(); }
        };
        titleField.getDocument().addDocumentListener(changeListener);
        contentArea.getDocument().addDocumentListener(changeListener);

        // 목록에서 다른 페이지를 고르면 그 페이지를 연다
        pageList.addListSelectionListener(e -> {
            // getValueIsAdjusting(): 마우스를 누르고 있는 중간 이벤트는 건너뜀
            if (!e.getValueIsAdjusting() && !selecting) {
                openSelectedPage();
            }
        });

        newButton.addActionListener(e -> newPage());
        deleteButton.addActionListener(e -> deletePage());
        saveButton.addActionListener(e -> savePage());

        setEditorEnabled(false);
        reload();
    }

    // 페이지 목록 다시 읽기 (탭을 열 때 MainFrame에서도 호출)
    public void reload() {
        try {
            List<PageDAO.PageItem> pages = PageDAO.listPages(conn, teamId);

            selecting = true;   // 아래에서 목록을 바꾸는 동안 선택 이벤트 무시
            listModel.clear();
            for (PageDAO.PageItem item : pages) {
                listModel.addElement(item);
            }
            selectInList(currentPageId); // 보고 있던 페이지를 다시 선택 표시
            selecting = false;

            // [새로고침 수정] 보고 있던 페이지를 내가 고치는 중이 아니면 DB 최신 내용으로 다시 연다.
            // (예전에는 목록만 새로 읽어서, 다른 팀원이 고친 본문이 안 보이고
            //  그대로 저장하면 팀원의 수정이 덮어써졌다)
            if (currentPageId != 0 && !changed) {
                refreshOpenPage();
            }

        } catch (SQLException e) {
            selecting = false;
            JOptionPane.showMessageDialog(this, "페이지 목록 불러오기 실패: " + e.getMessage());
        }
    }

    // 목록에서 고른 페이지 열기
    private void openSelectedPage() {
        PageDAO.PageItem item = pageList.getSelectedValue();
        if (item == null || item.getPageId() == currentPageId) {
            return;
        }

        // 저장 안 한 내용이 있으면 먼저 물어본다
        if (!askSaveIfChanged()) {
            selecting = true;
            selectInList(currentPageId); // 이동 취소: 원래 페이지 선택으로 되돌림
            selecting = false;
            return;
        }

        openPage(item.getPageId());
    }

    // 페이지 번호로 열어서 오른쪽에 보여준다
    private void openPage(int pageId) {
        try {
            PageDAO.PageInfo page = PageDAO.findPage(conn, pageId);
            if (page == null) {
                JOptionPane.showMessageDialog(this, "다른 팀원이 삭제한 페이지입니다.");
                currentPageId = 0;
                reload();
                return;
            }

            loading = true;
            titleField.setText(page.getTitle());
            contentArea.setText(page.getContent());
            contentArea.setCaretPosition(0); // 맨 위부터 보이게
            loading = false;

            currentPageId = page.getPageId();
            changed = false;
            setEditorEnabled(true);
            showInfo(page);

        } catch (SQLException e) {
            loading = false;
            JOptionPane.showMessageDialog(this, "페이지 열기 실패: " + e.getMessage());
        }
    }

    // 열려 있는 페이지 본문을 DB 최신 내용으로 바꾼다 (커서 위치는 최대한 유지)
    private void refreshOpenPage() throws SQLException {
        PageDAO.PageInfo page = PageDAO.findPage(conn, currentPageId);
        if (page == null) {
            // 다른 팀원이 삭제한 페이지
            clearEditor();
            JOptionPane.showMessageDialog(this, "보고 있던 페이지를 다른 팀원이 삭제했습니다.");
            return;
        }

        String content = (page.getContent() == null) ? "" : page.getContent();
        if (!page.getTitle().equals(titleField.getText()) || !content.equals(contentArea.getText())) {
            int caret = contentArea.getCaretPosition();
            loading = true;
            titleField.setText(page.getTitle());
            contentArea.setText(content);
            loading = false;
            contentArea.setCaretPosition(Math.min(caret, content.length()));
        }
        showInfo(page);
    }

    // [+ 새 페이지] 버튼: 서식을 고르고 페이지를 만든다
    private void newPage() {
        if (!askSaveIfChanged()) {
            return;
        }

        String choice = (String) JOptionPane.showInputDialog(this, "어떤 서식으로 만들까요?", "새 페이지",
                JOptionPane.PLAIN_MESSAGE, null, TEMPLATE_NAMES, TEMPLATE_NAMES[0]);
        if (choice == null) {
            return; // 취소
        }

        String title = choice.equals("빈 페이지") ? "제목 없음" : choice + " (" + LocalDate.now() + ")";

        try {
            int newId = PageDAO.insertPage(conn, teamId, title, templateText(choice), LoginUser.getUserId());
            currentPageId = newId;
            reload();
            openPage(newId);
            titleField.requestFocus(); // 바로 제목을 고칠 수 있게
            titleField.selectAll();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "페이지 만들기 실패: " + e.getMessage());
        }
    }

    // [저장] 버튼
    private void savePage() {
        if (currentPageId == 0) {
            return;
        }

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "제목을 입력하세요.");
            return;
        }
        if (title.length() > 100) {
            JOptionPane.showMessageDialog(this, "제목은 100자 이내로 입력하세요.");
            return;
        }

        try {
            PageDAO.updatePage(conn, currentPageId, title, contentArea.getText(), LoginUser.getUserId());
            changed = false;
            reload(); // 제목이 바뀌었을 수 있으니 목록도 새로
            PageDAO.PageInfo page = PageDAO.findPage(conn, currentPageId);
            if (page != null) {
                showInfo(page);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "저장 실패: " + e.getMessage());
        }
    }

    // [삭제] 버튼
    private void deletePage() {
        if (currentPageId == 0) {
            JOptionPane.showMessageDialog(this, "삭제할 페이지를 먼저 고르세요.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "\"" + titleField.getText() + "\" 페이지를 삭제할까요?\n삭제하면 되돌릴 수 없습니다.",
                "삭제 확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            PageDAO.deletePage(conn, currentPageId);
            clearEditor();
            reload();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "삭제 실패: " + e.getMessage());
        }
    }

    // 저장 안 한 수정이 있으면 저장할지 묻는다. 계속 진행해도 되면 true
    private boolean askSaveIfChanged() {
        if (!changed || currentPageId == 0) {
            return true;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "저장하지 않은 내용이 있습니다. 저장할까요?", "저장 확인",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (answer == JOptionPane.YES_OPTION) {
            savePage();
            return !changed; // 저장에 성공했을 때만 진행
        }
        if (answer == JOptionPane.NO_OPTION) {
            changed = false; // 버리고 진행
            return true;
        }
        return false; // 취소
    }

    // 사용자가 제목 · 본문을 고쳤을 때
    private void markChanged() {
        if (loading || currentPageId == 0) {
            return;
        }
        changed = true;
        infoLabel.setText("수정 중... (저장 버튼을 눌러 주세요)");
    }

    // 오른쪽 편집 영역 비우기
    private void clearEditor() {
        loading = true;
        titleField.setText("");
        contentArea.setText("");
        loading = false;

        currentPageId = 0;
        changed = false;
        setEditorEnabled(false);
        infoLabel.setText("왼쪽에서 페이지를 고르거나 새 페이지를 만드세요.");
    }

    // 페이지가 열려 있을 때만 입력할 수 있게
    private void setEditorEnabled(boolean enabled) {
        titleField.setEditable(enabled);
        contentArea.setEditable(enabled);
    }

    // 목록에서 해당 번호의 페이지를 선택 표시
    private void selectInList(int pageId) {
        pageList.clearSelection();
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getPageId() == pageId) {
                pageList.setSelectedIndex(i);
                break;
            }
        }
    }

    // "마지막 수정: 이름 · 시각" 안내
    private void showInfo(PageDAO.PageInfo page) {
        String who = (page.getUpdatedByName() == null) ? "알 수 없음" : page.getUpdatedByName();
        String when = (page.getUpdatedAt() == null) ? ""
                : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(page.getUpdatedAt());
        infoLabel.setText("마지막 수정: " + who + " · " + when);
    }

    // 서식 이름에 맞는 기본 본문
    private String templateText(String templateName) {
        String today = LocalDate.now().toString();

        if (templateName.equals("회의록")) {
            return "■ 일시: " + today + "\n"
                    + "■ 참석자: \n\n"
                    + "■ 안건\n1. \n2. \n\n"
                    + "■ 결정 사항\n- \n\n"
                    + "■ 다음 할 일 (담당자 / 마감일)\n[ ] \n[ ] \n";
        }
        if (templateName.equals("발표 준비")) {
            return "■ 발표일: \n■ 발표자: \n\n"
                    + "■ 슬라이드 구성\n1. 소개\n2. 구현 내용\n3. 시연\n4. 계획\n\n"
                    + "■ 준비물 체크\n[ ] 발표 자료\n[ ] 시연 계정 · DB 확인\n[ ] 예상 질문 정리\n";
        }
        if (templateName.equals("할 일 체크리스트")) {
            return "작성일: " + today + "\n\n"
                    + "[ ] \n[ ] \n[ ] \n\n"
                    + "※ 끝낸 일은 [ ]를 [v]로 바꿔 표시하세요.\n";
        }
        return ""; // 빈 페이지
    }
}
