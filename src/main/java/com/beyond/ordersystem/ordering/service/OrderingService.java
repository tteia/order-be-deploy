package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.controller.SseController;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import com.beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import com.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderingService{
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
//    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final SseController sseController;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, ProductRepository productRepository, OrderDetailRepository orderDetailRepository, StockInventoryService stockInventoryService, SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.stockInventoryService = stockInventoryService;
//        this.stockDecreaseEventHandler = stockDecreaseEventHandler;
        this.sseController = sseController;
    }

    // 한 번에 한 스레드만 건드릴 수 있게 하면 동시성 이슈를 잡을 수 있지 않을까 . . . ?
    // @Synchronized 를 설정한다 하더라도, 재고 감소가 DB 에 반영되는 시점은 트랜잭션이 커밋되고 종료되는 시점이라 싱크가 맞지 않는다.
    public Ordering orderCreate(List<OrderSaveReqDto> dtos) {

        // 방법 2. JPA 최적화 방식
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(() -> new EntityNotFoundException("회 원 없 음 ."));
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // 이 한 줄은 외워주기.
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(()-> new EntityNotFoundException("회 원 없 음 ."));
        Ordering ordering = Ordering.builder()
                .member(member)
//                .orderDetails() > 아직 없어서 세팅할 수가 없음! 주문을 해야 디테일이 생기니까 . .
                .build();
        for (OrderSaveReqDto orderDto : dtos) {
            Product product = productRepository.findById(orderDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("회 원 없 음 ."));
            int quantity = orderDto.getProductCount();
            // redis 를 통한 재고 관리 및 재고 잔량 확인
            if(product.getName().contains("sale")){
                // 판매 중이면 redis 재고 체크.
                // redis 에서 재고 관리하는데 우리가 잔량이 필요해 ? => 경우에 따라 DB 에서 빼고 redis 에서 빼고 ,,
                int newQuantity = stockInventoryService.decreaseStock(orderDto.getProductId(), orderDto.getProductCount()).intValue();
                if(newQuantity < 0){
                    throw new IllegalArgumentException("재고가 부족합니다. 재고를 확인해주세요.");
                }
                // rdb(relation db) 에 재고를 업데이트. rabbitmq 를 통해 비동기적으로 이벤트 처리.
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), orderDto.getProductCount()));

            }
            else{

                if(product.getStockQuantity() < quantity){
                    throw new IllegalArgumentException("재고가 부족합니다. 주문량을 확인해주세요.");
                }
                product.updateStockQuantity(quantity); // 변경 감지(더티 체킹)로 인해 별도의 save 불필요함.
            }
            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            // 방법 1 과의 차이점. Repository 에 save 하는 게 아님.
            // save 를 뒤에서 해줘도 JPA 에서 알아서 먼저 실행시켜준다.
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering savedOrder = orderingRepository.save(ordering);
        sseController.publishMessage(savedOrder.fromEntity(), "admin@test.com");

        return savedOrder;
    }

    public List<OrderListResDto> orderList() {
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for (Ordering ordering : orderings) {
            orderListResDtos.add(ordering.fromEntity());
        }
        return orderListResDtos;
    }
    public List<OrderListResDto> myOrders() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("회원을 찾을 수 없습니다."));
        // ordering 의 member 와 이메일이 일치하는 경우를 꺼내오기.
        List<Ordering> orderings = orderingRepository.findByMember(member);
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for (Ordering ordering : orderings) {
            orderListResDtos.add(ordering.fromEntity());
        }
        return orderListResDtos;
    }

    public Ordering orderCancel(Long id) {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()->new EntityNotFoundException("주문을 찾을 수 없습니다."));
        ordering.updateStatus(OrderStatus.CANCELED);
        return ordering;
    }







    // 방법 1. 조금 더 쉬운 방식
    // Ordering 생성 : member_id, status
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(() -> new EntityNotFoundException("회 원 없 음 ."));
//        Ordering ordering = orderingRepository.save(dto.toEntity(member));
//
//        // OrderDetail 생성 : order_id, product_id, quantity
//        for (OrderSaveReqDto.OrderDto orderDto : dto.getOrderDtos()) {
//            Product product = productRepository.findById(orderDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("회 원 없 음 ."));
//            int quantity = orderDto.getProductCount();
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            orderDetailRepository.save(orderDetail);
//        }
//        return ordering;
}
