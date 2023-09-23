package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable //어딘가에 저장될수 있다
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;
}
