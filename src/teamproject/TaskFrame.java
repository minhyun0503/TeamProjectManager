package teamproject;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import java.sql.Connection;
import java.sql.SQLException;

public class TaskFrame extends JFrame { //업무 관리 화면(GUI) 담당 클래스 | extends 키워드 :  JFrame의 기능 상속

    // 업무 목록을 표시할 표
    private JTable taskTable; //표 화면에 표시하는 역할
    private DefaultTableModel tableModel; //표 데이터 관리하는 역할
	
    // 업무 관리 화면 생성
    public TaskFrame(Connection conn) throws SQLException{

        setTitle("팀프로젝트 관리 프로그램"); //프로그램 창 제목 지정
        setSize(800, 500); // 창 크기 : 800x500
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //창 닫을 때 프로그램 종료
        setLocationRelativeTo(null); //화면 중앙에 창 배치

    
    
        // 표의 열 이름 설정
        String[] columns = {
        		"번호", "업무명", "담당자", "마감일", "상태"
        };
    
        // 표 데이터 모델 생성
        tableModel = new DefaultTableModel(columns, 0); //DefaultTableModel(): 표 데이터 구조를 만드는 생성자 | (columns = 표의 열이름, 0 = 처음 생성할 행의 개수)

        // 표 화면 생성 및 스크롤 기능 추가
        taskTable = new JTable(tableModel); //
        JScrollPane scrollPane = new JScrollPane(taskTable); //스크롤 기능

        add(scrollPane); //Swing 창에 컴포넌트 추가 메서드
        TaskDAO.loadTasks(conn, tableModel);
    }
}
