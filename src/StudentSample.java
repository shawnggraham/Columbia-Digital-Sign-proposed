// FILE: StudentSample.java
// Replace the ENTIRE contents of your StudentSample.java with this.

import java.util.List;

public class StudentSample {

//    // Legacy (kept so nothing else breaks if it still calls this)
//    public static void handleStudent(int studentId, String studentName, int tripsPerWeek) {
//        System.out.println("=== StudentSample.handleStudent() (legacy) ===");
//        System.out.println("studentId      = " + studentId);
//        System.out.println("studentName    = " + studentName);
//        System.out.println("tripsPerWeek   = " + tripsPerWeek);
//        System.out.println("=============================================");
//    }

    // NEW: Called by ColumbiaSignUI "Write Student Information to JSON"
    public static void handleStudents(List<ColumbiaSignUI.StudentDef> students) {
        System.out.println("=== StudentSample.handleStudents() ===");

        if (students == null || students.isEmpty()) {
            System.out.println("No students.");
            System.out.println("======================================");
            return;
        }

        System.out.println("Total students: " + students.size());
        System.out.println("--------------------------------------");

        for (ColumbiaSignUI.StudentDef s : students) {
            if (s == null) continue;

            System.out.println("studentId   = " + s.getStudentId());
            System.out.println("studentName = " + s.getStudentName());

            List<ColumbiaSignUI.ArrivalDef> arrivals = s.getArrivals();
            int count = (arrivals == null) ? 0 : arrivals.size();
            System.out.println("arrivals    = " + count);

            if (arrivals != null) {
                for (int i = 0; i < arrivals.size(); i++) {
                    ColumbiaSignUI.ArrivalDef a = arrivals.get(i);
                    if (a == null) continue;
                    System.out.println("  [" + i + "] " + a.getDay() + " @ " + a.getTime());
                }
            }

            System.out.println("--------------------------------------");
        }

        System.out.println("======================================");
    }
}
