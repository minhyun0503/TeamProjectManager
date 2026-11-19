# 실행 의존성

| 구성 | 출처 | 용도 |
|---|---|---|
| JDK25 | 사용자가 설치하는 JDK | 컴파일·Swing·JDBC·날짜·파일 API |
| MySQL Connector/J26.10.0 | 저장소의 기존 lib 포함본 | MySQL 연결 |
| H22.4.240 | https://repo.maven.apache.org/maven2/com/h2database/h2/2.4.240/h2-2.4.240.jar | 로컬 데모·회귀 테스트 |

각 JAR의 META-INF 라이선스 고지를 유지했습니다. H2는 MPL2.0/EPL1.0 이중 라이선스로 제공됩니다. https://h2database.com/html/license.html

벤치마킹 참고: [노션 프로젝트 관리](https://www.notion.com/help/guides/project-management-that-evolves-with-your-team), [노션 프로젝트·업무](https://www.notion.com/help/guides/getting-started-with-projects-and-tasks), [패들렛 협업](https://padlet.com/page/team-collaboration-tool). 기능의 아이디어를 학생 프로젝트 규모에 맞춰 구현했으며 해당 서비스의 API를 호출하거나 계정 데이터를 가져오지 않습니다.
