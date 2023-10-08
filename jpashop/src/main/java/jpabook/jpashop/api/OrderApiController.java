package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * V1. 엔티티 직접 노출
 * - 엔티티가 변하면 API 스펙이 변한다.
 * - 트랜잭션 안에서 지연 로딩 필요
 * - 양방향 연관관계 문제
 *
 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 * - 트랜잭션 안에서 지연 로딩 필요
 * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가능)
 *
 * V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
 * - 페이징 가능
 * V5. JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
 * - 페이징 가능
 * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 * - 페이징 불가능...
 *
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {


    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;


    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기환
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제 초기화
        }
        return all;
    }
/*
    orderItem , item 관계를 직접 초기화하면 Hibernate5Module 설정에 의해 엔티티를 JSON으로 생성
    한다.
    양방향 연관관계면 무한 루프에 걸리지 않게 한곳에 @JsonIgnore 를 추가해야 한다.
    엔티티를 직접 노출하므로 좋은 방법은 아니다.
*/

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }
/*
    지연 로딩으로 너무 많은 SQL 실행
    SQL 실행 수
    order 1번
    member , address N번(order 조회 수 만큼)
    orderItem N번(order 조회 수 만큼)
    item N번(orderItem 조회 수 만큼)
    참고: 지연 로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고 없으면 SQL을 실행한다.
    따라서 같은 영속성 컨텍스트에서 이미 로딩한 회원 엔티티를 추가로 조회하면 SQL을 실행하지 않는다.
*/

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }
/*
페치 조인으로 SQL이 1번만 실행됨
distinct 를 사용한 이유는 1대다 조인이 있으므로 데이터베이스 row가 증가한다.
그 결과 같은 order 엔티티의 조회 수도 증가하게 된다. JPA의 distinct는 SQL에 distinct를 추가하고,
더해서 같은 엔티티가 조회되면,
애플리케이션에서 중복을 걸러준다. 이 예에서 order가 컬렉션 페치 조인 때문에 중복 조회 되는 것을 막아준다.

단점: 페이징 불가능

참고: 컬렉션 페치 조인을 사용하면 페이징이 불가능하다. 하이버네이트는 경고 로그를 남기면서 모든 데이터를
DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험하다).
자세한 내용은 자바 ORM 표준 JPA프로그래밍의 페치 조인 부분을 참고하자.

참고: 컬렉션 페치 조인은 1개만 사용할 수 있다. 컬렉션 둘 이상에 페치 조인을 사용하면 안된다. 데이터가
부정합하게 조회될 수 있다. 자세한 내용은 자바 ORM 표준 JPA 프로그래밍을 참고하자.
*/


    /**
     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     * - ToOne 관계만 우선 모두 페치 조인으로 최적화
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit)
    {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

/*
    장점: 쿼리 호출 수가 1 + N 1 + 1 로 최적화 된다.
    조인보다 DB 데이터 전송량이 최적화 된다.
    (Order와 OrderItem을 조인하면 Order가 OrderItem 만큼 중복해서 조회된다.
    이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없다.)
    페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소한다.
    컬렉션 페치 조인은 페이징이 불가능 하지만 이 방법은 페이징이 가능하다.
    결론: ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다.
    따라서 ToOne 관계는 페치조인으로 쿼리 수를 줄이고 해결하고,
    나머지는 hibernate.default_batch_fetch_size 로 최적화 하자.

    참고: default_batch_fetch_size 의 크기는 적당한 사이즈를 골라야 하는데, 100~1000 사이를 선택하는 것을 권장한다.
    이 전략을 SQL IN 절을 사용하는데, 데이터베이스에 따라 IN 절 파라미터를 1000으로 제한하기도 한다.
    1000으로 잡으면 한번에 1000개를 DB에서 애플리케이션에 불러오므로 DB에 순간부하가 증가할 수 있다.
    하지만 애플리케이션은 100이든 1000이든 결국 전체 데이터를 로딩해야 하므로 메모리 사용량이 같다.
    1000으로 설정하는 것이 성능상 가장 좋지만, 결국 DB든 애플리케이션이든 순간 부하를 어디까지 견딜 수 있는지로 결정하면 된다.
*/

   /**
    V4 JPA에서 DTO 직접 조회
    **/
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }
/*
    Query: 루트 1번, 컬렉션 N 번 실행
    ToOne(N:1, 1:1) 관계들을 먼저 조회하고, ToMany(1:N) 관계는 각각 별도로 처리한다.
    이런 방식을 선택한 이유는 다음과 같다.
    ToOne 관계는 조인해도 데이터 row 수가 증가하지 않는다.
    ToMany(1:N) 관계는 조인하면 row 수가 증가한다.
    row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화 하기 쉬우므로 한번에 조회하고, ToMany 관계
    는 최적화 하기 어려우므로 findOrderItems() 같은 별도의 메서드로 조회한다.
*/

    /**
     V5: JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
     **/
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }
/*
    Query: 루트 1번, 컬렉션 1번
    ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId로 ToMany 관계인 OrderItem 을 한꺼번
    에 조회
    MAP을 사용해서 매칭 성능 향상(O(1))
*/



    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }
    @Data
    static class OrderItemDto {
        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count; //주문 수량
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
















}
