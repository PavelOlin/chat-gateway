package com.livechat.gateway.api.service.queue.db;

import com.livechat.gateway.api.entity.queue.QueueRecord;
import com.livechat.gateway.api.service.queue.IQueueService;
import com.livechat.gateway.api.service.queue.QueueMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.livechat.gateway.api.storage.queue.QueueStorage;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DbQueueService implements IQueueService {

    private final QueueStorage queueStorage;

    @Value("${dbQueueService.secondsToLock:120}")
    private Long secondsToLock;

    @Autowired
    public DbQueueService(QueueStorage queueStorage) {
        this.queueStorage = queueStorage;
    }

    @Override
    public void sendMessages(List<String> messages) {
        queueStorage.saveRecords(messages);
    }

    @Override
    public void deleteMessages(List<Long> messageIds) {
        queueStorage.deleteRecordsByIds(messageIds);
    }

    @Transactional
    @Override
    public List<QueueMessage> receiveMessages(int count) {
        List<QueueRecord> records = queueStorage.readRecords(count);
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
