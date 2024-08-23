package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSaveReqDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")                 // @RequestBody 쓰면 안 됨. @ModelAttribute (생략 가능) 쓰거나 아래처럼 json 두 번 받기.
//    public ResponseEntity<?> productCreate(@RequestPart ProductSaveReqDto createDto, @RequestPart MultipartFile productImage){
    public ResponseEntity<?> productCreate(ProductSaveReqDto createDto){
        Product product = productService.productAwsCreate(createDto); // s3 쓸 때는 productAwsCreate
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "상품이 성공적으로 등록 되었습니다.", product.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> productList(ProductSearchDto searchDto, Pageable pageable){
        Page<ProductResDto> listDto = productService.productList(searchDto, pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "상품 조회 완료 !", listDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

}
