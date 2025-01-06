package balancetalk.talkpick.domain.repository;

import static balancetalk.global.utils.QuerydslUtils.getOrderSpecifiers;
import static balancetalk.talkpick.domain.QTalkPick.talkPick;
import static balancetalk.talkpick.dto.TalkPickDto.TalkPickResponse;
import static balancetalk.vote.domain.QTalkPickVote.talkPickVote;

import balancetalk.talkpick.domain.SummaryStatus;
import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.dto.QTalkPickDto_TalkPickResponse;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class TalkPickRepositoryImpl implements TalkPickRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TalkPick> findCandidateTodayTalkPicks(int topN, List<TalkPick> yesterdayTalkPicks) {
        return queryFactory
                .selectFrom(talkPick)
                .leftJoin(talkPick.votes, talkPickVote)
                .where(talkPick.notIn(yesterdayTalkPicks))
                .groupBy(talkPick.id)
                .orderBy(talkPick.views.desc(), talkPickVote.count().desc(), talkPick.createdAt.desc())
                .limit(topN)
                .fetch();
    }

    @Override
    public Page<TalkPickResponse> findPagedTalkPicks(Pageable pageable) {
        List<TalkPickResponse> content = queryFactory
                .select(new QTalkPickDto_TalkPickResponse(
                        talkPick.id, talkPick.title, talkPick.member.nickname,
                        talkPick.createdAt, talkPick.views, talkPick.bookmarks
                ))
                .from(talkPick)
                .orderBy(getOrderSpecifiers(talkPick, pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(talkPick.count())
                .from(talkPick);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<TalkPickResponse> findBestTalkPicks() {
        return queryFactory
                .select(new QTalkPickDto_TalkPickResponse(
                        talkPick.id, talkPick.title, talkPick.member.nickname,
                        talkPick.createdAt, talkPick.views, talkPick.bookmarks
                ))
                .from(talkPick)
                .orderBy(talkPick.views.desc(), talkPick.createdAt.desc())
                .limit(3)
                .fetch();
    }

    @Override
    public List<Long> findIdsBySummaryStatus(SummaryStatus summaryStatus) {
        return queryFactory.select(talkPick.id)
                .from(talkPick)
                .where(talkPick.summaryStatus.eq(summaryStatus))
                .fetch();
    }
}
