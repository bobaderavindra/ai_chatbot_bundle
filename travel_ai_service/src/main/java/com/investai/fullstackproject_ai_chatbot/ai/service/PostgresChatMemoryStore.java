package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.config.PostgresStorageProperties;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatMessageView;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionDetail;
import com.investai.fullstackproject_ai_chatbot.ai.domain.ChatSessionSummary;
import com.investai.fullstackproject_ai_chatbot.ai.domain.StoredChatbotProfile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PostgresChatMemoryStore implements ChatMemoryStore {

    private final PostgresStorageProperties properties;

    public PostgresChatMemoryStore(PostgresStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<StoredChatbotProfile> listChatbots() {
        String sql = """
                SELECT
                    cd.chatbot_id::text,
                    cd.chatbot_code,
                    cd.display_name,
                    cd.domain_name,
                    cd.description,
                    cd.system_prompt,
                    cd.welcome_message,
                    cc.capability_name
                FROM chatbot_definition cd
                LEFT JOIN chatbot_capability cc ON cc.chatbot_id = cd.chatbot_id
                WHERE cd.is_active = TRUE
                ORDER BY cd.display_name, cc.capability_name
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapChatbots(resultSet);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load chatbots from PostgreSQL", exception);
        }
    }

    @Override
    public Optional<StoredChatbotProfile> findChatbotByCode(String chatbotCode) {
        String sql = """
                SELECT
                    cd.chatbot_id::text,
                    cd.chatbot_code,
                    cd.display_name,
                    cd.domain_name,
                    cd.description,
                    cd.system_prompt,
                    cd.welcome_message,
                    cc.capability_name
                FROM chatbot_definition cd
                LEFT JOIN chatbot_capability cc ON cc.chatbot_id = cd.chatbot_id
                WHERE cd.is_active = TRUE
                  AND cd.chatbot_code = ?
                ORDER BY cc.capability_name
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chatbotCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapChatbots(resultSet).stream().findFirst();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load chatbot " + chatbotCode, exception);
        }
    }

    @Override
    public ChatSessionDetail createSession(String userId, StoredChatbotProfile chatbotProfile, String title) {
        String effectiveTitle = title == null || title.isBlank()
                ? chatbotProfile.displayName() + " chat"
                : title;

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                UUID appUserId = ensureUser(connection, userId);
                UUID sessionId = insertSession(connection, chatbotProfile.chatbotCode(), appUserId, effectiveTitle);

                if (chatbotProfile.welcomeMessage() != null && !chatbotProfile.welcomeMessage().isBlank()) {
                    insertMessage(connection, sessionId, 1, "ASSISTANT", chatbotProfile.displayName(), chatbotProfile.welcomeMessage());
                    updateLastMessageAt(connection, sessionId);
                }

                connection.commit();
                return getSession(userId, sessionId.toString())
                        .orElseThrow(() -> new IllegalStateException("Created chat session could not be reloaded"));
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } catch (RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to create chat session in PostgreSQL", exception);
        }
    }

    @Override
    public List<ChatSessionSummary> listSessions(String userId) {
        String sql = """
                SELECT
                    cs.session_id::text,
                    cd.chatbot_code,
                    cd.display_name,
                    cs.session_title,
                    cs.session_status,
                    cs.started_at,
                    cs.last_message_at,
                    COALESCE(last_message.message_content, cd.welcome_message, '') AS last_message_preview
                FROM chat_session cs
                JOIN chatbot_definition cd ON cd.chatbot_id = cs.chatbot_id
                JOIN app_user au ON au.user_id = cs.user_id
                LEFT JOIN LATERAL (
                    SELECT cm.message_content
                    FROM chat_message cm
                    WHERE cm.session_id = cs.session_id
                    ORDER BY cm.message_sequence DESC
                    LIMIT 1
                ) AS last_message ON TRUE
                WHERE au.external_user_ref = ?
                ORDER BY cs.last_message_at DESC NULLS LAST, cs.started_at DESC
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ChatSessionSummary> sessions = new ArrayList<>();
                while (resultSet.next()) {
                    sessions.add(new ChatSessionSummary(
                            resultSet.getString("session_id"),
                            resultSet.getString("chatbot_code"),
                            resultSet.getString("display_name"),
                            resultSet.getString("session_title"),
                            resultSet.getString("session_status"),
                            toInstant(resultSet.getTimestamp("started_at")),
                            toInstant(resultSet.getTimestamp("last_message_at")),
                            resultSet.getString("last_message_preview")
                    ));
                }
                return sessions;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load chat sessions from PostgreSQL", exception);
        }
    }

    @Override
    public Optional<ChatSessionDetail> getSession(String userId, String sessionId) {
        String sql = """
                SELECT
                    cs.session_id::text,
                    cd.chatbot_code,
                    cd.display_name,
                    cs.session_title,
                    cs.session_status,
                    cs.started_at,
                    cs.last_message_at
                FROM chat_session cs
                JOIN chatbot_definition cd ON cd.chatbot_id = cs.chatbot_id
                JOIN app_user au ON au.user_id = cs.user_id
                WHERE cs.session_id = ?
                  AND au.external_user_ref = ?
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, UUID.fromString(sessionId));
            statement.setString(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new ChatSessionDetail(
                        resultSet.getString("session_id"),
                        resultSet.getString("chatbot_code"),
                        resultSet.getString("display_name"),
                        resultSet.getString("session_title"),
                        resultSet.getString("session_status"),
                        toInstant(resultSet.getTimestamp("started_at")),
                        toInstant(resultSet.getTimestamp("last_message_at")),
                        getMessages(userId, sessionId)
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load chat session from PostgreSQL", exception);
        }
    }

    @Override
    public List<ChatMessageView> getMessages(String userId, String sessionId) {
        requireSessionOwnership(userId, sessionId);

        String sql = """
                SELECT
                    message_id::text,
                    sender_role,
                    sender_name,
                    message_content,
                    created_at
                FROM chat_message
                WHERE session_id = ?
                ORDER BY message_sequence
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, UUID.fromString(sessionId));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<ChatMessageView> messages = new ArrayList<>();
                while (resultSet.next()) {
                    messages.add(new ChatMessageView(
                            resultSet.getString("message_id"),
                            resultSet.getString("sender_role"),
                            resultSet.getString("sender_name"),
                            resultSet.getString("message_content"),
                            toInstant(resultSet.getTimestamp("created_at"))
                    ));
                }
                return messages;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load chat messages from PostgreSQL", exception);
        }
    }

    @Override
    public void appendMessage(String userId, String sessionId, String senderRole, String senderName, String content) {
        requireSessionOwnership(userId, sessionId);

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                UUID sessionUuid = UUID.fromString(sessionId);
                int nextSequence = nextSequence(connection, sessionUuid);
                insertMessage(connection, sessionUuid, nextSequence, senderRole, senderName, content);
                updateSessionTitle(connection, sessionUuid, senderRole, content);
                updateLastMessageAt(connection, sessionUuid);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } catch (RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to append chat message in PostgreSQL", exception);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password()
        );
    }

    private UUID ensureUser(Connection connection, String userId) throws SQLException {
        try (PreparedStatement insertStatement = connection.prepareStatement("""
                INSERT INTO app_user (external_user_ref, full_name)
                VALUES (?, ?)
                ON CONFLICT (external_user_ref) DO NOTHING
                """)) {
            insertStatement.setString(1, userId);
            insertStatement.setString(2, userId);
            insertStatement.executeUpdate();
        }

        try (PreparedStatement selectStatement = connection.prepareStatement("""
                SELECT user_id
                FROM app_user
                WHERE external_user_ref = ?
                """)) {
            selectStatement.setString(1, userId);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getObject("user_id", UUID.class);
                }
            }
        }

        throw new IllegalStateException("Application user could not be resolved");
    }

    private UUID insertSession(Connection connection, String chatbotCode, UUID appUserId, String title) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO chat_session (
                    chatbot_id,
                    user_id,
                    session_title,
                    channel_name,
                    session_status,
                    language_code,
                    last_message_at,
                    session_context
                )
                VALUES (
                    (SELECT chatbot_id FROM chatbot_definition WHERE chatbot_code = ?),
                    ?,
                    ?,
                    'web',
                    'ACTIVE',
                    'en',
                    NOW(),
                    '{}'::jsonb
                )
                RETURNING session_id
                """)) {
            statement.setString(1, chatbotCode);
            statement.setObject(2, appUserId);
            statement.setString(3, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getObject("session_id", UUID.class);
                }
            }
        }

        throw new IllegalStateException("Chat session could not be inserted");
    }

    private int nextSequence(Connection connection, UUID sessionId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COALESCE(MAX(message_sequence), 0) + 1 AS next_sequence
                FROM chat_message
                WHERE session_id = ?
                """)) {
            statement.setObject(1, sessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("next_sequence");
            }
        }
    }

    private void insertMessage(Connection connection, UUID sessionId, int sequence, String senderRole, String senderName, String content)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO chat_message (
                    session_id,
                    message_sequence,
                    sender_role,
                    sender_name,
                    message_content,
                    content_type,
                    metadata
                )
                VALUES (?, ?, ?, ?, ?, 'TEXT', '{}'::jsonb)
                """)) {
            statement.setObject(1, sessionId);
            statement.setInt(2, sequence);
            statement.setString(3, senderRole);
            statement.setString(4, senderName);
            statement.setString(5, content);
            statement.executeUpdate();
        }
    }

    private void updateLastMessageAt(Connection connection, UUID sessionId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE chat_session
                SET last_message_at = NOW()
                WHERE session_id = ?
                """)) {
            statement.setObject(1, sessionId);
            statement.executeUpdate();
        }
    }

    private void updateSessionTitle(Connection connection, UUID sessionId, String senderRole, String content) throws SQLException {
        if (!"USER".equals(senderRole) || content == null || content.isBlank()) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE chat_session
                SET session_title = ?
                WHERE session_id = ?
                  AND (session_title IS NULL OR session_title = '' OR session_title LIKE '% chat')
                """)) {
            statement.setString(1, abbreviate(content));
            statement.setObject(2, sessionId);
            statement.executeUpdate();
        }
    }

    private void requireSessionOwnership(String userId, String sessionId) {
        String sql = """
                SELECT 1
                FROM chat_session cs
                JOIN app_user au ON au.user_id = cs.user_id
                WHERE cs.session_id = ?
                  AND au.external_user_ref = ?
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, UUID.fromString(sessionId));
            statement.setString(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Chat session not found");
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to validate chat session ownership", exception);
        }
    }

    private List<StoredChatbotProfile> mapChatbots(ResultSet resultSet) throws SQLException {
        Map<String, MutableChatbotProfile> grouped = new LinkedHashMap<>();
        while (resultSet.next()) {
            String chatbotCode = resultSet.getString("chatbot_code");
            MutableChatbotProfile chatbot = grouped.get(chatbotCode);
            if (chatbot == null) {
                chatbot = new MutableChatbotProfile(
                        resultSet.getString("chatbot_id"),
                        chatbotCode,
                        resultSet.getString("display_name"),
                        resultSet.getString("domain_name"),
                        resultSet.getString("description"),
                        resultSet.getString("system_prompt"),
                        resultSet.getString("welcome_message")
                );
                grouped.put(chatbotCode, chatbot);
            }
            String capabilityName = resultSet.getString("capability_name");
            if (capabilityName != null && !capabilityName.isBlank()) {
                chatbot.capabilities().add(capabilityName);
            }
        }

        return grouped.values().stream()
                .map(MutableChatbotProfile::toProfile)
                .toList();
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private String abbreviate(String content) {
        String normalized = content.trim();
        if (normalized.length() <= 48) {
            return normalized;
        }
        return normalized.substring(0, 45) + "...";
    }

    private record MutableChatbotProfile(
            String chatbotId,
            String chatbotCode,
            String displayName,
            String domainName,
            String description,
            String systemPrompt,
            String welcomeMessage,
            List<String> capabilities) {

        private MutableChatbotProfile(
                String chatbotId,
                String chatbotCode,
                String displayName,
                String domainName,
                String description,
                String systemPrompt,
                String welcomeMessage) {
            this(chatbotId, chatbotCode, displayName, domainName, description, systemPrompt, welcomeMessage, new ArrayList<>());
        }

        private StoredChatbotProfile toProfile() {
            return new StoredChatbotProfile(
                    chatbotId,
                    chatbotCode,
                    displayName,
                    domainName,
                    description,
                    systemPrompt,
                    welcomeMessage,
                    List.copyOf(capabilities)
            );
        }
    }
}
