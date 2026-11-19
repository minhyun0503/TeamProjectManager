# 코드를 이해하고 브리핑하는 순서

처음 읽는다면 [쉬운 설명서](easy/index.md)를 먼저 읽으세요. 이 문서는 그다음 실제 소스 전체를 확인할 때 사용합니다.

목표는 파일을 외우는 것이 아니라, 입력값을 바꿨을 때 결과를 **실행 전에 예측하는 것**입니다. 설명서만 읽고 이해했다고 판단하지 말고 아래 과정을 반복하세요.

## 1. 공통 구조부터 확인

`Main → LoginFrame → UserDAO → TeamDAO → MainFrame` 순서로 파일을 엽니다. 종이에 로그인 회원 번호와 선택한 팀 번호를 각각 적으세요. 회원 10과 팀 7은 서로 다른 값입니다. `p.setInt(1, teamId)`의 1은 첫 번째 물음표의 위치입니다.

## 2. 본인 기능 하나를 끝까지 추적

1. 생성자에서 버튼을 찾습니다.
2. `addActionListener`가 호출하는 메서드를 찾습니다.
3. 그 메서드가 어떤 입력값을 읽는지 적습니다.
4. DAO에 넘기는 인수를 순서대로 적습니다.
5. DAO의 SQL에서 `?`와 `setInt/setString`을 하나씩 대응시킵니다.
6. `executeQuery`인지 `executeUpdate`인지 확인합니다.
7. DB에서 바뀌는 행을 예측합니다.
8. 화면의 `reload`가 왜 다시 호출되는지 설명합니다.

처음에는 생성자와 가장 짧은 조회 함수부터 읽고, 등록→수정→삭제→예외 순서로 확장합니다. `getX`는 데이터 읽기, `setText`는 화면 글자 변경, `setInt`는 SQL 인수 설정입니다. 이름이 비슷해도 역할이 다릅니다.

## 3. 객체와 목록을 구분

`Note`, `Card`, `Schedule`, `Idea`는 데이터 한 건입니다. `ArrayList<Note>`는 조회한 여러 건입니다. `DefaultListModel<Note>`는 JList에 보여줄 목록입니다. `JList` 자체와 데이터 목록을 같은 것으로 설명하지 마세요. DAO는 화면을 그리지 않고 값을 돌려줍니다.

## 4. 담당자별 읽기 경로

| 담당 | 먼저 읽기 | 다음에 읽기 | 마지막 난도 높은 부분 |
|---|---|---|---|
| 장*민 | DB_connection·TeamDAO | Main·SchemaSetup | SwingWorker 시작 흐름·트랜잭션과 기록 |
| 이*찬 | UserDAO·LoginFrame·MemberFrame | MemberDAO·NotePanel | NoteDAO의 version 검사·자동 갱신 분리 |
| 윤*훈 | TaskFrame 생성자·TaskDAO 조회 | TaskBoardPanel·CardRenderer | 메모 연결의 생성 키·원자성 |
| 이*주 | InlineProgressData | InlineProgressDAO·Panel | 일정의 월간 겹침 조건과 달력 칸 계산 |
| 신*준 | MaterialPanel·MaterialDAO | MainFrame·DashboardPanel | IdeaDAO의 댓글·공감·트랜잭션과 렌더러 |

## 5. 이해 확인

본인 코드에서 아래를 모두 설명해 보세요.

- 메서드 입력, 반환값, 화면·DB에 생기는 변화.
- 팀 번호 조건을 없애면 어떤 다른 팀 데이터가 섞이는지.
- `try-with-resources`가 닫는 객체와 화면 전체가 공유하는 Connection의 차이.
- 실패 시 어떤 catch로 가는지, 입력은 유지되는지, 부분 저장은 남는지.
- 선택한 행이 없을 때와 동시에 다른 사람이 수정했을 때의 차이.
- 가장 핵심인 반복문 또는 조건문을 작은 예로 손으로 실행하기.

설명에 막히면 파일 전체를 다시 읽기보다, 해당 메서드의 한 입력 예를 만들어 실행 경로를 따라가세요. 색인의 메서드 목록은 빠뜨린 부분이 없는지 점검하는 용도입니다.
