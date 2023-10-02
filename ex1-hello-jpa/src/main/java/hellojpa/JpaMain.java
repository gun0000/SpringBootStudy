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


        Address address = new Address("OldCity", "street", "10000");

        Member member1 = new Member();
        Member member2 = new Member();

        member1.setUsername("member1");
        member1.setHomeAddress(address);


        em.persist(member1);

        //member1.getHomeAddress().setCity("NewCity"); //원천차단
        Address newAddress = new Address("NewCity", address.getStreet(), address.getZipcode());
        member1.setHomeAddress(newAddress);

        //불변 객체지만 실제로 값을 바꾸고 싶은 경우#
        //JpaMain.java - 새로 Address를 만들고 변경할 값을 넣고 복사할 값은 address.getZipcode() 등 getter 메소드로 가져와 사용
/*
* 객체 타입의 한계#
    항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수 있다.
    문제는 임베디드 타입처럼 직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입이다. 자바 기본타입(primitive type)은 대입하면 항상 복사되기 때문에 공유하여 사용할 수 없습니다.
    자바의 기본타입에 값을 대입하면 값을 복사한다.
    객체 타입은 참조 값(reference)을 직접 대입하는 것을 막을 방법이 없다.
    객체의 공유 참조는 피할 수 없다.
*
* 그래서 객체타입을 수정할 수 없게 만들면 부작용을 막을 수 있습니다. setter 지우기 생성자로 만들기
*
*
*
*
* 불변 객체#
    객체 타입을 수정할 수 없게 만들면 부작용을 원천 차단
    값 타입은 불변 객체(immutable object)로 설계해야함
    불변 객체 : 생성 시점 이후 절대 값을 변경할 수 없는 객체
    불변 객체로 만드는 방법
    생성자로만 값을 설정하고 수정자(setter)를 만들지 않음
    setter를 private로 생성하여 외부에서 접근을 막음
    속성에 final 키워드 추가
    객체 필드 참조 초기화
    unmodifiableList 사용
    참고 : Integer, String은 자바가 제공하는 대표적인 불변 객체
    *
    *
    * 정리 : 값 타입은 꼭 불변 객체로 만들어 사용해야 부작용을 겪지 않을 수 있습니다.
* */

    }


}
