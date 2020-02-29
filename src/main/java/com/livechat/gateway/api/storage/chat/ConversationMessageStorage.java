package com.livechat.gateway.api.storage.chat;

import com.livechat.gateway.api.entity.chat.ConversationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ConversationMessageStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ConversationMessageStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void save(List<ConversationMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate("INSERT INTO conversation_messages (user_id, conversation_id, send_timestamp, encoded_message, client_ip) " +
                "VALUES (?,?,?,?,?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int idx = 1;
                ConversationMessage message = messages.get(i);
                ps.setLong(idx++, message.getUserId());
                ps.setLong(idx++, message.getConversationId());
                ps.setTimestamp(idx++,  new Timestamp(message.getTimestamp()));
                ps.setString(idx++, message.getEncodedMessage());
                ps.setString(idx, message.getClientIp());
            }

            @Override
            public int getBatchSize() {
                return messages.size();
            }
        });
    }

    public List<ConversationMessage> readMessages(long userId, long conversationId, long fromMessageId, long count) {
        if (count < 1 || count > 100) {
            throw new IllegalArgumentException("'count' field must be from 1 to 100");
        }

        if (fromMessageId == 0) {
            fromMessageId = Long.MAX_VALUE;
        }

        return jdbcTemplate.query("SELECT id, user_id, conversation_id, send_timestamp, encoded_message, client_ip " +
                "FROM conversation_messages " +
                "WHERE conversation_id = ? AND id < ?" +
                "ORDER BY send_timestamp DESC " +
                "LIMIT ?",
                (rs, rowNum) -> {
                    ConversationMessage message = new ConversationMessage();
                    message.setId(rs.getLong("id"));
                    message.setUserId(rs.getLong("user_id"));
                    message.setConversationId(rs.getLong("conversation_id"));
                    message.setTimestamp(rs.getTimestamp("send_timestamp").getTime());
                    message.setEncodedMessage(rs.getString("encoded_message"));
                    message.setClientIp(rs.getString("client_ip"));
                    return message;
                }, conversationId, fromMessageId, count);
    }
}
