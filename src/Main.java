import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        //new Generator("data.JSON");

        //StudentList studentList = StudentList.generateStudentList();



        // methods to generate a slide list and student list,
        // use the size parameter to change the size of the lists
        SlideList slideList = SlideList.generateSlideList(20);
        StudentList studentList = StudentList.generateStudentList( 10);
    }
}