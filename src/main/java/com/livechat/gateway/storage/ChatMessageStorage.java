package com.livechat.gateway.storage;

import com.livechat.gateway.entity.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ChatMessageStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ChatMessageStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void save(List<ChatMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate("INSERT INTO chat_messages (user_id, chat_id, send_timestamp, encoded_message, client_ip) " +
                "VALUES (?,?,?,?,?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                writeTo(ps, messages.get(i));
            }

            @Override
            public int getBatchSize() {
                return messages.size();
            }
        });
    }

    public List<ChatMessage> readMessages(long conversationId, long fromMessageId, long count) {
        return jdbcTemplate.query("SELECT id, user_id, chat_id, send_timestamp, encoded_message, client_ip " +
                "FROM chat_messages " +
                "WHERE chat_id = ? AND id < ?" +
                "ORDER BY send_timestamp DESC " +
                "LIMIT ?",
                rowMapper(), conversationId, fromMessageId, count);
    }

    private static void writeTo(PreparedStatement ps, ChatMessage message) throws SQLException {
        int idx = 1;
        ps.setLong(idx++, message.getUserId());
        ps.setLong(idx++, message.getChatId());
        ps.setTimestamp(idx++,  new Timestamp(message.getTimestamp()));
        ps.setString(idx++, message.getEncodedMessage());
        ps.setString(idx, message.getClientIp());
    }

    private static RowMapper<ChatMessage> rowMapper() {
        return (rs, rowNum) -> {
            ChatMessage message = new ChatMessage();
            message.setId(rs.getLong("id"));
            message.setUserId(rs.getLong("user_id"));
            message.setChatId(rs.getLong("chat_id"));
            message.setTimestamp(rs.getTimestamp("send_timestamp").getTime());
            message.setEncodedMessage(rs.getString("encoded_message"));
            message.setClientIp(rs.getString("client_ip"));
            return message;
        };
    }
}
