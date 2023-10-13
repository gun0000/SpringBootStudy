package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    //도메인 클래스 컨버터 사용 전
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }
    //도메인 클래스 컨버터 사용 후
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Member member) {
        return member.getUsername();
    }
    /*
    HTTP 요청은 회원 id 를 받지만 도메인 클래스 컨버터가 중간에 동작해서 회원 엔티티 객체를 반환
    도메인 클래스 컨버터도 리파지토리를 사용해서 엔티티를 찾음

    주의: 도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 이 엔티티는 단순 조회용으로만 사용해야 한다.
    (트랜잭션이 없는 범위에서 엔티티를 조회했으므로, 엔티티를 변경해도 DB에 반영되지 않는다.)
    권장하지않음
     */

    //페이징과정렬
    @GetMapping("/members")
    public Page<Member> list(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return page;
    }
    /*
    파라미터로 Pageable 을 받을 수 있다.
    Pageable 은 인터페이스, 실제는 org.springframework.data.domain.PageRequest 객체 생성

    요청 파라미터
        예) /members?page=0&size=3&sort=id,desc&sort=username,desc
        page: 현재 페이지, 0부터 시작한다.
        size: 한 페이지에 노출할 데이터 건수
        sort: 정렬 조건을 정의한다. 예) 정렬 속성,정렬 속성...(ASC | DESC), 정렬 방향을 변경하고 싶으면 sort
        파라미터 추가 ( asc 생략 가능)
    기본값
        글로벌 설정: 스프링 부트
        spring.data.web.pageable.default-page-size=20 /# 기본 페이지 사이즈/
        spring.data.web.pageable.max-page-size=2000 /# 최대 페이지 사이즈/
     */
    @RequestMapping(value = "/members_page", method = RequestMethod.GET)
    public String list2(@PageableDefault(size = 12, sort = "username", direction = Sort.Direction.DESC)
                           Pageable pageable) {
        //,,,
        return "";
    }

    //Page 내용을 DTO로 변환하기
    /*
    엔티티를 API로 노출하면 다양한 문제가 발생한다. 그래서 엔티티를 꼭 DTO로 변환해서 반환해야 한다.
    Page는 map() 을 지원해서 내부 데이터를 다른 것으로 변경할 수 있다
     */
    @GetMapping("/members")
    public Page<MemberDto> list3(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> pageDto = page.map(MemberDto::new);
        return pageDto;
    }

    @GetMapping("/members")
    public Page<MemberDto> list4(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberDto::new);
    }


}