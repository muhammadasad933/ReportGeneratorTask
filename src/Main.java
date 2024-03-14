import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static void main(String[] args) {

      String url = "jdbc:sqlserver://localhost\\asad:1433;databaseName=coursera;user=asad;password=7661;Integrated Security=True;TrustServerCertificate=True";
        Scanner scanner = new Scanner(System.in);
      try{
          System.out.println("Enter comma separated list of personal identifiers (PIN) of the students to be included in the report (leave empty for all students):");
          List<String> pins=parseStudentPins(scanner.nextLine());
          System.out.println("Enter the required minimum credit:");
          int minCredit=Integer.parseInt(scanner.nextLine());
          System.out.println("Enter the start date of the time period (YYYY-MM-DD):");
          String startDate=scanner.nextLine();
          System.out.println("Enter the end date of the time period (YYYY-MM-DD):");
          String  endDate=scanner.nextLine();
          System.out.println("Enter the output format (csv or html):");
          String outputFormat=scanner.nextLine();
          System.out.println("Enter the path to the directory where the reports will be saved:");
          String outputDirectory=scanner.nextLine();
          if(args.length>5){
              String[] pinArray=args[5].split(",");
              pins.addAll(Arrays.asList(pinArray));
          }
          Connection connection=DriverManager.getConnection(url);
         List<OutputView> outputEntries=takeOutput(connection, pins, minCredit, startDate, endDate);
         generateReport(outputEntries,outputFormat,outputDirectory);
         connection.close();
      }
      catch (Exception e){
          e.printStackTrace();
      }
    }
    private static List<String> parseStudentPins(String input){
        List<String>pins=new ArrayList<>();
        if(!input.isEmpty()){
            String[] pinArray=input.split(",");
            for(String pin:pinArray){
                pins.add(pin.trim());
            }
        }
        return pins;
    }
    public static List<OutputView> takeOutput(Connection connection, List<String> pins, int minCredit, String startDate, String endDate) throws SQLException{
        List<OutputView> outputEntries=new ArrayList<>();
        String condition=pins.isEmpty() ? " ": "AND PIN IN ("+String.join(",",pins)+")";
        String sqlQuery="SELECT students.first_name AS first_name, students.last_name AS last_name, " +
                "course_sum.totalCredit AS totalCredit, " +
                "courses.name AS courseName, " +
                "SUM(courses.total_time) AS totalTime, " +
                "instructors.first_name AS inst_first_name, " +
                "instructors.last_name AS inst_last_name, " +
                "courses.credit AS credit " +
                "FROM students " +
                "INNER JOIN students_courses_xref ON students.pin = students_courses_xref.student_pin " +
                "INNER JOIN courses ON students_courses_xref.course_id = courses.id " +
                "INNER JOIN instructors ON courses.instructor_id=instructors.id " +
                "INNER JOIN (SELECT students.pin AS student_pin, SUM(courses.credit) AS totalCredit " +
                "            FROM students " +
                "            INNER JOIN students_courses_xref ON students.pin = students_courses_xref.student_pin " +
                "            INNER JOIN courses ON students_courses_xref.course_id = courses.id " +
                "            WHERE students_courses_xref.completion_date >= ? AND students_courses_xref.completion_date <= ? " +
                "            GROUP BY students.pin) AS course_sum " +
                "ON students.pin = course_sum.student_pin " +
                "WHERE students_courses_xref.completion_date >= ? AND students_courses_xref.completion_date <= ? " +
                condition +
                "GROUP BY students.first_name, students.last_name, courses.name, " +
                "instructors.first_name, instructors.last_name, courses.credit, course_sum.totalCredit";
        try(PreparedStatement statement=connection.prepareStatement(sqlQuery)){
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date sdate=dateFormat.parse(startDate);
            java.util.Date edate=dateFormat.parse(endDate);
            statement.setDate(1, new Date(sdate.getTime()));
            statement.setDate(2, new Date(edate.getTime()));
            statement.setDate(3, new Date(sdate.getTime()));
            statement.setDate(4, new Date(edate.getTime()));
            ResultSet resultSet=statement.executeQuery();
            while (resultSet.next()){
                OutputView output=new OutputView(
                resultSet.getString("first_name")+ " " + resultSet.getString("last_name"),
                resultSet.getInt("totalCredit"),
                resultSet.getString("courseName"),
                resultSet.getInt("totalTime"),
                resultSet.getInt("credit"),
                resultSet.getString("inst_first_name") + " " + resultSet.getString("inst_last_name"));
                outputEntries.add(output);
            }
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return outputEntries;
}
       private static void generateReport(List<OutputView> outputEntries,String outputFormat, String outputDirectory) throws IOException{

        if((outputFormat==null) || (outputFormat.equalsIgnoreCase("html"))){
            generateHtmlReport(outputEntries,outputDirectory);
        }
        if((outputFormat==null) || (outputFormat.equalsIgnoreCase("csv"))){
            generateCsvReport(outputEntries,outputDirectory);
        }
        if(outputFormat==""){
            generateHtmlReport(outputEntries,outputDirectory);
            generateCsvReport(outputEntries,outputDirectory);
        }
       }
       private static void generateHtmlReport(List<OutputView> outputEntries, String outputDirectory) throws IOException{
        try (PrintWriter writer=new PrintWriter(new File(outputDirectory,"report.html"))){
         writer.println("<html><head><title>Report</title></head><body><h1>Report</h1>");
         writer.println("<table border='1'><tr><th>Student Name</th><th>Total Credit</th><th></th><th></th><th></th></tr><tr><th></th><th>Course Name</th><th>Total Time</th><th>Credit</th><th>Instructor Name</th></tr>");
         String checkStudentName=" ";
         int checkTotalCredit=-1;
         for( OutputView output: outputEntries){
             String currentStudentName=output.getStudentName();
             int currentTotalCredit=output.getTotalCredit();
             if((!currentStudentName.equals(checkStudentName)|| currentTotalCredit != checkTotalCredit)){
                 writer.println("<tr>");
                 writer.println("<td >" + output.getStudentName() + "</td><td >" + output.getTotalCredit() + "</td>");
             }
             else{
                 writer.println("<tr><td></td><td></td>");
             }
             writer.println("<tr><td></td><td>" + output.getCourseName() + "</td><td>" + output.getTotalTime() + "</td><td>" + output.getCredit() + "</td><td>" + output.getInstructorName() + "</td></tr><td>");
             checkStudentName=currentStudentName;
             checkTotalCredit=currentTotalCredit;
         }
        writer.println("</table></body></html>");
        }
       }
       private static void generateCsvReport(List<OutputView> outputEntries, String outputDirectory) throws IOException{
        try (PrintWriter writer=new PrintWriter(new File(outputDirectory,"report.csv"))){
            writer.println("Student Name,Total Credit,,,,");
            writer.println(",Course Name,Total Time,Credit,Instructor Name");
            String checkStudentName=" ";
            int checkTotalCredit=-1;
            for (OutputView output: outputEntries){
                String currentStudentName=output.getStudentName();
                int currentTotalCredit=output.getTotalCredit();
                if((!currentStudentName.equals(checkStudentName))||(currentTotalCredit!=checkTotalCredit)) {
                    writer.println(output.getStudentName() + "," + output.getTotalCredit() + ",,,,");
                }
                else {
                    writer.print(" ");
                }
                writer.println(","+output.getCourseName()+ "," + output.getTotalTime()+ "," + output.getCredit()+ "," + output.getInstructorName());
            checkStudentName=currentStudentName;
            checkTotalCredit=currentTotalCredit;
            }
        }
       }
}
