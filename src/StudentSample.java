// FILE: src/StudentSample.java
// Purpose: Accept students from ColumbiaSignUI and write them to a JSON file using GSON.
// Minimal change for the team: keep console output + add JSON output (studentData.json).

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
       BLOCK 1 — GSON + Output Filename
       ========================================================= */
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String DEFAULT_OUTPUT_FILE = "studentData.json";

    /* =========================================================
       BLOCK 2 — Public entrypoint called by UI
       ========================================================= */
    public static void handleStudents(List<ColumbiaSignUI.StudentDef> students) {

        // Safety: avoid nulls
        if (students == null) students = new ArrayList<>();

        // Keep your console output (so you can verify quickly)
        printStudentsToConsole(students);

        // Write JSON file (minimal: one file, all students)
        writeStudentsToJsonFile(students, DEFAULT_OUTPUT_FILE);
    }

    /* =========================================================
       BLOCK 3 — Console output (matches what you showed)
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
       BLOCK 4 — JSON write (object graph as-is)
       Output format:
       {
         "generatedAt": "...",
         "students": [
           { studentId, studentName, arrivals:[{day,time},...] },
           ...
         ]
       }
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
       BLOCK 5 — Optional: If you want the OLD generator style
       Map keys: "Student 1", "Student 2", etc.
       This is NOT required, but included because your team already
       has that pattern.
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
       BLOCK 6 — Simple wrapper for clean JSON structure
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
