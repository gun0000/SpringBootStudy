package hellojpa;

import javax.persistence.Embeddable;

@Embeddable
public class Address {
    private String city;
    private String street;
    private String zipcode;

    public Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    private void setCity(String city) { // *** 접근제어자를 private로 변경 또는 삭제
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    private void setStreet(String street) { // *** 접근제어자를 private로 변경 또는 삭제
        this.street = street;
    }

    public String getZipcode() {
        return zipcode;
    }

    private void setZipcode(String zipcode) { // *** 접근제어자를 private로 변경 또는 삭제
        this.zipcode = zipcode;
    }
}
