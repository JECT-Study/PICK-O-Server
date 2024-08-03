package balancetalk.talkpick.dto;

import balancetalk.talkpick.domain.TalkPick;
import balancetalk.vote.domain.VoteOption;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static balancetalk.vote.domain.VoteOption.A;
import static balancetalk.vote.domain.VoteOption.B;

public class TalkPickDto {

    @Schema(description = "톡픽 생성/수정 요청")
    @Data
    @AllArgsConstructor
    public static class CreateTalkPickRequest {

        @Schema(description = "제목", example = "제목")
        private String title;

        @Schema(description = "본문 내용", example = "본문 내용")
        private String content;

        @Schema(description = "요약", example = "3줄 요약")
        private String summary;

        @Schema(description = "선택지 A 이름", example = "선택지 A 이름")
        private String optionA;

        @Schema(description = "선택지 B 이름", example = "선택지 B 이름")
        private String optionB;
    }

    @Schema(description = "톡픽 상세 조회 응답")
    @Data
    @AllArgsConstructor
    @Builder
    public static class TalkPickDetailResponse {

        @Schema(description = "톡픽 ID", example = "톡픽 ID")
        private long id;

        @Schema(description = "제목", example = "톡픽 제목")
        private String title;

        @Schema(description = "본문 내용", example = "톡픽 본문 내용")
        private String content;

        private SummaryDto summary;

        @Schema(description = "선택지 A 이름", example = "선택지 A 이름")
        private String optionA;

        @Schema(description = "선택지 B 이름", example = "선택지 B 이름")
        private String optionB;

        @Schema(description = "선택지 A 투표수", example = "12")
        private long votesCountOfOptionA;

        @Schema(description = "선택지 B 투표수", example = "12")
        private long votesCountOfOptionB;

        @Schema(description = "조회수", example = "152")
        private long views;

        @Schema(description = "저장수", example = "143")
        private long bookmarks;

        @Schema(description = "북마크 여부", example = "true")
        private Boolean myBookmark;

        @Schema(description = "투표한 선택지", example = "A")
        private VoteOption votedOption;

        @Schema(description = "작성자 닉네임", example = "hj30")
        private String writer;

        @Schema(description = "최근 수정일", example = "2024-08-04")
        private LocalDate lastModifiedAt;

        public static TalkPickDetailResponse from(TalkPick entity,
                                                  long bookmarks,
                                                  boolean myBookmark,
                                                  VoteOption votedOption) {
            return TalkPickDetailResponse.builder()
                    .id(entity.getId())
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .summary(new SummaryDto(entity.getSummary()))
                    .optionA(entity.getOptionA())
                    .optionB(entity.getOptionB())
                    .votesCountOfOptionA(entity.votesCountOf(A))
                    .votesCountOfOptionB(entity.votesCountOf(B))
                    .views(entity.getViews())
                    .bookmarks(bookmarks)
                    .myBookmark(myBookmark)
                    .votedOption(votedOption)
                    .writer(entity.getWriterNickname())
                    .lastModifiedAt(entity.getLastModifiedAt().toLocalDate())
                    .build();
        }
    }

    @Schema(description = "톡픽 목록 조회 응답")
    @Data
    @AllArgsConstructor
    @Builder
    public static class TalkPickResponse {

        @Schema(description = "톡픽 ID", example = "톡픽 ID")
        private long id;

        @Schema(description = "제목", example = "톡픽 제목")
        private String title;

        @Schema(description = "작성자 닉네임", example = "hj30")
        private String writer;

        @Schema(description = "작성일", example = "2024-08-04")
        private LocalDate createdAt;

        @Schema(description = "조회수", example = "152")
        private long views;

        @Schema(description = "저장수", example = "143")
        private long bookmarks;

        @QueryProjection
        public TalkPickResponse(Long id, String title, String writer, LocalDateTime createdAt, long views, long bookmarks) {
            this.id = id;
            this.title = title;
            this.writer = writer;
            this.createdAt = createdAt.toLocalDate();
            this.views = views;
            this.bookmarks = bookmarks;
        }
    }
}