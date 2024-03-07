package balancetalk.module.post.presentation;

import balancetalk.module.post.application.PostService;
import balancetalk.module.post.dto.PostRequest;
import balancetalk.module.post.dto.PostResponse;
import balancetalk.module.report.dto.ReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "post", description = "게시글 API")
public class PostController {

    private final PostService postService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "게시글 생성" , description = "로그인 상태인 회원이 게시글을 작성한다.")
    public PostResponse createPost(@RequestBody final PostRequest postRequestDto) {
        return postService.save(postRequestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @Operation(summary = "모든 게시글 조회", description = "해당 회원이 쓴 모든 글을 조회한다.")
    public List<PostResponse> findAllPosts(@RequestParam Long memberId) {
        return postService.findAll(memberId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{postId}")
    @Operation(summary = "게시글 조회", description = "post-id에 해당하는 게시글을 조회한다.")
    public PostResponse findPost(@PathVariable Long postId, @RequestParam Long memberId) {
        return postService.findById(postId, memberId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "post-id에 해당하는 게시글을 삭제한다.")
    public String deletePost(@PathVariable Long postId) {
        postService.deleteById(postId);
        return "요청이 정상적으로 처리되었습니다.";
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{postId}/likes")
    @Operation(summary = "게시글 추천", description = "post-id에 해당하는 게시글에 추천을 누른다.")
    public String likePost(@PathVariable Long postId) {
        postService.likePost(postId);
        return "요청이 정상적으로 처리되었습니다.";
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{postId}/likes")
    @Operation(summary = "게시글 추천 취소", description = "post-id에 해당하는 게시글에 누른 추천을 취소한다.")
    public String cancelLikePost(@PathVariable Long postId) {
        postService.cancelLikePost(postId);
        return "요청이 정상적으로 처리되었습니다.";
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{postId}/report")
    @Operation(summary = "게시글 신고", description = "post-id에 해당하는 게시글을 신고 처리한다.")
    public String reportPost(@PathVariable Long postId, @RequestBody ReportRequest request) {
        postService.reportPost(postId, request);
        return "신고가 성공적으로 접수되었습니다.";
    }
}
