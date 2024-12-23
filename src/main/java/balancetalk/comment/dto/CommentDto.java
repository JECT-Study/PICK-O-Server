package balancetalk.comment.dto;

import balancetalk.comment.domain.Comment;
import balancetalk.member.domain.Member;
import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.domain.ViewStatus;
import balancetalk.vote.domain.VoteOption;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CommentDto {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "댓글 생성 요청")
    public static class CreateCommentRequest {

        @Schema(description = "댓글 내용", example = "댓글 내용...")
        private String content;

        public Comment toEntity(Member member, TalkPick talkPick) {
            return Comment.builder()
                    .content(content)
                    .member(member)
                    .talkPick(talkPick)
                    .isBest(false)
                    .viewStatus(ViewStatus.NORMAL)
                    .isNotifiedForFirstReply(false)
                    .editedAt(LocalDateTime.now())
                    .isEdited(false)
                    .build();
        }

        public Comment toEntity(Member member, TalkPick talkPick, Comment parent) {
            return Comment.builder()
                    .content(content)
                    .member(member)
                    .talkPick(talkPick)
                    .isBest(false)
                    .viewStatus(ViewStatus.NORMAL)
                    .parent(parent)
                    .editedAt(LocalDateTime.now())
                    .isEdited(false)
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "댓글 수정 요청")
    public static class UpdateCommentRequest {

        @Schema(description = "수정할 댓글 내용", example = "댓글 내용...")
        private String content;
    }


    @Data
    @AllArgsConstructor
    @Builder
    @Schema(description = "최신순 댓글 조회 응답")
    public static class LatestCommentResponse {

        @Schema(description = "댓글 id", example = "1")
        private Long id;

        @Schema(description = "해당 댓글에 맞는 톡픽 id", example = "1")
        private Long talkPickId;

        @Schema(description = "해당 댓글에 맞는 톡픽 제목", example = "메시 vs 호날두")
        private String talkPickTitle;

        @Schema(description = "댓글 작성자", example = "운영자1")
        private String nickname;

        @Schema(description = "댓글 작성자 프로필 이미지", example = "https://balancetalk.com/profile/1")
        private String profileImage;

        @Schema(description = "댓글 내용", example = "너는나를존중해야한다나는발롱도르5개와수많은개인트로피를들어올렸으며"
                + "2016유로에서포르투갈을이끌고우승을차지했고동시에A매치역대최다득점자이다")
        private String content;

        @Schema(description = "해당 댓글 작성자가 투표한 선택지", example = "A")
        private VoteOption voteOption;

        @Schema(description = "댓글 좋아요 개수", example = "24")
        private int likesCount;

        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        private Boolean myLike;

        @Schema(description = "답글 수", example = "3")
        private int replyCount;

        @Schema(description = "댓글 수정 여부", example = "false")
        private boolean isEdited;

        @Schema(description = "댓글이 신고당한 횟수", example = "0")
        private int reportedCount;

        @Schema(description = "댓글 생성 날짜")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd hh:mm")
        private LocalDateTime createdAt;

        @Schema(description = "댓글 수정 날짜")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd hh:mm")
        private LocalDateTime lastModifiedAt;

        public static LatestCommentResponse fromEntity(Comment comment, VoteOption voteOption, String profileImgUrl, int likesCount, boolean myLike) {
            return LatestCommentResponse.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .profileImage(profileImgUrl)
                    .talkPickId(comment.getTalkPick().getId())
                    .talkPickTitle(comment.getTalkPick().getTitle())
                    .voteOption(voteOption)
                    .likesCount(likesCount)
                    .myLike(myLike)
                    .replyCount(comment.getReplies() == null ? 0 : comment.getReplies().size())
                    .isEdited(comment.isEdited())
                    .createdAt(comment.getCreatedAt())
                    .lastModifiedAt(comment.getLastModifiedAt())
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @Builder
    @Schema(description = "베스트순 댓글 조회 응답")
    public static class BestCommentResponse {

        @Schema(description = "댓글 id", example = "1")
        private Long id;

        @Schema(description = "해당 댓글에 맞는 톡픽 id", example = "1")
        private Long talkPickId;

        @Schema(description = "해당 댓글에 맞는 톡픽 제목", example = "메시 vs 호날두")
        private String talkPickTitle;

        @Schema(description = "댓글 작성자", example = "운영자1")
        private String nickname;

        @Schema(description = "댓글 작성자 프로필 이미지", example = "https://balancetalk.com/profile/1")
        private String profileImage;

        @Schema(description = "댓글 내용", example = "너는나를존중해야한다나는발롱도르5개와수많은개인트로피를들어올렸으며"
                + "2016유로에서포르투갈을이끌고우승을차지했고동시에A매치역대최다득점자이다")
        private String content;

        @Schema(description = "해당 댓글 작성자가 투표한 선택지", example = "A")
        private VoteOption voteOption;

        @Schema(description = "댓글 좋아요 개수", example = "24")
        private int likesCount;

        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        private Boolean myLike;

        @Schema(description = "답글 수", example = "3")
        private int replyCount;

        @Schema(description = "베스트 댓글 여부", example = "true")
        private boolean isBest;

        @Schema(description = "댓글 수정 여부", example = "false")
        private boolean isEdited;

        @Schema(description = "댓글이 신고당한 횟수", example = "0")
        private int reportedCount;

        @Schema(description = "댓글 생성 날짜")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd hh:mm")
        private LocalDateTime createdAt;

        @Schema(description = "댓글 수정 날짜")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd hh:mm")
        private LocalDateTime lastModifiedAt;

        public static BestCommentResponse fromEntity(Comment comment, VoteOption voteOption, String profileImageUrl, int likesCount, boolean myLike) {
            return BestCommentResponse.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .profileImage(profileImageUrl)
                    .talkPickId(comment.getTalkPick().getId())
                    .talkPickTitle(comment.getTalkPick().getTitle())
                    .voteOption(voteOption)
                    .likesCount(likesCount)
                    .myLike(myLike)
                    .replyCount(comment.getReplies() == null ? 0 : comment.getReplies().size())
                    .isBest(comment.getIsBest())
                    .isEdited(comment.isEdited())
                    .createdAt(comment.getCreatedAt())
                    .lastModifiedAt(comment.getLastModifiedAt())
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @Builder
    @Schema(description = "답글 조회 응답")
    public static class CommentReplyResponse {

        @Schema(description = "댓글 id", example = "1")
        private Long id;

        @Schema(description = "해당 댓글에 맞는 톡픽 id", example = "1")
        private Long talkPickId;

        @Schema(description = "해당 댓글에 맞는 톡픽 제목", example = "메시 vs 호날두")
        private String talkPickTitle;

        @Schema(description = "댓글 작성자", example = "운영자1")
        private String nickname;

        @Schema(description = "댓글 작성자 프로필 이미지", example = "https://balancetalk.com/profile/1")
        private String profileImage;

        @Schema(description = "댓글 내용", example = "너는나를존중해야한다나는발롱도르5개와수많은개인트로피를들어올렸으며"
                + "2016유로에서포르투갈을이끌고우승을차지했고동시에A매치역대최다득점자이다")
        private String content;

        @Schema(description = "해당 댓글 작성자가 투표한 선택지", example = "A")
        private VoteOption voteOption;

        @Schema(description = "댓글 좋아요 개수", example = "24")
        private int likesCount;

        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        private Boolean myLike;

        @Schema(description = "부모 댓글 id (답글이 아닐 경우, null 반환)", example = "5")
        private Long parentId;

        @Schema(description = "답글 수", example = "3")
        private int replyCount;

        @Schema(description = "댓글 수정 여부", example = "false")
        private boolean isEdited;

        @Schema(description = "댓글이 신고당한 횟수", example = "0")
        private int reportedCount;

        @Schema(description = "댓글 생성 날짜")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd hh:mm")
        private LocalDateTime createdAt;

        @Schema(description = "댓글 수정 날짜")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd hh:mm")
        private LocalDateTime lastModifiedAt;

        public static CommentReplyResponse fromEntity(Comment comment, VoteOption voteOption, String profileImageUrl, int likesCount, boolean myLike) {
            return CommentReplyResponse.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .nickname(comment.getMember().getNickname())
                    .profileImage(profileImageUrl)
                    .talkPickId(comment.getTalkPick().getId())
                    .talkPickTitle(comment.getTalkPick().getTitle())
                    .voteOption(voteOption)
                    .likesCount(likesCount)
                    .myLike(myLike)
                    .parentId(comment.getParent() == null ? null : comment.getParent().getId())
                    .replyCount(comment.getReplies() == null ? 0 : comment.getReplies().size())
                    .isEdited(comment.isEdited())
                    .createdAt(comment.getCreatedAt())
                    .lastModifiedAt(comment.getLastModifiedAt())
                    .build();
        }
    }
}
