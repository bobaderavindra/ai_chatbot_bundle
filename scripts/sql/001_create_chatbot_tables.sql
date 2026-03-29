CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS chatbot_definition (
    chatbot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chatbot_code VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    domain_name VARCHAR(100) NOT NULL,
    description TEXT,
    system_prompt TEXT NOT NULL,
    default_model VARCHAR(100) NOT NULL DEFAULT 'gpt-4o-mini',
    temperature NUMERIC(3,2) NOT NULL DEFAULT 0.40,
    max_response_tokens INTEGER NOT NULL DEFAULT 1000,
    welcome_message TEXT,
    configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_chatbot_temperature CHECK (temperature >= 0 AND temperature <= 2),
    CONSTRAINT chk_chatbot_max_tokens CHECK (max_response_tokens > 0)
);

CREATE TABLE IF NOT EXISTS chatbot_capability (
    capability_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chatbot_id UUID NOT NULL REFERENCES chatbot_definition(chatbot_id) ON DELETE CASCADE,
    capability_name VARCHAR(100) NOT NULL,
    capability_description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_chatbot_capability UNIQUE (chatbot_id, capability_name)
);

CREATE TABLE IF NOT EXISTS app_user (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_user_ref VARCHAR(150) UNIQUE,
    full_name VARCHAR(150),
    email_address VARCHAR(255) UNIQUE,
    locale_code VARCHAR(20) NOT NULL DEFAULT 'en-US',
    timezone_name VARCHAR(60) NOT NULL DEFAULT 'UTC',
    preferences JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat_session (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chatbot_id UUID NOT NULL REFERENCES chatbot_definition(chatbot_id),
    user_id UUID REFERENCES app_user(user_id),
    session_title VARCHAR(200),
    channel_name VARCHAR(50) NOT NULL DEFAULT 'web',
    session_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    language_code VARCHAR(20) NOT NULL DEFAULT 'en',
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMPTZ,
    last_message_at TIMESTAMPTZ,
    session_context JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT chk_chat_session_status CHECK (session_status IN ('ACTIVE', 'COMPLETED', 'ABANDONED', 'ARCHIVED'))
);

CREATE TABLE IF NOT EXISTS chat_message (
    message_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES chat_session(session_id) ON DELETE CASCADE,
    message_sequence INTEGER NOT NULL,
    sender_role VARCHAR(20) NOT NULL,
    sender_name VARCHAR(120),
    message_content TEXT NOT NULL,
    content_type VARCHAR(30) NOT NULL DEFAULT 'TEXT',
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    total_tokens INTEGER,
    response_time_ms INTEGER,
    parent_message_id UUID REFERENCES chat_message(message_id),
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_chat_message_sequence UNIQUE (session_id, message_sequence),
    CONSTRAINT chk_chat_message_role CHECK (sender_role IN ('SYSTEM', 'USER', 'ASSISTANT', 'TOOL')),
    CONSTRAINT chk_chat_message_content_type CHECK (content_type IN ('TEXT', 'MARKDOWN', 'JSON', 'HTML')),
    CONSTRAINT chk_chat_message_tokens CHECK (
        prompt_tokens IS NULL OR prompt_tokens >= 0
    ),
    CONSTRAINT chk_chat_message_completion_tokens CHECK (
        completion_tokens IS NULL OR completion_tokens >= 0
    ),
    CONSTRAINT chk_chat_message_total_tokens CHECK (
        total_tokens IS NULL OR total_tokens >= 0
    ),
    CONSTRAINT chk_chat_message_response_time CHECK (
        response_time_ms IS NULL OR response_time_ms >= 0
    )
);

CREATE TABLE IF NOT EXISTS session_feedback (
    feedback_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES chat_session(session_id) ON DELETE CASCADE,
    message_id UUID REFERENCES chat_message(message_id) ON DELETE SET NULL,
    rating SMALLINT NOT NULL,
    feedback_text TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_session_feedback_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX IF NOT EXISTS idx_chatbot_definition_active
    ON chatbot_definition (is_active, domain_name);

CREATE INDEX IF NOT EXISTS idx_chat_session_chatbot_id
    ON chat_session (chatbot_id);

CREATE INDEX IF NOT EXISTS idx_chat_session_user_id
    ON chat_session (user_id);

CREATE INDEX IF NOT EXISTS idx_chat_session_last_message_at
    ON chat_session (last_message_at DESC);

CREATE INDEX IF NOT EXISTS idx_chat_message_session_id_created_at
    ON chat_message (session_id, created_at);

CREATE INDEX IF NOT EXISTS idx_chat_message_sender_role
    ON chat_message (sender_role);

CREATE INDEX IF NOT EXISTS idx_app_user_external_user_ref
    ON app_user (external_user_ref);
