package balancetalk.bookmark.application;

import static balancetalk.global.notification.domain.NotificationMessage.GAME_BOOKMARK;
import static balancetalk.global.notification.domain.NotificationMessage.GAME_BOOKMARK_100;
import static balancetalk.global.notification.domain.NotificationMessage.GAME_BOOKMARK_1000;
import static balancetalk.global.notification.domain.NotificationStandard.FIRST_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.FOURTH_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.SECOND_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationStandard.THIRD_STANDARD_OF_NOTIFICATION;
import static balancetalk.global.notification.domain.NotificationTitleCategory.WRITTEN_GAME;

import balancetalk.bookmark.domain.GameBookmark;
import balancetalk.bookmark.domain.BookmarkGenerator;
import balancetalk.bookmark.domain.GameBookmarkRepository;
import balancetalk.game.domain.Game;
import balancetalk.game.domain.GameSet;
import balancetalk.game.domain.GameReader;
import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.global.notification.application.NotificationService;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import balancetalk.member.dto.ApiMember;
import balancetalk.vote.domain.VoteRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkGameService {

    private final GameReader gameReader;
    private final GameBookmarkRepository gameBookmarkRepository;
    private final BookmarkGenerator bookmarkGenerator;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final VoteRepository voteRepository;

    public void createBookmark(final Long gameSetId, Long gameId, final ApiMember apiMember) {
        GameSet gameSet = gameReader.findGameSetById(gameSetId);
        Member member = apiMember.toMember(memberRepository);

        if (member.isMyGameSet(gameSet)) {
            throw new BalanceTalkException(ErrorCode.CANNOT_BOOKMARK_MY_RESOURCE);
        }

        // 밸런스게임 세트, 게임 아이디가 모두 일치한다면 예외 처리
        if (member.hasBookmarked(gameSet, gameId)) {
            throw new BalanceTalkException(ErrorCode.ALREADY_BOOKMARKED);
        }

        // 해당 gameSet에 gameId가 존재하는지 확인
        if (!gameSet.hasGame(gameId)) {
            throw new BalanceTalkException(ErrorCode.NOT_FOUND_BALANCE_GAME_THAT_GAME_SET);
        }

        // 해당 멤버가 가진 GameSet 북마크 중, resourceId가 gameSetId와 일치하는 북마크가 있다면
        member.getGameBookmarkOf(gameSet)
                .ifPresentOrElse(
                        bookmark -> {
                            increaseBookmarkCountForActivation(bookmark, gameSet);
                            bookmark.activate();
                            bookmark.setIsEndGameSet(false); // 밸런스게임 세트 종료 표시 해제
                            bookmark.updateGameId(gameId); //gameId도 업데이트
                        },
                        () -> { // resourceId가 gameSetId와 일치하는 북마크가 없다면 새로 생성
                            gameBookmarkRepository.save(bookmarkGenerator.generate(gameSet, gameId, member));
                            gameSet.increaseBookmarks();
                            sendBookmarkGameNotification(gameSet);
                        });
    }

    private void increaseBookmarkCountForActivation(GameBookmark bookmark, GameSet gameSet) {
        if (!bookmark.isActive()) {
            gameSet.increaseBookmarks();
        }
    }

    public void createEndGameSetBookmark(final Long gameSetId, final ApiMember apiMember) {
        GameSet gameSet = gameReader.findGameSetById(gameSetId);
        Member member = apiMember.toMember(memberRepository);

        if (member.isMyGameSet(gameSet)) {
            throw new BalanceTalkException(ErrorCode.CANNOT_BOOKMARK_MY_RESOURCE);
        }

        // gameId를 해당 밸런스게임 세트의 첫 번째 밸런스게임 id로 설정
        long gameId = getFirstGameIdOrThrow(gameSet);

        // 해당 멤버가 가진 GameSet 북마크 중, resourceId가 gameSetId와 일치하는 북마크가 있다면
        member.getGameBookmarkOf(gameSet)
                .ifPresentOrElse(
                        bookmark -> {
                            increaseBookmarkCountForActivation(bookmark, gameSet);
                            bookmark.activate();
                            bookmark.setIsEndGameSet(true); // 밸런스게임 세트 종료 표시
                            voteRepository.updateVotesAsInactive(member.getId(), gameSet);
                            bookmark.updateGameId(gameId); //gameId도 업데이트
                        },
                        () -> { // resourceId가 gameSetId와 일치하는 북마크가 없다면 새로 생성
                            gameBookmarkRepository.save(bookmarkGenerator.generate(gameSet, gameId, member));
                            gameSet.increaseBookmarks();
                            sendBookmarkGameNotification(gameSet);
                        });
    }

    private Long getFirstGameIdOrThrow(GameSet gameSet) {
        return gameSet.getGames().stream()
                .findFirst()
                .map(Game::getId)
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_BALANCE_GAME));
    }

    public void deleteBookmark(final Long gameSetId, final ApiMember apiMember) {
        GameSet gameSet = gameReader.findGameSetById(gameSetId);
        Member member = apiMember.toMember(memberRepository);

        GameBookmark bookmark = member.getGameBookmarkOf(gameSet)
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_BOOKMARK));

        if (!bookmark.isActive()) {
            throw new BalanceTalkException(ErrorCode.ALREADY_DELETED_BOOKMARK);
        }
        bookmark.deactivate();
        bookmark.setIsEndGameSet(false);
        gameSet.decreaseBookmarks();
        sendBookmarkGameNotification(gameSet);
    }

    private void sendBookmarkGameNotification(GameSet gameSet) {
        Member member = gameSet.getMember();
        long bookmarkedCount = gameSet.getBookmarks();
        String bookmarkCountKey = "BOOKMARK_" + bookmarkedCount;
        Map<String, Boolean> notificationHistory = gameSet.getNotificationHistory().mappingNotification();
        String category = WRITTEN_GAME.getCategory();

        boolean isMilestoneBookmarked = (bookmarkedCount == FIRST_STANDARD_OF_NOTIFICATION.getCount() ||
                bookmarkedCount == SECOND_STANDARD_OF_NOTIFICATION.getCount() ||
                bookmarkedCount == THIRD_STANDARD_OF_NOTIFICATION.getCount() ||
                (bookmarkedCount > THIRD_STANDARD_OF_NOTIFICATION.getCount() &&
                        bookmarkedCount % THIRD_STANDARD_OF_NOTIFICATION.getCount() == 0) ||
                (bookmarkedCount > FOURTH_STANDARD_OF_NOTIFICATION.getCount() &&
                        bookmarkedCount % FOURTH_STANDARD_OF_NOTIFICATION.getCount() == 0));

        // 북마크 개수가 10, 50, 100*n개, 1000*n개 일 때 알림
        if (isMilestoneBookmarked && !notificationHistory.getOrDefault(bookmarkCountKey, false)) {
            notificationService.sendGameNotification(member, gameSet, category, GAME_BOOKMARK.format(bookmarkedCount));
            // 북마크 개수가 100개일 때 배찌 획득 알림
            if (bookmarkedCount == THIRD_STANDARD_OF_NOTIFICATION.getCount()) {
                notificationService.sendGameNotification(member, gameSet, category, GAME_BOOKMARK_100.getMessage());
            }
            // 북마크 개수가 1000개일 때 배찌 획득 알림
            else if (bookmarkedCount == FOURTH_STANDARD_OF_NOTIFICATION.getCount()) {
                notificationService.sendGameNotification(member, gameSet, category, GAME_BOOKMARK_1000.getMessage());
            }
            notificationHistory.put(bookmarkCountKey, true);
            gameSet.getNotificationHistory().setNotificationHistory(notificationHistory);
        }
    }
}
