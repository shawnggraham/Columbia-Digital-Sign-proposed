public class StudentSample {

    /*
    This is just a sample class and method to illustrate how we can pull the
     variables sent from the addButton click event on the form and man handle them
     this just outputs to the console we can delete this file later.
    */


    public static void handleStudent(int studentId, String studentName, int tripsPerWeek) {
        System.out.println("=== StudentSample.handleStudent() ===");
        System.out.println("studentId      = " + studentId);
        System.out.println("studentName    = " + studentName);
        System.out.println("tripsPerWeek   = " + tripsPerWeek);
        System.out.println("=====================================");
    }
}