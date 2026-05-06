package id.ac.ui.cs.advprog.ordernotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class OrderNotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderNotificationApplication.class, args);
    }

    @org.springframework.context.annotation.Bean(name = "taskExecutor")
    public org.springframework.core.task.TaskExecutor taskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("EmailExecutor-");
        executor.initialize();
        return executor;
    }
}