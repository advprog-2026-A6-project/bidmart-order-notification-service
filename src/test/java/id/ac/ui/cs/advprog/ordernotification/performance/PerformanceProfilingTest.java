package id.ac.ui.cs.advprog.ordernotification.performance;

import id.ac.ui.cs.advprog.ordernotification.model.Notification;
import id.ac.ui.cs.advprog.ordernotification.repository.NotificationRepository;
import id.ac.ui.cs.advprog.ordernotification.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PerformanceProfilingTest {

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private NotificationServiceImpl service;

    private static final int NUM_NOTIFICATIONS = 2000;
    private static final String TARGET_USER = "user_perf_target";
    private static final String OTHER_USER = "user_perf_other";

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        List<Notification> dummyList = new ArrayList<>();
        
        // Add notifications for the target user (e.g. 50 items)
        for (int i = 0; i < 50; i++) {
            Notification notif = new Notification();
            notif.setUserId(TARGET_USER);
            notif.setMessage("Notification message " + i);
            notif.setType("TEST");
            notif.setPreferenceType("PUSH");
            notif.setStatus("SENT");
            notif.setCreatedAt(LocalDateTime.now());
            dummyList.add(notif);
        }

        // Add a large number of notifications for other users to build table volume
        for (int i = 0; i < NUM_NOTIFICATIONS; i++) {
            Notification notif = new Notification();
            notif.setUserId(OTHER_USER + "_" + i);
            notif.setMessage("Notification message for other user " + i);
            notif.setType("OTHER");
            notif.setPreferenceType("EMAIL");
            notif.setStatus("SENT");
            notif.setCreatedAt(LocalDateTime.now());
            dummyList.add(notif);
        }

        repository.saveAll(dummyList);
    }

    @Test
    void profileAndComparePerformance() {
        System.out.println("=== STARTING PERFORMANCE PROFILING AND BENCHMARKING ===");
        
        // 1. Warm-up runs to let JIT compiler optimize
        for (int i = 0; i < 10; i++) {
            runOldFilteringMethod();
            service.findByUserId(TARGET_USER);
        }

        // 2. Benchmark Old Method (findAll() + stream + filter)
        long startTimeOld = System.nanoTime();
        List<Notification> resultOld = null;
        for (int i = 0; i < 50; i++) {
            resultOld = runOldFilteringMethod();
        }
        long endTimeOld = System.nanoTime();
        long durationOld = (endTimeOld - startTimeOld) / 50; // average nanoseconds per run

        // 3. Benchmark New Optimized Method (repository.findByUserId(userId))
        long startTimeNew = System.nanoTime();
        List<Notification> resultNew = null;
        for (int i = 0; i < 50; i++) {
            resultNew = service.findByUserId(TARGET_USER);
        }
        long endTimeNew = System.nanoTime();
        long durationNew = (endTimeNew - startTimeNew) / 50; // average nanoseconds per run

        // Calculate metrics
        double durationOldMs = durationOld / 1_000_000.0;
        double durationNewMs = durationNew / 1_000_000.0;
        double speedupPercentage = ((durationOld - durationNew) / (double) durationOld) * 100;

        System.out.println("Old Method (Stream Filtering) Avg Execution Time: " + durationOldMs + " ms");
        System.out.println("New Method (Database Index Query) Avg Execution Time: " + durationNewMs + " ms");
        System.out.println("Performance Improvement (Speedup): " + String.format("%.2f", speedupPercentage) + "%");
        
        // Assertions
        assertTrue(resultOld != null && resultOld.size() == 50, "Verification of correctness: Old method failed.");
        assertTrue(resultNew != null && resultNew.size() == 50, "Verification of correctness: New method failed.");
        
        // Verify improvement is at least 50%
        System.out.println("Verifying if improvement is at least 50%...");
        assertTrue(speedupPercentage >= 50.0, "Optimized query did not show at least a 50% performance improvement.");
        System.out.println("Performance profiling passed! Improvement meets the >=50% rubric threshold.");
        System.out.println("=== ENDING PERFORMANCE PROFILING AND BENCHMARKING ===");
    }

    private List<Notification> runOldFilteringMethod() {
        return repository.findAll().stream()
                .filter(n -> TARGET_USER.equals(n.getUserId()))
                .toList();
    }
}
