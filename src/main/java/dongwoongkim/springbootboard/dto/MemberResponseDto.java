package dongwoongkim.springbootboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dongwoongkim.springbootboard.domain.Member;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@ApiModel(value = "회원조회 응답")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberResponseDto {

    @NotNull
    @Size(min = 3, max = 50)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @Size(min = 3, max = 50)
    private String password;

    @NotNull
    @Size(min = 3, max = 50)
    private String nickname;

    @NotNull
    @Size(min = 3, max = 50)
    private String email;

    public static MemberResponseDto toDto(Member member) {
        return MemberResponseDto.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .email(member.getEmail())
                .nickname(member.getNickname()).build();
    }
}
