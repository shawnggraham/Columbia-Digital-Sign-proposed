public class CircularList {

    // main() runnable for testing

    public static void CircularList(String[] args) throws InterruptedException {

        // Creating a new circular list object.
        CircularList list = new CircularList();

        // Looping through the list to add 20 items.
        for (int i = 1; i < 21; i++) {
            list.insert(i);
        }

        // Printing the size of the list.
        System.out.println("List size: " + list.size());

        // Looping through the list to print the items.
        do {
            Thread.sleep(250);
            System.out.println("Slide #" + S + ": " + list.next());
            S++;
            if (S == 21) {
                S = 1;
                I++;
                System.out.println("Iteration #" + I);
            }
        } while (list.current.data != 21);
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
        int data;
        Node next;

        // Node constructor
        public Node(int data) {
            this.data = data;
        }
    }

    // Method to add a new node
    public void insert(int data) {
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
    public int next() {
        // Checking if the list is empty, throws an IllegalStateException if it is
        if (current == null) throw new IllegalStateException("List is empty");
        // Moving the current node forward
        int data = current.data;
        current = current.next;
        return data;
    }

    // Method to return the size of the list
    public int size() {
        return size;
    }
}




