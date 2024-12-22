package balancetalk.game.dto;

import static balancetalk.vote.domain.VoteOption.A;
import static balancetalk.vote.domain.VoteOption.B;

import balancetalk.bookmark.domain.GameBookmark;
import balancetalk.game.domain.Game;
import balancetalk.game.domain.GameOption;
import balancetalk.game.domain.GameSet;
import balancetalk.game.domain.MainTag;
import balancetalk.vote.domain.GameVote;
import balancetalk.vote.domain.VoteOption;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GameDto {

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "밸런스 게임 생성 혹은 수정 요청")
    public static class CreateOrUpdateGame {

        @Schema(description = "게임 추가 설명", example = "추가 설명")
        private String description;

        private List<GameOptionDto> gameOptions;

        public Game toEntity() {
            List<GameOption> options = gameOptions.stream()
                    .map(GameOptionDto::toEntity)
                    .toList();

            return Game.builder()
                    .description(description)
                    .gameOptions(options)
                    .editedAt(LocalDateTime.now())
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "밸런스 게임 조회 응답")
    public static class GameResponse {

        @Schema(description = "밸런스 게임 id", example = "1")
        private Long id;

        @Schema(description = "게임 추가 설명", example = "추가 설명")
        private String description;

        private List<GameOptionDto> gameOptions;

        @Schema(description = "북마크 여부", example = "false")
        private Boolean myBookmark;

        public static GameResponse fromEntity(Game game,
                                              boolean isBookmarked,
                                              Map<Long, String> gameOptionImgUrls
        ) {

            return GameResponse.builder()
                    .id(game.getId())
                    .description(game.getDescription())
                    .gameOptions(getGameOptionDtos(game, gameOptionImgUrls))
                    .myBookmark(isBookmarked)
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "밸런스 게임 상세 조회 응답")
    public static class GameDetailResponse {

        @Schema(description = "밸런스 게임 id", example = "1")
        private Long id;

        @Schema(description = "게임 추가 설명", example = "추가 설명")
        private String description;

        private List<GameOptionDto> gameOptions;

        @Schema(description = "선택지 A 투표수", example = "98")
        private long votesCountOfOptionA;

        @Schema(description = "선택지 B 투표수", example = "95")
        private long votesCountOfOptionB;

        @Schema(description = "북마크 여부", example = "false")
        private Boolean myBookmark;

        @Schema(description = "투표한 선택지", example = "A")
        private VoteOption votedOption;

        public static GameDetailResponse fromEntity(
                Game game,
                boolean myBookmark,
                VoteOption votedOption,
                Map<Long, String> gameOptionImgUrls
        ) {

            return GameDetailResponse.builder()
                    .id(game.getId())
                    .description(game.getDescription())
                    .gameOptions(getGameOptionDtos(game, gameOptionImgUrls))
                    .votesCountOfOptionA(game.getVoteCount(A))
                    .votesCountOfOptionB(game.getVoteCount(B))
                    .myBookmark(myBookmark)
                    .votedOption(votedOption)
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "밸런스 게임 메인 태그 생성")
    public static class CreateGameMainTagRequest {

        @Schema(description = "밸런스 게임 메인 태그", example = "커플")
        private String name;

        public MainTag toEntity() {
            return MainTag.builder()
                    .name(name)
                    .build();
        }
    }

    @Schema(description = "마이페이지 밸런스 게임 응답")
    @Data
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GameMyPageResponse {

        @Schema(description = "밸런스 게임 세트 ID", example = "1")
        private long gameSetId;

        @Schema(description = "밸런스 게임 ID", example = "1")
        private long gameId;

        @Schema(description = "밸런스게임 세트 제목", example = "제목")
        private String title;

        @Schema(description = "투표한 선택지", example = "A")
        private VoteOption voteOption;

        @Schema(description = "선택지 A 이미지", example = "https://pikko-image.s3.ap-northeast-2.amazonaws.com/balance-game/067cc56e-21b7-468f-a2c1-4839036ee7cd_unnamed.png")
        private String optionAImg;

        @Schema(description = "선택지 B 이미지", example = "https://pikko-image.s3.ap-northeast-2.amazonaws.com/balance-game/1157461e-a685-42fd-837e-7ed490894ca6_unnamed.png")
        private String optionBImg;

        @Schema(description = "최종 수정일(마이페이지 등록 날짜)")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDateTime editedAt;

        @Schema(description = "북마크 여부")
        private boolean isBookmarked;

        @Schema(description = "밸런스 게임 서브 태그", example = "화제의 중심")
        private String subTag;

        @Schema(description = "밸런스 게임 메인 태그 이름", example = "인기")
        private String mainTagName;

        public static GameMyPageResponse from(GameSet gameSet, String imgA, String imgB) {
            return GameMyPageResponse.builder()
                    .gameSetId(gameSet.getId())
                    .title(gameSet.getTitle())
                    .optionAImg(imgA)
                    .optionBImg(imgB)
                    .subTag(gameSet.getSubTag())
                    .mainTagName(gameSet.getMainTag().getName())
                    .editedAt(gameSet.getEditedAt())
                    .build();
        }

        public static GameMyPageResponse from(Game game, GameBookmark bookmark, String imgA, String imgB) {
            return GameMyPageResponse.builder()
                    .gameSetId(game.getGameSet().getId())
                    .gameId(game.getId())
                    .title(game.getGameSet().getTitle())
                    .optionAImg(imgA)
                    .optionBImg(imgB)
                    .isBookmarked(bookmark.isActive())
                    .subTag(game.getGameSet().getSubTag())
                    .mainTagName(game.getGameSet().getMainTag().getName())
                    .editedAt(game.getEditedAt())
                    .build();
        }

        public static GameMyPageResponse from(Game game, GameVote vote, String imgA, String imgB) {
            return GameMyPageResponse.builder()
                    .gameId(game.getId())
                    .title(game.getGameSet().getTitle())
                    .optionAImg(imgA)
                    .optionBImg(imgB)
                    .voteOption(vote.getVoteOption())
                    .subTag(game.getGameSet().getSubTag())
                    .mainTagName(game.getGameSet().getMainTag().getName())
                    .editedAt(game.getEditedAt())
                    .build();
        }
    }

    public static List<GameOptionDto> getGameOptionDtos(Game game, Map<Long, String> gameOptionImgUrls) {
        return game.getGameOptions().stream()
                .map(option -> {
                    Long fileId = option.getImgId();
                    String imgUrl = gameOptionImgUrls.get(option.getId());
                    return GameOptionDto.fromEntity(option, fileId, imgUrl);
                })
                .toList();
    }
}
