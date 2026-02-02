import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        main
        //new Generator("data.JSON");

        new JSONGenerator("data.JSON");
        main

        StudentList studentList = new StudentList();

        studentList.insert(new Student("Ben", 1, 0, 0));
        studentList.insert(new Student("Bob", 2, 0, 0));
        studentList.insert(new Student("Rob", 3, 0, 0));
        studentList.insert(new Student("Tim", 4, 0, 0));

        System.out.println(studentList.size());

        main
        System.out.println(studentList.next());
        System.out.println(studentList.next());
        System.out.println(studentList.next());
        System.out.println(studentList.next());
        System.out.println(studentList.next());

//        int variable = 0;

        // methods to generate a slide list and student list,
        // use the size parameter to change the size of the lists
//        SlideList slideList = SlideList.generateSlideList(20);
//        StudentList studentList = StudentList.generateStudentList( 10);
         main
    }
}