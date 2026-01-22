import java.util.Random;

public class CircularList {

    public static void main(String[] args) throws InterruptedException {

        // Creating a new circular list object.
        CircularList list = new CircularList();

        Random random = new Random();

        Queue queue = new Queue(random.nextInt(100));

        do {
            queue = new Queue(random.nextInt(4));
            list.insert(queue);

        } while (list.size() < 20);

        // Printing the size of the list.
        System.out.println("List size: " + list.size());

        // Looping through the list to print the items.

        I = 1;
        Queue student = list.next();

        do {
            System.out.println(student);
            System.out.println("Day #" + I);
            do {
                Thread.sleep(100);
                student.secondsWatched = random.nextInt(20);
                System.out.println("Slide #" + S + " watched by " + student);
                S++;

                if (student.secondsWatched > 0) {
                    student.slideWatched = true;
                    student.SLIDES_WATCHED_TOTAL++;
                }

                if (S == 21) {
                    S = 1;
                    I++;
                    if (I == 5) {
                        student.slideWatched = false;
                        I = 1;
                        break;
                    }
                    System.out.println("Day #" + I);
                }
            } while (I <= 4);
            student = list.next();
        }while (I < 20);
    }

    // slide counter
    public static int S = 1;
    // iteration counter
    public static int I = 1;
    // list size
    private int size = 0;
    // tail and current node
    private Node tail = null;
    private Node current = null;

    // Node class
    private static class Node {
        // Data and next node
        Queue data;
        Node next;

        // Node constructor
        public Node(Queue data) {
            this.data = data;
        }
    }

    // Method to add a new node
    public void insert(Queue data) {
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
    public Queue next() {
        // Checking if the list is empty, throws an IllegalStateException if it is
        if (current == null) throw new IllegalStateException("List is empty");
        // Moving the current node forward
        Queue data = current.data;
        current = current.next;
        return data;
    }

    // Method to return the size of the list
    public int size() {
        return size;
    }
}




