package balancetalk.talkpick.presentation;

import static balancetalk.talkpick.dto.TalkPickDto.CreateTalkPickRequest;
import static balancetalk.talkpick.dto.TalkPickDto.TalkPickDetailResponse;
import static balancetalk.talkpick.dto.TalkPickDto.TalkPickResponse;
import static balancetalk.talkpick.dto.TalkPickDto.UpdateTalkPickRequest;
import static org.springframework.data.domain.Sort.Direction.DESC;

import balancetalk.global.utils.AuthPrincipal;
import balancetalk.member.dto.ApiMember;
import balancetalk.member.dto.GuestOrApiMember;
import balancetalk.talkpick.application.TalkPickService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/talks")
@Tag(name = "talk_pick", description = "톡픽 API")
public class TalkPickController {

    private final TalkPickService talkPickService;

    @Operation(summary = "톡픽 생성", description = "톡픽을 생성합니다.")
    @PostMapping
    public Long createTalkPick(@RequestBody @Valid final CreateTalkPickRequest request,
                               @Parameter(hidden = true) @AuthPrincipal final ApiMember apiMember) {
        return talkPickService.createTalkPick(request, apiMember);
    }

    @Operation(summary = "톡픽 상세 조회", description = "톡픽 상세 페이지를 조회합니다.")
    @GetMapping("/{talkPickId}")
    public TalkPickDetailResponse findTalkPickDetail(
            @PathVariable final Long talkPickId,
            @Parameter(hidden = true) @AuthPrincipal final GuestOrApiMember guestOrApiMember) {
        return talkPickService.findById(talkPickId, guestOrApiMember);
    }

    @Operation(summary = "톡픽 목록 조회", description = "톡픽 목록을 조회합니다.")
    @GetMapping
    public Page<TalkPickResponse> findPagedTalkPicks(
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        return talkPickService.findPaged(pageable);
    }

    @Operation(summary = "톡픽 수정", description = "톡픽을 수정합니다.")
    @PutMapping("/{talkPickId}")
    public void updateTalkPick(@PathVariable final Long talkPickId,
                               @RequestBody @Valid final UpdateTalkPickRequest request,
                               @Parameter(hidden = true) @AuthPrincipal final ApiMember apiMember) {
        talkPickService.updateTalkPick(talkPickId, request, apiMember);
    }

    @Operation(summary = "톡픽 삭제", description = "톡픽을 삭제합니다.")
    @DeleteMapping("/{talkPickId}")
    public void deleteTalkPick(@PathVariable final Long talkPickId,
                               @Parameter(hidden = true) @AuthPrincipal final ApiMember apiMember) {
        talkPickService.deleteTalkPick(talkPickId, apiMember);
    }

    @Operation(summary = "인기 톡픽 조회", description = "인기 톡픽들을 조회합니다.")
    @GetMapping("/best")
    public List<TalkPickResponse> findBestTalkPicks() {
        return talkPickService.findBestTalkPicks();
    }
}
