package com.livechat.gateway.transformer;

import com.livechat.gateway.dto.ChatMessageDto;
import com.livechat.gateway.entity.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageTransformer implements IDtoEntityTransformer<ChatMessageDto, ChatMessage> {
    @Override
    public ChatMessage toEntity(ChatMessageDto dto) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChatMessageDto toDto(ChatMessage entity) {
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setId(entity.getId());
        chatMessageDto.setUserId(entity.getUserId());
        chatMessageDto.setClientIp(entity.getClientIp());
        chatMessageDto.setTimestamp(entity.getTimestamp());
        chatMessageDto.setEncodedMessage(entity.getEncodedMessage());

        // probably something else here with info about chat and users
        return chatMessageDto;
    }
}
