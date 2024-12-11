package UI;

import javax.swing.*;
import java.awt.*;
import Utils.DatabaseUtils;
import java.sql.*;

public class ScheduleApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private int loggedInUserId = -1;
    private TaskPanel taskPanel;
    private TimetablePanel timetablePanel;

    public ScheduleApp() {
        setTitle("일정 관리 프로그램");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPanel(this), "Login");
        taskPanel = new TaskPanel(this);
        timetablePanel = new TimetablePanel(this);
        mainPanel.add(taskPanel, "Tasks");
        mainPanel.add(timetablePanel, "Timetable");
        mainPanel.add(new SharedSchedulePanel(this), "SharedSchedule");
        mainPanel.add(new RegisterPanel(this), "Register");

        add(mainPanel);
        switchScreen("Login");
    }

    public void switchScreen(String panelName) {
        cardLayout.show(mainPanel, panelName); // 화면 전환

        switch (panelName) {
            case "Login":
                setSize(1200, 500); // 로그인 화면 크기
                setLocationRelativeTo(null); // 창 중앙 정렬
                break;
            default:
                setSize(800, 800); // 기본 화면 크기
                setLocationRelativeTo(null); // 창 중앙 정렬
                break;
        }

        validate(); // 레이아웃 강제 갱신
    }


    public TaskPanel getTaskPanel() {
        return taskPanel; // TaskPanel 인스턴스를 반환
    }

    public TimetablePanel getTimetablePanel() {
        return timetablePanel; // TimetablePanel 인스턴스를 반환
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    public CardLayout getCardLayout() {
        return cardLayout;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ScheduleApp().setVisible(true));
        try (Connection connection = DatabaseUtils.getConnection()) {
            System.out.println("데이터베이스 연결 성공!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("데이터베이스 연결 실패!");
        }
    }
}
