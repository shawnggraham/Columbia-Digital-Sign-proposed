import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        //new Generator("data.JSON");

        StudentList studentList = new StudentList();

        studentList.insert(new Student("Ben", 1, 0, 0));
        studentList.insert(new Student("Bob", 2, 0, 0));
        studentList.insert(new Student("Rob", 3, 0, 0));
        studentList.insert(new Student("Tim", 4, 0, 0));

        System.out.println(studentList.size());

        System.out.println(studentList.next());
        System.out.println(studentList.next());
        System.out.println(studentList.next());
        System.out.println(studentList.next());
        System.out.println(studentList.next());

    }
}