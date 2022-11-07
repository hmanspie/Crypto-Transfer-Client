package com.rebalcomb.mapper;

import com.rebalcomb.model.dto.MessageRequest;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MessageRequestMapper {

    public static MessageRequest mapMessageRequest(MessageRequest request, String username) {
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss");
        request.setDateTime(LocalDateTime.now().format(formatter));
        request.setUser_from(username);
        return request;
    }
}
