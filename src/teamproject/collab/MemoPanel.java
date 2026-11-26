package teamproject.collab;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import teamproject.common.LoginUser;

// 팀 공동 메모장 화면 (구글 문서 · 실시간 메모장처럼 여러 명이 같은 메모를 함께 씀)
//
// 동작 방식 (3초마다 한 번씩 sync() 실행)
//  - 내가 고친 내용이 있으면 → 자동 저장
//  - 고친 내용이 없으면       → DB를 확인해서 다른 팀원이 바꾼 내용을 불러옴
//  - 두 사람이 동시에 고쳐서 저장이 겹치면 → 어느 내용을 남길지 물어봄
public class MemoPanel extends JPanel {

    private static final int SYNC_MILLIS = 3000; // 동기화 간격: 3초

    private final Connection conn;
    private final int teamId;

    private JTextArea memoArea;     // 메모 입력칸
    private JLabel statusLabel;     // 저장 상태 안내
    private JLabel lastEditLabel;   // 마지막 수정자 · 시각
    private Timer syncTimer;        // 3초마다 sync()를 부르는 타이머

    private int myVersion = 0;      // 내가 마지막으로 불러온(저장한) 메모 번호
    private boolean changed = false; // 저장하지 않은 수정 내용이 있는지
    private boolean loading = false; // 프로그램이 글을 채우는 중인지 (사용자 입력과 구분)

    public MemoPanel(Connection conn, int teamId) {
        this.conn = conn;
        this.teamId = teamId;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── 위쪽: 제목과 안내 ──
        JLabel titleLabel = new JLabel("팀 공동 메모장  ·  3초마다 자동 저장되고 팀원이 고친 내용이 바로 보입니다");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        add(titleLabel, BorderLayout.NORTH);

        // ── 가운데: 메모 입력칸 ──
        memoArea = new JTextArea();
        memoArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        memoArea.setLineWrap(true);       // 글이 길면 자동 줄바꿈
        memoArea.setWrapStyleWord(true);  // 단어 단위로 줄바꿈
        add(new JScrollPane(memoArea), BorderLayout.CENTER);

        // ── 아래쪽: 상태 글자와 버튼 ──
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("불러오는 중...");
        lastEditLabel = new JLabel("");
        labelPanel.add(statusLabel);
        labelPanel.add(new JLabel("|"));
        labelPanel.add(lastEditLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("지금 저장");
        JButton reloadButton = new JButton("새로고침");
        buttonPanel.add(saveButton);
        buttonPanel.add(reloadButton);

        bottomPanel.add(labelPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // 글자를 입력하거나 지우면 "수정됨"으로 표시
        memoArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { markChanged(); }
            public void removeUpdate(DocumentEvent e) { markChanged(); }
            public void changedUpdate(DocumentEvent e) { markChanged(); }
        });

        saveButton.addActionListener(e -> sync());
        reloadButton.addActionListener(e -> reload());

        // 처음 한 번 불러오고, 3초마다 sync() 실행
        reload();
        syncTimer = new Timer(SYNC_MILLIS, e -> {
            // 다른 탭을 보고 있을 때는 DB에 묻지 않는다 (불필요한 접속 줄이기)
            if (isShowing()) {
                sync();
            }
        });
        syncTimer.start();
    }

    // 사용자가 글을 바꿨을 때 호출 (프로그램이 채우는 중이면 무시)
    private void markChanged() {
        if (loading) {
            return;
        }
        changed = true;
        statusLabel.setText("수정 중... (곧 자동 저장)");
    }

    // 탭을 열었을 때 MainFrame에서도 호출한다
    public void reload() {
        // [새로고침 수정] 아직 저장 안 한 글이 있으면 덮어쓰지 말고 먼저 저장한다.
        // (입력 후 3초 안에 새로고침을 누르거나 다른 탭에 다녀오면 쓴 글이 사라지던 문제)
        if (changed) {
            sync();
            return;
        }
        try {
            MemoDAO.MemoInfo memo = MemoDAO.loadMemo(conn, teamId);
            showMemo(memo);
            statusLabel.setText("최신 내용입니다");
        } catch (SQLException e) {
            statusLabel.setText("불러오기 실패: " + e.getMessage());
        }
    }

    // 3초마다 실행: 저장하거나, 다른 사람이 바꾼 내용을 불러온다
    private void sync() {
        try {
            if (changed) {
                saveMyMemo();
            } else {
                checkOtherChanges();
            }
        } catch (SQLException e) {
            statusLabel.setText("DB 연결 오류: " + e.getMessage());
        }
    }

    // 내 메모 저장
    private void saveMyMemo() throws SQLException {
        String text = memoArea.getText();
        boolean saved = MemoDAO.saveMemo(conn, teamId, text, myVersion, LoginUser.getUserId());

        if (saved) {
            myVersion++;     // DB 번호도 1 올라갔으므로 나도 맞춘다
            changed = false;
            statusLabel.setText("저장됨 (" + formatTime(new Date()) + ")");
            lastEditLabel.setText("마지막 수정: " + LoginUser.getName() + " · " + formatTime(new Date()));
        } else {
            // 내가 불러온 뒤에 다른 팀원이 먼저 저장했다
            handleConflict(text);
        }
    }

    // 다른 팀원이 저장한 새 내용이 있는지 확인
    private void checkOtherChanges() throws SQLException {
        MemoDAO.MemoInfo memo = MemoDAO.loadMemo(conn, teamId);

        if (memo.getVersion() != myVersion) {
            showMemo(memo);
            statusLabel.setText("팀원이 수정한 내용을 불러왔습니다");
        }
    }

    // 저장이 겹쳤을 때: 어떤 내용을 남길지 사용자에게 묻는다
    private void handleConflict(String myText) throws SQLException {
        syncTimer.stop(); // 물어보는 동안 타이머가 또 실행되지 않게 잠시 멈춤

        MemoDAO.MemoInfo latest = MemoDAO.loadMemo(conn, teamId);
        String who = (latest.getUpdatedByName() == null) ? "다른 팀원" : latest.getUpdatedByName();

        String[] options = {"내 내용으로 덮어쓰기", "팀원 내용 불러오기"};
        int choice = JOptionPane.showOptionDialog(this,
                who + "님이 방금 메모를 먼저 저장했습니다.\n어떤 내용을 남길까요?",
                "메모 저장 충돌",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[1]);

        if (choice == 0) {
            // 최신 번호로 맞춘 뒤 내 글로 다시 저장
            myVersion = latest.getVersion();
            if (MemoDAO.saveMemo(conn, teamId, myText, myVersion, LoginUser.getUserId())) {
                myVersion++;
                changed = false;
                statusLabel.setText("내 내용으로 저장했습니다");
            }
        } else {
            // 팀원 내용을 보여준다 (내가 쓴 글은 사라짐)
            showMemo(latest);
            statusLabel.setText("팀원 내용을 불러왔습니다");
        }

        syncTimer.start();
    }

    // DB에서 읽은 메모를 화면에 보여준다
    private void showMemo(MemoDAO.MemoInfo memo) {
        int caret = memoArea.getCaretPosition(); // 커서 위치 기억

        loading = true;                          // 아래 setText는 사용자 입력이 아님
        memoArea.setText(memo.getContent());
        loading = false;

        // 커서를 원래 자리 근처로 돌려놓기 (글이 짧아졌으면 맨 끝)
        memoArea.setCaretPosition(Math.min(caret, memoArea.getText().length()));

        myVersion = memo.getVersion();
        changed = false;

        if (memo.getUpdatedAt() == null) {
            lastEditLabel.setText("아직 저장된 메모가 없습니다");
        } else {
            String who = (memo.getUpdatedByName() == null) ? "알 수 없음" : memo.getUpdatedByName();
            lastEditLabel.setText("마지막 수정: " + who + " · " + formatTime(memo.getUpdatedAt()));
        }
    }

    // 날짜를 "11-26 14:05:30" 모양 글자로 바꾼다
    private String formatTime(Date date) {
        return new SimpleDateFormat("MM-dd HH:mm:ss").format(date);
    }
}
