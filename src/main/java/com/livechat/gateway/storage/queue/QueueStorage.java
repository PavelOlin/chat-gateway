package com.livechat.gateway.storage.queue;

import com.livechat.gateway.entity.queue.QueueRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class QueueStorage {
    private static final int READ_RECORDS_COUNT_MIN = 1;
    private static final int READ_RECORDS_COUNT_MAX = 100;
    private static final long SECONDS_TO_LOCK_MIN = 30;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public QueueStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveRecords(List<String> messages) {
        if (messages.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate("INSERT INTO queue (payload) VALUES (?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, messages.get(i));
            }

            @Override
            public int getBatchSize() {
                return messages.size();
            }
        });
    }

    public void deleteRecordsByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }
        jdbcTemplate.update("DELETE FROM queue WHERE id IN (" + getCommaSeparatedQuotes(ids) + ")", ids.toArray());
    }

    public List<QueueRecord> readRecords(int count) {
        validateCount(count);
        return jdbcTemplate.query("SELECT id, payload, added_timestamp, locked_until FROM queue " +
                        "WHERE locked_until < current_timestamp " +
                        "ORDER BY added_timestamp LIMIT ?", rowMapper(), count);
    }

    public void lockRecordsForRead(long secondsToLock, List<Long> ids) {
        validateSecondsToLock(secondsToLock);
        if (ids.isEmpty()) {
            return;
        }
        List<Object> params = new ArrayList<>(ids.size() + 1);
        params.add(secondsToLock);
        params.addAll(ids);
        jdbcTemplate.update("UPDATE queue " +
                        "SET locked_until = current_timestamp  + (? * interval '1 second') " +
                        "WHERE id IN (" + getCommaSeparatedQuotes(ids) + ")",
                params.toArray());
    }

    private static void validateCount(long count) {
        if (count < READ_RECORDS_COUNT_MIN || count > READ_RECORDS_COUNT_MAX) {
            throw new IllegalArgumentException(String.format("Count must be from %d to %d",
                    READ_RECORDS_COUNT_MIN, READ_RECORDS_COUNT_MAX));
        }
    }

    private static void validateSecondsToLock(long secondsToLock) {
        if (secondsToLock < SECONDS_TO_LOCK_MIN) {
            throw new IllegalArgumentException("secondsToLock must be >= " + SECONDS_TO_LOCK_MIN + " seconds");
        }
    }

    private static RowMapper<QueueRecord> rowMapper() {
        return (rs, rowNum) -> {
            QueueRecord record = new QueueRecord();
            record.setId(rs.getLong("id"));
            record.setPayload(rs.getString("payload"));
            record.setAddedTimestamp(rs.getTimestamp("added_timestamp").getTime());
            record.setLockedUntil(rs.getTimestamp("locked_until").getTime());
            return record;
        };
    }

    private static String getCommaSeparatedQuotes(List<Long> ids) {
        return ids.stream().map(id -> "?").collect(Collectors.joining(","));
    }
}
