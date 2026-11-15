package com.streamchat.chat_backend.port;

import com.streamchat.chat_backend.domain.ChatMessage;

public interface GroupBroadcaster {

    int difundir(ChatMessage mensaje);
}
