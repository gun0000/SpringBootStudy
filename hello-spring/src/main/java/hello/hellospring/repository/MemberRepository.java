package hello.hellospring.repository;

import hello.hellospring.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id); //Java 8에서 도입된 Optional<T> 클래스는 null 값을 가질 수 있는 객체를 대상으로 사용할 수 있는 래퍼
    Optional<Member> findByName(String name);
    List<Member> findAll();
}
