package balancetalk.game.application;

import static balancetalk.file.domain.FileType.TEMP_GAME;

import balancetalk.file.domain.repository.FileRepository;
import balancetalk.game.domain.MainTag;
import balancetalk.game.domain.TempGame;
import balancetalk.game.domain.TempGameOption;
import balancetalk.game.domain.TempGameSet;
import balancetalk.game.domain.repository.GameTagRepository;
import balancetalk.game.domain.repository.TempGameSetRepository;
import balancetalk.game.dto.TempGameDto.CreateTempGameRequest;
import balancetalk.game.dto.TempGameDto.CreateTempGameSetRequest;
import balancetalk.game.dto.TempGameDto.TempGameOptionDto;
import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import balancetalk.member.dto.ApiMember;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TempGameService {
    private final MemberRepository memberRepository;
    private final TempGameSetRepository tempGameSetRepository;
    private final FileRepository fileRepository;
    private final GameTagRepository gameTagRepository;

    @Transactional
    public void createTempGame(CreateTempGameSetRequest request, ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        MainTag mainTag = gameTagRepository.findByName(request.getMainTag())
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_GAME_TOPIC));
//        if (member.hasTempGame()) {
//
//        }

        TempGameSet tempGameSet = request.toEntity(mainTag, member);
        List<TempGame> tempGames = tempGameSet.getTempGames();

        for (TempGame tempGame : tempGames) {
            tempGame.addTempGameSet(tempGameSet);
            List<TempGameOption> tempGameOptions = tempGame.getTempGameOptions();
            for (TempGameOption tempGameOption : tempGameOptions) {
                tempGameOption.addTempGame(tempGame);
            }
        }

        TempGameSet savedGameSet = tempGameSetRepository.save(tempGameSet);
        List<String> storedNames = extractStoredNames(request);
        updateFileResourceIdByStoredNames(savedGameSet.getId(), storedNames);
    }

    private void updateFileResourceIdByStoredNames(Long resourceId, List<String> storedNames) {
        fileRepository.updateResourceIdAndTypeByStoredNames(resourceId, TEMP_GAME, storedNames);
    }

    private List<String> extractStoredNames(CreateTempGameSetRequest request) {
        List<String> storedNames = new ArrayList<>();
        List<CreateTempGameRequest> tempGames = request.getTempGames();
        for (CreateTempGameRequest tempGame : tempGames) {
            List<TempGameOptionDto> tempGameOptions = tempGame.getTempGameOptions();
            for (TempGameOptionDto tempGameOption : tempGameOptions) {
                storedNames.add(tempGameOption.getStoredName());
            }
        }
        return storedNames;
    }
}
