package hello.core;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.FixDiscountPolicy;
import hello.core.discount.RateDiscountPolicy;
import hello.core.member.MemberRepository;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import hello.core.member.MemoryMemberRepository;
import hello.core.order.OrderService;
import hello.core.order.OrderServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*@Configuration의 역할
- Bean을 등록할 때 싱글톤(singleton)이 되도록 보장해준다.

- 스프링 컨테이너에서 Bean을 관리할 수 있게 됨.
크게 고민할 것이 없다. 스프링 설정 정보는 항상 @Configuration 을 사용하자
*/
@Configuration
public class AppConfig {

    //회원서비스
    @Bean //스프링 컨테이너에 등록
    public MemberService memberService(){
        return new MemberServiceImpl(memberRepository());
    }
    @Bean
    //회원저장소
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
    @Bean
    //주문서비스
    public OrderService orderService(){
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }
    @Bean
    //할인정책
    public DiscountPolicy discountPolicy(){
        //return new FixDiscountPolicy();
        return new RateDiscountPolicy();
    }


}
