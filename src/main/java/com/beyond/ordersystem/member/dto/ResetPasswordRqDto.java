package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRqDto {
    private String email;
    private String asIsPassword;
    private String toBePassword;
}
