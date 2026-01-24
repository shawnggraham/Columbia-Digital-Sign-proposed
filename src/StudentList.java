import java.util.Random;

public class StudentList {

    // list size
    private int size = 0;
    // tail and current node
    private Node tail = null;
    private Node current = null;

    public static StudentList generateStudentList() throws InterruptedException {
        StudentList list = new StudentList();
        Random random = new Random();
        // Creating a new student list object.
        // Creating students, adding 20 to the circular list.
        // Storing slide names and durations
        int studentID = 1;
        int watchDuration = random.nextInt(20);
        boolean slideWatched = false;

        System.out.println("Generating lists of students");
        Thread.sleep(1000);

        while (list.size() < 5){
            System.out.println("\nAdding student ID: " + studentID);
            Student student = new Student(studentID, watchDuration,slideWatched);
            studentID++;
            list.insert(student);
            System.out.println(student);
        }
        System.out.println("\nList size: " + list.size());
        return list;
    }

    // Node class
    private static class Node {
        // Data and next node
        Student data;
        Node next;

        // Node constructor
        public Node(Student data) {
            this.data = data;
        }
    }

    // Method to add a new node
    public void insert(Student data) {
        Node newNode = new Node(data);

        if (tail == null) {
            // Set the tail and current node to the new node
            tail = newNode;
            newNode.next = tail;
            current = newNode;
        } else {
            // Set the next node of the tail to the new node
            newNode.next = tail.next;
            tail.next = newNode;
            tail = newNode;
        }
        // Increasing the size of the list
        size++;
    }

    // Method to move the current node forward
    public Student next() {
        // Checking if the list is empty, throws an IllegalStateException if it is
        if (current == null) throw new IllegalStateException("List is empty");
        // Moving the current node forward
        Student data = current.data;
        current = current.next;
        return data;
    }

    // Method to return the size of the list
    public int size() {
        return size;
    }
}




