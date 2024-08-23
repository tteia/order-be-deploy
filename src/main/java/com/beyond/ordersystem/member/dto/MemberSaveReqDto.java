package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.common.domain.Address;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberSaveReqDto {
    private String name;
    @NotEmpty(message = "이메일을 작성해주세요. (필수)")
    private String email;
    @NotEmpty(message = "비밀번호를 작성해주세요. (필수)")
//    @Size(min = 8, message = "비밀번호는 8자 이상 작성해주세요.")
    // Size, NotEmpty => Valid 를 빌드에서 추가함으로써 쓰게 됨. Handler 에서 Valid 를 작성했기 때문 !
    private String password;
    private Address address;
    private Role role;

    public Member toEntity(String password){
        Member member = Member.builder()
                .name(this.name)
                .email(this.email)
                .password(password)
                .address(this.address)
                .role(this.role)
                .build();
        return member;
    }
}
