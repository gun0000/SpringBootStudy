package hellojpa;
import javax.persistence.*;

@Entity
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @ManyToOne(fetch = FetchType.EAGER) //(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    // 주소
    //private String city;
    //private String street;
    //private String zipcode;
    @Embedded
    private Address homeAddress;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

/*
        * 데이터베이스 스키마 자동 생성 - 속성
    hibernate.hbm2ddl.auto
    옵션 설명
    create 기존테이블 삭제 후 다시 생성 (DROP + CREATE)
    create-drop create와 같으나 종료시점에 테이블 DROP
    update 변경분만 반영(운영DB에는 사용하면 안됨)
    validate 엔티티와 테이블이 정상 매핑되었는지만 확인
    none 사용하지 않음
    *
        * 데이터베이스 스키마 자동 생성 - 속성
    hibernate.hbm2ddl.auto
    옵션 설명
    create 기존테이블 삭제 후 다시 생성 (DROP + CREATE)
    create-drop create와 같으나 종료시점에 테이블 DROP
    update 변경분만 반영(운영DB에는 사용하면 안됨)
    validate 엔티티와 테이블이 정상 매핑되었는지만 확인
    none 사용하지 않음
    * */

    /*
    * @Id
     private Long id;
     @Column(name = "name")
     private String username;
     private Integer age;
     @Enumerated(EnumType.STRING)
     private RoleType roleType;
     @Temporal(TemporalType.TIMESTAMP)
     private Date createdDate;
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastModifiedDate;
     @Lob
     private String description;
     *
     * 매핑 어노테이션 정리
    hibernate.hbm2ddl.auto
    어노테이션 설명
    @Column 컬럼 매핑
    @Temporal 날짜 타입 매핑
    @Enumerated
    enum 타입 매핑
    @Lob BLOB, CLOB 매핑
    @Transient 특정 필드를 컬럼에 매핑하지 않음(매핑 무시)
    * */
}