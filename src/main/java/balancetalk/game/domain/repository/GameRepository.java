package balancetalk.game.domain.repository;

import balancetalk.game.domain.Game;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameRepository extends JpaRepository<Game, Long> {


    @Query(value = """
         SELECT DISTINCT 
         g.*,
         gs.views AS gs_views,
         gs.created_at AS gs_created_at
         FROM game g
         JOIN game_set gs ON g.game_set_id = gs.id
         JOIN game_option go ON go.game_id = g.id
         WHERE 
             -- 완전 일치
             (gs.sub_tag = :query OR gs.title = :query OR g.description = :query 
              OR go.name = :query OR go.description = :query)
             OR
             -- 공백 제거 쿼리
             (gs.sub_tag = :queryWithoutSpaces OR gs.title = :queryWithoutSpaces 
              OR g.description = :queryWithoutSpaces OR go.name = :queryWithoutSpaces 
              OR go.description = :queryWithoutSpaces)
             OR
             -- 자연어 검색
             (MATCH(gs.sub_tag) AGAINST (:query IN NATURAL LANGUAGE MODE)
              OR MATCH(gs.title) AGAINST (:query IN NATURAL LANGUAGE MODE)
              OR MATCH(g.description) AGAINST (:query IN NATURAL LANGUAGE MODE)
              OR MATCH(go.name) AGAINST (:query IN NATURAL LANGUAGE MODE)
              OR MATCH(go.description) AGAINST (:query IN NATURAL LANGUAGE MODE))
         ORDER BY
             -- sort 파라미터가 'views'면 gs.views DESC,
             CASE WHEN :sort = 'views' THEN gs.views END DESC,
             -- sort 파라미터가 'createdAt'이면 gs.created_at DESC
             CASE WHEN :sort = 'createdAt' THEN gs.created_at END DESC
         """,
            countQuery = """
         SELECT COUNT(DISTINCT g.id)
         FROM game g
         JOIN game_set gs ON g.game_set_id = gs.id
         JOIN game_option go ON go.game_id = g.id
         WHERE 
             (gs.sub_tag = :query OR gs.title = :query OR g.description = :query 
              OR go.name = :query OR go.description = :query)
             OR
             (gs.sub_tag = :queryWithoutSpaces OR gs.title = :queryWithoutSpaces 
              OR g.description = :queryWithoutSpaces OR go.name = :queryWithoutSpaces 
              OR go.description = :queryWithoutSpaces)
             OR
             (MATCH(gs.sub_tag) AGAINST (:query IN NATURAL LANGUAGE MODE)
              OR MATCH(gs.title) AGAINST (:query IN NATURAL LANGUAGE MODE)
              OR MATCH(g.description) AGAINST (:query IN NATURAL LANGUAGE MODE)
              OR MATCH(go.name) AGAINST (:query IN NATURAL LANGUAGE MODE)
              OR MATCH(go.description) AGAINST (:query IN NATURAL LANGUAGE MODE))
         """,
            nativeQuery = true)
    Page<Game> searchAll(
            @Param("query") String query,
            @Param("queryWithoutSpaces") String queryWithoutSpaces,
            @Param("sort") String sort,
            Pageable pageable
    );

    @Query("""
    SELECT g FROM Game g
    WHERE g.id = (
        SELECT g2.id FROM GameVote gv
        JOIN gv.gameOption go
        JOIN go.game g2
        WHERE gv.member.id = :memberId
        AND g2.gameSet.id = g.gameSet.id
        ORDER BY gv.createdAt DESC
        LIMIT 1
    )
    ORDER BY (SELECT MAX(gv2.createdAt) FROM GameVote gv2 
              JOIN gv2.gameOption go2 
              JOIN go2.game g3 
              WHERE gv2.member.id = :memberId 
              AND g3.gameSet.id = g.gameSet.id) DESC
        """)
    List<Game> findLatestVotedGamesByMember(@Param("memberId") Long memberId);

}
