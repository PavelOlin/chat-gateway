package com.livechat.gateway.service.queue.db;

import com.livechat.gateway.entity.queue.QueueRecord;
import com.livechat.gateway.entity.queue.QueueType;
import com.livechat.gateway.service.queue.IQueueService;
import com.livechat.gateway.service.queue.QueueMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.livechat.gateway.storage.queue.QueueStorage;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DbQueueService implements IQueueService {

    private final QueueStorage queueStorage;

    @Autowired
    public DbQueueService(QueueStorage queueStorage) {
        this.queueStorage = queueStorage;
    }

    @Override
    public void sendMessages(QueueType queueType, List<String> messages) {
        queueStorage.saveRecords(queueType, messages);
    }

    @Override
    public void deleteMessages(List<Long> messageIds) {
        queueStorage.deleteRecordsByIds(messageIds);
    }

    @Transactional
    @Override
    public List<QueueMessage> receiveMessages(QueueType queueType, long secondsToLock, int count) {
        List<QueueRecord> records = queueStorage.readRecords(queueType, count);
        List<Long> ids = new ArrayList<>(records.size());
        List<QueueMessage> messages = new ArrayList<>(records.size());
        for (QueueRecord record: records) {
            ids.add(record.getId());
            messages.add(new QueueMessage(record.getId(), record.getPayload()));
        }
        queueStorage.lockRecordsForRead(secondsToLock, ids);
        return messages;
    }
}
