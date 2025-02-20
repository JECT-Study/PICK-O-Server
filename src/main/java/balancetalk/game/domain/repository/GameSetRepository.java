package balancetalk.game.domain.repository;

import balancetalk.game.domain.GameSet;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameSetRepository extends JpaRepository<GameSet, Long> ,GameSetRepositoryCustom {

    Page<GameSet> findAllByMemberIdOrderByEditedAtDesc(Long memberId, Pageable pageable);

    @Query("SELECT g FROM GameSet g " +
            "WHERE g.mainTag.name = :name " +
            "ORDER BY g.createdAt DESC")
    List<GameSet> findGamesByCreationDate(@Param("name") String mainTag, Pageable pageable);

    @Query("SELECT g FROM GameSet g " +
            "WHERE g.mainTag.name = :name " +
            "ORDER BY g.views DESC, g.createdAt DESC")
    List<GameSet> findGamesByViews(@Param("name") String mainTag, Pageable pageable);

    @Query("SELECT g FROM GameSet g "
            + "ORDER BY g.views DESC, "
            + "g.createdAt DESC")
    List<GameSet> findPopularGames(Pageable pageable);
}
