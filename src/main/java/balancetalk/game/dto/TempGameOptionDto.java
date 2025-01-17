package balancetalk.game.dto;

import balancetalk.game.domain.TempGameOption;
import balancetalk.vote.domain.VoteOption;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
@Schema(description = "임시 밸런스 게임 선택지")
public class TempGameOptionDto {

    @Schema(description = "선택지 이름", example = "선택지 이름")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "선택지 이미지 파일 ID", example = "1")
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

    public TempGameOption toEntity() {
        return TempGameOption.builder()
                .name(name)
                .imgId(fileId)
                .description(description)
                .optionType(optionType)
                .build();
    }

    public static TempGameOptionDto fromEntity(TempGameOption tempGameOption, Long fileId, String imgUrl) {
        return TempGameOptionDto.builder()
                .name(tempGameOption.getName())
                .fileId(fileId)
                .imgUrl(imgUrl)
                .description(tempGameOption.getDescription())
                .optionType(tempGameOption.getOptionType())
                .build();
    }
}