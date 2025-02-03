package balancetalk.vote.domain;

import balancetalk.game.domain.GameSet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface VoteRepository extends JpaRepository<GameVote, Long> {

    @Query("""
    SELECT gv FROM GameVote gv
    WHERE gv.member.id = :memberId
    AND gv.gameOption.id IN :gameOptionIds
    ORDER BY gv.createdAt DESC
    LIMIT 1""")
    GameVote findFirstByMemberIdAndGameOptionIdIn(@Param("memberId") Long memberId,
                                                  @Param("gameOptionIds") List<Long> gameOptionIds);
    void deleteAllByMemberIdAndGameOption_Game_GameSet(Long memberId, GameSet gameSet);
}
