package com.streamchat.analytics_service.port;

import com.streamchat.analytics_service.domain.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, String> {
}
