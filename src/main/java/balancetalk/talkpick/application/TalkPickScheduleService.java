package balancetalk.talkpick.application;

import static balancetalk.talkpick.domain.SummaryStatus.FAIL;

import balancetalk.talkpick.domain.TalkPickSummarizer;
import balancetalk.talkpick.domain.repository.TalkPickRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TalkPickScheduleService {

    private final TalkPickRepository talkPickRepository;
    private final TalkPickSummarizer talkPickSummarizer;
    private final TodayTalkPickService todayTalkPickService;

//    @Scheduled(cron = "0 30 00 * * ?")
    public void retryFailedSummaries() {
        List<Long> summaryFailedTalkPickIds = talkPickRepository.findIdsBySummaryStatus(FAIL);
        for (Long summaryFailedTalkPickId : summaryFailedTalkPickIds) {
            talkPickSummarizer.summarizeTalkPick(summaryFailedTalkPickId);
        }
    }

    @Scheduled(cron = "0 00 00 * * ?")
    public void updateTodayTalkPick() {
        todayTalkPickService.updateTodayTalkPick();
    }
}
