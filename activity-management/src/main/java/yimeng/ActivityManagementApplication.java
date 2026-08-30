package yimeng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ActivityManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivityManagementApplication.class, args);
    }

}
