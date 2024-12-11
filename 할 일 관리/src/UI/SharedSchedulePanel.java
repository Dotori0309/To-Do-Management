package UI;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import Utils.DatabaseUtils;

/**
 * SharedSchedulePanel 클래스는 공유된 일정 정보를 표시하고 관리하는 UI 패널입니다.
 */
public class SharedSchedulePanel extends JPanel {
    private ScheduleApp app; // ScheduleApp 클래스 참조
    private DefaultTableModel sharedScheduleTableModel; // 공유한 일정 테이블 모델
    private DefaultTableModel receivedScheduleTableModel; // 공유받은 일정 테이블 모델
    private JTable sharedScheduleTable; // 공유한 일정 테이블
    private JTable receivedScheduleTable; // 공유받은 일정 테이블
    private JButton viewSchedulesButton; // 일정 확인 버튼

    public SharedSchedulePanel(ScheduleApp app) {
        this.app = app;
        setLayout(new BorderLayout());

        // 제목 라벨
        JLabel titleLabel = new JLabel("공유 일정 관리");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 탭 패널 생성
        JTabbedPane tabbedPane = new JTabbedPane();

        // 공유한 일정 탭
        sharedScheduleTableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"할 일 ID", "제목", "공유된 사용자"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 모든 셀 편집 불가능
            }
        };
        sharedScheduleTable = new JTable(sharedScheduleTableModel);
        JScrollPane sharedScrollPane = new JScrollPane(sharedScheduleTable);
        JPanel sharedPanel = new JPanel(new BorderLayout());
        sharedPanel.add(sharedScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("공유한 일정", sharedPanel);
     // 공유한 일정 테이블의 할 일 ID 열 숨기기
        sharedScheduleTable.getColumnModel().getColumn(0).setMinWidth(0);
        sharedScheduleTable.getColumnModel().getColumn(0).setMaxWidth(0);
        sharedScheduleTable.getColumnModel().getColumn(0).setWidth(0);

        // 공유받은 일정 탭
        receivedScheduleTableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"할 일 ID", "제목", "공유한 사용자"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 모든 셀 편집 불가능
            }
        };
        receivedScheduleTable = new JTable(receivedScheduleTableModel);
        JScrollPane receivedScrollPane = new JScrollPane(receivedScheduleTable);
        JPanel receivedPanel = new JPanel(new BorderLayout());
        receivedPanel.add(receivedScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("공유받은 일정", receivedPanel);
     // 공유받은 일정 테이블의 할 일 ID 열 숨기기
        receivedScheduleTable.getColumnModel().getColumn(0).setMinWidth(0);
        receivedScheduleTable.getColumnModel().getColumn(0).setMaxWidth(0);
        receivedScheduleTable.getColumnModel().getColumn(0).setWidth(0);

        add(tabbedPane, BorderLayout.CENTER);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton shareButton = new JButton("공유 일정 등록");
        JButton backButton = new JButton("뒤로가기");
        viewSchedulesButton = new JButton("일정 확인");

        buttonPanel.add(shareButton);
        buttonPanel.add(viewSchedulesButton);
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 버튼 이벤트 처리
        shareButton.addActionListener(e -> displayTaskSelectionDialog());
        backButton.addActionListener(e -> app.getCardLayout().show(app.getMainPanel(), "Tasks"));
        viewSchedulesButton.addActionListener(e -> refreshSharedAndReceivedSchedules());

        // 초기 데이터 로드
        loadSharedSchedules();
        loadReceivedSchedules();

        // 테이블 클릭 시 본문 표시
        sharedScheduleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = sharedScheduleTable.getSelectedRow();
                if (selectedRow != -1) {
                    int taskId = (int) sharedScheduleTable.getValueAt(selectedRow, 0);
                    String description = fetchTaskDescriptionFromDatabase(taskId);
                    if (description != null) {
                        showTaskDescriptionDialog(description);
                    }
                }
            }
        });

        receivedScheduleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = receivedScheduleTable.getSelectedRow();
                if (selectedRow != -1) {
                    int taskId = (int) receivedScheduleTable.getValueAt(selectedRow, 0);
                    String description = fetchTaskDescriptionFromDatabase(taskId);
                    if (description != null) {
                        showTaskDescriptionDialog(description);
                    }
                }
            }
        });
    }

    /**
     * 본문 데이터를 데이터베이스에서 불러옵니다.
     */
    private String fetchTaskDescriptionFromDatabase(int taskId) {
        String description = null;
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT description FROM doyeondb.Tasks WHERE task_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, taskId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                description = resultSet.getString("description");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "본문 데이터를 불러오는 중 오류가 발생했습니다.");
        }
        return description;
    }

    /**
     * 본문 내용을 다이얼로그로 표시합니다.
     */
    private void showTaskDescriptionDialog(String description) {
        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);

        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "본문 내용",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * 할 일 목록을 보여주고 사용자가 선택할 수 있는 다이얼로그 표시
     */
    private void displayTaskSelectionDialog() {
        DefaultTableModel taskTableModel = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"ID", "제목", "상태"}
        );

        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT task_id, title, status FROM doyeondb.Tasks WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, app.getLoggedInUserId());
            ResultSet resultSet = stmt.executeQuery();

            taskTableModel.setRowCount(0);
            while (resultSet.next()) {
                int taskId = resultSet.getInt("task_id");
                String title = resultSet.getString("title");
                String status = resultSet.getString("status");
                taskTableModel.addRow(new Object[]{taskId, title, status});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "할 일 목록을 불러오는 중 오류가 발생했습니다.");
            return;
        }

        JTable taskTable = new JTable(taskTableModel);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        JTextField userIdField = new JTextField();
        Object[] message = {
            "할 일 목록:", scrollPane,
            "공유할 사용자 닉네임:", userIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "공유 일정 등록", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow != -1) {
                int selectedTaskId = (int) taskTable.getValueAt(selectedRow, 0);
                String username = userIdField.getText().trim();
                if (!username.isEmpty() && isValidUserId(username)) {
                    addSharedSchedule(username, selectedTaskId);
                } else {
                    JOptionPane.showMessageDialog(this, "유효한 사용자 닉네임을 입력해주세요.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "할 일을 선택해주세요.");
            }
        }
    }

    /**
     * 공유한 일정 데이터 로드
     */
    private void loadSharedSchedules() {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = """
                    SELECT s.task_id, t.title, u.username 
                    FROM doyeondb.SharedSchedules s
                    JOIN doyeondb.Tasks t ON s.task_id = t.task_id
                    JOIN doyeondb.Users u ON s.shared_with_user_id = u.user_id
                    WHERE s.user_id = ?""";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, app.getLoggedInUserId());
            ResultSet resultSet = stmt.executeQuery();

            sharedScheduleTableModel.setRowCount(0);
            while (resultSet.next()) {
                int taskId = resultSet.getInt("task_id");
                String title = resultSet.getString("title");
                String username = resultSet.getString("username");
                sharedScheduleTableModel.addRow(new Object[]{taskId, title, username});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "공유한 일정을 불러오는 중 오류가 발생했습니다.");
        }
    }

    /**
     * 공유받은 일정 데이터 로드
     */
    private void loadReceivedSchedules() {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = """
                    SELECT s.task_id, t.title, u.username 
                    FROM doyeondb.SharedSchedules s
                    JOIN doyeondb.Tasks t ON s.task_id = t.task_id
                    JOIN doyeondb.Users u ON s.user_id = u.user_id
                    WHERE s.shared_with_user_id = ?""";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, app.getLoggedInUserId());
            ResultSet resultSet = stmt.executeQuery();

            receivedScheduleTableModel.setRowCount(0);
            while (resultSet.next()) {
                int taskId = resultSet.getInt("task_id");
                String title = resultSet.getString("title");
                String username = resultSet.getString("username");
                receivedScheduleTableModel.addRow(new Object[]{taskId, title, username});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "공유받은 일정을 불러오는 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 닉네임으로 ID를 확인합니다.
     */
    private boolean isValidUserId(String username) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT COUNT(*) FROM doyeondb.Users WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next() && resultSet.getInt(1) > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 공유 일정 등록
     */
    private void addSharedSchedule(String username, int taskId) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "INSERT INTO doyeondb.SharedSchedules (user_id, task_id, shared_with_user_id) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, app.getLoggedInUserId());
            stmt.setInt(2, taskId);
            stmt.setInt(3, fetchUserIdByUsername(username));
            stmt.executeUpdate();

            loadSharedSchedules();
            JOptionPane.showMessageDialog(this, "공유 일정이 등록되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "공유 일정 등록 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 닉네임으로 ID 반환
     */
    private int fetchUserIdByUsername(String username) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT user_id FROM doyeondb.Users WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * 일정 확인 버튼 동작
     */
    private void refreshSharedAndReceivedSchedules() {
        loadSharedSchedules();
        loadReceivedSchedules();
        JOptionPane.showMessageDialog(this, "일정 목록이 갱신되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
    }
}
