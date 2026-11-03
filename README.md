# 대학생 팀프로젝트 관리 프로그램 (자바프로그래밍 3조)

팀프로젝트의 업무 분담, 일정, 자료를 한곳에서 관리하는 Java Swing + JDBC + MySQL 데스크톱 프로그램입니다.

## 기술 스택
- Java (Swing GUI), JavaSE-25
- MySQL 8 (Aiven 무료 클라우드 DB, SSL 필수)
- JDBC (MySQL Connector/J)
- 구조: Java GUI → DAO(표마다 하나) → JDBC → 클라우드 MySQL

## 실행 방법
같은 클라우드 DB 를 사용합니다. MySQL을 따로 설치하거나 `schema.sql`을 실행할 필요가 없습니다.

1. GitHub Desktop에서 이 레포를 Clone 합니다. (`File` → `Clone repository`)
2. Eclipse에서 프로젝트를 가져옵니다. (`File` → `Import` → `Existing Projects into Workspace` → 클론한 폴더 선택)
3. MySQL Connector/J jar를 추가합니다.
   - 프로젝트 우클릭 → `Build Path` → `Configure Build Path` → `Libraries` → `Classpath` → `Add External JARs...`
4. `src/teamproject/Main.java`를 실행하고, 비밀번호 창에 DB 비밀번호를 입력합니다.
   - DB 비밀번호는 단톡방으로만 공유합니다.

※ Aiven 무료 서비스는 오래 사용하지 않으면 자동으로 꺼질 수 있습니다. 연결이 안 되면 팀장에게 알려주세요. (발표 전 Running 상태 확인)

## 테스트 계정 (프로그램 로그인용)
| 이름 | 아이디(학번) | 비밀번호 |
|---|---|---|
| 장현민 | 20253289 | 1234 |
| 이해찬 | 20253293 | 1234 |
| 이동주 | 20253307 | 1234 |
| 윤정훈 | 20253364 | 1234 |
| 신예준 | 20253584 | 1234 |

시연용 계정이며, 비밀번호는 현재 평문으로 저장됩니다. (향후 암호화 예정)
DB 비밀번호(접속용)와 위 로그인 비밀번호는 서로 다른 것입니다.

## 데이터베이스 구조 (`team_project`)
| 표 | 설명 |
|---|---|
| `users` | 사용자 (로그인 아이디 = 학번) |
| `teams` | 팀(프로젝트) |
| `team_members` | 팀 소속과 역할 |
| `tasks` | 업무 (담당자 `assignee_id` → `users`, 마감일, 상태) |
| `schedules` | 일정 |
| `materials` | 자료(링크) |

- 진행률은 별도 표 없이 tasks의 완료 개수 / 전체 개수로 계산합니다.
- `db/schema.sql`은 DB 구조 참고용입니다. (클라우드에 이미 반영되어 있으니 다시 실행하지 마세요.)

## 파일 구조
```
TeamProjectManager/
├── src/
│   ├── module-info.java
│   └── teamproject/
│       ├── Main.java            프로그램 시작, DB 비밀번호 입력, 연결 후 로그인 창 실행
│       ├── LoginFrame.java      로그인 화면 (성공하면 업무 화면으로 이동)
│       ├── DB_connection.java   클라우드 MySQL 연결
│       ├── UserDAO.java         users 표 처리 (로그인 확인)
│       ├── TaskDAO.java         tasks 표 처리 (업무 조회, users와 JOIN)
│       └── TaskFrame.java       업무 목록 GUI
├── db/
│   └── schema.sql               DB 생성 SQL (참고용)
└── README.md
```

## 로그인 사용법 (화면 담당자용)
```java
// 아이디/비밀번호가 맞으면 이름, 틀리면 null 반환
String name = UserDAO.login(conn, loginId, password);
```

## 역할 분담
|  이름  |
|--------|
| 장현민 | DB 설계, JDBC 연결, 로그인 확인 로직, 로그인 화면 기본 구현 |
| 이해찬 | 로그인 화면 (LoginFrame) |
| 윤정훈 | 업무 관리 GUI |
| 이동주 | 일정, 진행률 |
| 신예준 | 자료, 대시보드 |

## 현재 구현 상태
- [x] DB 표 6개 설계 및 클라우드(Aiven) 반영
- [x] 클라우드 MySQL 연결 (`DB_connection`)
- [x] 로그인 확인 (`UserDAO.login`)
- [x] 업무 목록 조회 (`TaskDAO.showTasks`, `TaskDAO.loadTasks`)
- [x] 로그인 화면 (`LoginFrame`)
- [x] 로그인 성공 후 업무 목록 표 표시 (`TaskFrame`에서 `loadTasks` 호출)
- [ ] 업무 등록 · 완료 처리 (GUI)
- [ ] 일정 · 자료 · 대시보드

## 참고
- 실행 흐름: `Main`(DB 연결) → `LoginFrame`(로그인) → `TaskFrame`(업무 목록)
- DB 연결(`conn`)은 `Main`에서 만들어 화면끼리 넘겨 가며 하나만 사용합니다. (프로그램이 끝나면 자동으로 닫힘)
- 새 화면에서 DB가 필요하면 생성자에서 `Connection conn`을 전달받아 사용하세요.
