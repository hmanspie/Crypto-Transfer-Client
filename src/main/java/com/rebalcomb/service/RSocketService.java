package com.rebalcomb.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rebalcomb.model.dto.KeyPair;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.model.dto.SecretBlock;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.util.LocalDateTimeDeserializer;
import com.rebalcomb.util.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

//todo обробити виняток якщо сервер не доступний Connection refused: no further information
@Service
public class RSocketService {

    private final Type type = new TypeToken<List<MessageRequest>>() {}.getType();
    private final RSocketRequester rSocketRequester;
    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson;

    @Autowired
    public RSocketService(RSocketRequester rSocketRequester) {
        this.rSocketRequester = rSocketRequester;
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        gson = gsonBuilder.setPrettyPrinting().create();
    }

    protected String sendUser(User user) {
        return rSocketRequester
                .route("signUp")
                .data(gson.toJson(user))
                .retrieveMono(String.class).block();
    }

    protected KeyPair getPublicKey(String serverId) {
        return gson.fromJson(rSocketRequester
                .route("getPublicKey")
                .data(serverId)
                .retrieveMono(String.class).block(), KeyPair.class);
    }

    public User searchUserInMainServer(String username) {
        return  gson.fromJson(rSocketRequester
                .route("searchUser")
                .data(username)
                .retrieveMono(String.class).block(), User.class);
    }

    protected String sendMessage(SecretBlock secretBlock) {
        return rSocketRequester
                .route("sendMessage")
                .data(gson.toJson(secretBlock))
                .retrieveMono(String.class).block();
    }

    protected List<MessageRequest> getIncoming(String serverId) {
        return gson.fromJson(rSocketRequester
                .route("getIncoming")
                .data(serverId)
                .retrieveMono(String.class).block(), type);
    }
}
