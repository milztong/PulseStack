package dev.pulsestack.ingestion.application.service;

import dev.pulsestack.domain.model.Channel;

import java.util.List;

/**
 * Port: Laedt alle konfigurierten Channels.
 * Implementiert durch ChannelRepository im Infrastructure-Layer.
 */
public interface ChannelLoader {
    List<Channel> loadAll();
}
