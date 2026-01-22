import java.util.Random;

public class CircularList2 {

    public static void main(String[] args) throws InterruptedException {

        Random random = new Random();

        // Creating a new circular list object.
        CircularList2 list = new CircularList2();
        CircularList2 studentList = new CircularList2();


        // Creating slides, adding 20 to the circular list.
        Slide slide;
        // Storing slide names and durations
        int slideName = 1;
        int slideDuration;
        int slideDurationTotal = 0;

        System.out.println("Generating lists of slides");
        Thread.sleep(1000);
        while (list.size() < 20){
            System.out.println("\nAdding slide " + slideName);
            slideDuration = random.nextInt(10,20);
            slide = new Slide(slideName, slideDuration);
            slideName++;
            list.insert(slide);
            System.out.println(slide);
            slideDurationTotal += slideDuration;
        }
        System.out.println("\nTotal list duration: "+slideDurationTotal);
        System.out.println("\nList size: " + list.size());

        for (int i = 0; i < 40; i++) {
            System.out.println(list.next());
        }
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
        Slide data;
        Node next;

        // Node constructor
        public Node(Slide data) {
            this.data = data;
        }
    }

    // Method to add a new node
    public void insert(Slide data) {
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
    public Slide next() {
        // Checking if the list is empty, throws an IllegalStateException if it is
        if (current == null) throw new IllegalStateException("List is empty");
        // Moving the current node forward
        Slide data = current.data;
        current = current.next;
        return data;
    }

    // Method to return the size of the list
    public int size() {
        return size;
    }

    public String toString(){
        return "List size: " + size + "List contents: " + tail.data;
    }
}




