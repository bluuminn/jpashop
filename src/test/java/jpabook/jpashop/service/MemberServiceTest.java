package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    // jpa에서는 같은 트랜잭션 안에서 같은 entity. pk 값이 똑같으면 같은 영속성 컨텍스트에서 똑같은애로 관리가 된다. 두개 세개 생기지 않고 딱 한개로만 관리가 된다.

    @DisplayName("회원가입")
    @Test
    void join() throws Exception {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        Long savedId = memberService.join(member);

        // then
        assertThat(member).isEqualTo(memberRepository.findOne(savedId));
    }

    @DisplayName("중복 회원이 있을 경우 예외가 발생한다.")
    @Test
    void validate_duplicate_member() throws Exception {
        // given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        // when
        memberService.join(member1);
        assertThatThrownBy(() -> {
            memberService.join(member2); // 예외가 발생해야 한다!
        }).isInstanceOf(IllegalStateException.class);

        // then
    }
}