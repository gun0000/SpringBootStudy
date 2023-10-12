package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    List<Member> findTop3HelloBy();

    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m from Member m where m.username = :name")
    Member findMembers(@Param("name") String username);

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    List<Member> findListMemberByUsername(String name); //컬렉션
    Member findMemberByUsername(String name); //단건
    Optional<Member> findOptionalByUsername(String name); //단건 Optional
    //컬렉션
    //결과 없음: 빈 컬렉션 반환
    //단건 조회
    //결과 없음: null 반환
    //결과가 2건 이상: javax.persistence.NonUniqueResultException 예외 발생
/*
    Page<Member> findByUsername(String name, Pageable pageable); //count 쿼리 사용
    Slice<Member> findByUsername(String name, Pageable pageable); //count 쿼리 사용 안함
    List<Member> findByUsername(String name, Pageable pageable); //count 쿼리 사용 안함
    List<Member> findByUsername(String name, Sort sort);
*/

    Page<Member> findByAge(int age, Pageable pageable);

    //count 쿼리를 다음과 같이 분리할 수 있음
    @Query(value = "select m from Member m",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findMemberAllCountBy(Pageable pageable);


    @Modifying(clearAutomatically = true)//벌크성 쿼리를 실행하고 나서 영속성 컨텍스트 초기화
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);


    //JPQL 페치 조인 되도록 그냥 이걸쓰자
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    //공통 메서드 오버라이드
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();
    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();
    //메서드 이름으로 쿼리에서 특히 편리하다.
    //@EntityGraph(attributePaths = {"team"})
    //List<Member> findByUsername(String username);

    //EntityGraph 정리
    //사실상 페치 조인(FETCH JOIN)의 간편 버전
    //LEFT OUTER JOIN 사용


    //JPA 쿼리 힌트(SQL 힌트가 아니라 JPA 구현체에게 제공하는 힌트)
    //조회만 사용하기(Read Only)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    //Look
    @Lock(LockModeType.PESSIMISTIC_WRITE) //비관적 락
    List<Member> findLockByUsername(String name);
    /*
    A라는 사람이 게시 글 조회수에 접근하고 있는 상황(Update까지 끝내기 전)에서는 B라는 사람이 게시 글 조회수에 접근하지 못하도록 막는 것이다.
    A라는 사람이 조회수에 접근할 때, 문을 걸어잠갔다고하여 "락"이라고 표현한다.

    낙관적 락(Optimistic Lock)이란?
        데이터 갱신 시, 총돌이 발생하지 않을 것이라는 가정을 두고 진행하는 락 기법이다.
        걸어잠그어 접근을 못하게 하기 보다는 충돌을 방지하기 위한 방법이다.

        낙관적 락 문제
            낙관적 락과 같은 경우는 1000개의 요청이 존재한다면, 가장 처음의 1개는 적용되고 나머지 999개는 버전 변경이 맞지않아서 롤백된다. 그만큼 자원 소모가 발생한다.
            모든 작업이 수행되고, commit 하는 시점에 충돌 여부를 알 수 있기 때문에 느리게 될 경우가 존재한다.

    비관적 락(Pessimistic Lock)이란?
        데이터 갱신 시, 충돌이 발생할 것이라는 가정을 두고 진행하는 락 기법이다.

        비관적 락 문제
            레코드 자체에 락을 걸기 때문에 줄을 서야한다. 그만큼 병목현상이 생길 수 있다.
    */


}
