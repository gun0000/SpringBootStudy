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
        //팀 저장
        Team team = new Team();
        team.setName("TeamA");
        em.persist(team);
        //회원 저장
        Member member = new Member();
        member.setName("member1");
        member.setTeam(team); //단방향 연관관계 설정, 참조 저장
        em.persist(member);

        transaction.commit(); // [트랜잭션] 커밋
    }


}
