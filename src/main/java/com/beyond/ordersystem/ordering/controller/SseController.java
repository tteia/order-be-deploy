package com.beyond.ordersystem.ordering.controller;

import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SseController implements MessageListener {
    // SSE Emitter 는 연결된 사용자 정보를 의미한다.
    // ConcurrentHashMap 은 Thread-safe 한 map (동시성 이슈 발생 X)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    // HashSet 이라고 생각하면 된다. 여러번 구독하는 것을 방지하기 위한 ConCurrentHashSet 변수 생성.
    private Set<String> subscribeList = ConcurrentHashMap.newKeySet();

    @Qualifier("4")
    private final RedisTemplate<String,Object> sseRedisTemplate;

    @Autowired
    public SseController(RedisMessageListenerContainer redisMessageListenerContainer, @Qualifier("4") RedisTemplate<String, Object> sseRedisTemplate) {
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.sseRedisTemplate = sseRedisTemplate;
    }

    // email 에 해당되는 메세지를 listen 하는 listener 를 추가하자.
    public void subscribeChannel(String email){
        // 이미 구독한 email 일 경우에 더 이상 구독하지 않도록 처리.
        if(!subscribeList.contains(email)) {
            MessageListenerAdapter listenerAdapter = createListenerAdapter(this);
            redisMessageListenerContainer.addMessageListener(listenerAdapter, new PatternTopic(email));
            subscribeList.add(email);
        }
    }

    private MessageListenerAdapter createListenerAdapter(SseController sseController){
        return new MessageListenerAdapter(sseController, "onMessage");
    }


    @GetMapping("/subscribe")
    public SseEmitter subscribe(){
        SseEmitter emitter = new SseEmitter(14400 * 60 * 1000L); // emitter 지속 시간 설정.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        emitters.put(email, emitter);
        emitter.onCompletion(()->emitters.remove(email));
        emitter.onTimeout(()->emitters.remove(email));
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected !"));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        subscribeChannel(email);
        return emitter;
    }

    public void publishMessage(OrderListResDto dto, String email){
        SseEmitter emitter = emitters.get(email);
//        if(emitter != null){
//            try {
//                emitter.send(SseEmitter.event().name("ordered").data(dto));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        else{ // emitter 없으면 redis 한테 던질게~
            sseRedisTemplate.convertAndSend(email, dto);
//        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Message 내용 parsing
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OrderListResDto dto = objectMapper.readValue(message.getBody(), OrderListResDto.class);
            String email = new String(pattern, StandardCharsets.UTF_8);
            SseEmitter emitter = emitters.get(email);
            if(emitter != null){
                emitter.send(SseEmitter.event().name("ordered").data(dto));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
