package com.beyond.ordersystem.product.domain;

import com.beyond.ordersystem.common.domain.Address;
import com.beyond.ordersystem.member.domain.Role;
import com.beyond.ordersystem.product.dto.ProductResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String name;
    @Column(length = 30, nullable = false)
    private String category;
    @Column(nullable = false)
    private Integer price;
    @Column(nullable = false)
    private Integer stockQuantity;
    private String imagePath;

    public void updateImagePath(String imagePath){
        this.imagePath = imagePath;
    }

    public void updateStockQuantity(int stockQuantity){
        this.stockQuantity -= stockQuantity;
    }

    public ProductResDto fromEntity(){
        return ProductResDto.builder()
                .id(this.id)
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .imagePath(this.imagePath)
                .build();
    }

}
