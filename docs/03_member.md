# 이*찬 — 로그인·팀원·공유 메모 브리핑

[먼저 읽는 쉬운 설명](easy/member.md) → 이 문서 → [실제 코드 색인](07_code_index.md) 순서로 확인하세요.

담당 파일은 member 패키지의 UserDAO, LoginFrame, MemberDAO, MemberFrame, NoteDAO, NotePanel입니다. 기존 로그인·팀원 관리에 **사용자를 식별할 수 있는 공유 메모**를 추가했습니다. 메모에서 업무를 만드는 창과 SQL은 윤*훈 담당 파일을 호출합니다.

## 기존 로그인과 팀원 관리

UserDAO.User는 회원 번호와 이름을 갖는 작은 데이터 객체입니다. `login`은 아이디·비밀번호를 PreparedStatement에 넣고 한 행이 있으면 User, 없으면 null을 반환합니다. 이름만 돌려주면 소속 팀이나 작성자를 식별할 수 없어 userId도 함께 반환합니다.

LoginFrame 생성자는 입력칸·로그인 버튼을 만들고 doLogin을 연결합니다. doLogin은 빈 값 검사→UserDAO.login→TeamDAO.findTeams→팀 선택→MainFrame 열기→로그인 창 dispose 순서입니다. 인증 실패·소속 팀 없음·팀 선택 취소는 서로 다른 분기입니다.

MemberFrame은 JTable과 DefaultTableModel을 구분합니다. 등록·수정창의 값을 읽고 MemberDAO에 넘긴 뒤 목록을 다시 조회합니다. 수정창에는 이름·학번·역할이 있습니다. JTable 열 0의 user_id를 사용하므로 화면 행 번호 자체를 회원 번호로 쓰면 안 됩니다.

MemberDAO의 주요 메서드:

| 메서드 | 입력 | DB 처리 | 성공·실패 |
|---|---|---|---|
| loadMembers | conn, 모델, teamId | users와 team_members JOIN, 팀 조건 | 표 행을 다시 채움 |
| insertMember | 팀·아이디·비밀번호·이름·역할 | users INSERT 후 생성 키로 team_members INSERT | 두 저장 모두 성공 시 commit, 실패 시 rollback |
| updateMember | 팀·회원·이름·학번·역할 | 소속 확인 후 users와 team_members UPDATE | 부분 변경을 트랜잭션으로 방지 |
| deleteMember | 팀·회원 | team_members만 DELETE | 사용자 로그인 계정과 기존 담당 업무는 남음 |

같은 아이디로 이미 가입한 사용자를 다른 팀에 초대하는 기능은 별도입니다. insertMember는 신규 계정을 등록합니다. 역할 문자열을 바꿔도 teams.leader_id가 바뀌는 팀장 이관 기능은 구현하지 않았습니다.

## 공유 메모 데이터와 화면

NoteDAO.Note의 필드: id(메모 번호), version(수정 버전), authorId(원 작성자), title·content, editor(마지막 수정자 이름), updatedAt. `final`은 생성 후 필드에 다른 값을 다시 대입하지 못하게 합니다. 배경 갱신에서 받은 객체는 화면에 표시할 스냅샷입니다.

NotePanel은 왼쪽 JList·오른쪽 읽기 전용 내용·위쪽 버튼으로 구성됩니다. 편집은 별도 모달 입력창입니다. 배경 목록이 바뀌어도 그 입력창의 JTextArea를 갱신하지 않으므로 작성 중인 글을 잃지 않습니다.

## 새 메모 저장 흐름

`새 메모 버튼 → edit(null) → 제목·내용 읽기 → NoteDAO.save(id=0) → notes INSERT → 생성된 note_id 받기 → note_history v1 INSERT → ActivityDAO.record → commit → reload`.

번호를 SELECT MAX로 추측하지 않고 getGeneratedKeys로 받는 이유는, 다른 사용자가 동시에 INSERT해도 **내가 생성한 행의 번호**를 얻기 위해서입니다.

## 수정 충돌을 막는 알고리즘

```sql
UPDATE notes
SET content=?, version=version+1, ...
WHERE note_id=? AND team_id=? AND version=?
```

화면에서 읽은 버전과 DB의 버전이 같은 경우만 UPDATE합니다.

| 순서 | 사용자 A | 사용자 B | DB 버전 |
|---|---|---|---|
| 1 | v1을 읽음 | v1을 읽음 | 1 |
| 2 | v1 조건으로 저장 성공 | 편집 중 | 2 |
| 3 | 저장 완료 | v1 조건으로 저장 시도 | 2 |
| 4 | — | 영향 행 0 → 충돌 안내 | 2 유지 |

`executeUpdate()==0`이면 rollback 후 false를 반환합니다. 나중에 저장한 사용자가 먼저 저장한 내용을 조용히 덮어쓰지 않습니다. 수정 성공 후에만 다음 버전의 내용 이력과 활동 기록을 넣습니다. 삭제도 화면 버전과 같은 경우만 수행하여 최신 수정본을 모르고 삭제하는 일을 줄입니다.

이 방식은 **낙관적 잠금**입니다. 편집창을 열어 놓은 동안 DB 행을 잠그지 않고, 저장 순간 충돌 여부를 확인합니다. 글자를 자동 병합하지는 않습니다. 충돌하면 현재 입력을 복사하고 취소한 뒤 최신 메모를 다시 열어 사용자가 합칩니다.

## 자동 갱신 알고리즘

`Timer(3000)` → 탭이 보이는지 검사 → reload → loading이면 건너뜀 → SwingWorker.doInBackground → 별도 Connection으로 NoteDAO.list → done → 목록 교체·조회가 끝난 순간의 선택 ID 복원 → loading=false.

메서드별 역할:

- `addNotify/removeNotify`: 패널이 화면에 붙거나 떨어질 때 타이머 시작·중지.
- `reload`: 배경 조회와 화면 갱신 조정. 일반 conn과 별도의 읽기 연결을 사용.
- `selected`: 선택이 없으면 안내. 선택한 Note 또는 null 반환.
- `edit`: 입력창을 열고 저장 실패 시 반복하여 수정 기회 제공.
- `delete`: 사용자 확인→버전 조건 삭제→갱신.
- `history`: teamId와 noteId로 내용 이력을 읽어 읽기 전용 창 표시.

`list`의 JOIN은 notes.updated_by로 마지막 수정자의 이름을 얻습니다. `history`는 notes와 JOIN해 같은 팀의 이력만 읽습니다. 이력은 내용(content) 중심이며 과거 제목 전체를 복원하는 기능은 아닙니다.

## 사용 모듈·자료구조

| 도구 | 왜 필요한가 |
|---|---|
| ArrayList<Note> | SQL 조회 결과 여러 건을 보관 |
| DefaultListModel·JList | 보관 목록과 표시 목록을 분리 |
| JTextArea·JOptionPane | 긴 메모 입력, 입력 유지·취소 |
| Swing Timer | 정해진 간격에 갱신 이벤트 발생 |
| SwingWorker | 배경 SQL과 EDT 화면 변경을 분리 |
| PreparedStatement·Connection | SQL 인수 설정과 트랜잭션 |

## 브리핑 원고와 확인 문제

“로그인한 회원 번호를 팀 조회와 메모 작성자 정보에 사용합니다. 메모 자동 갱신은 3초 폴링이며, 수정 충돌은 별도로 version 조건으로 검사합니다. v1을 읽은 두 사람 중 먼저 저장한 사람이 v2를 만들고, 나중 사람의 UPDATE는 0행이 되어 내용을 덮어쓰지 않습니다.”

스스로 답하세요: loading을 없애면? 팀 조건을 없애면? history INSERT만 실패하면? 팀원 삭제가 users까지 삭제하지 않는 이유는? 정답: 중복 배경 작업 가능, 타 팀 데이터 노출 가능, 메모 저장도 rollback, 기존 업무·자료가 참조하는 사용자와 로그인 계정을 보존해야 하기 때문입니다.
