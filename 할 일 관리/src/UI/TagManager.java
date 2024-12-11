package UI;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Utils.DatabaseUtils;

public class TagManager {
    private int userId; // 현재 로그인한 사용자 ID

    public TagManager(int userId) {
        this.userId = userId;
    }

    // 데이터베이스에서 모든 태그 가져오기
    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT name FROM doyeondb.GT WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                tags.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "태그를 로드하는 중 오류가 발생했습니다.");
        }
        return tags;
    }

    // 특정 Task에 연결된 태그 가져오기
    public List<String> getTagsForTask(int taskId) {
        List<String> tags = new ArrayList<>();
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT name FROM doyeondb.GT JOIN doyeondb.Tasks ON GT.GT_id = Tasks.GT_id WHERE Tasks.task_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, taskId);

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                tags.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "태그를 로드하는 중 오류가 발생했습니다.");
        }
        return tags;
    }

    // 특정 Task에 태그 저장
    public void saveTagsForTask(int taskId, List<String> tags) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            // 기존 태그 해제
            String deleteQuery = "UPDATE doyeondb.Tasks SET GT_id = NULL WHERE task_id = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, taskId);
            deleteStmt.executeUpdate();

            // 새로운 태그 연결
            for (String tag : tags) {
                String query = "UPDATE doyeondb.Tasks SET GT_id = (SELECT GT_id FROM GT WHERE name = ? AND user_id = ?) WHERE task_id = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, tag);
                stmt.setInt(2, userId);
                stmt.setInt(3, taskId);
                stmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "태그가 저장되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "태그를 저장하는 중 오류가 발생했습니다.");
        }
    }

    // 새로운 태그 추가
    public void addNewTag(String tagName) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "INSERT INTO doyeondb.GT (user_id, name) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, tagName);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "새 태그가 추가되었습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "새 태그를 추가하는 중 오류가 발생했습니다.");
        }
    }
}
