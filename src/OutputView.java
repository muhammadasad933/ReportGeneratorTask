public class OutputView {
    private String studentName;
    private int totalCredit;
    private String courseName;
    private int totalTime;
    private int credit;
    private String instructorName;
    public OutputView(String studentName, int totalCredit, String courseName, int totalTime, int credit, String instructorName) {
        this.studentName = studentName;
        this.totalCredit = totalCredit;
        this.courseName = courseName;
        this.totalTime = totalTime;
        this.credit =  credit;
        this.instructorName = instructorName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    public void setTotalCredit(int totalCredit) {
        this.totalCredit = totalCredit;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
    public void setCredit(int credit) {
        this.credit = credit;
    }
    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }
    public String getStudentName() {
        return studentName;
    }
    public int getTotalCredit() {
        return totalCredit;
    }
    public String getCourseName() {
        return courseName;
    }
    public int getTotalTime() {
        return totalTime;
    }
    public int getCredit() {
        return credit;
    }
    public String getInstructorName() {
        return instructorName;
    }
}
