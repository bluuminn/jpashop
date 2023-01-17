package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class OrderServiceTest {
    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("상품 주문")
    void order() throws Exception {
        // given
        Member member = getMember();
        Book book = getBook("시골 JPA", 10_000, 10);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order foundOrder = orderRepository.findOne(orderId);

        assertThat(foundOrder.getStatus()).isSameAs(OrderStatus.ORDER);
        assertThat(foundOrder.getOrderItems().size()).isEqualTo(1);
        assertThat(foundOrder.getTotalPrice()).isEqualTo(book.getPrice() * orderCount);
        assertThat(book.getStockQuantity()).isEqualTo(8);
    }

    @Test
    @DisplayName("상품주문 재고수량초과")
    void order_over_quantity() throws Exception {
        // given
        Member member = getMember();
        Book book = getBook("시골 JPA", 10_000, 10);

        int orderCount = 11;

        // when, then
        assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);

        assertThatExceptionOfType(NotEnoughStockException.class)
                .isThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount));
    }

    @Test
    @DisplayName("주문 취소")
    void cancelOrder() throws Exception {
        // given
        Member member = getMember();
        Book book = getBook("시골 JPA", 10_000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // when - 테스트 하고 싶은 것
        orderService.cancelOrder(orderId);

        // then
        Order foundOrder = orderRepository.findOne(orderId);
        assertThat(foundOrder.getStatus()).isSameAs(OrderStatus.CANCEL);
        assertThat(book.getStockQuantity()).isEqualTo(10);
    }

    private Book getBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member getMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강남", "12345"));
        em.persist(member);
        return member;
    }

}