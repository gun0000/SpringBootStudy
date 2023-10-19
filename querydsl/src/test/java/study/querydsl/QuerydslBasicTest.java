package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.*; //QMember qMember = QMember.member; //기본 인스턴스 사용
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @PersistenceContext
    EntityManager em;
    JPAQueryFactory queryFactory;
    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        //초기화
        em.flush();
        em.clear();
    }
    @Test
    public void startQuerydsl2() {
        //member1을 찾아라.
        QMember m = new QMember("m");
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


/*
    Q클래스 인스턴스를 사용하는 2가지 방법
    QMember qMember = new QMember("m"); //별칭 직접 지정
     참고: 같은 테이블을 조인해야 하는 경우가 아니면 기본 인스턴스를 사용하자
    QMember qMember = QMember.member; //기본 인스턴스 사용
    기본 인스턴스를 static import와 함께 사용
*/
    @Test
    public void startQuerydsl3() {
        //QMember qMember = QMember.member; //기본 인스턴스 사용 //기본 인스턴스를 static import와 함께 사용
        //member1을 찾아라.
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /*
        검색 조건 쿼리
        검색 조건은 .and() , . or() 를 메서드 체인으로 연결할 수 있다.
        참고: select , from 을 selectFrom 으로 합칠 수 있음
     */
    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    //AND 조건을 파라미터로 처리
    @Test
    public void searchAndParam() {
        List<Member> result1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"), member.age.eq(10))
                .fetch();
        assertThat(result1.size()).isEqualTo(1);
    }
    //where() 에 파라미터로 검색조건을 추가하면 AND 조건이 추가됨
    //이 경우 null 값은 무시 메서드 추출을 활용해서 동적 쿼리를 깔끔하게 만들 수 있음 뒤에서 설명
    /*
    member.username.eq("member1") // username = 'member1'
    member.username.ne("member1") //username != 'member1'
    member.username.eq("member1").not() // username != 'member1'
    member.username.isNotNull() //이름이 is not null
    member.age.in(10, 20) // age in (10,20)
    member.age.notIn(10, 20) // age not in (10, 20)
    member.age.between(10,30) //between 10, 30
    member.age.goe(30) // age >= 30
    member.age.gt(30) // age > 30
    member.age.loe(30) // age <= 30
    member.age.lt(30) // age < 30
    member.username.like("member%") //like 검색
    member.username.contains("member") // like ‘%member%’ 검색
    member.username.startsWith("member") //like ‘member%’ 검색
...
     */

    //결과 조회
    @Test
    public void resultFetchTest(){

        //List
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        /*
            fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환

            select member1 from Member member1
            [Member(id=1, username=member1, age=10),
            Member(id=2, username=member2, age=20),
            Member(id=3, username=member3, age=30),
            Member(id=4, username=member4, age=40)]
         */

        //단건
//        Member findMember1 = queryFactory
//                .selectFrom(member)
//                .fetchOne();
        //fetchOne() : 단 건 조회
        //결과가 없으면 : null
        //결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException

        //처음 한 건 조회 fetchFirst() : limit(1).fetchOne()
        Member findMember2 = queryFactory
                .selectFrom(member)
                .fetchFirst();
        /*
            Member(id=1, username=member1, age=10)
        */

        //페이징에서 사용 fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        /*
            results
                com.querydsl.core.QueryResults@62d4dac42023
            results.getResults()
                [Member(id=1, username=member1, age=10),
                 Member(id=2, username=member2, age=20),
                 Member(id=3, username=member3, age=30),
                 Member(id=4, username=member4, age=40)]
            results.getTotal()
                 4개
        */

        //count 쿼리로 변경 fetchCount() : count 쿼리로 변경해서 count 수 조회
        long count = queryFactory
                .selectFrom(member)
                .fetchCount();
        /*
            count = 4 개
        */
    }
    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
        /*
            result

            desc() , asc() : 일반 정렬
            nullsLast() , nullsFirst() : null 데이터 순서 부여

            [Member(id=6, username=member5, age=100),
            Member(id=7, username=member6, age=100),
            Member(id=5, username=null, age=100)]
         */
    }


    /*
        페이징
        조회 건수 제한
     */
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetch();
        assertThat(result.size()).isEqualTo(2);
        //[Member(id=3, username=member3, age=30), Member(id=2, username=member2, age=20)]
    }

    //전체 조회 수가 필요하면?
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
        /*
            Total 4
            Limit 2
            Offset 1
            Results().size() 2
         */
        //com.querydsl.core.QueryResults@1a345c6c
    }
    /*
        주의: count 쿼리가 실행되니 성능상 주의!
        참고: 실무에서 페이징 쿼리를 작성할 때, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만,
        count 쿼리는 조인이 필요 없는 경우도 있다. 그런데 이렇게 자동화된 count 쿼리는 원본 쿼리와 같이 모두
        조인을 해버리기 때문에 성능이 안나올 수 있다. count 쿼리에 조인이 필요없는 성능 최적화가 필요하다면,
        count 전용 쿼리를 별도로 작성해야 한다.
     */


    //집합 함수

    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이
     * from Member m
     */

    @Test
    public void aggregation() throws Exception{
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        /*
            result  [[4, 100, 25.0, 40, 10]]
            result.get(0) [4, 100, 25.0, 40, 10]
            count  4
            sum 100
            avg 25
            max 40
            min 10
            JPQL이 제공하는 모든 집합 함수를 제공한다.
            tuple은 프로젝션과 결과반환에서 설명한다.
            데이터 타입이 여러개 들어올때
            멤버 단일 타입을 조회하는 게 아니라
            그럴 때는 Tuple을 쓴다
            실무에서는 Tuple을 많이 쓰진 않고 DTO로 직접 뽑아온다
         */
    }
    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }
    /*
    result          [[teamA, 15.0], [teamB, 35.0]]
    result.get(0)   [teamA, 15.0]
    result.get(1)   [teamB, 35.0]
    groupBy , 그룹화된 결과를 제한하려면 having

    groupBy(), having() 예시
    .groupBy(item.price)
    .having(item.price.gt(1000)
     */



}
