package balancetalk.comment.domain;

import balancetalk.like.domain.LikeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByTalkPickIdAndParentIsNull(Long talkPickId, Pageable pageable);

    List<Comment> findAllByTalkPickIdAndParentIsNullOrderByCreatedAtDesc(Long talkPickId);

    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId " +
            "ORDER BY CASE WHEN c.member.id = :currentMemberId THEN 0 ELSE 1 END, c.createdAt ASC")
    List<Comment> findAllRepliesByParentIdOrderByMemberAndCreatedAt(@Param("parentId") Long parentId,
                                                               @Param("currentMemberId") Long currentMemberId);
    @Query("SELECT c FROM Comment c LEFT JOIN Like l ON c.id = l.resourceId AND l.likeType = :likeType " +
            "WHERE c.talkPick.id = :talkPickId AND c.parent IS NULL " +
            "GROUP BY c " +
            "ORDER BY COUNT(l) DESC, c.createdAt ASC")
    List<Comment> findByTalkPickIdAndParentIsNullOrderByLikesCountDescCreatedAtAsc(@Param("talkPickId") Long talkPickId,
                                                                     @Param("likeType") LikeType likeType);

    @Query("SELECT c FROM Comment c WHERE c.member.id = :memberId AND c.talkPick IS NOT NULL " +
            "AND c.editedAt IN (SELECT MAX(c2.editedAt) FROM Comment c2 WHERE c2.member.id = :memberId GROUP BY c2.talkPick.id) " +
            "ORDER BY c.editedAt DESC")
    Page<Comment> findAllLatestCommentsByMemberIdAndOrderByDesc(@Param("memberId") Long memberId, Pageable pageable);
}
