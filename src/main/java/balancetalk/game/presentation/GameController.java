package balancetalk.game.presentation;

import balancetalk.game.application.GameService;
import balancetalk.game.dto.GameSetDto.CreateGameSetRequest;
import balancetalk.game.dto.GameSetDto.GameSetDetailResponse;
import balancetalk.game.dto.GameSetDto.GameSetResponse;
import balancetalk.game.dto.GameSetDto.UpdateGameSetRequest;
import balancetalk.global.utils.AuthPrincipal;
import balancetalk.member.dto.ApiMember;
import balancetalk.member.dto.GuestOrApiMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
@Tag(name = "game", description = "밸런스 게임 API")
public class GameController {

    private final GameService gameService;

    @PostMapping
    @Operation(summary = "밸런스 게임 세트 생성", description = "10개 단위의 밸런스 게임을 가지고 있는 게임 세트를 생성합니다.")
    public Long createGameSetRequest(@RequestBody final CreateGameSetRequest request,
                              @Parameter(hidden = true) @AuthPrincipal final ApiMember apiMember) {
        return gameService.createBalanceGameSet(request, apiMember);
    }

    @PutMapping("/{gameSetId}")
    @Operation(summary = "밸런스 게임 수정", description = "밸런스 게임을 수정합니다.")
    public void updateGame(@PathVariable final Long gameSetId,
                           @RequestBody final UpdateGameSetRequest request,
                           @Parameter(hidden = true) @AuthPrincipal final ApiMember apiMember) {
        gameService.updateBalanceGame(gameSetId, request, apiMember);
    }


    @GetMapping("/{gameSetId}")
    @Operation(summary = "밸런스 게임 세트 상세 조회", description = "10개 단위의 밸런스 게임을 가지고 있는 게임 세트를 조회합니다.")
    public GameSetDetailResponse findGame(@PathVariable final Long gameSetId,
                                          @Parameter(hidden = true) @AuthPrincipal final GuestOrApiMember guestOrApiMember) {
        return gameService.findBalanceGameSet(gameSetId, guestOrApiMember);
    }

    @DeleteMapping("/{gameSetId}")
    @Operation(summary = "밸런스 게임 세트 삭제", description = "밸런스 게임 세트를 삭제합니다.")
    public void deleteGameSet(@PathVariable final Long gameSetId,
                              @Parameter(hidden = true) @AuthPrincipal final ApiMember apiMember) {
        gameService.deleteBalanceGameSet(gameSetId, apiMember);
    }

    @GetMapping("/latest")
    @Operation(summary = "최신순으로 밸런스 게임 조회", description = "최신순으로 정렬된 16개의 게임 목록을 리턴합니다.")
    public List<GameSetResponse> findLatestGames(
            @RequestParam String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @Parameter(hidden = true) @AuthPrincipal final GuestOrApiMember guestOrApiMember
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return gameService.findLatestGames(tagName, pageable, guestOrApiMember);
    }

    @GetMapping("/popular")
    @Operation(summary = "인기순으로 밸런스 게임 조회",
            description = "메인 태그가 주어지면 해당 태그의 게임을 인기순으로 조회, 없으면 밸런스 게임 전체를 인기순으로 조회합니다.")
    public List<GameSetResponse> findPopularGamesWithTag(
            @RequestParam(required = false) String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @Parameter(hidden = true) @AuthPrincipal final GuestOrApiMember guestOrApiMember
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return gameService.findPopularGames(tagName, pageable, guestOrApiMember);
    }

    @GetMapping("/random")
    @Operation(summary = "랜덤 밸런스 게임 조회",
        description = "랜덤으로 id를 가져와 밸런스 게임을 조회한다.")
    public GameSetDetailResponse findRandomGame(@AuthPrincipal final GuestOrApiMember guestOrApiMember) {
        return gameService.findRandomGame(guestOrApiMember);
    }
}
