package teamproject.resource;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MaterialPanel extends JPanel {

    private Connection conn;
    private int teamId;
    private JTable materialTable;
    private DefaultTableModel tableModel;

    public MaterialPanel(Connection conn, int teamId) {
        this.conn = conn;
        this.teamId = teamId;

        setLayout(new BorderLayout(5, 5));

        String[] columns = {"번호", "자료명", "설명", "URL 링크", "등록일"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        materialTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(materialTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("자료 등록");
        JButton updateButton = new JButton("자료 수정");
        JButton deleteButton = new JButton("자료 삭제");
        JButton openUrlButton = new JButton("링크 열기");

        addButton.addActionListener(e -> registerMaterial());
        updateButton.addActionListener(e -> updateMaterial());
        deleteButton.addActionListener(e -> deleteMaterial());
        openUrlButton.addActionListener(e -> openSelectedUrl());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(openUrlButton);

        add(buttonPanel, BorderLayout.SOUTH);

        materialTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedUrl();
                }
            }
        });

        reload();
    }

    public void reload() {
        try {
            MaterialDAO.loadMaterials(conn, tableModel, teamId);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "자료 목록 조회 실패: " + e.getMessage());
        }
    }

    private int getSelectedMaterialId() {
        int row = materialTable.getSelectedRow();
        if (row == -1) return -1;
        return Integer.parseInt(tableModel.getValueAt(row, 0).toString());
    }

    private void registerMaterial() {
        MaterialDAO.MaterialInfo input = showMaterialDialog("자료 등록", null);
        if (input == null) return;

        try {
            MaterialDAO.insertMaterial(conn, teamId, input.getTitle(), input.getDescription(), input.getUrl());
            JOptionPane.showMessageDialog(this, "자료가 등록되었습니다.");
            reload();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "자료 등록 실패: " + e.getMessage());
        }
    }

    private void updateMaterial() {
        int materialId = getSelectedMaterialId();
        if (materialId == -1) {
            JOptionPane.showMessageDialog(this, "수정할 자료를 선택하세요.");
            return;
        }

        try {
            MaterialDAO.MaterialInfo old = MaterialDAO.findMaterial(conn, materialId);
            if (old == null) {
                JOptionPane.showMessageDialog(this, "존재하지 않는 자료입니다.");
                reload();
                return;
            }

            MaterialDAO.MaterialInfo input = showMaterialDialog("자료 수정", old);
            if (input == null) return;

            boolean updated = MaterialDAO.updateMaterial(conn, materialId, input.getTitle(), input.getDescription(), input.getUrl());
            if (updated) {
                JOptionPane.showMessageDialog(this, "자료가 수정되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "수정 실패");
            }
            reload();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "자료 수정 실패: " + e.getMessage());
        }
    }

    private void deleteMaterial() {
        int materialId = getSelectedMaterialId();
        if (materialId == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 자료를 선택하세요.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "선택한 자료를 삭제하시겠습니까?", "자료 삭제", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            MaterialDAO.deleteMaterial(conn, materialId);
            JOptionPane.showMessageDialog(this, "자료가 삭제되었습니다.");
            reload();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "자료 삭제 실패: " + e.getMessage());
        }
    }

    private void openSelectedUrl() {
        int row = materialTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "열고자 하는 링크의 자료를 선택하세요.");
            return;
        }

        String url = tableModel.getValueAt(row, 3).toString();
        if (url == null || url.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "유효한 URL이 없습니다.");
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                JOptionPane.showMessageDialog(this, "웹 브라우저를 직접 실행할 수 없는 환경입니다.\n주소: " + url);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "링크 열기 실패: " + e.getMessage());
        }
    }

    private MaterialDAO.MaterialInfo showMaterialDialog(String title, MaterialDAO.MaterialInfo old) {
        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        JTextField urlField = new JTextField(20);

        if (old != null) {
            titleField.setText(old.getTitle());
            descArea.setText(old.getDescription() == null ? "" : old.getDescription());
            urlField.setText(old.getUrl());
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0; g.gridx = 0; panel.add(new JLabel("자료명 *"), g);
        g.gridx = 1; panel.add(titleField, g);

        g.gridy = 1; g.gridx = 0; panel.add(new JLabel("설명"), g);
        g.gridx = 1; panel.add(new JScrollPane(descArea), g);

        g.gridy = 2; g.gridx = 0; panel.add(new JLabel("URL/링크 *"), g);
        g.gridx = 1; panel.add(urlField, g);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return null;

            String inputTitle = titleField.getText().trim();
            String inputDesc = descArea.getText().trim();
            String inputUrl = urlField.getText().trim();

            if (inputTitle.isEmpty()) {
                JOptionPane.showMessageDialog(this, "자료명을 입력하세요.");
                continue;
            }
            if (inputUrl.isEmpty()) {
                JOptionPane.showMessageDialog(this, "URL 링크를 입력하세요.");
                continue;
            }

            return new MaterialDAO.MaterialInfo(old == null ? 0 : old.getMaterialId(), inputTitle, inputDesc, inputUrl, null);
        }
    }
}