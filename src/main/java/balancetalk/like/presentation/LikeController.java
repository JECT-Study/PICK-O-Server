package balancetalk.like.presentation;

import balancetalk.like.application.CommentLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/talks")
@RequiredArgsConstructor
@Tag(name = "like", description = "좋아요 API")
public class LikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping("/{talkPickId}/{commentId}/likes")
    @Operation(summary = "댓글 좋아요", description = "commentId에 해당하는 댓글에 좋아요를 활성화합니다.")
    public void likeComment(@PathVariable Long commentId) {
        commentLikeService.likeComment(commentId);
    }

    @DeleteMapping("/{talkPickId}/{commentId}/likes")
    @Operation(summary = "댓글 좋아요 취소", description = "commentId에 해당하는 댓글의 좋아요를 취소합니다.")
    public void unlikeComment(@PathVariable Long commentId) { // TODO : 추후 talkPickId 파라미터를 받아 validate 필요
        commentLikeService.unLikeComment(commentId);
    }
}