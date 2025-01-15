package balancetalk.game.application;

import static balancetalk.global.exception.ErrorCode.BALANCE_GAME_SEARCH_BLANK;
import static balancetalk.global.exception.ErrorCode.BALANCE_GAME_SEARCH_LENGTH;

import balancetalk.file.domain.File;
import balancetalk.file.domain.FileType;
import balancetalk.file.domain.repository.FileRepository;
import balancetalk.game.domain.Game;
import balancetalk.game.domain.GameOption;
import balancetalk.game.domain.repository.GameRepository;
import balancetalk.game.dto.SearchGameResponse;
import balancetalk.global.exception.BalanceTalkException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchGameService {

    private final GameRepository gameRepository;
    private final FileRepository fileRepository;

    private static final int MINIMUM_SEARCH_LENGTH = 2;

    public Page<SearchGameResponse> search(String query, Pageable pageable, String sort) {

        validateQuery(query);
        String queryWithoutSpaces = removeSpaces(query);

        Page<Game> pageResult = gameRepository.searchAll(query, queryWithoutSpaces, sort, pageable);

        List<SearchGameResponse> responses = convertToResponse(pageResult.getContent());

        return new PageImpl<>(responses, pageable, pageResult.getTotalElements());
    }

    private void validateQuery(String query) {
        if (query.isBlank()) {
            throw new BalanceTalkException(BALANCE_GAME_SEARCH_BLANK);
        }

        if (query.replace(" ", "").length() < MINIMUM_SEARCH_LENGTH) {
            throw new BalanceTalkException(BALANCE_GAME_SEARCH_LENGTH);
        }
    }

    private String removeSpaces(String query) {
        return query.replaceAll("\\s+", ""); // 모든 공백 제거
    }

    private List<SearchGameResponse> convertToResponse(List<Game> games) {
        return games.stream()
                .map(game -> {
                    List<Long> resourceIds = getResourceIds(game);
                    List<File> files =
                            fileRepository.findAllByResourceIdsAndFileType(resourceIds, FileType.GAME_OPTION);
                    String imgA = files.isEmpty() ? null : game.getImgA(files);
                    String imgB = files.isEmpty() ? null : game.getImgB(files);
                    return SearchGameResponse.from(game, imgA, imgB);
                })
                .distinct() // 중복 제거
                .toList();
    }

    private List<Long> getResourceIds(Game game) {
        return game.getGameOptions().stream()
                .filter(option -> option.getImgId() != null)
                .map(GameOption::getImgId)
                .toList();
    }
}