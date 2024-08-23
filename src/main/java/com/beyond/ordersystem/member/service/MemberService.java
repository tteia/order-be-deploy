package com.beyond.ordersystem.member.service;

import com.beyond.ordersystem.member.dto.MemberLoginDto;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.member.dto.MemberSaveReqDto;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.ResetPasswordRqDto;
import com.beyond.ordersystem.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member memberCreate(MemberSaveReqDto createDto){
        if(memberRepository.findByEmail(createDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 Email 입니다.");
        }
        if (createDto.getPassword().length() < 8){
            throw new IllegalArgumentException("비밀번호의 길이가 짧습니다.");
        }
        Member member = createDto.toEntity(passwordEncoder.encode(createDto.getPassword()));
        Member success = memberRepository.save(member);
        return success;
    }

    public Page<MemberResDto> memberList(Pageable pageable){
        Page<Member> members = memberRepository.findAll(pageable);
        return members.map(a->a.fromEntity());
    }

    public MemberResDto myInfo(){
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("멤버를 찾을 수 없습니다."));
        return member.fromEntity();
    }

    public Member login(MemberLoginDto dto){
        // email 존재 여부
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 이메일입니다."));
        // password 일치 여부 => 들어온 dto 의 password 를 암호화해서 DB 에서 암호화되어 들어있던 비밀번호와 비교.
        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public void resetPassword(ResetPasswordRqDto resetDto) {
        Member member = memberRepository.findByEmail(resetDto.getEmail()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 이메일입니다."));
        if(!passwordEncoder.matches(resetDto.getAsIsPassword(), member.getPassword())){
            throw new IllegalArgumentException("사용하던 비밀번호를 확인 후 입력해주세요.");
        }
        if (resetDto.getToBePassword().length() < 8){
            throw new IllegalArgumentException("비밀번호의 길이가 짧습니다.");
        }
//        Member resetPasswordMember = resetDto.toEntity(passwordEncoder.encode(resetDto.getToBePassword()));
//        memberRepository.save(resetPasswordMember);
        member.updatePassword(passwordEncoder.encode(resetDto.getToBePassword()));
    }
}
