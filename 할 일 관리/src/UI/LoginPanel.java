package UI;

import javax.swing.*;
import java.awt.*;
import Utils.DatabaseUtils;
import java.sql.*;

public class LoginPanel extends JPanel {
    private ScheduleApp app;  // ScheduleApp 객체를 저장
    private JTextField userIdField;  // 사용자 아이디 입력 필드
    private JPasswordField passwordField;  // 비밀번호 입력 필드
    private JButton loginButton;  // 로그인 버튼
    private JButton registerButton;  // 회원가입 버튼
    private static final long serialVersionUID = 1L;

    public LoginPanel(ScheduleApp app) {
        this.app = app;  // ScheduleApp 객체 참조
        setLayout(null);  // 레이아웃 설정 (자유로운 위치 설정)

        // 로그인 UI 구성
        JLabel titleLabel = new JLabel("로그인");
        titleLabel.setBounds(500, 50, 200, 50);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel);

        // 아이디 입력 레이블 및 필드
        JLabel idLabel = new JLabel("아이디:");
        idLabel.setBounds(400, 150, 100, 30);
        add(idLabel);

        userIdField = new JTextField();
        userIdField.setBounds(500, 150, 200, 30);
        add(userIdField);

        // 비밀번호 입력 레이블 및 필드
        JLabel passLabel = new JLabel("비밀번호:");
        passLabel.setBounds(400, 200, 100, 30);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(500, 200, 200, 30);
        add(passwordField);

        // 로그인 버튼
        loginButton = new JButton("로그인");
        loginButton.setBounds(500, 250, 200, 40);
        add(loginButton);

        // 회원가입 버튼
        registerButton = new JButton("회원가입");
        registerButton.setBounds(500, 300, 200, 40);
        add(registerButton);

        // 로그인 버튼 클릭 시 처리
        loginButton.addActionListener(e -> {
            try {
                String inputId = userIdField.getText().trim();  // 사용자가 입력한 ID
                String password = new String(passwordField.getPassword()).trim(); // 사용자가 입력한 비밀번호

                if (inputId.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.");
                    return;
                }

                // 데이터베이스에서 로그인 인증
                int userId = login(inputId, password);
                if (userId != -1) {
                    // 로그인 성공: ScheduleApp에 로그인된 사용자 ID 저장
                    app.setLoggedInUserId(userId);

                    // TaskPanel의 loadTasksFromDatabase 호출
                    TaskPanel taskPanel = app.getTaskPanel();
                    taskPanel.loadTasksFromDatabase(); // 할 일 목록 로드

                    // TimetablePanel의 loadUserTimetable 호출
                    TimetablePanel timetablePanel = app.getTimetablePanel();
                    timetablePanel.loadUserTimetable(); // 시간표 로드

                    // 화면 전환
                    app.getCardLayout().show(app.getMainPanel(), "Tasks");
                } else {
                    // 로그인 실패
                    JOptionPane.showMessageDialog(this, "로그인 실패. 아이디 또는 비밀번호를 확인해주세요.");
                }
            } catch (Exception ex) {
                // 예기치 않은 오류 처리
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "로그인 중 오류가 발생했습니다. 다시 시도해주세요.");
            }
        });

        // 회원가입 버튼 클릭 시 처리
        registerButton.addActionListener(e -> app.getCardLayout().show(app.getMainPanel(), "Register"));
    }

    // 로그인 처리 메서드
    private int login(String inputId, String password) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT user_id FROM doyeondb.Users WHERE ID = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, inputId);
            stmt.setString(2, password);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("user_id"); // 로그인 성공 시 user_id 반환
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 로그인 실패 시 -1 반환
    }
}
