package hello.hellospring.repository;

import hello.hellospring.domain.Member;

import java.util.*;

public class MemoryMemberRepository implements MemberRepository{

    // 회원 정보를 저장할 Map. 키는 회원의 ID, 값은 Member 객체.
    private static Map<Long, Member> store = new HashMap<>();

    // 회원 ID 생성을 위한 sequence. 새로운 회원이 저장될 때마다 1씩 증가.
    private static long sequence = 0L;

    @Override
    public Member save(Member member) {
        // 새로운 회원의 ID 설정. 기존 sequence 값에 1을 더함.
        member.setId(++sequence);
        // Map에 새로운 회원 정보 저장. 키는 ID, 값은 Member 객체.
        store.put(member.getId(), member);
        return member; // 저장된 회원 반환
    }

    @Override
    public Optional<Member> findById(Long id) {
        // 주어진 id와 일치하는 회원 찾아서 반환.
        // 만약 해당 id를 가진 회원이 없다면 null을 감싸는 Optional 객체 반환 (즉, Optional.empty()).
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Member> findByName(String name) {
        /* store에서 모든 멤버를 가져와 스트림 생성 후,
            filter를 사용하여 이름이 입력값과 일치하는 멤버만 선택하고,
            그 중 아무 멤버나 선택(findAny).
            만약 일치하는 멤버가 없다면 null을 감싸는 Optional 객체 반환 (즉, Optional.empty()). */
        return store.values().stream()
                .filter(member -> member.getName().equals(name))
                .findAny();
    }

    @Override
    public List<Member> findAll() {
         /* 모든 멤버 반환.
            ArrayList 생성자에 store의 values() 전달하여 모든 값을 포함하는 리스트 생성 후 반환.*/
        return new ArrayList<>(store.values());
    }

    public  void clearStore(){
        store.clear(); //비우기
    }


}
