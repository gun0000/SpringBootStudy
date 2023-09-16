package hello.hellospring;
import hello.hellospring.repository.JpaMemberRepository;
import hello.hellospring.repository.MemberRepository;
import hello.hellospring.repository.MemoryMemberRepository;
import hello.hellospring.service.MemberService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class SpringConfig {
/*
    private final EntityManager em;
    public SpringConfig(EntityManager em) {
        this.em = em;
    }
    @Bean
    public MemberRepository memberRepository() {
        //return new MemoryMemberRepository(); //메모리
        return new JpaMemberRepository(em); //JPA
    }
 */
    //스프링 데이터 JPA
    private final MemberRepository memberRepository;
    @Autowired
    public SpringConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository);
    }

}