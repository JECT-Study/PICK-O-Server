package balancetalk.game.dto;

import balancetalk.game.domain.TempGameSet;
import balancetalk.game.dto.TempGameDto.CreateTempGameRequest;
import balancetalk.game.dto.TempGameDto.TempGameResponse;
import balancetalk.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class TempGameSetDto {

    @Data
    public static class CreateTempGameSetRequest {

        @Schema(description = "밸런스게임 세트 제목", example = "밸런스게임 세트 제목")
        @Size(max = 50, message = "제목은 최대 50자까지 입력 가능합니다")
        private String title;

        @Schema(description = "최근 임시저장된 밸런스 게임 불러오기 여부", example = "false")
        @NotNull(message = "isLoaded 필드는 NULL을 허용하지 않습니다.")
        private Boolean isLoaded;

        private List<CreateTempGameRequest> tempGames;

        public TempGameSet toEntity(Member member) {
            return TempGameSet.builder()
                    .title(title)
                    .member(member)
                    .build();
        }

        @JsonIgnore
        public List<Long> getAllFileIds() {
            return tempGames.stream()
                    .flatMap(game -> game.getTempGameOptions().stream())
                    .map(TempGameOptionDto::getFileId)
                    .filter(Objects::nonNull)
                    .toList();
        }

        @JsonIgnore
        public boolean isNewRequest() {
            return !isLoaded;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "임시 밸런스 게임 세트 조회 응답")
    public static class TempGameSetResponse {

        @Schema(description = "밸런스게임 세트 제목", example = "밸런스게임 세트 제목")
        private String title;

        @Schema(description = "게임 리스트")
        private List<TempGameResponse> tempGames;

        public static TempGameSetResponse fromEntity(TempGameSet tempGameSet, List<TempGameResponse> tempGames) {
            return TempGameSetResponse.builder()
                    .title(tempGameSet.getTitle())
                    .tempGames(tempGames)
                    .build();
        }

    }
}
