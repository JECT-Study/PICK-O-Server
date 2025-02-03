package balancetalk.member.application;

import balancetalk.bookmark.domain.GameBookmark;
import balancetalk.bookmark.domain.TalkPickBookmark;
import balancetalk.bookmark.domain.GameBookmarkRepository;
import balancetalk.bookmark.domain.TalkPickBookmarkRepository;
import balancetalk.comment.domain.Comment;
import balancetalk.comment.domain.CommentRepository;
import balancetalk.file.domain.File;
import balancetalk.file.domain.FileType;
import balancetalk.file.domain.repository.FileRepository;
import balancetalk.game.domain.Game;
import balancetalk.game.domain.GameOption;
import balancetalk.game.domain.GameSet;
import balancetalk.game.domain.repository.GameRepository;
import balancetalk.game.domain.repository.GameSetRepository;
import balancetalk.game.dto.GameDto.GameMyPageResponse;
import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import balancetalk.member.dto.ApiMember;
import balancetalk.member.dto.MemberDto.MemberActivityResponse;
import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.domain.repository.TalkPickRepository;
import balancetalk.talkpick.dto.TalkPickDto.TalkPickMyPageResponse;
import balancetalk.vote.domain.TalkPickVote;
import balancetalk.vote.domain.TalkPickVoteRepository;
import balancetalk.vote.domain.GameVote;
import balancetalk.vote.domain.VoteRepository;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MemberRepository memberRepository;
    private final TalkPickRepository talkPickRepository;
    private final GameBookmarkRepository gameBookmarkRepository;
    private final TalkPickBookmarkRepository talkPickBookmarkRepository;
    private final TalkPickVoteRepository talkPickVoteRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final GameRepository gameRepository;
    private final GameSetRepository gameSetRepository;
    private final FileRepository fileRepository;

    public Page<TalkPickMyPageResponse> findAllBookmarkedTalkPicks(ApiMember apiMember, Pageable pageable) {
        Member member = apiMember.toMember(memberRepository);
        Page<TalkPickBookmark> bookmarks =
                talkPickBookmarkRepository.findActivatedByMemberOrderByDesc(member, pageable);

        List<TalkPickMyPageResponse> responses = bookmarks.stream()
                .map(bookmark -> {
                    TalkPick talkPick = bookmark.getTalkPick();
                    return TalkPickMyPageResponse.from(talkPick, bookmark);
                })
                .toList();

        return new PageImpl<>(responses, pageable, bookmarks.getTotalElements());
    }

    public Page<TalkPickMyPageResponse> findAllVotedTalkPicks(ApiMember apiMember, Pageable pageable) {
        Member member = apiMember.toMember(memberRepository);
        Page<TalkPickVote> votes = talkPickVoteRepository.findAllByMemberIdAndTalkPickDesc(member.getId(), pageable);

        List<TalkPickMyPageResponse> responses = votes.stream()
                .map(vote -> TalkPickMyPageResponse.from(vote.getTalkPick(), vote))
                .toList();

        return new PageImpl<>(responses, pageable, votes.getTotalElements());
    }

    public Page<TalkPickMyPageResponse> findAllCommentedTalkPicks(ApiMember apiMember, Pageable pageable) {
        Member member = apiMember.toMember(memberRepository);
        Page<Comment> comments =
                commentRepository.findAllLatestCommentsByMemberIdAndOrderByDesc(member.getId(), pageable);

        List<TalkPickMyPageResponse> responses = comments.stream()
                .map(comment -> TalkPickMyPageResponse.from(comment.getTalkPick(), comment))
                .toList();

        return new PageImpl<>(responses, pageable, comments.getTotalElements());
    }

    public Page<TalkPickMyPageResponse> findAllTalkPicksByMember(ApiMember apiMember, Pageable pageable) {
        Member member = apiMember.toMember(memberRepository);
        Page<TalkPick> talkPicks = talkPickRepository.findAllByMemberIdOrderByEditedAtDesc(member.getId(), pageable);

        List<TalkPickMyPageResponse> responses = talkPicks.stream()
                .map(TalkPickMyPageResponse::fromMyTalkPick)
                .toList();

        return new PageImpl<>(responses, pageable, talkPicks.getTotalElements());
    }

    public Page<GameMyPageResponse> findAllBookmarkedGames(ApiMember apiMember, Pageable pageable) {
        Member member = apiMember.toMember(memberRepository);
        Page<GameBookmark> bookmarks = gameBookmarkRepository.findActivatedByMemberOrderByDesc(member, pageable);

        List<GameMyPageResponse> responses = bookmarks.stream()
                .map(bookmark -> {
                    Game game = gameRepository.findById(bookmark.getGameId()) // 사용자가 북마크한 위치의 밸런스게임을 찾음
                            .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_BALANCE_GAME));

                    return createGameMyPageResponse(game, null, bookmark);
                })
                .toList();

        return new PageImpl<>(responses, pageable, bookmarks.getTotalElements());
    }

    private List<Long> getResourceIds(Game game) {
        return game.getGameOptions().stream()
                .filter(option -> option.getImgId() != null)
                .map(GameOption::getId)
                .toList();
    }


    public Page<GameMyPageResponse> findAllVotedGames(ApiMember apiMember, Pageable pageable) {
        Member member = apiMember.toMember(memberRepository);

        List<Game> latestGames = gameRepository.findLatestVotedGamesByMember(member.getId());

        List<GameMyPageResponse> responses = latestGames.stream()
                .map(game -> {
                    GameVote vote = voteRepository.findLatestVoteByMemberIdAndGameOptionIds(
                            member.getId(), game.getGameOptions().stream()
                                    .map(GameOption::getId)
                                    .toList()
                    );

                    if (vote == null) {
                        return null;
                    }

                    GameBookmark gameBookmark = gameBookmarkRepository.findByMemberAndGameSetId(member,
                                    game.getGameSet().getId())
                            .orElse(null);
                    return createGameMyPageResponse(game, gameBookmark, vote);
                })
                .filter(Objects::nonNull)
                .toList();


        return new PageImpl<>(responses, pageable, responses.size());
    }

    public Page<GameMyPageResponse> findAllGamesByMember(ApiMember apiMember, Pageable pageable) {
        Member member = apiMember.toMember(memberRepository);
        Page<GameSet> gameSets = gameSetRepository.findAllByMemberIdOrderByEditedAtDesc(member.getId(), pageable);

        List<GameMyPageResponse> responses = gameSets.stream()
                .map(gameSet -> {
                    Game game = gameSet.getGames().get(0);
                    GameBookmark gameBookmark = gameBookmarkRepository.findByMemberAndGameSetId(member, gameSet.getId())
                            .orElse(null);
                    return createGameMyPageResponse(game, gameBookmark, game);
                })
                .toList();

        return new PageImpl<>(responses, pageable, gameSets.getTotalElements());
    }

    private GameMyPageResponse createGameMyPageResponse(Game game, GameBookmark gameBookmark, Object source) {
        List<Long> resourceIds = getResourceIds(game);
        List<File> files = fileRepository.findAllByResourceIdsAndFileType(resourceIds, FileType.GAME_OPTION);
        String imgA = files.isEmpty() ? null : game.getImgA(files);
        String imgB = files.isEmpty() ? null : game.getImgB(files);

        return Stream.of(
                new SourceHandler<>(GameBookmark.class, bookmark
                        -> GameMyPageResponse.from(game, bookmark, imgA, imgB)),
                new SourceHandler<>(GameVote.class, vote -> GameMyPageResponse.from(game, gameBookmark,
                        vote, imgA, imgB)),
                new SourceHandler<>(Game.class, myGame -> GameMyPageResponse.from(myGame.getGameSet(),
                        gameBookmark, imgA, imgB))
        )
                .filter(handler -> handler.getType().isInstance(source))
                .findFirst()
                .map(handler -> handler.handle(source))
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.INVALID_SOURCE_TYPE));
    }

    public MemberActivityResponse getMemberActivity(ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        return MemberActivityResponse.fromEntity(member);
    }

    @Getter
    @AllArgsConstructor
    private static class SourceHandler<T> {
        private final Class<T> type;
        private final Function<T, GameMyPageResponse> handler;

        public GameMyPageResponse handle(Object source) {
            return handler.apply(type.cast(source));
        }
    }
}
