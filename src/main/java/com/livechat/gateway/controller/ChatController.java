package com.livechat.gateway.controller;

import com.livechat.gateway.dto.ChatMessageDto;
import com.livechat.gateway.dto.PostMessageRequest;
import com.livechat.gateway.dto.PostMessageResponse;
import com.livechat.gateway.service.ChatApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatApiService chatApiService;

    @Autowired
    public ChatController(ChatApiService chatApiService) {
        this.chatApiService = chatApiService;
    }

    @RequestMapping(value = "/users/{userId}/chats/{chatId}/messages/getHistory", method = RequestMethod.GET)
    public List<ChatMessageDto> getChatHistory(@PathVariable long userId,
                                                @PathVariable long chatId,
                                                @RequestParam(value = "fromMessageId", defaultValue = "0") Long fromMessageId,
                                                @RequestParam(value = "count", defaultValue = "100") Long count) {
        return chatApiService.readMessages(userId, chatId, fromMessageId, count);
    }

    @RequestMapping(value = "/users/{userId}/chats/{chatId}/messages/postMessage", method = RequestMethod.POST)
    public PostMessageResponse postMessage(@PathVariable long userId,
                                           @PathVariable long chatId,
                                           @RequestBody PostMessageRequest reqBody,
                                           HttpServletRequest request) {
        chatApiService.postMessage(userId, chatId, reqBody, getClientAddress(request));
        return new PostMessageResponse("OK", "Added into queue");
    }

    private static String getClientAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

}
