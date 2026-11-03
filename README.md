# 대학생 팀프로젝트 관리 프로그램 (자바프로그래밍 3조)

팀프로젝트의 업무 분담, 일정, 자료를 한곳에서 관리하는 Java Swing + JDBC + MySQL 데스크톱 프로그램입니다.

## 기술 스택
- Java (Swing GUI)
- MySQL 8.0
- JDBC (MySQL Connector/J)
- 구조: Java GUI → Java 기능 로직(DAO) → JDBC → MySQL

## 실행 방법
1. MySQL 8.0을 설치하고 Workbench에서 `db/schema.sql`을 실행합니다. (DB와 표 6개, 테스트 데이터 생성)
   - ※ `schema.sql`은 **처음 한 번만** 실행하세요. 이미 실행했다면 "already exists" 오류가 나는 게 정상입니다.
2. Eclipse에서 프로젝트를 가져옵니다. (`File` → `Import` → `Existing Projects into Workspace`)
3. MySQL Connector/J jar를 추가합니다.
   - 프로젝트 우클릭 → `Build Path` → `Configure Build Path` → `Libraries` → `Classpath` → `Add External JARs...`
4. `src/teamproject/Main.java`를 실행하고, 본인 MySQL root 비밀번호를 입력합니다.

## 테스트 계정
| 이름 | 아이디(학번) | 비밀번호 |
|---|---|---|
| 장현민 | 20253289 | 1234 |
| 이해찬 | 20253293 | 1234 |
| 이동주 | 20253307 | 1234 |
| 윤정훈 | 20253364 | 1234 |
| 신예준 | 20253584 | 1234 |

시연용 계정이며, 비밀번호는 현재 평문으로 저장됩니다. (향후 암호화 적용 예정)

## 데이터베이스 구조 (`team_project`)
| 표 | 설명 |
|---|---|
| `users` | 사용자 (로그인 정보) |
| `teams` | 팀(프로젝트) |
| `team_members` | 팀 소속과 역할 |
| `tasks` | 업무 (담당자, 마감일, 상태) |
| `schedules` | 일정 |
| `materials` | 자료(링크) |

진행률은 별도 표 없이 `tasks`의 완료 개수 / 전체 개수로 계산합니다.

## 파일 구조
```
TeamProjectManager/
├── src/
│   ├── module-info.java
│   └── teamproject/
│       ├── Main.java            프로그램 시작, 비밀번호 입력, DB 연결
│       ├── DB_connection.java   MySQL 연결
│       ├── UserDAO.java         users 표 처리 (로그인 확인)
│       ├── TaskDAO.java         tasks 표 처리 (업무 조회)
│       └── TaskFrame.java       업무 목록 GUI
├── db/
│   └── schema.sql               DB 생성 SQL
└── README.md
```

## 로그인 사용법 (화면 담당자용)
```java
// 아이디/비밀번호가 맞으면 이름, 틀리면 null 반환
String name = UserDAO.login(conn, loginId, password);
```

## 현재 구현 상태
- [x] MySQL 연결 (`DB_connection`)
- [x] 로그인 확인 (`UserDAO.login`)
- [x] 업무 목록 조회 (`TaskDAO.showTasks`, `TaskDAO.loadTasks`)
- [ ] 로그인 화면 (GUI)
- [ ] 업무 등록 · 완료 처리 (GUI)
- [ ] 일정 · 자료 · 대시보드
