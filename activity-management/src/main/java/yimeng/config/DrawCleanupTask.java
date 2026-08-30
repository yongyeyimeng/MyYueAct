package yimeng.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yimeng.mapper.DrawMapper;

import java.time.LocalDateTime;

@Component
public class DrawCleanupTask {

    @Autowired
    private DrawMapper drawMapper;

    @Scheduled(fixedDelay = 3600000)
    public void cleanupDrawnDraws() {
        drawMapper.deleteDrawnBefore(LocalDateTime.now().minusDays(3));
    }
}
