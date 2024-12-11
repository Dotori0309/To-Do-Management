package UI;
import javax.swing.*;
import Utils.DatabaseUtils;
import java.awt.*;
import java.sql.*;

public class RegisterPanel extends JPanel {
    private ScheduleApp app;

    public RegisterPanel(ScheduleApp app) {
        this.app = app;
        setLayout(null);

        // 회원가입 UI 구성
        JLabel titleLabel = new JLabel("회원가입");
        titleLabel.setBounds(500, 50, 200, 50);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel);

        // 아이디 입력
        JLabel idLabel = new JLabel("아이디:");
        idLabel.setBounds(400, 150, 100, 30);
        add(idLabel);

        JTextField userField = new JTextField();
        userField.setBounds(500, 150, 200, 30);
        add(userField);

        // 비밀번호 입력
        JLabel passLabel = new JLabel("비밀번호:");
        passLabel.setBounds(400, 200, 100, 30);
        add(passLabel);

        JPasswordField passField = new JPasswordField();
        passField.setBounds(500, 200, 200, 30);
        add(passField);

        // 사용자 이름 입력
        JLabel usernameLabel = new JLabel("사용자 이름:");
        usernameLabel.setBounds(400, 250, 100, 30);
        add(usernameLabel);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(500, 250, 200, 30);
        add(usernameField);

        // 회원가입 버튼
        JButton registerButton = new JButton("회원가입");
        registerButton.setBounds(500, 300, 200, 40);
        add(registerButton);

        // 뒤로가기 버튼
        JButton backButton = new JButton("뒤로가기");
        backButton.setBounds(500, 350, 200, 40);
        add(backButton);

        // 회원가입 버튼 동작
        registerButton.addActionListener(e -> {
            String userId = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String username = usernameField.getText().trim();

            // 입력값 유효성 검사
            if (userId.isEmpty() || password.isEmpty() || username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디, 비밀번호, 사용자 이름을 모두 입력해주세요.");
                return;
            }

            // 회원가입 처리
            if (registerUser(userId, password, username)) {
                JOptionPane.showMessageDialog(this, "회원가입 성공! 로그인 화면으로 이동합니다.");
                app.getCardLayout().show(app.getMainPanel(), "Login");
            } else {
                JOptionPane.showMessageDialog(this, "회원가입 실패. 아이디가 이미 존재하거나 오류가 발생했습니다.");
            }
        });

        // 뒤로가기 버튼 동작
        backButton.addActionListener(e -> app.getCardLayout().show(app.getMainPanel(), "Login"));
    }

    // 데이터베이스에 사용자 등록
    private boolean registerUser(String userId, String password, String username) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "INSERT INTO doyeondb.Users (ID, password, username) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, userId);  // 입력한 사용자 아이디
            stmt.setString(2, password);  // 입력한 비밀번호
            stmt.setString(3, username);  // 입력한 사용자 이름
            int rowsInserted = stmt.executeUpdate();  // 데이터 삽입

            return rowsInserted > 0;  // 삽입 성공 시 true 반환
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // 오류 발생 시 false 반환
    }
}
