package balancetalk.game.application;

import static balancetalk.file.domain.FileType.TEMP_GAME_OPTION;
import static balancetalk.game.dto.TempGameDto.CreateTempGameRequest;

import balancetalk.file.domain.File;
import balancetalk.file.domain.FileHandler;
import balancetalk.file.domain.repository.FileRepository;
import balancetalk.game.domain.MainTag;
import balancetalk.game.domain.TempGame;
import balancetalk.game.domain.TempGameOption;
import balancetalk.game.domain.TempGameSet;
import balancetalk.game.domain.repository.MainTagRepository;
import balancetalk.game.domain.repository.TempGameSetRepository;
import balancetalk.game.dto.TempGameOptionDto;
import balancetalk.game.dto.TempGameSetDto.CreateTempGameSetRequest;
import balancetalk.game.dto.TempGameSetDto.TempGameSetResponse;
import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import balancetalk.member.dto.ApiMember;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TempGameService {

    private final MemberRepository memberRepository;
    private final TempGameSetRepository tempGameSetRepository;
    private final FileRepository fileRepository;
    private final FileHandler fileHandler;
    private final MainTagRepository mainTagRepository;

    @Transactional
    public void createTempGame(CreateTempGameSetRequest request, ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        MainTag mainTag = mainTagRepository.findByName(request.getMainTag())
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_MAIN_TAG));

        List<CreateTempGameRequest> tempGames = request.getTempGames();

        List<TempGame> newTempGames = request.getTempGames().stream()
        .map(game -> game.toEntity(fileRepository))
        .toList();

        if (member.hasTempGameSet()) { // 기존 임시저장이 존재하는 경우
            TempGameSet existGame = member.getTempGameSet();

            if (request.isNewRequest()) { // 새롭게 임시저장 하는 경우, 파일 모두 삭제
                List<Long> oldFileIds = existGame.getAllFileIds();
                fileRepository.deleteByResourceIdInAndFileType(oldFileIds, TEMP_GAME_OPTION);
            }

            // 새롭게 불러온 경우, 파일만 재배치 (isLoaded: true)
            relocateFiles(request, existGame);
            existGame.updateTempGameSet(request.getTitle(), request.getSubTag(), mainTag, newTempGames);
            return;
        }

        TempGameSet tempGameSet = request.toEntity(mainTag, member);
        List<TempGame> games = new ArrayList<>();

        for (CreateTempGameRequest tempGame : tempGames) {
            TempGame game = tempGame.toEntity(fileRepository);
            games.add(game);
        }
        tempGameSet.addGames(games);
        tempGameSetRepository.save(tempGameSet);
        processTempGameFiles(tempGames, tempGameSet.getTempGames());
    }

    private void relocateFiles(CreateTempGameSetRequest request, TempGameSet tempGameSet) {
        if (request.getAllFileIds().isEmpty()) {
            return;
        }

        List<Long> deletedFileIds = deleteFiles(request, tempGameSet);

        List<Long> newFileIds = getNewNonDuplicateFileIds(request, deletedFileIds, tempGameSet);

        relocateFiles(request, newFileIds, tempGameSet);
    }

    private void relocateFiles(CreateTempGameSetRequest request, List<Long> newFileIds, TempGameSet tempGameSet) {
        if (!newFileIds.isEmpty()) {
            Map<Long, Long> fileToOptionMap = tempGameSet.getFileToOptionMap(request, newFileIds);

            for (Map.Entry<Long, Long> entry : fileToOptionMap.entrySet()) {
                Long fileId = entry.getKey();
                Long tempGameOptionId = entry.getValue();

                File file = fileRepository.findById(fileId)
                        .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_FILE));
                fileHandler.relocateFile(file, tempGameOptionId, TEMP_GAME_OPTION);
            }
        }
    }

    private List<Long> getNewNonDuplicateFileIds(CreateTempGameSetRequest request,  List<Long> deletedFileIds, TempGameSet tempGameSet) {
        List<Long> existingFileIds = tempGameSet.getAllFileIds();
        return request.getAllFileIds().stream()
                .filter(fileId -> !deletedFileIds.contains(fileId))
                .filter(fileId -> !existingFileIds.contains(fileId))
                .toList();
    }

    private List<Long> deleteFiles(CreateTempGameSetRequest request, TempGameSet tempGameSet) {
        List<Long> existingFileIds = tempGameSet.getAllFileIds();

        List<Long> newFileIds = request.getAllFileIds();

        List<Long> filesIdsToDelete = existingFileIds.stream()
                .filter(existingFileId -> !newFileIds.contains(existingFileId))
                .toList();

        if (!filesIdsToDelete.isEmpty()) {
            List<File> filesToDelete = fileRepository.findAllById(filesIdsToDelete);
            fileHandler.deleteFiles(filesToDelete);
        }
        return filesIdsToDelete;
    }

    private void processTempGameFiles(List<CreateTempGameRequest> requests, List<TempGame> tempGames) {

        for (int i = 0; i < requests.size(); i++) {
            CreateTempGameRequest gameRequest = requests.get(i);
            List<TempGameOptionDto> tempGameOptions = gameRequest.getTempGameOptions();
            TempGame tempGame = tempGames.get(i);

            List<TempGameOption> gameOptions = tempGame.getTempGameOptions();

            for (int j = 0; j < tempGameOptions.size(); j++) {
                TempGameOptionDto tempGameOptionDto = tempGameOptions.get(j);
                TempGameOption tempGameOption = gameOptions.get(j);

                Long fileId = tempGameOptionDto.getFileId();
                Long gameOptionId = tempGameOption.getId();
                if (fileId != null) {
                    fileRepository.findById(fileId)
                        .ifPresent(file -> fileHandler.relocateFiles(Collections.singletonList(file),
                               gameOptionId, TEMP_GAME_OPTION));
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public TempGameSetResponse findTempGameSet(ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        TempGameSet tempGameSet = tempGameSetRepository.findByMember(member)
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_BALANCE_GAME_SET));
        return TempGameSetResponse.fromEntity(tempGameSet, fileRepository);
    }
}
