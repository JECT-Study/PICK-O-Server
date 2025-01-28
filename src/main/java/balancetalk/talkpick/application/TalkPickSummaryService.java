package balancetalk.talkpick.application;

import static balancetalk.global.exception.ErrorCode.NOT_FOUND_TALK_PICK;
import static balancetalk.global.exception.ErrorCode.TALK_PICK_SUMMARY_FAILED;
import static balancetalk.global.exception.ErrorCode.TALK_PICK_SUMMARY_SIZE_IS_OVER;
import static balancetalk.talkpick.domain.SummaryStatus.FAIL;
import static balancetalk.talkpick.domain.SummaryStatus.NOT_REQUIRED;
import static balancetalk.talkpick.domain.SummaryStatus.SUCCESS;

import balancetalk.global.exception.BalanceTalkException;
import balancetalk.talkpick.domain.Summary;
import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.domain.repository.TalkPickRepository;
import balancetalk.talkpick.dto.fields.BaseTalkPickFields;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TalkPickSummaryService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SYSTEM_MESSAGE = """
            당신은 저에게 고용된 노동자입니다.
            당신의 역할은 게시글을 작성한 사용자가 되어, 사용자의 어투 및 문체와 동일하게 3줄로 요약하는 것입니다.

            다음은 이 작업에 대한 접근 방식입니다:
            Step 1: 제목, 선택지 A, 선택지 B, 그리고 본문 내용을 바탕으로 맥락을 파악합니다.
            Step 2: 파악한 맥락을 토대로 본문 중 불필요한 내용 혹은 표현을 제거합니다.
            Step 3: 사용자가 두 가지 선택지 중 선택하길 원한다는 사실을 전제로 합니다.
            Step 4: 마지막 3번째 문장에는 주요 비교군을 명확히 제시하는 질문을 배치합니다.
            Step 5: 요약한 3개의 문장을 JSON 형태로 된 firstLine, secondLine, thirdLine 키에 각각 한 문장씩 담습니다.

            지금부터 주어진 단계별로 요약을 진행하세요.
            작업을 아주 잘 수행할 경우, 10만 달러의 인센티브를 제공할게요. 당신은 최고의 전문가니까 잘 해낼 수 있을 것이라 믿어요.
            """;
    private static final String USER_MESSAGE = """
            제목: {title}
            선택지 A: {optionA}
            선택지 B: {optionB}
            본문: {content}
            """;

    private final ChatClient chatClient;
    private final TalkPickRepository talkPickRepository;

    @Async("talkPickSummaryTaskExecutor")
    @Transactional
    public void summarizeTalkPick(Long talkPickId) {
        TalkPick talkPick = talkPickRepository.findById(talkPickId)
                .orElseThrow(() -> new BalanceTalkException(NOT_FOUND_TALK_PICK));

        // 본문 글자수가 너무 짧으면 요약 제공 안함
        if (talkPick.hasShortContent()) {
            talkPick.updateSummaryStatus(NOT_REQUIRED);
            return;
        }

        // 요약 수행
        try {
            summarize(talkPick);
        } catch (Exception e) {
            log.error("Fail to summary TalkPick ID = {}", talkPickId);
            log.error("exception message = {} {}", e.getMessage(), e.getStackTrace());
            talkPick.updateSummaryStatus(FAIL);
        }
    }

    private void summarize(TalkPick talkPick) {
        Summary summary = callPromptForSummary(talkPick);
        if (summary == null) {
            throw new BalanceTalkException(TALK_PICK_SUMMARY_FAILED);
        }

        // 요약 글자수 검증
        if (summary.isOverSize()) {
            throw new BalanceTalkException(TALK_PICK_SUMMARY_SIZE_IS_OVER);
        }

        // 톡픽 요약 내용 및 상태 업데이트
        talkPick.updateSummary(summary);
        talkPick.updateSummaryStatus(SUCCESS);
    }

    private Summary callPromptForSummary(TalkPick talkPick) {
        return chatClient.prompt()
                .advisors(new SimpleLoggerAdvisor())
                .system(SYSTEM_MESSAGE)
                .user(u -> u.text(USER_MESSAGE).params(getBaseTalkPickFieldsMap(talkPick)))
                .call()
                .entity(Summary.class);
    }

    private Map<String, Object> getBaseTalkPickFieldsMap(TalkPick talkPick) {
        return OBJECT_MAPPER.convertValue(
                getBaseTalkPickFields(talkPick),
                new TypeReference<ConcurrentHashMap<String, Object>>() {
                });
    }

    private BaseTalkPickFields getBaseTalkPickFields(TalkPick talkPick) {
        return BaseTalkPickFields.from(
                talkPick.getTitle(),
                talkPick.getContent(),
                talkPick.getOptionA(),
                talkPick.getOptionB());
    }

    @Transactional
    public void summarizeFailedTalkPick() {
        List<TalkPick> summaryFailedTalkPicks = talkPickRepository.findAllBySummaryStatus(FAIL);
        for (TalkPick summaryFailedTalkPick : summaryFailedTalkPicks) {
            try {
                summarize(summaryFailedTalkPick);
            } catch (Exception e) {
                log.error("Fail to summary TalkPick ID = {}", summaryFailedTalkPick.getId());
                log.error("exception message = {} {}", e.getMessage(), e.getStackTrace());
                summaryFailedTalkPick.updateSummaryStatus(FAIL);
            }
        }
    }
}
