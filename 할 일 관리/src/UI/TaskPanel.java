package UI;

import Utils.DatabaseUtils;
import Utils.TaskSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskPanel extends JPanel {
    private ScheduleApp app;
    private DefaultTableModel taskTableModel;
    private JTable tasksTable;

    // TaskSorter 인스턴스 생성
    private TaskSorter taskSorter;

    public TaskPanel(ScheduleApp app) {
        this.app = app;
        this.taskSorter = new TaskSorter(); // TaskSorter 초기화

        setLayout(new BorderLayout());

        // 제목
        JLabel titleLabel = new JLabel("할 일 관리");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 테이블 모델 (ID 포함)
        taskTableModel = new DefaultTableModel(new Object[][] {}, new String[] { "ID", "제목", "진행도", "마감일", "우선순위" }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 셀 수정 비활성화
            }
        };
        tasksTable = new JTable(taskTableModel);

        // ID 열 숨기기
        tasksTable.getColumnModel().getColumn(0).setMinWidth(0);
        tasksTable.getColumnModel().getColumn(0).setMaxWidth(0);
        tasksTable.getColumnModel().getColumn(0).setWidth(0);

        tasksTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String progress = table.getValueAt(row, 2).toString();
                String priority = table.getValueAt(row, 4).toString();

                if ("Completed".equalsIgnoreCase(progress)) {
                    cell.setBackground(Color.GRAY);
                } else {
                    switch (priority.toLowerCase()) {
                        case "high":
                            cell.setBackground(Color.RED);
                            break;
                        case "medium":
                            cell.setBackground(Color.ORANGE);
                            break;
                        case "low":
                            cell.setBackground(Color.GREEN);
                            break;
                        default:
                            cell.setBackground(Color.WHITE);
                            break;
                    }
                }

                if (isSelected) {
                    cell.setBackground(cell.getBackground().darker());
                }

                return cell;
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(tasksTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // 제목 셀 선택 시 본문 내용 표시
        tasksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tasksTable.getSelectedRow();
                if (selectedRow != -1) {
                    int taskId = getTaskIdByRow(selectedRow); // 내부 ID 가져오기
                    String description = fetchDescriptionFromDatabase(taskId);
                    if (description != null) {
                        showDescriptionDialog(description);
                    }
                }
            }
        });

        // 동쪽 버튼 패널
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));

        JButton addButton = new JButton("할 일 추가");
        JButton editButton = new JButton("할 일 수정");
        JButton deleteButton = new JButton("할 일 삭제");
        JButton completeButton = new JButton("완료 처리");
        JButton uncompleteButton = new JButton("미완료 처리");

        eastPanel.add(addButton);
        eastPanel.add(editButton);
        eastPanel.add(deleteButton);
        eastPanel.add(completeButton);
        eastPanel.add(uncompleteButton);

        add(eastPanel, BorderLayout.EAST);

        // 서쪽 버튼 패널
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));

        JButton viewTimetableButton = new JButton("시간표 보기");
        JButton sharedScheduleButton = new JButton("공유 일정 보기");

        westPanel.add(viewTimetableButton);
        westPanel.add(sharedScheduleButton);

        add(westPanel, BorderLayout.WEST);

        // 남쪽 버튼 패널
        JPanel southPanel = new JPanel();
        JButton sortDeadlineButton = new JButton("마감일 정렬");
        JButton sortPriorityButton = new JButton("우선순위 정렬");
        JButton sortProgressButton = new JButton("진행도 정렬");
        JButton logoutButton = new JButton("로그아웃");

        southPanel.add(sortDeadlineButton);
        southPanel.add(sortPriorityButton);
        southPanel.add(sortProgressButton);
        southPanel.add(logoutButton);
        add(southPanel, BorderLayout.SOUTH);

        // 버튼 동작 구현
        addButton.addActionListener(e -> handleAddTask());
        editButton.addActionListener(e -> handleEditTask());
        deleteButton.addActionListener(e -> handleDeleteTask());
        completeButton.addActionListener(e -> handleCompleteTask());
        uncompleteButton.addActionListener(e -> handleUncompleteTask());
        sortDeadlineButton.addActionListener(e -> handleSortDeadline());
        sortPriorityButton.addActionListener(e -> handleSortPriority());
        sortProgressButton.addActionListener(e -> handleSortProgress());
        logoutButton.addActionListener(e -> handleLogout());

        viewTimetableButton.addActionListener(e -> showTimetable());
        sharedScheduleButton.addActionListener(e -> showSharedSchedule());
    }

    private void showTimetable() {
        app.getCardLayout().show(app.getMainPanel(), "Timetable");
    }

    private void showSharedSchedule() {
        app.getCardLayout().show(app.getMainPanel(), "SharedSchedule");
    }

    private int getTaskIdByRow(int row) {
        return Integer.parseInt(taskTableModel.getValueAt(row, 0).toString());
    }

    private String fetchDescriptionFromDatabase(int taskId) {
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
            JOptionPane.showMessageDialog(this, "본문 내용을 불러오는 중 오류가 발생했습니다.");
        }
        return description;
    }

    private void showDescriptionDialog(String description) {
        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "본문 내용",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void loadTasksFromDatabase() {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT * FROM doyeondb.Tasks WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, app.getLoggedInUserId());

            ResultSet resultSet = stmt.executeQuery();
            taskTableModel.setRowCount(0);
            while (resultSet.next()) {
                int id = resultSet.getInt("task_id");
                String title = resultSet.getString("title");
                String progress = resultSet.getString("status");
                String deadline = resultSet.getString("end_time");
                String priority = resultSet.getString("priority");

                taskTableModel.addRow(new Object[] { id, title, progress, deadline, priority });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "할 일을 로드하는 중 오류가 발생했습니다.");
        }
    }

    private void handleSortDeadline() {
        List<RowData> rows = getRows();
        rows = taskSorter.sortByDeadline(rows);
        updateTable(rows);
    }

    private void handleSortPriority() {
        List<RowData> rows = getRows();
        rows = taskSorter.sortByPriority(rows);
        updateTable(rows);
    }

    private void handleSortProgress() {
        List<RowData> rows = getRows();
        rows = taskSorter.sortByProgress(rows);
        updateTable(rows);
    }

    private List<RowData> getRows() {
        List<RowData> rows = new ArrayList<>();
        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            String id = taskTableModel.getValueAt(i, 0).toString();
            String title = taskTableModel.getValueAt(i, 1).toString();
            String progress = taskTableModel.getValueAt(i, 2).toString();
            String deadline = taskTableModel.getValueAt(i, 3).toString();
            String priority = taskTableModel.getValueAt(i, 4).toString();

            rows.add(new RowData(id, title, progress, deadline, priority));
        }
        return rows;
    }

    private void updateTable(List<RowData> rows) {
        taskTableModel.setRowCount(0);
        for (RowData row : rows) {
            taskTableModel.addRow(new Object[] { row.getId(), row.getTitle(), row.getProgress(), row.getDeadline(),
                    row.getPriority() });
        }
    }

    public static class RowData {
        private String id;
        private String title;
        private String progress;
        private String deadline;
        private String priority;

        public RowData(String id, String title, String progress, String deadline, String priority) {
            this.id = id;
            this.title = title;
            this.progress = progress;
            this.deadline = deadline;
            this.priority = priority;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getProgress() {
            return progress;
        }

        public String getDeadline() {
            return deadline;
        }

        public String getPriority() {
            return priority;
        }
    }

    // 할 일 추가
    private void handleAddTask() {
        JTextField titleField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField timeField = new JTextField();
        JTextArea descriptionArea = new JTextArea(5, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        JComboBox<String> priorityComboBox = new JComboBox<>(new String[] { "High", "Medium", "Low" });

        Object[] message = {
                "제목:", titleField,
                "마감일 (yyyy-MM-dd):", dateField,
                "마감시간 (HH:mm):", timeField,
                "우선순위:", priorityComboBox,
                "본문:", descriptionScroll
        };

        int option = JOptionPane.showConfirmDialog(this, message, "할 일 추가", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();
            String description = descriptionArea.getText().trim();
            String deadline = date + " " + time;

            if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 필드를 입력하세요.");
                return;
            }

            try (Connection connection = DatabaseUtils.getConnection()) {
                String query = "INSERT INTO doyeondb.Tasks (user_id, title, end_time, priority, status, description) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, app.getLoggedInUserId());
                stmt.setString(2, title);
                stmt.setString(3, deadline);
                stmt.setString(4, (String) priorityComboBox.getSelectedItem());
                stmt.setString(5, "In Progress");
                stmt.setString(6, description);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "할 일이 추가되었습니다.");
                loadTasksFromDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "할 일을 추가하는 중 오류가 발생했습니다.");
            }
        }
    }

    // 할 일 수정
    private void handleEditTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수정할 할 일을 선택하세요.");
            return;
        }

        int taskId = getTaskIdByRow(selectedRow);
        String currentTitle = taskTableModel.getValueAt(selectedRow, 1).toString();
        String currentDeadline = taskTableModel.getValueAt(selectedRow, 3).toString();
        String currentPriority = taskTableModel.getValueAt(selectedRow, 4).toString();
        String currentDescription = fetchDescriptionFromDatabase(taskId);

        JTextField titleField = new JTextField(currentTitle);
        JTextField deadlineField = new JTextField(currentDeadline);
        JComboBox<String> priorityComboBox = new JComboBox<>(new String[] { "High", "Medium", "Low" });
        priorityComboBox.setSelectedItem(currentPriority);
        JTextArea descriptionArea = new JTextArea(currentDescription, 5, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        Object[] message = {
                "제목:", titleField,
                "마감일 (yyyy-MM-dd HH:mm):", deadlineField,
                "우선순위:", priorityComboBox,
                "본문:", descriptionScroll
        };

        int option = JOptionPane.showConfirmDialog(this, message, "할 일 수정", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String deadline = deadlineField.getText().trim();
            String priority = (String) priorityComboBox.getSelectedItem();
            String description = descriptionArea.getText().trim();

            if (title.isEmpty() || deadline.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 필드를 입력하세요.");
                return;
            }

            try (Connection connection = DatabaseUtils.getConnection()) {
                String query = "UPDATE doyeondb.Tasks SET title = ?, end_time = ?, priority = ?, description = ? WHERE task_id = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, title);
                stmt.setString(2, deadline);
                stmt.setString(3, priority);
                stmt.setString(4, description);
                stmt.setInt(5, taskId);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "할 일이 수정되었습니다.");
                loadTasksFromDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "할 일을 수정하는 중 오류가 발생했습니다.");
            }
        }
    }

    // 할 일 삭제
    private void handleDeleteTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 할 일을 선택하세요.");
            return;
        }

        int taskId = getTaskIdByRow(selectedRow);
        int option = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try (Connection connection = DatabaseUtils.getConnection()) {
                String query = "DELETE FROM doyeondb.Tasks WHERE task_id = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, taskId);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "할 일이 삭제되었습니다.");
                loadTasksFromDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "할 일을 삭제하는 중 오류가 발생했습니다.");
            }
        }
    }

    // 완료 처리
    private void handleCompleteTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "완료 처리할 할 일을 선택하세요.");
            return;
        }

        int taskId = getTaskIdByRow(selectedRow);
        updateTaskStatus(taskId, "Completed");
    }

    // 미완료 처리
    private void handleUncompleteTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "미완료 처리할 할 일을 선택하세요.");
            return;
        }

        int taskId = getTaskIdByRow(selectedRow);
        updateTaskStatus(taskId, "In Progress");
    }

    // 상태 업데이트 공통 메서드
    private void updateTaskStatus(int taskId, String status) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "UPDATE doyeondb.Tasks SET status = ? WHERE task_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, status);
            stmt.setInt(2, taskId);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "상태가 변경되었습니다.");
            loadTasksFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "상태 변경 중 오류가 발생했습니다.");
        }
    }

    private void handleLogout() {
        app.setLoggedInUserId(-1);
        taskTableModel.setRowCount(0);
        app.getCardLayout().show(app.getMainPanel(), "Login");
    }
}
