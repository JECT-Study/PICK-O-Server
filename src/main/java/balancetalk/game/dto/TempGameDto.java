package balancetalk.game.dto;

import balancetalk.game.domain.TempGame;
import balancetalk.game.domain.TempGameOption;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class TempGameDto {

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "임시 밸런스 게임 저장 요청")
    public static class CreateTempGameRequest {

        @Schema(description = "게임 추가 설명", example = "추가 설명")
        private String description;

        private List<TempGameOptionDto> tempGameOptions;

        public TempGame toEntity() {
            List<TempGameOption> options = tempGameOptions.stream()
                    .map(TempGameOptionDto::toEntity)
                    .toList();

            return TempGame.builder()
                    .description(description)
                    .tempGameOptions(options)
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "임시 밸런스 게임 응답")
    public static class TempGameResponse {

        @Schema(description = "게임 추가 설명", example = "추가 설명")
        private String description;

        private List<TempGameOptionDto> tempGameOptions;

        public static TempGameResponse fromEntity(TempGame tempGame, Map<Long, String> tempGameOptionImgUrls) {
            return TempGameResponse.builder()
                    .description(tempGame.getDescription())
                    .tempGameOptions(getTempGameOptionDtos(tempGame, tempGameOptionImgUrls))
                    .build();
        }
    }

    public static List<TempGameOptionDto> getTempGameOptionDtos(
            TempGame tempGame,
            Map<Long, String> tempGameOptionImgUrls
    ) {
        return tempGame.getTempGameOptions().stream()
                .map(option -> {
                    Long fileId = option.getImgId();
                    String imgUrl = tempGameOptionImgUrls.get(option.getId());
                    return TempGameOptionDto.fromEntity(option, fileId, imgUrl);
                })
                .toList();
    }
}
