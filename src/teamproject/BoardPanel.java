package teamproject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

// 포스트잇 보드 화면 (패들렛처럼 팀원이 아이디어 · 공지를 카드로 붙이는 게시판)
// - 한 줄에 카드 4장씩 최신 글부터 보여준다.
// - 좋아요(♥)를 누를 수 있고, 내가 쓴 카드만 뗄(삭제) 수 있다.
// - 5초마다 다른 팀원이 붙인 카드가 있는지 확인해서 자동으로 다시 그린다.
public class BoardPanel extends JPanel {

    private static final int REFRESH_MILLIS = 5000; // 자동 새로고침 간격: 5초

    private final Connection conn;
    private final int teamId;

    private JPanel cardArea;            // 카드가 붙는 영역
    private JLabel infoLabel;           // 카드 수 · 새로고침 시각 안내
    private String lastSnapshot = "";   // 마지막으로 그린 목록 요약 (바뀌었는지 비교용)

    public BoardPanel(Connection conn, int teamId) {
        this.conn = conn;
        this.teamId = teamId;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── 위쪽: 제목과 버튼 ──
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("아이디어 보드  ·  자유롭게 포스트잇을 붙여 의견을 나눠요");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("+ 포스트잇 붙이기");
        JButton refreshButton = new JButton("새로고침");
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ── 가운데: 카드 영역 ──
        // GridLayout(0, 4): 한 줄에 4장, 줄 수는 카드 개수에 따라 자동
        cardArea = new JPanel(new GridLayout(0, 4, 10, 10));
        // 카드가 적을 때 세로로 늘어나지 않도록 위쪽(NORTH)에 붙여 둔다
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(cardArea, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // 마우스 휠 스크롤 속도
        add(scrollPane, BorderLayout.CENTER);

        // ── 아래쪽: 안내 글자 ──
        infoLabel = new JLabel(" ");
        add(infoLabel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addPost());
        refreshButton.addActionListener(e -> reload());

        reload();

        // 5초마다, 이 탭을 보고 있을 때만 바뀐 내용 확인
        Timer refreshTimer = new Timer(REFRESH_MILLIS, e -> {
            if (isShowing()) {
                refreshIfChanged();
            }
        });
        refreshTimer.start();
    }

    // 무조건 다시 그리기 (버튼, 탭 열기)
    public void reload() {
        lastSnapshot = "";   // 비교값을 지워서 반드시 다시 그리게 함
        refreshIfChanged();
    }

    // DB 목록이 마지막으로 그린 것과 다를 때만 다시 그린다 (스크롤이 자꾸 튀지 않게)
    private void refreshIfChanged() {
        try {
            List<BoardDAO.Post> posts = BoardDAO.loadPosts(conn, teamId);

            // 글 번호와 좋아요 수를 이어 붙인 글자로 "목록 요약"을 만든다
            StringBuilder snapshot = new StringBuilder();
            for (BoardDAO.Post post : posts) {
                snapshot.append(post.getPostId()).append(':').append(post.getLikes()).append(',');
            }

            if (snapshot.toString().equals(lastSnapshot)) {
                return; // 바뀐 것이 없으면 그대로 둔다
            }
            lastSnapshot = snapshot.toString();

            drawCards(posts);
            infoLabel.setText("포스트잇 " + posts.size() + "장  ·  "
                    + new SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + " 기준");

        } catch (SQLException e) {
            infoLabel.setText("불러오기 실패: " + e.getMessage());
        }
    }

    // 카드 목록을 화면에 다시 그린다
    private void drawCards(List<BoardDAO.Post> posts) {
        cardArea.removeAll();

        if (posts.isEmpty()) {
            cardArea.add(new JLabel("아직 포스트잇이 없습니다. 첫 아이디어를 붙여 보세요!"));
        }

        for (BoardDAO.Post post : posts) {
            cardArea.add(makeCard(post));
        }

        // 구성 요소를 바꾼 뒤에는 다시 배치(revalidate)하고 다시 그려야(repaint) 화면에 반영된다
        cardArea.revalidate();
        cardArea.repaint();
    }

    // 포스트잇 한 장(카드)을 만든다
    private JPanel makeCard(BoardDAO.Post post) {
        Color background = colorOf(post.getColor());

        JPanel card = new JPanel(new BorderLayout(3, 3));
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),   // 테두리
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));          // 안쪽 여백
        card.setPreferredSize(new Dimension(200, 170));

        // 내용: 읽기 전용 글상자 (줄바꿈되도록 JTextArea 사용)
        JTextArea contentArea = new JTextArea(post.getContent());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setOpaque(false); // 배경을 카드 색으로 보이게
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        card.add(contentArea, BorderLayout.CENTER);

        // 아래: 작성자 · 시각 + 좋아요 · 삭제 버튼
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        String time = new SimpleDateFormat("MM-dd HH:mm").format(post.getCreatedAt());
        JLabel writerLabel = new JLabel(post.getWriterName() + " · " + time);
        writerLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        bottom.add(writerLabel, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        buttons.setOpaque(false);

        JButton likeButton = new JButton("♥ " + post.getLikes());
        likeButton.addActionListener(e -> likePost(post.getPostId()));
        buttons.add(likeButton);

        // 내가 쓴 카드에만 삭제 버튼을 보여준다
        if (post.getUserId() == LoginUser.getUserId()) {
            JButton deleteButton = new JButton("떼기");
            deleteButton.addActionListener(e -> deletePost(post.getPostId()));
            buttons.add(deleteButton);
        }

        bottom.add(buttons, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    // 색 이름 → 실제 색 (연한 파스텔 색)
    private Color colorOf(String colorName) {
        if ("분홍".equals(colorName)) {
            return new Color(248, 187, 208);
        } else if ("하늘".equals(colorName)) {
            return new Color(179, 229, 252);
        } else if ("연두".equals(colorName)) {
            return new Color(197, 225, 165);
        }
        return new Color(255, 245, 157); // 기본: 노랑
    }

    // [포스트잇 붙이기] 버튼
    private void addPost() {
        if (LoginUser.getUserId() == 0) {
            JOptionPane.showMessageDialog(this, "로그인 정보가 없어 글을 쓸 수 없습니다.");
            return;
        }

        JTextArea inputArea = new JTextArea(5, 25);
        inputArea.setLineWrap(true);
        JComboBox<String> colorBox = new JComboBox<>(BoardDAO.COLORS);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("내용 (500자 이내)"), BorderLayout.NORTH);
        panel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(new JLabel("색상"));
        colorPanel.add(colorBox);
        panel.add(colorPanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "포스트잇 붙이기",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String content = inputArea.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "내용을 입력하세요.");
            return;
        }
        if (content.length() > 500) {
            JOptionPane.showMessageDialog(this, "내용은 500자 이내로 입력하세요. (현재 " + content.length() + "자)");
            return;
        }

        try {
            BoardDAO.insertPost(conn, teamId, LoginUser.getUserId(),
                    content, (String) colorBox.getSelectedItem());
            reload();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "저장 실패: " + e.getMessage());
        }
    }

    // ♥ 버튼
    private void likePost(int postId) {
        try {
            BoardDAO.likePost(conn, postId);
            reload();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "좋아요 실패: " + e.getMessage());
        }
    }

    // [떼기] 버튼
    private void deletePost(int postId) {
        int answer = JOptionPane.showConfirmDialog(this, "이 포스트잇을 뗄까요?", "삭제 확인",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (!BoardDAO.deletePost(conn, postId, LoginUser.getUserId())) {
                JOptionPane.showMessageDialog(this, "본인이 붙인 포스트잇만 뗄 수 있습니다.");
            }
            reload();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "삭제 실패: " + e.getMessage());
        }
    }
}
