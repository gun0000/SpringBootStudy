package hello.core.order;
import hello.core.discount.DiscountPolicy;
import hello.core.member.Member;
import hello.core.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
//@RequiredArgsConstructor //lombok 라이브러리
public class OrderServiceImpl implements OrderService {

    //DIP를 지키고있음
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;
// lombok 롬복이 자바의 애노테이션 프로세서라는 기능을 이용해서 컴파일시점에 생성자 코드를 자동으로 생성해준다
    //생성자 주입 ,연결 -DI 의존성 주입
    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);
        return new Order(memberId, itemName, itemPrice, discountPrice);
    }

    //테스트 용도
    public MemberRepository getMemberRepository() {
        return memberRepository;
    }
}