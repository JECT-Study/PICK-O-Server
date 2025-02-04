package balancetalk.vote.application;

import static balancetalk.global.notification.domain.NotificationMessage.GAME_VOTE;
import static balancetalk.global.notification.domain.NotificationMessage.GAME_VOTE_100;
import static balancetalk.global.notification.domain.NotificationMessage.GAME_VOTE_1000;
import static balancetalk.global.notification.domain.NotificationStandard.FIRST_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.FOURTH_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.SECOND_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.THIRD_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationTitleCategory.WRITTEN_GAME;

import balancetalk.game.domain.Game;
import balancetalk.game.domain.GameOption;
import balancetalk.game.domain.GameReader;
import balancetalk.game.domain.GameSet;
import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.global.notification.application.NotificationService;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import balancetalk.member.dto.ApiMember;
import balancetalk.vote.domain.GameVote;
import balancetalk.vote.domain.VoteRepository;
import balancetalk.vote.dto.VoteGameDto.VoteRequest;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class VoteGameService {

    private final GameReader gameReader;
    private final VoteRepository voteRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    public void createVote(Long gameId, VoteRequest request, ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        Game game = gameReader.findGameById(gameId);
        GameOption gameOption = getGameOption(game, request);

        if (member.hasVotedGame(game)) {
            throw new BalanceTalkException(ErrorCode.ALREADY_VOTE);
        }

        Optional<GameVote> existingVote = voteRepository.findByMemberIdAndGameId(member.getId(), gameId);

        if (existingVote.isPresent()) {
            // 기존 투표를 활성화하고 선택한 옵션을 변경
            GameVote vote = existingVote.get();
            vote.updateActive(true);
            vote.updateGameOption(gameOption);
        } else {
            // 새로운 투표 생성
            voteRepository.save(request.toEntity(member, gameOption));
        }
        gameOption.increaseVotesCount();
        sendVoteGameNotification(game.getGameSet());
    }

    public void updateVote(Long gameId, VoteRequest request, ApiMember apiMember) {
        Game game = gameReader.findGameById(gameId);
        Member member = apiMember.toMember(memberRepository);
        Optional<GameVote> voteOnGame = member.getVoteOnGame(game);

        if (voteOnGame.isEmpty()) {
            throw new BalanceTalkException(ErrorCode.NOT_FOUND_VOTE);
        }

        GameVote vote = voteOnGame.get();
        String previousVote = vote.getVoteOption().name();

        if (previousVote.equals(request.getVoteOption())) { // 수정하려는 투표가 이전과 동일할 때 예외처리
            throw new BalanceTalkException(ErrorCode.SAME_VOTE);
        }

        GameOption gameOption = getGameOption(game, request);

        vote.getGameOption().decreaseVotesCount(); // 이전 선택지의 투표수는 감소
        gameOption.increaseVotesCount(); // 바꾼 선택지의 투표수는 증가

        vote.updateGameOption(gameOption);
    }

    private GameOption getGameOption(Game game, VoteRequest request) {
        String requestOption = request.getVoteOption();
        return game.getGameOptions().stream()
                .filter(option -> {
                    String optionType = option.getOptionType().name();
                    return optionType.equals(requestOption);
                })
                .findFirst()
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_VOTE_OPTION));
    }

    public void deleteVote(Long gameId, ApiMember apiMember) {
        Game game = gameReader.findGameById(gameId);
        Member member = apiMember.toMember(memberRepository);

        Optional<GameVote> voteOnGame = member.getVoteOnGame(game);

        if (voteOnGame.isEmpty()) {
            throw new BalanceTalkException(ErrorCode.NOT_FOUND_VOTE);
        }

        GameVote vote = voteOnGame.get();
        vote.updateActive(false);
        vote.getGameOption().decreaseVotesCount(); // 해당 선택지의 투표수 감소
    }

    private void sendVoteGameNotification(GameSet gameSet) {
        Member member = gameSet.getMember();
        long votedCount = gameSet.getVotesCount();
        String voteCountKey = "VOTE_" + votedCount;
        Map<String, Boolean> notificationHistory = gameSet.getNotificationHistory().mappingNotification();
        String category = WRITTEN_GAME.getCategory();

        boolean isMilestoneVoted = (votedCount == FIRST_STANDARD_OF_NOTIFICATION.getCount() ||
                votedCount == SECOND_STANDARD_OF_NOTIFICATION.getCount() ||
                votedCount == THIRD_STANDARD_OF_NOTIFICATION.getCount() ||
                (votedCount > THIRD_STANDARD_OF_NOTIFICATION.getCount() &&
                        votedCount % THIRD_STANDARD_OF_NOTIFICATION.getCount() == 0) ||
                (votedCount > FOURTH_STANDARD_OF_NOTIFICATION.getCount() &&
                        votedCount % FOURTH_STANDARD_OF_NOTIFICATION.getCount() == 0));

        // 투표 개수가 10, 50, 100*n개, 1000*n개 일 때 알림
        if (isMilestoneVoted && !notificationHistory.getOrDefault(voteCountKey, false)) {
            notificationService.sendGameNotification(member, gameSet, category, GAME_VOTE.format(votedCount));
            // 투표 개수가 100개일 때 배찌 획득 알림
            if (votedCount == THIRD_STANDARD_OF_NOTIFICATION.getCount()) {
                notificationService.sendGameNotification(member, gameSet, category, GAME_VOTE_100.getMessage());
            }
            // 투표 개수가 1000개일 때 배찌 획득 알림
            else if (votedCount == FOURTH_STANDARD_OF_NOTIFICATION.getCount()) {
                notificationService.sendGameNotification(member, gameSet, category, GAME_VOTE_1000.getMessage());
            }
            notificationHistory.put(voteCountKey, true);
            gameSet.getNotificationHistory().setNotificationHistory(notificationHistory);
        }
    }
}
