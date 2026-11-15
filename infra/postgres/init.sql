CREATE TABLE IF NOT EXISTS chat_groups (
    group_id VARCHAR(40) PRIMARY KEY,
    total_messages BIGINT NOT NULL DEFAULT 0,
    first_message_at TIMESTAMPTZ,
    last_message_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS chat_messages (
    message_id UUID PRIMARY KEY,
    group_id VARCHAR(40) NOT NULL REFERENCES chat_groups (group_id),
    user_id VARCHAR(40) NOT NULL,
    text VARCHAR(1000) NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL,
    archived_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_group_sent ON chat_messages (group_id, sent_at);
