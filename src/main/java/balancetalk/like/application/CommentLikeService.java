package balancetalk.like.application;

import balancetalk.comment.domain.Comment;
import balancetalk.comment.domain.CommentRepository;
import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.notification.application.NotificationService;
import balancetalk.like.domain.Like;
import balancetalk.like.domain.LikeRepository;
import balancetalk.like.domain.LikeType;
import balancetalk.like.dto.LikeDto;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import balancetalk.member.dto.ApiMember;
import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.domain.repository.TalkPickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static balancetalk.global.exception.ErrorCode.*;
import static balancetalk.global.notification.domain.NotificationMessage.*;
import static balancetalk.global.notification.domain.NotificationStandard.FIRST_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.FOURTH_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.SECOND_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.THIRD_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationTitleCategory.OTHERS_TALK_PICK;
import static balancetalk.global.notification.domain.NotificationTitleCategory.WRITTEN_TALK_PICK;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentRepository commentRepository;

    private final LikeRepository likeRepository;

    private final MemberRepository memberRepository;

    private final TalkPickRepository talkPickRepository;

    private final NotificationService notificationService;

    @Transactional
    public void likeComment(Long commentId, Long talkPickId, ApiMember apiMember) {
        // 톡픽, 댓글, 회원 존재 여부 예외 처리
        validateTalkPick(talkPickId);
        Member member = apiMember.toMember(memberRepository);

        // 톡픽에 속한 댓글이 아닐 경우 예외 처리
        Comment comment = validateCommentByTalkPick(commentId, talkPickId);

        // 본인 댓글에는 좋아요 불가
        if (comment.getMember().getId().equals(member.getId())) {
            throw new BalanceTalkException(FORBIDDEN_LIKE_OWN_COMMENT);
        }

        // 이미 좋아요를 누른 댓글일 경우 예외 처리
        Optional<Like> existingLike = likeRepository.findByResourceIdAndMemberId(commentId, member.getId());

        if (existingLike.isPresent() && existingLike.get().getActive()) {
            throw new BalanceTalkException(ALREADY_LIKED_COMMENT);
        }

        // 이미 좋아요가 존재하지만 비활성화 상태인 경우 활성화 처리
        if (existingLike.isPresent()) {
            Like commentLike = existingLike.get();
            commentLike.activate();
        } else {
            Like commentLike = LikeDto.CreateLikeRequest.toEntity(commentId, member);
            likeRepository.save(commentLike);

            sendLikeNotification(comment);

        }
    }

    @Transactional
    public void unLikeComment(Long commentId, Long talkPickId, ApiMember apiMember) {
        validateTalkPick(talkPickId);
        Member member = apiMember.toMember(memberRepository);

        // 톡픽에 속한 댓글이 아닐 경우 예외 처리
        Comment comment = validateCommentByTalkPick(commentId, talkPickId);

        // 좋아요를 누르지 않은 댓글에 좋아요 취소를 누를 경우 예외 처리
        Like commentLike = likeRepository.findByResourceIdAndMemberId(comment.getId(), member.getId())
                .orElseThrow(() -> new BalanceTalkException(NOT_LIKED_COMMENT));

        if (!commentLike.getActive()) {
            throw new BalanceTalkException(NOT_LIKED_COMMENT);
        }

        commentLike.deActive();
    }

    private void validateTalkPick(Long talkPickId) {
        talkPickRepository.findById(talkPickId)
                .orElseThrow(() -> new BalanceTalkException(NOT_FOUND_TALK_PICK));
    }

    private Comment validateCommentByTalkPick(Long commentId, Long talkPickId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BalanceTalkException(NOT_FOUND_COMMENT));

        if (!comment.getTalkPick().getId().equals(talkPickId)) {
            throw new BalanceTalkException(NOT_FOUND_COMMENT_AT_THAT_TALK_PICK);
        }

        return comment;
    }

    private void sendLikeNotification(Comment comment) {
        for (int i = 2; i <= 99; i++) {
            System.out.println("(" + i + ", 2, 1, 'COMMENT', '2024-08-10 11:00:00.123425', '2024-08-10 11:00:00.123425', b'1'),");
        }
        long likeCount = likeRepository.countByResourceIdAndLikeType(comment.getId(), LikeType.COMMENT);
        Member member = comment.getMember();
        TalkPick talkPick = comment.getTalkPick();
        String likeCountKey = "LIKE_" + likeCount;
        Map<String, Boolean> notificationHistory = comment.getNotificationHistory().mappingNotification();
        String category = OTHERS_TALK_PICK.getCategory();

        if (member.equals(talkPick.getMember())) {
            category = WRITTEN_TALK_PICK.getCategory();
        }

        boolean isMilestoneLike = (likeCount == FIRST_STANDARD_OF_NOTIFICATION.getCount() ||
                likeCount == SECOND_STANDARD_OF_NOTIFICATION.getCount() ||
                likeCount == THIRD_STANDARD_OF_NOTIFICATION.getCount() ||
                (likeCount > THIRD_STANDARD_OF_NOTIFICATION.getCount() &&
                        likeCount % THIRD_STANDARD_OF_NOTIFICATION.getCount() == 0) ||
                (likeCount > FOURTH_STANDARD_OF_NOTIFICATION.getCount() &&
                        likeCount % FOURTH_STANDARD_OF_NOTIFICATION.getCount() == 0));

        // 좋아요 개수가 10, 50, 100*n개, 1000*n개 일 때 알림
        if (isMilestoneLike && !notificationHistory.getOrDefault(likeCountKey, false)) {
            notificationService.sendTalkPickNotification(member, talkPick, category, COMMENT_LIKE.format(likeCount));
            // 좋아요 개수가 100개일 때 배찌 획득 알림
            if (likeCount == THIRD_STANDARD_OF_NOTIFICATION.getCount()) {
                notificationService.sendTalkPickNotification(member, talkPick, category, COMMENT_LIKE_100.getMessage());
            }
            // 좋아요 개수가 1000개일 때 배찌 획득 알림
            else if (likeCount == FOURTH_STANDARD_OF_NOTIFICATION.getCount()) {
                notificationService.sendTalkPickNotification(member, talkPick, category, COMMENT_LIKE_1000.getMessage());
            }
            notificationHistory.put(likeCountKey, true);
            comment.getNotificationHistory().setNotificationHistory(notificationHistory);
        }
    }
}
