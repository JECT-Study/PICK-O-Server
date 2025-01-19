package balancetalk.talkpick.domain.event;

import balancetalk.talkpick.application.TalkPickFileService;
import balancetalk.talkpick.application.TalkPickSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TalkPickEventHandler {

    private final TalkPickSummaryService talkPickSummaryService;
    private final TalkPickFileService talkPickFileService;

    @TransactionalEventListener
    public void handleTalkPickCreatedEvent(TalkPickCreatedEvent event) {
        talkPickFileService.handleFilesOnTalkPickCreate(event.getFileIds(), event.getTalkPickId());
        talkPickSummaryService.summarizeTalkPick(event.getTalkPickId());
    }

    @TransactionalEventListener
    public void handleTalkPickUpdatedEvent(TalkPickUpdatedEvent event) {
        talkPickFileService.handleFilesOnTalkPickUpdate(
                event.getNewFileIds(), event.getDeleteFileIds(), event.getTalkPickId());
        talkPickSummaryService.summarizeTalkPick(event.getTalkPickId());
    }

    @TransactionalEventListener
    public void handleTalkPickDeletedEvent(TalkPickDeletedEvent event) {
        talkPickFileService.handleFilesOnTalkPickDelete(event.getTalkPickId());
    }
}
