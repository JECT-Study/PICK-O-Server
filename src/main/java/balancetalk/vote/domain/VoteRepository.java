package balancetalk.vote.domain;

import balancetalk.game.domain.GameSet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface VoteRepository extends JpaRepository<GameVote, Long> {

    @Query("""
    SELECT gv FROM GameVote gv
    WHERE gv.member.id = :memberId
    AND gv.isActive = true
    AND gv.gameOption.id IN :gameOptionIds
    ORDER BY gv.createdAt DESC
    LIMIT 1
""")
    GameVote findFirstByMemberIdAndGameOptionIdIn(@Param("memberId") Long memberId, @Param("gameOptionIds") List<Long> gameOptionIds);

    // [1] "내가 투표한 밸런스게임 목록" 전용: 비활성화 포함 (isActive 조건 제거)
    @Query("""
        SELECT gv
        FROM GameVote gv
        WHERE gv.member.id = :memberId
          AND gv.gameOption.id IN :gameOptionIds
        ORDER BY gv.createdAt DESC
        LIMIT 1
    """)
    GameVote findLatestVoteByMemberIdAndGameOptionIds(
            @Param("memberId") Long memberId,
            @Param("gameOptionIds") List<Long> gameOptionIds
    );

    // [2] 활성화된 투표만 조회 (기존 로직 그대로 사용 - 필요하면 유지)
    @Query("""
        SELECT gv
        FROM GameVote gv
        WHERE gv.member.id = :memberId
          AND gv.gameOption.game.id = :gameId
          AND gv.isActive = true
    """)
    Optional<GameVote> findActiveVoteByMemberIdAndGameId(@Param("memberId") Long memberId,
                                                         @Param("gameId") Long gameId);

    @Modifying
    @Query("""
        UPDATE GameVote gv SET gv.isActive = false 
        WHERE gv.member.id = :memberId AND gv.gameOption.game.gameSet = :gameSet
    """)
    void updateVotesAsInactive(@Param("memberId") Long memberId, @Param("gameSet") GameSet gameSet);

    // 특정 사용자가 특정 게임에 대해 투표한 기록 조회 (비활성화된 투표도 포함)
    @Query("SELECT gv FROM GameVote gv WHERE gv.member.id = :memberId AND gv.gameOption.game.id = :gameId")
    Optional<GameVote> findByMemberIdAndGameId(@Param("memberId") Long memberId, @Param("gameId") Long gameId);}
