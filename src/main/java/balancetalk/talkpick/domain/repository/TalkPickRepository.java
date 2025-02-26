package balancetalk.talkpick.domain.repository;

import balancetalk.member.domain.Member;
import balancetalk.talkpick.domain.SummaryStatus;
import balancetalk.talkpick.domain.TalkPick;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalkPickRepository extends JpaRepository<TalkPick, Long>, TalkPickRepositoryCustom {

    Page<TalkPick> findAllByMemberOrderByEditedAtDesc(Member member, Pageable pageable);

    List<TalkPick> findAllBySummaryStatus(SummaryStatus summaryStatus);
}
