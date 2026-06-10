package dev.pulsestack.processing.api;

import dev.pulsestack.processing.api.dto.TrendDataPoint;
import dev.pulsestack.processing.infrastructure.persistence.AnalyticsRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/v1/analytics")
@CrossOrigin(origins = "http://localhost:5173")
public class AnalyticsController {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsController(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @GetMapping("/trends")
    public List<TrendDataPoint> getTrends(@RequestParam(defaultValue = "7") int days) {
        Instant since = Instant.now().minus(Math.min(days, 30), ChronoUnit.DAYS);
        List<Object[]> rows = analyticsRepository.countByChannelAndSource(since);

        Map<String, long[]> counts = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String channelId   = row[0].toString();
            String channelName = row[1].toString();
            String source      = row[2].toString();
            long   cnt         = ((Number) row[3]).longValue();

            counts.computeIfAbsent(channelId, k -> new long[4]);
            names.put(channelId, channelName);

            switch (source) {
                case "REDDIT"  -> counts.get(channelId)[0] += cnt;
                case "YOUTUBE" -> counts.get(channelId)[1] += cnt;
                case "GITHUB"  -> counts.get(channelId)[2] += cnt;
                case "NEWSAPI" -> counts.get(channelId)[3] += cnt;
            }
        }

        return counts.entrySet().stream()
                .map(e -> {
                    long[] c = e.getValue();
                    return new TrendDataPoint(e.getKey(), names.get(e.getKey()), c[0], c[1], c[2], c[3]);
                })
                .toList();
    }
}
