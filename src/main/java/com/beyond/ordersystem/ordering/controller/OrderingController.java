package com.beyond.ordersystem.ordering.controller;

import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import com.beyond.ordersystem.ordering.service.OrderingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderingController {
    private final OrderingService orderingService;

    @Autowired
    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> orderCreate(@RequestBody List<OrderSaveReqDto> dto){
        Ordering ordering = orderingService.orderCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "주문 완료 !", ordering.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> orderList(){
        List<OrderListResDto> orderList = orderingService.orderList();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "정상 조회 완료 !", orderList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // 내 주문만 볼 수 있는 myOrders
    @GetMapping("/myorders")
    public ResponseEntity<?> productList(){
        List<OrderListResDto> listDto = orderingService.myOrders();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "주문 조회 완료 !", listDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    // admin 사용자가 주문 취소
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> orderCancel(@PathVariable Long id){
        Ordering ordering = orderingService.orderCancel(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "주문 취소 완료 !", ordering.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
