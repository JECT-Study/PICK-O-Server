package balancetalk.talkpick.dto;

import static balancetalk.talkpick.domain.ViewStatus.NORMAL;
import static balancetalk.vote.domain.VoteOption.A;
import static balancetalk.vote.domain.VoteOption.B;

import balancetalk.bookmark.domain.TalkPickBookmark;
import balancetalk.comment.domain.Comment;
import balancetalk.member.domain.Member;
import balancetalk.talkpick.domain.SummaryStatus;
import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.dto.fields.BaseTalkPickFields;
import balancetalk.talkpick.dto.fields.ValidatedNotBlankTalkPickFields;
import balancetalk.vote.domain.TalkPickVote;
import balancetalk.vote.domain.VoteOption;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class TalkPickDto {

    @Schema(description = "톡픽 생성 요청")
    @Data
    @AllArgsConstructor
    public static class CreateTalkPickRequest {

        @Valid
        private ValidatedNotBlankTalkPickFields baseFields;

        @Schema(description = "첨부한 이미지 파일 ID 목록", example = "[12, 41]")
        @Size(max = 10, message = "톡픽 생성 시 업로드할 수 있는 파일 개수는 최대 10개입니다.")
        private List<Long> fileIds;

        public TalkPick toEntity(Member member) {
            return TalkPick.builder()
                    .member(member)
                    .title(baseFields.getTitle())
                    .content(baseFields.getContent())
                    .optionA(baseFields.getOptionA())
                    .optionB(baseFields.getOptionB())
                    .sourceUrl(baseFields.getSourceUrl())
                    .summaryStatus(SummaryStatus.PENDING)
                    .views(0L)
                    .bookmarks(0L)
                    .viewStatus(NORMAL)
                    .editedAt(LocalDateTime.now())
                    .build();
        }

        public boolean containsFileIds() {
            return fileIds != null;
        }
    }

    @Schema(description = "톡픽 수정 요청")
    @Data
    @AllArgsConstructor
    public static class UpdateTalkPickRequest {

        @Valid
        private ValidatedNotBlankTalkPickFields baseFields;

        @Schema(description = "새로 첨부한 이미지 파일 ID 목록", example = "[12, 41]")
        @Size(max = 10, message = "톡픽 생성 시 업로드할 수 있는 파일 개수는 최대 10개입니다.")
        private List<Long> newFileIds;

        @Schema(description = "제거할 이미지 파일 ID 목록", example = "[3, 7]")
        private List<Long> deleteFileIds;

        public TalkPick toEntity(Member member) {
            return TalkPick.builder()
                    .member(member)
                    .title(baseFields.getTitle())
                    .content(baseFields.getContent())
                    .optionA(baseFields.getOptionA())
                    .optionB(baseFields.getOptionB())
                    .sourceUrl(baseFields.getSourceUrl())
                    .summaryStatus(SummaryStatus.PENDING)
                    .editedAt(LocalDateTime.now())
                    .build();
        }

        public boolean containsNewFileIds() {
            return newFileIds != null && !newFileIds.isEmpty();
        }

        public boolean containsDeleteFileIds() {
            return deleteFileIds != null && !deleteFileIds.isEmpty();
        }

        public boolean notContainsAnyFileIds() {
            return !containsNewFileIds() && !containsDeleteFileIds();
        }
    }

    @Schema(description = "톡픽 상세 조회 응답")
    @Data
    @AllArgsConstructor
    @Builder
    public static class TalkPickDetailResponse {

        @Schema(description = "톡픽 ID", example = "톡픽 ID")
        private long id;

        private BaseTalkPickFields baseFields;

        private SummaryResponse summary;

        @Schema(description = "톡픽 작성 시 첨부한 이미지 URL 목록",
                example = "["
                        + "\"https://picko-image.amazonaws.com/temp-talk-pick/ad80-a94e083301d2_czz.png\",\n"
                        + "\"https://picko-image.amazonaws.com/temp-talk-pick/957e6ed4830b_prom.jpeg\""
                        + "]")
        private List<String> imgUrls;

        @Schema(description = "톡픽 작성 시 첨부한 이미지 파일 ID", example = "[1, 41]")
        private List<Long> fileIds;

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

        @Schema(description = "작성일", example = "2024-08-04")
        private LocalDate createdAt;

        @Schema(description = "수정 여부", example = "true")
        private Boolean isEdited;

        public static TalkPickDetailResponse from(TalkPick entity,
                                                  List<String> imgUrls,
                                                  List<Long> fileIds,
                                                  boolean myBookmark,
                                                  VoteOption votedOption) {
            return TalkPickDetailResponse.builder()
                    .id(entity.getId())
                    .baseFields(BaseTalkPickFields.builder()
                            .title(entity.getTitle())
                            .content(entity.getContent())
                            .optionA(entity.getOptionA())
                            .optionB(entity.getOptionB())
                            .sourceUrl(entity.getSourceUrl())
                            .build())
                    .summary(new SummaryResponse(entity.getSummary()))
                    .imgUrls(imgUrls)
                    .fileIds(fileIds)
                    .votesCountOfOptionA(entity.votesCountOf(A))
                    .votesCountOfOptionB(entity.votesCountOf(B))
                    .views(entity.getViews())
                    .bookmarks(entity.getBookmarks())
                    .myBookmark(myBookmark)
                    .votedOption(votedOption)
                    .writer(entity.getWriterNickname())
                    .createdAt(entity.getCreatedAt().toLocalDate())
                    .isEdited(entity.isEdited())
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
        public TalkPickResponse(Long id, String title, String writer, LocalDateTime createdAt,
                                long views, long bookmarks) {
            this.id = id;
            this.title = title;
            this.writer = writer;
            this.createdAt = createdAt.toLocalDate();
            this.views = views;
            this.bookmarks = bookmarks;
        }
    }

    @Schema(description = "마이페이지 톡픽 응답")
    @Data
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TalkPickMyPageResponse {

        @Schema(description = "톡픽 ID", example = "1")
        private long id;

        @Schema(description = "제목", example = "톡픽 제목")
        private String title;

        @Schema(description = "투표한 선택지", example = "A")
        private VoteOption voteOption;

        @Schema(description = "댓글 내용", example = "댓글 내용")
        private String commentContent;

        @Schema(description = "북마크 된 개수", example = "12")
        private long bookmarks;

        @Schema(description = "북마크 여부")
        private boolean isBookmarked;

        @Schema(description = "댓글 개수", example = "2")
        private long commentCount;

        @Schema(description = "최종 수정일(마이페이지 등록 날짜)")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDateTime editedAt;

        public static TalkPickMyPageResponse from(TalkPick talkPick, TalkPickBookmark talkPickBookmark) {
            return TalkPickMyPageResponse.builder()
                    .id(talkPick.getId())
                    .title(talkPick.getTitle())
                    .editedAt(talkPick.getEditedAt())
                    .isBookmarked(talkPickBookmark.isActive())
                    .bookmarks(talkPick.getBookmarks())
                    .commentCount(!talkPick.getComments().isEmpty() ? talkPick.getComments().size() : 0)
                    .editedAt(talkPick.getEditedAt())
                    .build();
        }

        public static TalkPickMyPageResponse from(TalkPick talkPick, TalkPickVote vote) {
            return TalkPickMyPageResponse.builder()
                    .id(talkPick.getId())
                    .title(talkPick.getTitle())
                    .voteOption(vote.getVoteOption())
                    .bookmarks(talkPick.getBookmarks())
                    .commentCount(!talkPick.getComments().isEmpty() ? talkPick.getComments().size() : 0)
                    .editedAt(talkPick.getEditedAt())
                    .build();
        }

        public static TalkPickMyPageResponse from(TalkPick talkPick, Comment comment) {
            return TalkPickMyPageResponse.builder()
                    .id(talkPick.getId())
                    .title(talkPick.getTitle())
                    .commentContent(comment.getContent())
                    .bookmarks(talkPick.getBookmarks())
                    .commentCount(!talkPick.getComments().isEmpty() ? talkPick.getComments().size() : 0)
                    .editedAt(talkPick.getEditedAt())
                    .build();
        }

        public static TalkPickMyPageResponse fromMyTalkPick(TalkPick talkPick) {
            return TalkPickMyPageResponse.builder()
                    .id(talkPick.getId())
                    .title(talkPick.getTitle())
                    .bookmarks(talkPick.getBookmarks())
                    .commentCount(!talkPick.getComments().isEmpty() ? talkPick.getComments().size() : 0)
                    .editedAt(talkPick.getEditedAt())
                    .build();
        }
    }
}