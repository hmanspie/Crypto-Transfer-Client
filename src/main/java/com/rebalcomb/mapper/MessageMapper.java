package com.rebalcomb.mapper;

import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.model.entity.Message;
import com.rebalcomb.model.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageMapper {

    public static Message mapMessage(MessageRequest request, User from, User to) {
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss");
        Message message = new Message();
        message.setFrom(from);
        message.setTo(to);
        message.setTitle(request.getTitle());
        message.setBody(request.getBodyMessage());
        message.setDate_time(LocalDateTime.parse(request.getDateTime(), formatter));
        message.setIs_read(false);
        message.setIs_send(false);
        return message;
    }
}
