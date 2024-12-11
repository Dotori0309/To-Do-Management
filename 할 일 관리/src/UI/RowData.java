package UI;

public class RowData {
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
