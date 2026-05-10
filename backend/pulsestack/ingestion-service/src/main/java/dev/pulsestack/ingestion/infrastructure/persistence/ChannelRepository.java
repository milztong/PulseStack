package dev.pulsestack.ingestion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<ChannelEntity, UUID> {}
