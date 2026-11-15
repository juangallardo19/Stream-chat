package com.streamchat.analytics_service.port;

import com.streamchat.analytics_service.domain.ArchivedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ArchivedMessage, UUID> {
}
