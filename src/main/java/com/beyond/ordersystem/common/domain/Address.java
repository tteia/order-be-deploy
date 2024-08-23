package com.beyond.ordersystem.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable // 타 엔티티에서 사용 가능한 형태로 만드는 어노테이션.
// Entity 를 붙이면 테이블이 따로 만들어지기 때문에 써주지 않음.
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private String city;
    private String street;
    private String zipcode;
}
