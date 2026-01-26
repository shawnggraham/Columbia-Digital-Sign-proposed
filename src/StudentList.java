/**
 * StudentList class
 *
 * generateStudentList() method generates a circular list of students
 * .insert() method adds a new node to the list
 * .next() method moves the cursor forward
 * .size() method returns the size of the list
 * .currentStudent() method returns the current student data
 */

import java.util.Random;

public class StudentList {

    // list size
    private int size = 0;
    // tail and current node
    private Node tail = null;
    private Node current = null;

    public static StudentList generateStudentList(int size) throws InterruptedException {
        StudentList list = new StudentList();
        Random random = new Random();

        int studentID = 1;
        boolean slideWatched = false;

        System.out.println("Generating lists of students");
        Thread.sleep(1000);

        while (list.size() < size){
            System.out.println("\nAdding student ID: " + studentID);
            Student student = new Student(studentID, 0, slideWatched);
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

    public Student currentStudent(){
        return current.data;
    }

    // Method to return the size of the list
    public int size() {
        return size;
    }
}




