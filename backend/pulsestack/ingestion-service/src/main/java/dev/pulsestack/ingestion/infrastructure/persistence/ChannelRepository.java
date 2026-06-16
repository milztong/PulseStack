package dev.pulsestack.ingestion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

import java.util.List;

public interface ChannelRepository extends JpaRepository<ChannelEntity, UUID> {
    Optional<ChannelEntity> findByName(String name);
    List<ChannelEntity> findByExternalOnlyFalse();
}
