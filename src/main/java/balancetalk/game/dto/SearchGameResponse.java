package balancetalk.game.dto;

import balancetalk.game.domain.Game;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Schema(description = "밸런스 게임 검색 응답")
@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchGameResponse {

    @Schema(description = "작성자 id", example = "1")
    private Long writerId;

    @Schema(description = "밸런스 게임 ID", example = "1")
    private long id;

    @Schema(description = "밸런스 게임 세트 ID", example = "1")
    private long gameSetId;

    @Schema(description = "밸런스게임 세트 제목", example = "제목")
    private String title;

    @Schema(description = "선택지 A 이미지", example = "https://pikko-image.s3.ap-northeast-2.amazonaws.com/balance-game/067cc56e-21b7-468f-a2c1-4839036ee7cd_unnamed.png")
    private String optionAImg;

    @Schema(description = "선택지 B 이미지", example = "https://pikko-image.s3.ap-northeast-2.amazonaws.com/balance-game/1157461e-a685-42fd-837e-7ed490894ca6_unnamed.png")
    private String optionBImg;

    @Schema(description = "밸런스 게임 서브 태그", example = "화제의 중심")
    private String subTag;

    @Schema(description = "밸런스 게임 메인 태그", example = "인기")
    private String mainTag;

    public static SearchGameResponse from(Game game, String imgA, String imgB) {
        return SearchGameResponse.builder()
                .writerId(game.getWriterId())
                .gameSetId(game.getGameSet().getId())
                .id(game.getId())
                .title(game.getGameSet().getTitle())
                .optionAImg(imgA)
                .optionBImg(imgB)
                .subTag(game.getGameSet().getSubTag())
                .mainTag(game.getGameSet().getMainTag().getName())
                .build();
    }
}
