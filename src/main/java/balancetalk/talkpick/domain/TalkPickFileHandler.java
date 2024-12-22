package balancetalk.talkpick.domain;

import static balancetalk.file.domain.FileType.TALK_PICK;

import balancetalk.file.domain.File;
import balancetalk.file.domain.FileHandler;
import balancetalk.file.domain.repository.FileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TalkPickFileHandler {

    private final FileRepository fileRepository;
    private final FileHandler fileHandler;

    @Async
    @Retryable(backoff = @Backoff(delay = 1000))
    @Transactional
    public void handleFilesOnTalkPickCreate(List<Long> fileIds, Long talkPickId) {
        relocateFiles(fileIds, talkPickId);
    }

    private void relocateFiles(List<Long> fileIds, Long talkPickId) {
        List<File> files = fileRepository.findAllById(fileIds);
        fileHandler.relocateFiles(files, talkPickId, TALK_PICK);
    }

    public List<String> findImgUrlsBy(Long talkPickId) {
        return fileRepository.findImgUrlsByResourceIdAndFileType(talkPickId, TALK_PICK);
    }

    public List<Long> findFileIdsBy(Long talkPickId) {
        return fileRepository.findIdsByResourceIdAndFileType(talkPickId, TALK_PICK);
    }

    @Async
    @Retryable(backoff = @Backoff(delay = 1000))
    @Transactional
    public void handleFilesOnTalkPickUpdate(List<Long> newFileIds, List<Long> deleteFileIds, Long talkPickId) {
        deleteFiles(deleteFileIds);
        newFileIds.removeIf((deleteFileIds::contains));
        relocateFiles(newFileIds, talkPickId);
    }

    private void deleteFiles(List<Long> deleteFileIds) {
        if (deleteFileIds.isEmpty()) {
            return;
        }
        List<File> files = fileRepository.findAllById(deleteFileIds);
        fileHandler.deleteFiles(files);
    }

    @Async
    @Retryable(backoff = @Backoff(delay = 1000))
    @Transactional
    public void handleFilesOnTalkPickDelete(Long talkPickId) {
        if (notExistsFilesBy(talkPickId)) {
            return;
        }
        List<File> files = fileRepository.findAllByResourceIdAndFileType(talkPickId, TALK_PICK);
        fileHandler.deleteFiles(files);
    }

    private boolean notExistsFilesBy(Long talkPickId) {
        return !fileRepository.existsByResourceIdAndFileType(talkPickId, TALK_PICK);
    }
}
