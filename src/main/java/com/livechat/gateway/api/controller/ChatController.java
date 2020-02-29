package com.livechat.gateway.api.controller;

import com.livechat.gateway.api.controller.dto.ChatMessage;
import com.livechat.gateway.api.controller.dto.PostMessageRequest;
import com.livechat.gateway.api.controller.dto.PostMessageResponse;
import com.livechat.gateway.api.service.chat.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @RequestMapping(value = "/users/{userId}/conversations/{conversationId}/currentChat", method = RequestMethod.GET)
    public List<ChatMessage> getConversations(@PathVariable long userId,
                                              @PathVariable long conversationId,
                                              @RequestParam(value = "fromMessageId", defaultValue = "0") Long fromMessageId,
                                              @RequestParam(value = "count", defaultValue = "100") Long count) {
        return chatService.readMessages(userId, conversationId, fromMessageId, count);
    }

    @RequestMapping(value = "/users/{userId}/conversations/{conversationId}/postMessage", method = RequestMethod.POST)
    public PostMessageResponse postMessage(@PathVariable long userId,
                                           @PathVariable long conversationId,
                                           @RequestBody PostMessageRequest reqBody,
                                           HttpServletRequest request) {
        chatService.postMessage(userId, conversationId, reqBody, getClientAddress(request));
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
