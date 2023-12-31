package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
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


    /*
        조인 - 기본 조인
        기본 조인
        조인의 기본 문법은 첫 번째 파라미터에 조인 대상을 지정하고, 두 번째 파라미터에 별칭(alias)으로 사용할
        Q 타입을 지정하면 된다.

        join(조인 대상, 별칭으로 사용할 Q타입)
     */
    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() throws Exception {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        assertThat(result)
                .extracting("username")//List<Member> 형태의 result에서 username 필드값만 추출
                .containsExactly("member1", "member2");//추출된 username 값들이 정확히 "member1", "member2"인지 확인
        //result        [Member(id=1, username=member1, age=10), Member(id=2, username=member2, age=20)]
        //result.get(0) Member(id=1, username=member1, age=10)
    /*
        join() , innerJoin() : 내부 조인(inner join)
        leftJoin() : left 외부 조인(left outer join)
        rightJoin() : rigth 외부 조인(rigth outer join)
        JPQL의 on 과 성능 최적화를 위한 fetch 조인 제공 다음 on 절에서 설명
     */
    }


    /**
     * 세타 조인
     * 연관관계가 없는 필드로 조인
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }
    /*
        from 절에 여러 엔티티를 선택해서 세타 조인
        외부 조인 불가능 다음에 설명할 조인 on을 사용하면 외부 조인 가능
    */





    /*
        조인 - on절
        ON절을 활용한 조인(JPA 2.1부터 지원)
        1. 조인 대상 필터링
        2. 연관관계 없는 엔티티 외부 조인
        1. 조인 대상 필터링
        예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     */
    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and
     t.name='teamA'
     */
    @Test
    public void join_on_filtering() throws Exception {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }
    /*
        t=[Member(id=3, username=member1, age=10), Team(id=1, name=teamA)]
        t=[Member(id=4, username=member2, age=20), Team(id=1, name=teamA)]
        t=[Member(id=5, username=member3, age=30), null]
        t=[Member(id=6, username=member4, age=40), null]

        참고: on 절을 활용해 조인 대상을 필터링 할 때, 외부조인이 아니라 내부조인(inner join)을 사용하면,
        where 절에서 필터링 하는 것과 기능이 동일하다. 따라서 on 절을 활용한 조인 대상 필터링을 사용할 때,
        내부조인 이면 익숙한 where 절로 해결하고, 정말 외부조인이 필요한 경우에만 이 기능을 사용하자.

     */
    /*
        2. 연관관계 없는 엔티티 외부 조인
        예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     */
    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }
    }
    /*
        결과
        t=[Member(id=3, username=member1, age=10), null]
        t=[Member(id=4, username=member2, age=20), null]
        t=[Member(id=5, username=member3, age=30), null]
        t=[Member(id=6, username=member4, age=40), null]
        t=[Member(id=7, username=teamA, age=0), Team(id=1, name=teamA)]
        t=[Member(id=8, username=teamB, age=0), Team(id=2, name=teamB)]

        하이버네이트 5.1부터 on 을 사용해서 서로 관계가 없는 필드로 외부 조인하는 기능이 추가되었다. 물론 내부 조인도 가능하다.
        주의! 문법을 잘 봐야 한다. leftJoin() 부분에 일반 조인과 다르게 엔티티 하나만 들어간다.
        일반조인: leftJoin(member.team, team)
        on조인: from(member).leftJoin(team).on(xxx)
     */


/*
    조인 - 페치 조인
    페치 조인은 SQL에서 제공하는 기능은 아니다. SQL조인을 활용해서 연관된 엔티티를 SQL 한번에 조회하
    는 기능이다. 주로 성능 최적화에 사용하는 방법이다.
    페치 조인 미적용
    지연로딩으로 Member, Team SQL 쿼리 각각 실행

 */
    @PersistenceUnit
    EntityManagerFactory emf;
    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }
    /*
        페치 조인 적용
        즉시로딩으로 Member, Team SQL 쿼리 조인으로 한번에 조회
    */

    @Test
    public void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }
    /*
        사용방법 join(), leftJoin() 등 조인 기능 뒤에 fetchJoin() 이라고 추가하면 된다.
     */





/*
    서브 쿼리
    com.querydsl.jpa.JPAExpressions 사용
    서브 쿼리 eq 사용
*/
    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(40);
        //[Member(id=4, username=member4, age=40)]
    }


    /**
     * 서브 쿼리 goe 사용
     * 나이가 평균 나이 이상인 회원
     */
    @Test
    public void subQueryGoe() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(30,40);

        //[Member(id=3, username=member3, age=30), Member(id=4, username=member4, age=40)]
    }



    /**
     * 서브쿼리 여러 건 처리, in 사용
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
        System.out.printf("asdasdasd"+result);
        //[Member(id=2, username=member2, age=20), Member(id=3, username=member3, age=30), Member(id=4, username=member4, age=40)]

        //select 절에 subquery
        List<Tuple> fetch = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(memberSub.age.avg())
                            .from(memberSub)));
        }

    }

    /*
    static import 활용
    import static com.querydsl.jpa.JPAExpressions.select;
        List<Member> result = queryFactory
         .selectFrom(member)
         .where(member.age.eq(
         select(memberSub.age.max())
         .from(memberSub)
         ))
         .fetch();
     */
    /*
        from 절의 서브쿼리 한계
            JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl도 지원하지 않는다.
            하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다.
            Querydsl도 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.
            from 절의 서브쿼리 해결방안
            1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
            2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
            3. nativeSQL을 사용한다.
     */

/*
    Case 문
    select, 조건절(where), order by에서 사용 가능
*/
    @Test
    public void 단순한조건() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();
        //[열살, 스무살, 기타, 기타]

    }
    @Test
    public void 복잡한조건 (){
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타")
                        )
                .from(member)
                .fetch();
        //[0~20살, 0~20살, 21~30살, 기타]
    }
    /*
        orderBy에서 Case 문 함께 사용하기 예제
        예를 들어서 다음과 같은 임의의 순서로 회원을 출력하고 싶다면?
        1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력
        2. 0 ~ 20살 회원 출력
        3. 21 ~ 30살 회원 출력
     */
    @Test
    public  void 복잡한조건2(){
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> result = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = " + rank);
        }
       //[[member4, 40, 3], [member1, 10, 2], [member2, 20, 2], [member3, 30, 1]]
    }
    /*
            상수문자더하기
     */
    @Test
    public void 상수문자더하기(){
        Tuple result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetchFirst();
        System.out.println("asdasd"+result);
        //[member1, A]
        /*
           위와 같이 최적화가 가능하면 SQL에 constant 값을 넘기지 않는다. 상수를 더하는 것 처럼 최적화가
           어려우면 SQL에 constant 값을 넘긴다.
         */
    }
    @Test
    public void 문자더하기(){
        String result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        System.out.println("asdz"+result);
        //member1_10
        /*
           member.age.stringValue() 부분이 중요한데, 문자가 아닌 다른 타입들은 stringValue() 로
           문자로 변환할 수 있다. 이 방법은 ENUM을 처리할 때도 자주 사용한다.
         */
    }


/*
중급 문법
프로젝션과 결과 반환 - 기본
*/


    /*
        프로젝션: select 대상 지정
     */
    @Test
    public void 반환타입하나지정(){
        //String
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        //프로젝션 대상이 하나면 타입을 명확하게 지정할 수 있음
    }
    //프로젝션 대상이 둘 이상이면 튜플이나 DTO로 조회
    //튜플 조회
    //프로젝션 대상이 둘 이상일 때 사용
    @Test
    public void 반환타입여러개지정(){
        //Tuple
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username=" + username);
            System.out.println("age=" + age);
        }
    }
    //Tuple은 다른 계층에서까지 쓰기는 안좋기 때문에 레파지토리 내에서 쓰거나 DTO방식을 쓰는게 좋다





}
