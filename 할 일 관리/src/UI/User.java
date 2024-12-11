package UI;
import java.sql.*;
import javax.swing.JOptionPane;
import Utils.DatabaseUtils;

public class User {
    private int userId;
    private String username;
    private String password;

    // 기본 생성자
    public User() {}

    // 생성자
    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    // 데이터베이스 연결을 위한 유틸리티 메서드
    private static Connection getConnection() throws SQLException {
        return DatabaseUtils.getConnection();
    }

    // 로그인 처리
    public static boolean login(int userId, String password) {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM doyeondb.Users WHERE user_id = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);  // 숫자형 user_id 설정
            stmt.setString(2, password);
            ResultSet resultSet = stmt.executeQuery();

            return resultSet.next();  // 로그인 성공 여부 반환
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // 로그인 실패
    }

    // 회원가입 처리
    public static boolean register(String username, int userId, String password) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "INSERT INTO doyeondb.Users (username, user_id, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setInt(2, userId);  // 숫자형 user_id 설정
            stmt.setString(3, password);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "회원가입 성공!");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "회원가입 중 오류가 발생했습니다.");
        }
        return false;
    }

    // 사용자 정보 조회
    public static User getUserById(int userId) {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM doyeondb.Users WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                return new User(
                    resultSet.getInt("user_id"),
                    resultSet.getString("username"),
                    resultSet.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // 사용자 정보가 없으면 null 반환
    }

    // Getter 및 Setter
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
