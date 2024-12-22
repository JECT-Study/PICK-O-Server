package balancetalk.game.dto;

import balancetalk.game.domain.GameOption;
import balancetalk.vote.domain.VoteOption;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "밸런스 게임 선택지")
public class GameOptionDto {

    private Long id;

    @Schema(description = "선택지 이름", example = "선택지 이름")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "선택지 이미지 파일 ID", example = "12")
    private Long fileId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "선택지 이미지",
            example = "https://pikko-image.s3.ap-northeast-2.amazonaws.com/balance-game/4839036ee7cd_unnamed.png",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String imgUrl;

    @Schema(description = "선택지 추가설명", example = "선택지 추가 설명")
    private String description;

    @Schema(description = "선택지", example = "A")
    private VoteOption optionType;

    public static GameOptionDto fromEntity(GameOption gameOption, Long fileId, String imgUrl) {
        return GameOptionDto.builder()
                .id(gameOption.getId())
                .name(gameOption.getName())
                .fileId(fileId)
                .imgUrl(imgUrl)
                .description(gameOption.getDescription())
                .optionType(gameOption.getOptionType())
                .build();
    }

    public GameOption toEntity() {
        return GameOption.builder()
                .name(name)
                .imgId(fileId)
                .description(description)
                .optionType(optionType)
                .build();
    }
}
