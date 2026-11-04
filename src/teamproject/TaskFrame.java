package teamproject;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class TaskFrame extends JFrame { //업무 관리 화면(GUI) 담당 클래스 | extends 키워드 :  JFrame의 기능 상속

    // taskTable과 tableModel을 잘구분해서 코드 이해하기.
    private JTable taskTable; //데이터를 화면에 어떻게 표로 보여주는지, 창
    private DefaultTableModel tableModel; //어떤 데이터를 담고 있는지, 데이터
	
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
    
        tableModel = new DefaultTableModel(columns, 0) {//열 이름을 표시하고, 기본 데이터는 0
            @Override
            public boolean isCellEditable(int row, int column) {//사용자가 표의 셀을 직접 수정못하게 하는 코드
                return false;
            }
        };

        taskTable = new JTable(tableModel); //화면에 보이는 표를 생성 (이 시점까지 표안에 데이터 x)
        
        JScrollPane scrollPane = new JScrollPane(taskTable); //taskTable을 스크롤 가능한 영역 안에 추가
        
     // 업무 목록 더블클릭 시 팀원 관리창 열기
        taskTable.addMouseListener(new java.awt.event.MouseAdapter() {//더블 클릭 시 실행할 동작 등록하는 메서드
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {//클릭이 2번???
                    try {
                        System.out.println("더블클릭 감지");
                        MemberFrame frame = new MemberFrame(conn, 1);//조회된 업무현황 표를 더블 클릭 시 해당 팀원을 표시(현재는 1번팀으로만 고정값)
                        frame.setVisible(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(TaskFrame.this, ex.getMessage());
                    }
                }
            }
        });

        add(scrollPane); //Swing 창에 스크롤 가능한 표 배치
        TaskDAO.loadTasks(conn, tableModel); //taskDAO클래스에서 tableModel(표)에 데이터 채워넣는 단계
    }
}
