# 신*준 — 자료·대시보드·아이디어 협업 브리핑

[먼저 읽는 쉬운 설명](easy/resource.md) → 이 문서 → [실제 코드 색인](07_code_index.md) 순서로 확인하세요.

담당 파일은 resource 패키지의 MainFrame, DashboardPanel, MaterialDAO, MaterialPanel, IdeaDAO, IdeaPanel, IdeaCardRenderer, ActivityPanel입니다. 공통 ActivityDAO는 장*민 담당이며, 이 영역은 기록을 보여주는 화면을 맡습니다.

## 통합 MainFrame

로그인에서 User와 Team을 받습니다. teamId는 Team 객체에서 읽고 userId는 User 객체에서 읽습니다. 제목에 선택한 팀과 로그인 이름을 표시합니다.

기존 4개 탭: 대시보드, 업무, 팀원, 자료. 추가5개: 업무 보드·내 업무, 공유 메모, 일정·달력, 아이디어·댓글, 최근 활동. 각 화면 생성자는 같은 conn·teamId를 받고 작성자가 필요한 기능은 userId도 받습니다.

탭 변경 이벤트는 숫자 인덱스 대신 getSelectedComponent를 비교합니다. 중간 탭 생성 실패로 인덱스가 달라져도 다른 기능을 잘못 갱신하지 않게 하기 위해서입니다. 선택한 화면의 reload를 호출하므로 다른 탭에서 바꾼 결과가 보입니다.

## 기존 자료 관리

MaterialPanel은 표·등록·수정·삭제·링크 버튼과 입력창을 만듭니다. 제목은100자·링크는500자 제한, 빈 제목·링크를 검사합니다. URL을 클릭하면 Desktop.BROWSE로 OS 기본 브라우저에 넘깁니다. 실행 환경에서 지원하지 않으면 주소를 안내합니다. 파일 자체를 업로드하지는 않습니다.

MaterialDAO는 teamId 조건으로 자료를 읽고, 자료 번호와 teamId를 함께 조건으로 수정·삭제합니다. insertMaterial은 로그인 userId를 필수 uploader_id에 저장합니다. 조회는 url을 우선 읽고 비어 있으면 link_url을 대신 읽어 기존 링크도 보입니다. getRecentMaterials는 material_id 내림차순과 LIMIT으로 최근 항목을 가져옵니다.

## DashboardPanel

reload는 InlineProgressDAO의 계산 데이터를 받아 전체 진행률, 오늘 마감·지연 개수를 표시합니다. 업무 목록은 현재 팀 조건으로 마감일순 정렬합니다. 자료 목록은 getRecentMaterials로 최근5건을 표시합니다. 화면 제목의 '마감 임박' 표현과 달리 업무 목록 자체가 임박 업무만 필터링하는 것은 아니므로 정확히 설명하세요.

## 아이디어 데이터 구조

| 표·객체 | 역할 |
|---|---|
| ideas / Idea | 제목·내용·링크·작성자·팀 |
| idea_comments | 게시물별 댓글과 작성자 |
| idea_reactions | 게시물과 사용자 한 쌍의 공감 |
| IdeaDAO.Idea.comments/likes | 화면에 표시할 집계 결과 |

댓글과 공감을 게시물 문자열에 붙이지 않고 별도 표에 두면 각각의 사용자·건수를 관리할 수 있습니다. `(idea_id,user_id)` 복합 기본키는 같은 사용자의 중복 공감 행을 허용하지 않습니다.

## IdeaDAO의 메서드

- `list`: 현재 팀 게시물과 작성자 이름을 JOIN합니다. 각 게시물의 댓글·공감은 하위 COUNT 쿼리로 가져와 Idea 객체로 반환합니다.
- `save`: 제목·링크 길이, http/https 형식, 팀 소속 검사. 새 글이면 INSERT, 기존이면 teamId·authorId·ideaId가 모두 일치할 때 UPDATE합니다. 활동 기록과 같은 트랜잭션입니다.
- `delete`: 작성자와 팀 조건을 모두 검사합니다. 게시물 삭제 시 댓글·공감은 FK의 ON DELETE CASCADE로 삭제됩니다.
- `requireIdea`: 현재 팀의 게시물인지 SELECT1로 검사합니다.
- `comments`: 부모 ideas와 JOIN해 teamId까지 제한하고 comment_id 순으로 반환합니다.
- `comment`: 빈 값·1000자 제한 검사→소속·게시물 확인→댓글 INSERT→활동 기록→commit.
- `toggleLike`: 같은 사용자 공감 DELETE→삭제 행이0이면 INSERT→활동 기록→commit.

## 공감 토글을 손으로 실행

| 기존 공감 행 | DELETE 결과 | 다음 동작 | 최종 상태 |
|---|---|---|---|
| 없음 | 0 | INSERT | 공감1개 |
| 있음 | 1 | 추가 INSERT 안 함 | 공감0개 |

다른 사용자의 공감은 user_id가 달라 삭제되지 않습니다. 빠르게 두 번 눌렀을 때 화면이 이미 바뀌었는지 믿고 계산하지 않고 DB의 실제 삭제 결과를 기준으로 합니다. 같은 사용자의 여러 프로세스가 정확히 동시에 새 공감을 넣으면 기본키가 중복을 막으며, 한쪽은 저장 오류를 안내하고 최신 목록을 다시 확인해야 합니다. 무제한 중복 공감이 저장되지는 않습니다.

## IdeaPanel과 카드 렌더러

왼쪽은 아이디어 카드 JList, 오른쪽은 내용과 댓글, 아래는 댓글 입력입니다. list 선택 이벤트가 showDetail을 호출합니다. 내 글 수정은 UI와 DAO에서 모두 작성자 조건을 확인합니다. 댓글 등록 성공 후에만 입력칸을 비우므로 오류가 나면 입력을 잃지 않습니다.

5초 Timer가 목록을 갱신합니다. SQL은 별도 Connection의 SwingWorker에서 실행하고 done에서 모델을 바꿉니다. 이전 선택 id를 기억하므로 갱신 후 같은 게시물을 다시 선택합니다. 댓글 입력 JTextField는 목록 갱신에서 바꾸지 않습니다. 선택한 게시물의 댓글은 showDetail에서 현재 내용으로 읽습니다.

IdeaCardRenderer는 제목·작성자·댓글/공감 수를 JLabel3개에 넣어 작은 카드로 보여줍니다. 역할은 시각적 표현뿐이며, 데이터 저장은 하지 않습니다. ListCellRenderer 인터페이스는 JList가 카드 모양을 요청하는 약속입니다.

## 최근 활동

ActivityPanel.reload는 ActivityDAO.recent에서 문자열 목록을 받아 JList에 표시합니다. 탭을 열거나 버튼을 누르면 갱신합니다. 별도 실시간 푸시 알림은 아니며 메모·일정·아이디어의 명시된 저장·댓글·공감 활동을 보여줍니다.

## 설명 원고·질문

“자료는 링크와 업로더를 저장합니다. 새 아이디어 게시판은 게시물·댓글·공감 표를 분리했고, 복합 기본키로 사용자당 공감1개를 보장합니다. 카드 렌더러는 표현만, DAO는 SQL만 맡습니다. 탭을 바꾸면 같은 팀 데이터를 다시 읽어 다른 기능의 결과를 확인합니다.”

질문: 게시물 삭제 후 댓글이 왜 남지 않나? 왜 공감 수를 ideas의 숫자 필드에만 저장하지 않나? 왜 renderer에서 DB를 읽지 않나? 답: FK CASCADE, 누가 눌렀는지 관리하고 중복을 막아야 해서, 그리기는 자주 호출되므로 화면 표현과 DB처리를 분리해야 해서입니다.
