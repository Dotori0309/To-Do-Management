package Utils;

import UI.TaskPanel.RowData;

import java.util.List;

public class TaskSorter {
    private boolean isDeadlineAscending = true;
    private boolean isPriorityAscending = true;
    private boolean isProgressAscending = true;

    // 우선순위 값을 숫자 값으로 매핑: High -> 3, Medium -> 2, Low -> 1
    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High":
                return 3;
            case "Medium":
                return 2;
            case "Low":
                return 1;
            default:
                return 0;
        }
    }

    public List<RowData> sortByDeadline(List<RowData> rows) {
        rows.sort((row1, row2) -> {
            try {
                java.util.Date date1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").parse(row1.getDeadline());
                java.util.Date date2 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").parse(row2.getDeadline());
                return isDeadlineAscending ? date1.compareTo(date2) : date2.compareTo(date1);
            } catch (Exception e) {
                return 0;
            }
        });
        isDeadlineAscending = !isDeadlineAscending; // 정렬 순서 반전
        return rows;
    }

    public List<RowData> sortByPriority(List<RowData> rows) {
        rows.sort((row1, row2) -> {
            int priority1 = getPriorityValue(row1.getPriority());
            int priority2 = getPriorityValue(row2.getPriority());
            return isPriorityAscending ? Integer.compare(priority1, priority2) : Integer.compare(priority2, priority1);
        });
        isPriorityAscending = !isPriorityAscending; // 정렬 순서 반전
        return rows;
    }

    public List<RowData> sortByProgress(List<RowData> rows) {
        rows.sort((row1, row2) -> isProgressAscending ? row1.getProgress().compareTo(row2.getProgress())
                : row2.getProgress().compareTo(row1.getProgress()));
        isProgressAscending = !isProgressAscending; // 정렬 순서 반전
        return rows;
    }
}
