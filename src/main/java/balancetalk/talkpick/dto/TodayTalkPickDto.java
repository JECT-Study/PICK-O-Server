package balancetalk.talkpick.dto;

import balancetalk.talkpick.domain.TalkPick;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class TodayTalkPickDto {

    @Schema(description = "오늘의 톡픽 조회 응답")
    @Data
    @Builder
    @AllArgsConstructor
    public static class TodayTalkPickResponse {

        @Schema(description = "톡픽 ID", example = "톡픽 ID")
        private Long id;

        @Schema(description = "제목", example = "톡픽 제목")
        private String title;

        @Schema(description = "선택지 A 이름", example = "선택지 A 이름")
        private String optionA;

        @Schema(description = "선택지 B 이름", example = "선택지 B 이름")
        private String optionB;

        public static TodayTalkPickResponse from(TalkPick talkPick) {
            return TodayTalkPickResponse.builder()
                    .id(talkPick.getId())
                    .title(talkPick.getTitle())
                    .optionA(talkPick.getOptionA())
                    .optionB(talkPick.getOptionB())
                    .build();
        }
    }
}
