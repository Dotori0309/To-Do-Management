package UI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;
import Utils.DatabaseUtils;
import Utils.ImgBBImageUploadAPI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.net.URL;
import java.sql.*;

public class TimetablePanel extends JPanel {
    private ScheduleApp app;
    private JTextField userIdField; // 다른 사용자 검색
    private JLabel imageLabel; // 시간표 표시용
    private ImgBBImageUploadAPI imgBBAPI; // 이미지 업로드 API
    private static final long serialVersionUID = 1L;
    // public ImageIcon i1;

    public TimetablePanel(ScheduleApp app) {
        this.app = app;
        setLayout(new BorderLayout());

        // ImgBB API Key 설정
        imgBBAPI = new ImgBBImageUploadAPI(""); // key

        // 제목
        JLabel titleLabel = new JLabel("시간표 관리");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 사용자 ID 입력 및 버튼
        JPanel inputPanel = new JPanel();
        userIdField = new JTextField(15);
        JButton searchButton = new JButton("사용자 시간표 검색");
        JButton uploadButton = new JButton("시간표 업로드");
        JButton backButton = new JButton("뒤로가기");
        inputPanel.add(new JLabel("사용자 ID:"));
        inputPanel.add(userIdField);
        inputPanel.add(searchButton);
        inputPanel.add(uploadButton);
        inputPanel.add(backButton);
        add(inputPanel, BorderLayout.SOUTH);

        // 이미지 표시용
        imageLabel = new JLabel("시간표 이미지 없음", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(900, 900)); // 이미지 크기 설정
        add(imageLabel, BorderLayout.CENTER);

        // 이벤트 처리
        searchButton.addActionListener(e -> searchOtherUserTimetable());
        uploadButton.addActionListener(e -> uploadTimetableImage());
        backButton.addActionListener(e -> app.getCardLayout().show(app.getMainPanel(), "Tasks"));
    }

    // 시간표 업로드
    private void uploadTimetableImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("시간표 이미지 선택");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                // ImgBB에 업로드
                String imageUrl = imgBBAPI.uploadImage(file).get("image");
                saveTimetableImage(imageUrl); // DB에 저장
                displayImage(imageUrl); // 이미지 표시
                JOptionPane.showMessageDialog(this, "시간표 업로드 성공!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "시간표 업로드 실패!");
            }
        }
    }

    // 시간표 저장
    private void saveTimetableImage(String imageUrl) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "UPDATE doyeondb.Users SET timetable_image = ? WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, imageUrl);
            stmt.setInt(2, app.getLoggedInUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "시간표를 저장하는 중 오류가 발생했습니다.");
        }
    }

    // 다른 사용자의 시간표 검색
    private void searchOtherUserTimetable() {
        String inputId = userIdField.getText().trim(); // 사용자 입력값
        if (inputId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "사용자 ID를 입력해주세요.");
            return;
        }

        try {
            String imageUrl = fetchTimetableImagePath(inputId); // 입력된 ID로 이미지 경로 가져오기

            if (imageUrl != null) {
                displayImage(imageUrl); // 이미지 표시
            } else {
                JOptionPane.showMessageDialog(this, "해당 사용자의 시간표를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "시간표 검색 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    public void loadUserTimetable() {
        try {
            // 로그인된 사용자 ID 기반 시간표 이미지 경로 가져오기
            String imageUrl = fetchTimetableImagePathByUserId(app.getLoggedInUserId());
            if (imageUrl != null) {
                displayImage(imageUrl); // 이미지 표시
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText("시간표 이미지 없음");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "시간표를 로드하는 중 오류가 발생했습니다.");
        }
    }

    private String fetchTimetableImagePathByUserId(int userId) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            String query = "SELECT timetable_image FROM doyeondb.Users WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("timetable_image");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 시간표 이미지 경로 가져오기
    private String fetchTimetableImagePath(String inputId) {
        try (Connection connection = DatabaseUtils.getConnection()) {
            // ID 값으로 검색하도록 쿼리 수정
            String query = "SELECT timetable_image FROM doyeondb.Users WHERE ID = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, inputId); // 입력된 ID 값을 바인딩
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("timetable_image"); // 시간표 이미지 경로 반환
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 시간표 이미지가 없거나 오류가 발생하면 null 반환
    }

    // 이미지 표시
    private void displayImage(String imageUrl) {
        try {
            ImageIcon icon = new ImageIcon(new URL(imageUrl));
            Image image = icon.getImage(); // ImageIcon에서 Image 가져오기

            // 원하는 고정 크기 설정
            int scaledWidth = 600; // 원하는 너비
            int scaledHeight = 700; // 원하는 높이

            // 이미지를 조정된 크기로 스케일링
            Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

            // 조정된 이미지를 ImageIcon으로 설정
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            imageLabel.setIcon(scaledIcon);
            imageLabel.setText(null); // 텍스트 제거

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "이미지를 표시하는 중 오류가 발생했습니다.");
        }
    }
}
