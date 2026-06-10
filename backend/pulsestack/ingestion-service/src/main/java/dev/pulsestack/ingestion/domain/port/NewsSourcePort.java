package dev.pulsestack.ingestion.domain.port;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.domain.model.NewsItem;

import java.util.List;

public interface NewsSourcePort {

    List<NewsItem> fetchLatest(Channel channel);

    dev.pulsestack.domain.model.NewsSource getSource();
}
