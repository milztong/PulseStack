package dev.pulsestack.ingestion.api;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.ingestion.application.service.ChannelLoader;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/channels")
@CrossOrigin(origins = "http://localhost:5173")
public class ChannelController {

    private final ChannelLoader channelLoader;

    public ChannelController(ChannelLoader channelLoader) {
        this.channelLoader = channelLoader;
    }

    @GetMapping
    public List<Channel> getAllChannels() {
        return channelLoader.loadAll();
    }
}
