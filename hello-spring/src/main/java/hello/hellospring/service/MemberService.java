package hello.hellospring.service;

import hello.hellospring.domain.Member;
import hello.hellospring.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /*
    * 회원가입
    */
    public Long join(Member member){
        // 입력받은 회원이 이미 존재하는지 검증. 만약 이미 존재한다면 예외를 발생시킴.
        validateDuplicateMember(member); //중복 회원 검증
        // 중복 회원이 없다면, 저장소에 회원 정보를 저장.
        memberRepository.save(member);
        // 저장된 회원의 ID 반환.
        return member.getId();

    }
    /*
     * 중복 회원 검증 메서드
     */
    private void validateDuplicateMember(Member member){
        /* 저장소에서 같은 이름을 가진 멤버가 있는지 찾아봄.
            만약 있다면(즉, Optional 객체가 값을 가지고 있다면),
            IllegalStateException을 발생시키고 "이미 존재하는 회원입니다."라는 메시지와 함께 예외 처리.*/
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 회원입니다.");
                });
    }

    /**
     * 전체 회원 조회
     */
    public List<Member> findMembers() {
        /* 저장소에 있는 모든 멤버 정보를 리스트로 가져와 반환함.
          이 때, 리스트의 순서는 특정하지 않음 (즉, 어떠한 순서도 보장되지 않음). */
        return memberRepository.findAll();
    }
    /**
     * 특정 ID의 멤버 조회 메서드
     */
    public Optional<Member> findOne(Long memberId) {
        /* 주어진 ID와 일치하는 멤버를 찾아 반환함.
          만약 해당 ID를 가진 멤버가 없다면, 빈 Optional 객체 (Optional.empty()) 반환.*/
        return memberRepository.findById(memberId);
    }



}
