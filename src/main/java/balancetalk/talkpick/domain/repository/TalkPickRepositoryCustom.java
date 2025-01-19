package balancetalk.talkpick.domain.repository;

import static balancetalk.talkpick.dto.TalkPickDto.TalkPickResponse;

import balancetalk.talkpick.domain.TalkPick;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TalkPickRepositoryCustom {

    List<TalkPick> findCandidateTodayTalkPicks(int topN, List<TalkPick> yesterdayTalkPicks);

    Page<TalkPickResponse> findPagedTalkPicks(Pageable pageable);

    List<TalkPickResponse> findBestTalkPicks();
}
