/**
 * SlideList class
 *
 * generateSlideList() method generates a circular list of slides
 * .insert() method adds a new node to the list
 * .next() method moves the cursor forward
 * .currentSlide() method returns the current slide data
 * .size() method returns the size of the list
 * .cycleDuration() method returns the duration of the list
 * .copy() method returns a deep copy of the list
 */

import java.util.Random;

public class SlideList {

    // list size
    private int size = 0;
    // tail and current node
    private Node tail = null;
    private Node current = null;
    public static int slideDuration;
    public static int slideDurationTotal = 0;

    public static SlideList generateSlideList(int size) throws InterruptedException {

        SlideList list = new SlideList();
        Random random = new Random();

        // Creating slides, adding 20 to the circular list.
        Slide slide;
        // Storing slide names and durations
        int slideName = 1;
        String name = "Slide";

        System.out.println("Generating lists of slides");
        Thread.sleep(1000);
        while (list.size() < size) {
            System.out.println("\nAdding slide " + slideName);
            slideDuration = random.nextInt(10, 20);
            slide = new Slide(name, slideName, slideDuration);
            slideName++;
            list.insert(slide);
            System.out.println(slide);
            slideDurationTotal += slideDuration;
        }
        System.out.println("\nTotal list duration: " + slideDurationTotal);
        System.out.println("Total Slides: " + list.size() + "\n");

        return list;
    }

    public int getSlideListDuration(){
        return slideDurationTotal;
    }

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

    public Slide currentSlide(){
        return current.data;
    }

    // Method to return the size of the list
    public int size() {
        return size;
    }

    private Node headNode(){
        return (tail == null) ? null : tail.next;
    }

    // Compute cycle duration by walking the linked list once (no arrays)
    public int cycleDuration() {
        if (tail == null) return 0;
        int total = 0;
        Node n = headNode();
        for (int i = 0; i < size; i++) {
            total += n.data.duration;
            n = n.next;
        }
        return total;
    }

    // Deep-copy the circular list so callers can move an independent cursor
    public SlideList copy() {
        SlideList copy = new SlideList();
        if (tail == null) return copy;

        Node n = headNode();
        for (int i = 0; i < size; i++) {
            Slide s = n.data;
            copy.insert(new Slide(s.name, s.ID, s.duration));
            n = n.next;
        }
        return copy;
    }

    public String toString() {
        return "List size: " + size + "List contents: " + tail.data;
    }
}




