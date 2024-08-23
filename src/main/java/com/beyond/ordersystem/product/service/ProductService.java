package com.beyond.ordersystem.product.service;

import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSaveReqDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService{

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final StockInventoryService stockInventoryService;

    @Autowired
    public ProductService(ProductRepository productRepository, S3Client s3Client, StockInventoryService stockInventoryService) {
        this.productRepository = productRepository;
        this.s3Client = s3Client;
        this.stockInventoryService = stockInventoryService;
    }

    public Product productCreate(ProductSaveReqDto createDto){
        MultipartFile image = createDto.getProductImage();
        Product product = null;
        try {
            product = productRepository.save(createDto.toEntity());
            byte[] bytes = image.getBytes();
//            Path path = Paths.get("/Users/tteia/Desktop/tmp/", UUID.randomUUID() + "_" + image.getOriginalFilename());  // UUID = 난수 값. 파일명이 겹치지 않게 해줌.
            Path path = Paths.get("/Users/tteia/Desktop/tmp/", product.getId() + "_" + image.getOriginalFilename());
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            product.updateImagePath(path.toString());

            if(createDto.getName().contains("sale")){
                stockInventoryService.increaseStock(product.getId(), createDto.getStockQuantity());
            }
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패 !"); // 트랜잭션 처리를 위해 예외 잡아주기
        }
        return product;
    }

    public Page<ProductResDto> productList(ProductSearchDto searchDto, Pageable pageable){
        // 검색을 위해 Specification 객체를 사용함.
        // 복잡한 쿼리를 명세를 이용해 정의하는 방식으로, 쿼리를 쉽게 생성하게 해준다.
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(searchDto.getSearchName() != null){
                    // root : DB Column 조회. Entity 의 속성에 접근하기 위한 객체.
                    // CriteriaBuilder : 쿼리를 생성하기 위한 객체.
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + searchDto.getSearchName() + "%"));
                }
                if(searchDto.getCategory() != null){
                    predicates.add(criteriaBuilder.like(root.get("category"), "%" + searchDto.getCategory() + "%"));
                }
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i = 0; i < predicateArr.length; i++){
                    predicateArr[i] = predicates.get(i);
                    // 위 2개의 쿼리 조건문을 and 조건으로 연결.
                    // 우리는 지금 name 검색 / category 검색 나눠놔서 독립적이지만, and 로 엮여도 상관 없다.
                    // 추후 and 가 필요할 수 있으니 미리 작성하였음.
                }Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> products = productRepository.findAll(specification, pageable);
        Page<ProductResDto> resDtos = products.map(a->a.fromEntity());
        return resDtos;

    }

    public Product productAwsCreate(ProductSaveReqDto createDto){
        MultipartFile image = createDto.getProductImage();
        Product product = null;
        try {
            product = productRepository.save(createDto.toEntity());
            byte[] bytes = image.getBytes();
            String fileName = product.getId() + "_" + image.getOriginalFilename();
            Path path = Paths.get("/Users/tteia/Desktop/tmp/", fileName);

            // local pc 에 임시 저장.
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            // aws 에 pc 저장 파일을 업로드.
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();                                                                      // 실습 시 확인을 위해 .fromFile 해줬는데 .fromByte 로 바로 해줘도 됨 !
            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
            String s3Path = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm(); // 우리 파일 이름이 아니라 s3 관련 이름으로 나옴.
            product.updateImagePath(s3Path);
            // https://tteia-file.s3.ap-northeast-2.amazonaws.com/A1.png
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패 !"); // 트랜잭션 처리를 위해 예외 잡아주기
        }
        return product;
    }
}
