package balancetalk.vote.application;

import static balancetalk.global.notification.domain.NotificationMessage.TALK_PICK_RATIO_2_1;
import static balancetalk.global.notification.domain.NotificationMessage.TALK_PICK_RATIO_3_1;
import static balancetalk.global.notification.domain.NotificationMessage.TALK_PICK_VOTE;
import static balancetalk.global.notification.domain.NotificationMessage.TALK_PICK_VOTE_100;
import static balancetalk.global.notification.domain.NotificationMessage.TALK_PICK_VOTE_1000;
import static balancetalk.global.notification.domain.NotificationMessage.VOTE_RATIO_COUNT_KEY;
import static balancetalk.global.notification.domain.NotificationStandard.FIRST_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.FIRST_STANDARD_OF_VOTE_RATIO;
import static balancetalk.global.notification.domain.NotificationStandard.FIRST_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.FIRST_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.FOURTH_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.SECOND_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.SECOND_STANDARD_OF_VOTE_RATIO;
import static balancetalk.global.notification.domain.NotificationStandard.SECOND_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.SECOND_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.THIRD_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.THIRD_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.THIRD_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationTitleCategory.MY_PICK;
import static balancetalk.global.notification.domain.NotificationTitleCategory.WRITTEN_TALK_PICK;
import static balancetalk.vote.domain.VoteOption.A;
import static balancetalk.vote.domain.VoteOption.B;

import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.global.notification.application.NotificationService;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import balancetalk.member.dto.ApiMember;
import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.domain.TalkPickReader;
import balancetalk.vote.domain.TalkPickVote;
import balancetalk.vote.domain.TalkPickVoteRepository;
import balancetalk.vote.domain.VoteOption;
import balancetalk.vote.dto.VoteTalkPickDto.VoteRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteTalkPickService {

    private final TalkPickReader talkPickReader;
    private final TalkPickVoteRepository voteRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Transactional
    public void createVote(long talkPickId, VoteRequest request, ApiMember apiMember) {
        TalkPick talkPick = talkPickReader.readById(talkPickId);
        Member member = apiMember.toMember(memberRepository);
        if (member.hasVotedTalkPick(talkPick)) {
            throw new BalanceTalkException(ErrorCode.ALREADY_VOTE);
        }

        voteRepository.save(request.toEntity(member, talkPick));

        sendVoteTalkPickNotification(talkPick);
        sendVoteTalkPickRatioNotification(talkPick);
    }

    @Transactional
    public void updateVote(long talkPickId, VoteRequest request, ApiMember apiMember) {
        TalkPick talkPick = talkPickReader.readById(talkPickId);
        Member member = apiMember.toMember(memberRepository);

        TalkPickVote vote = member.getVoteOnTalkPick(talkPick)
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_VOTE));

        vote.updateVoteOption(request.getVoteOption());

        talkPick.getComments().stream()
                .filter(comment -> comment.getMember().equals(member))
                        .forEach(comment -> comment.updateVoteOption(request.getVoteOption()));

        sendVoteTalkPickRatioNotification(talkPick);
    }

    @Transactional
    public void deleteVote(long talkPickId, ApiMember apiMember) {
        TalkPick talkPick = talkPickReader.readById(talkPickId);
        Member member = apiMember.toMember(memberRepository);

        TalkPickVote vote = member.getVoteOnTalkPick(talkPick)
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_VOTE));

        voteRepository.delete(vote);
        sendVoteTalkPickRatioNotification(talkPick);
    }

    private void sendVoteTalkPickNotification(TalkPick talkPick) {
        Member member = talkPick.getMember();
        long votedCount = talkPick.getVotes().size();
        String voteCountKey = "VOTE_" + votedCount;
        Map<String, Boolean> notificationHistory = talkPick.getNotificationHistory().mappingNotification();
        String category = WRITTEN_TALK_PICK.getCategory();

        boolean isMilestoneVoted = (votedCount == FIRST_STANDARD_OF_NOTIFICATION.getCount() ||
                votedCount == SECOND_STANDARD_OF_NOTIFICATION.getCount() ||
                votedCount == THIRD_STANDARD_OF_NOTIFICATION.getCount() ||
                (votedCount > THIRD_STANDARD_OF_NOTIFICATION.getCount() &&
                        votedCount % THIRD_STANDARD_OF_NOTIFICATION.getCount() == 0) ||
                (votedCount > FOURTH_STANDARD_OF_NOTIFICATION.getCount() &&
                        votedCount % FOURTH_STANDARD_OF_NOTIFICATION.getCount() == 0));

        // 투표 개수가 10, 50, 100*n개, 1000*n개 일 때 알림
        if (isMilestoneVoted && !notificationHistory.getOrDefault(voteCountKey, false)) {
            notificationService.sendTalkPickNotification(member, talkPick, category, TALK_PICK_VOTE.format(votedCount));
            // 투표 개수가 100개일 때 배찌 획득 알림
            if (votedCount == THIRD_STANDARD_OF_NOTIFICATION.getCount()) {
                notificationService.sendTalkPickNotification(member, talkPick, category, TALK_PICK_VOTE_100.getMessage());
            }
            // 투표 개수가 1000개일 때 배찌 획득 알림
            else if (votedCount == FOURTH_STANDARD_OF_NOTIFICATION.getCount()) {
                notificationService.sendTalkPickNotification(member, talkPick, category, TALK_PICK_VOTE_1000.getMessage());
            }
            notificationHistory.put(voteCountKey, true);
            talkPick.getNotificationHistory().setNotificationHistory(notificationHistory);
        }
    }

    private void sendVoteTalkPickRatioNotification(TalkPick talkPick) {
        long votesCountOfA = talkPick.votesCountOf(A);
        long votesCountOfB = talkPick.votesCountOf(B);
        long totalVotesCount = votesCountOfA + votesCountOfB;

        if (votesCountOfB > 0) {  // B에 대한 투표수가 0일 경우 대비
            double ratioAtoB = (double) votesCountOfA / votesCountOfB;
            notifyMembersBasedOnRatio(talkPick, A, ratioAtoB, totalVotesCount);
        }

        if (votesCountOfA > 0) {  // A에 대한 투표수가 0일 경우 대비
            double ratioBtoA = (double) votesCountOfB / votesCountOfA;
            notifyMembersBasedOnRatio(talkPick, B, ratioBtoA, totalVotesCount);
        }
    }

    private void notifyMembersBasedOnRatio(TalkPick talkPick, VoteOption voteOption, double ratio, long totalVotesCount) {
        Map<String, Boolean> notificationHistory = talkPick.getNotificationHistory().mappingNotification();
        long countKey = 0;
        String voteRatioCountKey = "";
        String category = MY_PICK.getCategory();

        if (totalVotesCount >= FIRST_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION.getCount()
                && totalVotesCount < SECOND_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION.getCount()) {
            countKey = FIRST_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION.getCount();
            voteRatioCountKey = VOTE_RATIO_COUNT_KEY.format("2:1", countKey);
        } else if (totalVotesCount >= SECOND_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION.getCount()
                && totalVotesCount <= THIRD_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION.getCount()) {
            countKey = SECOND_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION.getCount();
            voteRatioCountKey = VOTE_RATIO_COUNT_KEY.format("2:1", countKey);
        } else if (totalVotesCount >= THIRD_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION.getCount()) {
            countKey = THIRD_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION.getCount();
            voteRatioCountKey = VOTE_RATIO_COUNT_KEY.format("2:1", countKey);
        }


        if (ratio >= SECOND_STANDARD_OF_VOTE_RATIO.getCount()) {
            if (totalVotesCount >= FIRST_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION.getCount()
                    && totalVotesCount < SECOND_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION.getCount()) {
                countKey = FIRST_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION.getCount();
            } else if (totalVotesCount >= SECOND_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION.getCount()
                    && totalVotesCount < THIRD_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION.getCount()) {
                countKey = SECOND_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION.getCount();
            } else if (totalVotesCount >= THIRD_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION.getCount()) {
                countKey = THIRD_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION.getCount();
            }
            voteRatioCountKey = VOTE_RATIO_COUNT_KEY.format("3:1", countKey);
        }

        // 2:1 비율 알림
        if (ratio >= FIRST_STANDARD_OF_VOTE_RATIO.getCount() &&
                (totalVotesCount >= FIRST_STANDARD_OF_VOTE_RATIO_2_1_NOTIFICATION.getCount()) &&
                !notificationHistory.getOrDefault(voteRatioCountKey + voteOption.name(), false)) {

            List<Member> members = getMembersWhoVotedForOption(talkPick, voteOption);

            for (Member member : members) {
                // 사용자가 선택한 선택지에 따라 옵션 결정
                String optionMessage = voteOption == A ? talkPick.getOptionA() : talkPick.getOptionB();

                sendNotificationToMembers(member, talkPick, category, TALK_PICK_RATIO_2_1.format(category, optionMessage));
            }
            notificationHistory.put(voteRatioCountKey + voteOption.name(), true);
        }

        // 3:1 비율 알림
        if (ratio >= SECOND_STANDARD_OF_VOTE_RATIO.getCount()
                && (totalVotesCount >= FIRST_STANDARD_OF_VOTE_RATIO_3_1_NOTIFICATION.getCount())
                && !notificationHistory.getOrDefault(voteRatioCountKey + voteOption.name(), false)) {

            List<Member> members = getMembersWhoVotedForOption(talkPick, voteOption);

            for (Member member : members) {
                // 사용자가 선택한 선택지에 따라 옵션 결정
                String optionMessage = voteOption == A ? talkPick.getOptionA() : talkPick.getOptionB();

                sendNotificationToMembers(member, talkPick, category, TALK_PICK_RATIO_3_1.format(category, optionMessage));
            }
            notificationHistory.put(voteRatioCountKey + voteOption.name(), true);
        }

        talkPick.getNotificationHistory().setNotificationHistory(notificationHistory);
    }

    private List<Member> getMembersWhoVotedForOption(TalkPick talkPick, VoteOption option) {
        return talkPick.getVotes().stream()
                .filter(vote -> vote.getVoteOption().equals(option))
                .map(TalkPickVote::getMember)
                .toList();
    }

    private void sendNotificationToMembers(Member member, TalkPick talkPick, String category, String message) {
        notificationService.sendTalkPickNotification(member, talkPick, category, message);
    }
}
