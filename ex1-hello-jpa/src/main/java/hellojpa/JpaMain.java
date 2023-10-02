package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();


        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("member1");
        member.setTeam(team);
        em.persist(member);

        em.flush();
        em.clear();

        Member findMember = em.find(Member.class, member.getId());

//        EAGER는 사전적 의미인 열심인, 열렬한 처럼 Member를 조회하면 연관관계에 있는
//        Team 역시 함께 조회는 반면에, LAZY는 게을러서 Member만 조회해오고 연관관계에 있는
//        나머지 데이터는 조회를 미룬다.

        /*
        * 비지니스 로직 상 Member 데이터가 필요한 곳에 대부분 Team의 데이터 역시 같이 사용 할 필요가 있다면 어떨까? FetchType을 EAGER로 설정하여 항상 Member와 Team을 같이 조회해오는 것이 더 좋을 것이다.

           Member를 사용하는 곳 대부분에서 Team 데이터가 필요하지 않다면? FetchType을 LAZY로 설정하여 Member만 조회하고, 드물게 Team이 필요할 땐 그 때 Team에 대한 쿼리를 한번 더 날려 조회하는것이 좋을 것이다.

           하지만 위에서 언급했다시피 이 부분은 지극히 이론적인 설명이며, 실무에서는 EAGER LOADING을 사용하지 않는 것을 권장한다.
        * */
    }


}
