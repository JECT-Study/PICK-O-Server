package balancetalk.module.post.presentation;

import balancetalk.module.post.application.BookmarkService;
import balancetalk.module.post.dto.BookmarkRequestDto;
import balancetalk.module.post.dto.BookmarkResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "bookmark", description = "게시글 북마크 API")
@RequestMapping("/bookmark")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/posts/{postId}")
    @Operation(summary = "북마크 추가", description = "post-id에 해당하는 게시글을 북마크에 추가한다.")
    public String addBookmark(@PathVariable Long postId, @RequestBody final BookmarkRequestDto bookmarkRequestDto) {
        bookmarkService.save(bookmarkRequestDto, postId);

        return "북마크가 등록 되었습니다.";
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @Operation(summary = "북마크에 추가된 게시글 목록 조회", description = "회원이 북마크한 모든 게시글을 조회한다.")
    public List<BookmarkResponseDto> findAllPost() {
        return bookmarkService.findAllByMember();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{postId}")
    @Operation(summary = "북마크 삭제" , description = "회원이 북마크한 게시글을 삭제한다.")
    public String deleteBookmark(@PathVariable Long postId) {
        bookmarkService.deleteByPostId(postId);
        return "북마크가 삭제되었습니다.";
    }
}
