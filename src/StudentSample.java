// FILE: src/StudentSample.java
// Purpose: Takes students from the UI and writes studentData.json using Gson.
// Also prints everything to console so you can double-check what just got saved.

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentSample {

    /* =========================================================
       Gson + output file

       One pretty-print Gson instance and one default filename.
       Nothing complicated here.
       ========================================================= */
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String DEFAULT_OUTPUT_FILE = "studentData.json";

    /* =========================================================
       Main entrypoint (called by UI)

       UI hands us the full student list.
       We:
         1) Print it to console (quick sanity check)
         2) Write studentData.json
       ========================================================= */
    public static void handleStudents(List<ColumbiaSignUI.StudentDef> students) {

        // Don’t crash on null input
        if (students == null) students = new ArrayList<>();

        // Dump to console so you can verify fast
        printStudentsToConsole(students);

        // Write everything to one JSON file
        writeStudentsToJsonFile(students, DEFAULT_OUTPUT_FILE);
    }

    /* =========================================================
       Console output

       This shows exactly what’s about to be written.
       Helps catch missing arrivals, wrong IDs, etc.
       ========================================================= */
    private static void printStudentsToConsole(List<ColumbiaSignUI.StudentDef> students) {

        System.out.println("\n=== StudentSample.handleStudents() ===");
        System.out.println("Total students: " + students.size());
        System.out.println("--------------------------------------");

        for (ColumbiaSignUI.StudentDef s : students) {
            if (s == null) continue;

            List<ColumbiaSignUI.ArrivalDef> arrivals = s.getArrivals();
            if (arrivals == null) arrivals = new ArrayList<>();

            System.out.println("studentId   = " + s.getStudentId());
            System.out.println("studentName = " + s.getStudentName());
            System.out.println("arrivals    = " + arrivals.size());

            for (int i = 0; i < arrivals.size(); i++) {
                ColumbiaSignUI.ArrivalDef a = arrivals.get(i);
                if (a == null) continue;
                System.out.println("  [" + i + "] " + a.getDay() + " @ " + a.getTime());
            }
            System.out.println("--------------------------------------");
        }
    }

    /* =========================================================
       JSON write

       Output format:
       {
         "generatedAt": "...",
         "students": [
           { studentId, studentName, arrivals:[{day,time},...] },
           ...
         ]
       }

       We wrap the list so the JSON has a clean top-level structure.
       ========================================================= */
    private static void writeStudentsToJsonFile(List<ColumbiaSignUI.StudentDef> students, String filename) {

        StudentFile payload = new StudentFile(students);

        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(payload, writer);
            System.out.println("Wrote " + filename);
        } catch (IOException e) {
            System.err.println("Failed to write " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* =========================================================
       Optional legacy-style JSON

       If you ever need the old format:
       "Student 1", "Student 2", etc.

       Not required for the new system,
       but left here so nobody panics.
       ========================================================= */
    public static void writeStudentsAsMapJson(List<ColumbiaSignUI.StudentDef> students, String filename) {

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("generatedAt", LocalDateTime.now().toString());

        Map<String, Object> studentsMap = new LinkedHashMap<>();
        int idx = 1;

        for (ColumbiaSignUI.StudentDef s : students) {
            if (s == null) continue;

            Map<String, Object> one = new LinkedHashMap<>();
            one.put("studentId", s.getStudentId());
            one.put("studentName", s.getStudentName());

            List<Map<String, String>> arrivals = new ArrayList<>();
            if (s.getArrivals() != null) {
                for (ColumbiaSignUI.ArrivalDef a : s.getArrivals()) {
                    if (a == null) continue;
                    Map<String, String> ar = new LinkedHashMap<>();
                    ar.put("day", a.getDay());
                    ar.put("time", a.getTime());
                    arrivals.add(ar);
                }
            }

            one.put("arrivals", arrivals);

            studentsMap.put("Student " + idx, one);
            idx++;
        }

        out.put("students", studentsMap);

        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(out, writer);
            System.out.println("Wrote " + filename + " (map-style)");
        } catch (IOException e) {
            System.err.println("Failed to write " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* =========================================================
       JSON wrapper object

       Defines the actual shape of studentData.json:
       timestamp + list of students.
       ========================================================= */
    private static class StudentFile {
        String generatedAt;
        List<ColumbiaSignUI.StudentDef> students;

        StudentFile(List<ColumbiaSignUI.StudentDef> students) {
            this.generatedAt = LocalDateTime.now().toString();
            this.students = (students == null) ? new ArrayList<>() : students;
        }
    }
}
