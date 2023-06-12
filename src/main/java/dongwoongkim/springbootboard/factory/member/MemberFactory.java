package dongwoongkim.springbootboard.factory.member;

import dongwoongkim.springbootboard.domain.member.Member;
import dongwoongkim.springbootboard.domain.role.RoleType;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public class MemberFactory {
    public static Member createMember() {
        return new Member("user", "1q2w3e4r!!", "nick", "ee@naver.com", emptyList());
    }

}