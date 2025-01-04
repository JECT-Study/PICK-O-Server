package balancetalk.member.presentation;

import balancetalk.global.utils.AuthPrincipal;
import balancetalk.member.application.MemberService;
import balancetalk.member.dto.ApiMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static balancetalk.member.dto.MemberDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Validated
@Tag(name = "member", description = "회원 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    @Operation(summary = "회원 가입", description = "닉네임, 이메일, 비밀번호를 입력하여 회원 가입을 한다.")
    public void join(@Valid @RequestBody final JoinRequest joinRequest) {
        memberService.join(joinRequest);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "회원 가입 한 이메일과 패스워드를 사용하여 로그인 한다.")
    public String login(@Valid @RequestBody final LoginRequest loginRequest, HttpServletResponse response) {
        return memberService.login(loginRequest, response);
    }

    @GetMapping("/info")
    @Operation(summary = "단일 회원 조회", description = "회원 정보를 조회한다.")
    public MemberResponse findMemberInfo(@Parameter(hidden = true) @AuthPrincipal ApiMember apiMember) {
        return memberService.findMemberInfo(apiMember);
    }

    @GetMapping
    @Operation(summary = "전체 회원 조회", description = "모든 회원 정보를 조회한다.")
    public List<MemberResponse> findAllMemberInfo() {
        return memberService.findAll();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "회원 삭제", description = "회원 정보를 삭제한다.")
    public void deleteMember(@Valid @RequestBody final LoginRequest loginRequest,
                             @Parameter(hidden = true) @AuthPrincipal ApiMember apiMember) {
        memberService.delete(loginRequest, apiMember);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그인 된 회원을 로그아웃 처리한다.")
    public String logout() {
        return "로그아웃 처리되었습니다.";
    }

    @GetMapping("/duplicate")
    @Operation(summary = "닉네임 중복 검증", description = "중복된 닉네임이 존재하는지 체크한다.")
    public void verifyNickname(@RequestParam @NotBlank
                               @Size(min = 2, max = 10) String nickname) {
        memberService.verifyNickname(nickname);
    }

    @GetMapping("/reissue")
    @Operation(summary = "액세스 토큰 재발급", description = "액세스 토큰을 재발급 받는다.")
    public String reissueAccessToken(HttpServletRequest request) {
        return memberService.reissueAccessToken(request);
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "단일 회원 조회", description = "memberId와 일치하는 회원 정보를 조회한다.")
    public MemberResponse findMemberInfo(@PathVariable("memberId") Long memberId) {
        return memberService.findById(memberId);
    }

    @PostMapping("/verify-password")
    @Operation(summary = "비밀번호 검증", description = "회원의 비밀번호를 검증한다.")
    public void validatePassword(@RequestParam @NotBlank String password,
                                 @Parameter(hidden = true) @AuthPrincipal ApiMember apiMember) {
        memberService.verifyPassword(password, apiMember);
    }
}
