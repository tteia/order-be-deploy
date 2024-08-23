package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSaveReqDto {
//    private Long memberId; => 1) Authentication 이 전역적으로 활용 가능하기 때문에 memberId 가 더 이상 필요없음
    private Long productId;
    private Integer productCount;
//    private List<OrderDto> orderDtos;

    // 2)
//    @Data
//    @Builder
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class OrderDto{ // 외부 클래스로 빼줘도 되지만 직관적으로 보기 위해 내부에. 내부라서 앞에 public 떼줌.
//        private Long productId;
//        private Integer productCount;
//    }

    public Ordering toEntity(Member member){
        return Ordering.builder()
                .member(member)
                .build();
    }

}
