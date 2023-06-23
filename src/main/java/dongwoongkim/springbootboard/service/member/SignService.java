package dongwoongkim.springbootboard.service.member;

import dongwoongkim.springbootboard.domain.member.Member;
import dongwoongkim.springbootboard.domain.role.RoleType;
import dongwoongkim.springbootboard.dto.member.LoginRequestDto;
import dongwoongkim.springbootboard.dto.member.SignUpRequestDto;
import dongwoongkim.springbootboard.dto.member.LogInResponseDto;
import dongwoongkim.springbootboard.exception.member.DuplicateEmailException;
import dongwoongkim.springbootboard.exception.member.DuplicateUsernameException;
import dongwoongkim.springbootboard.exception.auth.LoginFailureException;
import dongwoongkim.springbootboard.exception.member.MemberNotFoundException;
import dongwoongkim.springbootboard.exception.role.RoleNotFoundException;
import dongwoongkim.springbootboard.repository.member.MemberRepository;
import dongwoongkim.springbootboard.repository.member.RoleRepository;
import dongwoongkim.springbootboard.token.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SignService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenService tokenService;

    @Transactional
    public void signUp(SignUpRequestDto signUpRequestDto) {
        validateDuplicateSingUpInfo(signUpRequestDto);
        Member member = new Member(signUpRequestDto.getUsername(),
                passwordEncoder.encode(signUpRequestDto.getPassword()),
                signUpRequestDto.getNickname(),
                signUpRequestDto.getEmail(),
                List.of(roleRepository.findByRoleType(RoleType.USER).orElseThrow(() -> new RoleNotFoundException("해당 권한을 찾을 수 없습니다"))));
        memberRepository.save(member);
    }

    public LogInResponseDto login(LoginRequestDto loginRequestDto) {
        Member member = memberRepository.findByUsername(loginRequestDto.getUsername()).orElseThrow(MemberNotFoundException::new);
        if (member != null) {
            TokenService.PrivateClaims privateClaims = createPrivateClaims(member);
            String jwt = jwtLoginRequest(loginRequestDto, privateClaims);
            return LogInResponseDto.toDto(jwt);
        }
        throw new MemberNotFoundException("요청한 회원은 존재하지 않습니다.");
    }

    private TokenService.PrivateClaims createPrivateClaims(Member member) {
        return new TokenService.PrivateClaims(
                String.valueOf(member.getId()),
                member.getRoles().stream()
                        .map(memberRole -> memberRole.getRole())
                        .map(role -> role.getRoleType())
                        .map(roleType -> roleType.toString())
                        .collect(Collectors.toList()));
    }

    private String jwtLoginRequest(LoginRequestDto loginRequestDto, TokenService.PrivateClaims privateClaims) {
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken); // loadUserByUsername 메소드 실행
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 토큰 생성 및 리턴
        String jwt = tokenService.createAccessToken(authentication, privateClaims);
        if (!StringUtils.hasText(jwt)) {
            throw new LoginFailureException("로그인에 실패하였습니다.");
        }
        return jwt;
    }

    private void validateDuplicateSingUpInfo(SignUpRequestDto signUpRequestDto) {
        Member member = memberRepository.findOneWithRolesByUsername(signUpRequestDto.getUsername()).orElse(null);
        if (member != null) {
            throw new DuplicateUsernameException("해당 아이디는 이미 등록된 아이디입니다.");
        } else if (memberRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new DuplicateEmailException("해당 이메일은 이미 등록된 이메일입니다.");
        }
    }

}
