//package com.beyond.ordersystem.ordering.service;
//
//import com.beyond.ordersystem.common.configs.RabbitMqConfig;
//import com.beyond.ordersystem.ordering.dto.OrderSaveReqDto;
//import com.beyond.ordersystem.ordering.dto.StockDecreaseEvent;
//
//import com.beyond.ordersystem.product.domain.Product;
//import com.beyond.ordersystem.product.repository.ProductRepository;
//import com.beyond.ordersystem.product.service.ProductService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.persistence.EntityNotFoundException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class StockDecreaseEventHandler {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//    @Autowired
//    private ProductRepository productRepository;
//
//
//    public void publish(StockDecreaseEvent event){
//        rabbitTemplate.convertAndSend(RabbitMqConfig.STOCK_DECREASE_QUEUE, event);
//    }
//
//    // 트랜잭션이 완료된 이후에 메세지를 수신하므로 동시성 이슈가 발생하지 않는다.
//    @Transactional
//    @RabbitListener(queues = RabbitMqConfig.STOCK_DECREASE_QUEUE) // 선언된 큐만 바라보고 있다가 메세지를 받아서 redis 처리
//    public void listen(Message message){
//        String messageBody = new String(message.getBody());
//        // 재고를 rdb 로 업데이트
//        // json 메세지를 parsing (StockDecreaseEvent 를 Object Mapper 로 직접 parsing)
//        ObjectMapper objectMapper = new ObjectMapper();
//        try{
//            StockDecreaseEvent stockDecreaseEvent = objectMapper.readValue(messageBody, StockDecreaseEvent.class);
//            Product product = productRepository.findById(stockDecreaseEvent.getProductId()).orElseThrow(()-> new EntityNotFoundException("해당 상품이 없습니다."));
//            product.updateStockQuantity(stockDecreaseEvent.getProductCount());// 재고 update => id 로 찾은 product 의 재고를 감소
//        }
//        catch (JsonProcessingException e){
//            throw new RuntimeException(e);
//        } // 왜..................
//        // checked , unchecked Exception . . . . 나는 클래스에 throw JsonException 어쩌구 붙여줬는데
//        // 왜 try-catch 를 써줘야 하는가 . . . . . .
//
//    }
//}
