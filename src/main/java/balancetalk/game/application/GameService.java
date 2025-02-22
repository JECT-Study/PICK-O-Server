package balancetalk.game.application;

import static balancetalk.file.domain.FileType.GAME_OPTION;

import balancetalk.bookmark.domain.GameBookmark;
import balancetalk.file.domain.File;
import balancetalk.file.domain.FileHandler;
import balancetalk.file.domain.repository.FileRepository;
import balancetalk.game.domain.Game;
import balancetalk.game.domain.GameOption;
import balancetalk.game.domain.GameSet;
import balancetalk.game.domain.MainTag;
import balancetalk.game.domain.repository.GameSetRepository;
import balancetalk.game.domain.repository.MainTagRepository;
import balancetalk.game.dto.GameDto.CreateGameMainTagRequest;
import balancetalk.game.dto.GameDto.CreateOrUpdateGame;
import balancetalk.game.dto.GameDto.GameDetailResponse;
import balancetalk.game.dto.GameSetDto.CreateGameSetRequest;
import balancetalk.game.dto.GameSetDto.GameSetDetailResponse;
import balancetalk.game.dto.GameSetDto.GameSetResponse;
import balancetalk.game.dto.GameSetDto.UpdateGameSetRequest;
import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import balancetalk.member.dto.ApiMember;
import balancetalk.member.dto.GuestOrApiMember;
import balancetalk.vote.domain.GameVote;
import balancetalk.vote.domain.VoteOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameSetRepository gameSetRepository;
    private final MemberRepository memberRepository;
    private final MainTagRepository mainTagRepository;
    private final FileRepository fileRepository;
    private final FileHandler fileHandler;

    @Transactional
    public Long createBalanceGameSet(final CreateGameSetRequest request, final ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        MainTag mainTag = mainTagRepository.findByName(request.getMainTag())
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_MAIN_TAG));

        List<CreateOrUpdateGame> gameRequests = request.getGames();
        GameSet gameSet = request.toEntity(mainTag, member);
        List<Game> games = new ArrayList<>();

        for (CreateOrUpdateGame gameRequest : gameRequests) {
            Game game = gameRequest.toEntity();
            games.add(game);
        }

        gameSet.addGames(games);
        GameSet savedGameSet = gameSetRepository.save(gameSet);

        for (Game game : savedGameSet.getGames()) {
            for (GameOption gameOption : game.getGameOptions()) {
                relocateFileIfHasImage(gameOption);
            }
        }

        return savedGameSet.getId();
    }

    private void relocateFileIfHasImage(GameOption gameOption) {
        if (gameOption.hasImage()) {
            fileRepository.findById(gameOption.getImgId())
                    .ifPresentOrElse(
                            file -> fileHandler.relocateFile(file, gameOption.getId(), GAME_OPTION),
                            () -> {
                                throw new BalanceTalkException(ErrorCode.NOT_FOUND_FILE);
                            }
                );
        }
    }

    @Transactional
    public void updateBalanceGame(Long gameSetId, UpdateGameSetRequest request, ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        MainTag mainTag = mainTagRepository.findByName(request.getMainTag())
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_MAIN_TAG));
        GameSet gameSet = member.getGameSetById(gameSetId);

        List<Game> newGames = request.getGames().stream()
                .map(CreateOrUpdateGame::toEntity)
                .toList();
        List<Game> oldGames = gameSet.getGames();

        updateGameFiles(newGames, oldGames);

        gameSet.updateGameSetRequest(request.getTitle(), mainTag, request.getSubTag(), newGames);
    }

    private void updateGameFiles(List<Game> newGames, List<Game> oldGames) {
        for (int i = 0; i < 10; i++) {
            Game oldGame = oldGames.get(i);
            Game newGame = newGames.get(i);
            processGameFiles(oldGame, newGame);
        }
    }

    private void processGameFiles(Game oldGame, Game newGame) {
        for (int j = 0; j < 2; j++) {
            List<GameOption> oldGameOptions = oldGame.getGameOptions();
            List<GameOption> newGameOptions = newGame.getGameOptions();
            processGameOptionFiles(oldGameOptions.get(j), newGameOptions.get(j));
        }
    }

    private void processGameOptionFiles(GameOption oldGameOption, GameOption newGameOption) {
        if (oldGameOption.hasImage()) {
            if (isEqualsImgId(oldGameOption, newGameOption)) {
                // 기존 파일 유지
                return;
            }
            // 기존 파일 제거
            deleteOldFile(oldGameOption);
        }
        if (newGameOption.hasImage()) {
            // 새로운 파일 매핑
            relocateNewFile(oldGameOption, newGameOption);
        }
    }

    private boolean isEqualsImgId(GameOption oldGameOption, GameOption newGameOption) {
        return newGameOption.hasImage() && newGameOption.getImgId().equals(oldGameOption.getImgId());
    }

    private void deleteOldFile(GameOption oldGameOption) {
        File oldFile = fileRepository.findById(oldGameOption.getImgId())
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_FILE));
        fileHandler.deleteFile(oldFile);
    }

    private void relocateNewFile(GameOption oldGameOption, GameOption newGameOption) {
        File newFile = fileRepository.findById(newGameOption.getImgId())
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_FILE));
        fileHandler.relocateFile(newFile, oldGameOption.getId(), GAME_OPTION);
    }

    @Transactional
    public GameSetDetailResponse findBalanceGameSet(final Long gameSetId, final GuestOrApiMember guestOrApiMember) {
        GameSet gameSet = gameSetRepository.findById(gameSetId)
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_BALANCE_GAME_SET));
        gameSet.increaseViews();

        Map<Long, String> gameOptionImgUrls = getGameOptionImgUrls(gameSet); // 게임 id, 이미지 url을 가진 맵을 생성

        if (guestOrApiMember.isGuest()) { // 비회원인 경우
            // 게스트인 경우 북마크, 선택 옵션 없음
            return GameSetDetailResponse.fromEntity(gameSet, null, false,
                    gameSet.getGames().stream()
                            .map(game -> GameDetailResponse.fromEntity(game, false, null, gameOptionImgUrls))
                            .toList());
        }

        Member member = guestOrApiMember.toMember(memberRepository);
        List<Game> games = gameSet.getGames();

        GameBookmark gameBookmark = member.getGameBookmarkOf(gameSet)
                .orElse(null);

        Map<Long, VoteOption> voteOptionMap = new ConcurrentHashMap<>();

        boolean isEndGameSet = (gameBookmark != null) && gameBookmark.getIsEndGameSet();

        for (Game game : games) {
            Optional<GameVote> myVote = member.getVoteOnGameOption(member, game);
            myVote.ifPresent(gameVote -> voteOptionMap.put(game.getId(), gameVote.getVoteOption()));
        }

        return GameSetDetailResponse.fromEntity(gameSet, gameBookmark, isEndGameSet,
                gameSet.getGames().stream()
                        .map(game -> GameDetailResponse.fromEntity(
                                game,
                                isBookmarkedActiveForGame(gameBookmark, game),
                                voteOptionMap.get(game.getId()), gameOptionImgUrls))
                        .toList());
    }

    @Transactional(readOnly = true)
    public Long findRandomGame() {
        return gameSetRepository.findRandomGameSetId();
    }

    private List<Long> getResourceIds(GameSet gameSet) {
        return gameSet.getGames().stream()
                .flatMap(game -> game.getGameOptions().stream())
                .filter(option -> option.getImgId() != null)
                .map(GameOption::getId)
                .toList();
    }

    private List<File> getFilesByResourceIds(GameSet gameSet) {
        List<Long> resourceIds = getResourceIds(gameSet);
        return fileRepository.findAllByResourceIdsAndFileType(resourceIds, GAME_OPTION);
    }

    private Map<Long, String> getGameOptionImgUrls(GameSet gameSet) {
        List<File> files = getFilesByResourceIds(gameSet);
        return files.stream()
                .collect(Collectors.toMap(File::getResourceId, File::getImgUrl));
    }

    public boolean isBookmarkedActiveForGame(GameBookmark gameBookmark, Game game) {
        return gameBookmark != null
                && gameBookmark.getGameId().equals(game.getId())
                && gameBookmark.isActive();
    }

    @Transactional
    public void deleteBalanceGameSet(final Long gameSetId, final ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        GameSet gameSet = member.getGameSetById(gameSetId);
        gameSetRepository.delete(gameSet);
        List<Long> gameOptionIds = gameSet.getGameOptionIds();
        deleteFiles(gameOptionIds);
    }

    private void deleteFiles(List<Long> gameOptionIds) {
        List<File> files = fileRepository.findAllByResourceIdsAndFileType(gameOptionIds, GAME_OPTION);
        if (files.isEmpty()) {
            return;
        }
        fileHandler.deleteFiles(files);
    }

    @Transactional(readOnly = true)
    public List<GameSetResponse> findLatestGames(final String tagName, final Pageable pageable,
                                                 final GuestOrApiMember guestOrApiMember) {
        List<GameSet> gameSets = gameSetRepository.findGamesByCreationDate(tagName, pageable);
        return gameSetResponses(guestOrApiMember, gameSets);
    }

    @Transactional(readOnly = true)
    public List<GameSetResponse> findPopularGames(
            final String tagName,
            final Pageable pageable,
            final GuestOrApiMember guestOrApiMember
    ) {
        if (tagName != null) {
            List<GameSet> gameSets = gameSetRepository.findGamesByViews(tagName, pageable);
            return gameSetResponses(guestOrApiMember, gameSets);
        }
        List<GameSet> popularGames = gameSetRepository.findPopularGames(pageable);
        return gameSetResponses(guestOrApiMember, popularGames);
    }

    private List<GameSetResponse> gameSetResponses(GuestOrApiMember guestOrApiMember, List<GameSet> gameSets) {
        if (guestOrApiMember.isGuest()) {
            return gameSets.stream()
                    .map(gameSet -> GameSetResponse.fromEntity(gameSet, null, getFirstGameImages(gameSet)))
                    .toList();
        }
        Member member = guestOrApiMember.toMember(memberRepository);

        return gameSets.stream()
                .map(gameSet -> GameSetResponse.fromEntity(gameSet, member, getFirstGameImages(gameSet)))
                .toList();
    }

    private List<String> getFirstGameImages(GameSet gameSet) {
        Game firstGame = gameSet.getGames().get(0);
        List<Long> resourceIds = new ArrayList<>();
        List<GameOption> gameOptions = firstGame.getGameOptions();
        for (GameOption gameOption : gameOptions) {
            if (gameOption.hasImage()) {
                resourceIds.add(gameOption.getId());
            }
        }

        if (resourceIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<File> files = fileRepository.findAllByResourceIdsAndFileType(resourceIds, GAME_OPTION);
        return files.stream()
                .map(File::getImgUrl)
                .toList();
    }

    @Transactional
    public void createGameMainTag(final CreateGameMainTagRequest request, final ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        if (member.isRoleUser()) {
            throw new BalanceTalkException(ErrorCode.FORBIDDEN_MAIN_TAG_CREATE);
        }

        boolean hasGameTag = mainTagRepository.existsByName(request.getName());
        if (hasGameTag) {
            throw new BalanceTalkException(ErrorCode.ALREADY_REGISTERED_TAG);
        }
        MainTag mainTag = request.toEntity();
        mainTagRepository.save(mainTag);
    }
}
