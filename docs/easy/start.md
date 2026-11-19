# 모두 먼저 읽기
공통 1장 · 화면 부품과 코드 읽는 법

화면은 입력을 받고, 자바 코드는 처리 순서를 정하고, 저장소(DB)는 결과를 기억합니다. 본인 담당 장을 읽기 전에 아래 부품부터 확인하세요.

## 코드에 나오는 화면 부품

| 이름 | 화면에서 하는 일 |
|---|---|
| JFrame / JPanel | 프로그램 창 / 창 안에서 버튼과 글자를 묶는 영역 |
| JLabel / JButton | 내용을 보여 주는 글자 / 눌러서 일을 시키는 버튼 |
| JTextField / JTextArea | 한 줄 입력칸 / 여러 줄 입력·표시 칸 |
| JList | 메모 제목처럼 항목을 세로로 나열하는 목록. 하나를 누르면 해당 항목을 선택합니다. |
| DefaultListModel | JList에 보여 줄 내용을 보관하는 목록. 보관 목록이 바뀌면 화면 목록도 바뀝니다. |
| JTable / DefaultTableModel | 행·열이 있는 표 / 그 표에 표시할 내용을 보관하는 목록 |
| JComboBox / JCheckBox | 펼쳐서 하나를 고르는 선택칸 / 켜고 끄는 확인칸 |
| JScrollPane / JTabbedPane | 내용이 길면 스크롤하는 영역 / 화면을 골라 여는 탭 |

## JList는 이렇게 연결합니다

```java
DefaultListModel<NoteDAO.Note> model = new DefaultListModel<>();
JList<NoteDAO.Note> list = new JList<>(model);
model.addElement(note);
```

1줄은 메모를 담을 보관 목록, 2줄은 그것을 보여 줄 화면 목록입니다. 3줄은 조회한 메모 한 건(note)을 목록에 넣습니다. 화면에 넣는 것과 DB에 저장하는 것은 다릅니다.

## 파일과 메서드를 읽는 기준

| 코드 | 쉽게 풀면 |
|---|---|
| Frame / Panel → DAO | 화면에서 값을 읽음 → DB 조회·저장을 담당하는 코드에 전달 |
| 메서드 / 생성자 | 이름 붙인 처리 과정 / new로 객체를 만들 때 실행되는 준비 과정 |
| reload() / addActionListener(...) | 목록을 다시 읽어 표시 / 버튼을 눌렀을 때 실행할 일을 연결 |
| setInt(1, teamId) | 첫 번째 ?에 팀 번호를 넣음. 숫자 1이 팀 번호라는 뜻은 아닙니다. |

화면 도구는 Swing(java.desktop), DB 요청은 JDBC(java.sql), 날짜 계산은 LocalDate·YearMonth를 사용합니다. 낯선 이름은 역할부터 기억하세요. 브리핑은 “입력 → 검사 → DB 처리 → 결과 표시” 순서로 설명하면 됩니다.
