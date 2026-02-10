import java.util.Random;

public class Test {
    public static void main(String[] args) {

        // Create a new StudentList
        StudentList list = new StudentList();

        // Create local variables for Student data
        int ID = 1;
        int arrivalTime = 8;
        int slidesWatchedTotal = 0;

        // Generate a list of student objects with random names using the getRandomName() method.
        while (list.size() < 8) {
            list.insert(new Student(StudentList.getRandomName(), ID, slidesWatchedTotal, arrivalTime));
            ID++;
            arrivalTime++;
        }

        // Print the list of students
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.next());
        }
    }
}
