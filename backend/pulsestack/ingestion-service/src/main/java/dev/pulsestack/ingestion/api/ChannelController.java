package dev.pulsestack.ingestion.api;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.ingestion.api.dto.ChannelRequest;
import dev.pulsestack.ingestion.application.service.ChannelLoader;
import dev.pulsestack.ingestion.infrastructure.persistence.ChannelEntity;
import dev.pulsestack.ingestion.infrastructure.persistence.ChannelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/channels")
public class ChannelController {

    private final ChannelLoader channelLoader;
    private final ChannelRepository channelRepository;

    public ChannelController(ChannelLoader channelLoader, ChannelRepository channelRepository) {
        this.channelLoader = channelLoader;
        this.channelRepository = channelRepository;
    }

    @GetMapping
    public List<Channel> getAllChannels() {
        return channelLoader.loadAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Channel createChannel(@RequestBody ChannelRequest request) {
        if (channelRepository.findByName(request.name()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Channel already exists: " + request.name());
        }
        ChannelEntity entity = new ChannelEntity(request.name(), request.displayName(), request.description());
        ChannelEntity saved = channelRepository.save(entity);
        return new Channel(saved.getId(), saved.getName(), saved.getDisplayName(), saved.getDescription());
    }

    @PutMapping("/{name}")
    public Channel updateChannel(@PathVariable String name, @RequestBody ChannelRequest request) {
        ChannelEntity entity = channelRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found: " + name));
        entity.setDisplayName(request.displayName());
        entity.setDescription(request.description());
        ChannelEntity saved = channelRepository.save(entity);
        return new Channel(saved.getId(), saved.getName(), saved.getDisplayName(), saved.getDescription());
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChannel(@PathVariable String name) {
        ChannelEntity entity = channelRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found: " + name));
        channelRepository.delete(entity);
    }
}
