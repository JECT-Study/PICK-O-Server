package balancetalk.module.post.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

//@ExtendWith(MockitoExtension.class)
//class PostServiceTest {
//
//    @Mock
//    MemberRepository memberRepository;
//
//    @Mock
//    PostRepository postRepository;
//
//    @Mock
//    PostLikeRepository postLikeRepository;
//
//    @InjectMocks
//    PostService postService;
//
//    @Test
//    @DisplayName("게시글 작성 테스트")
//    void createPost_success() {
//        // given
//        Member member = Member.builder()
//                .id(1L)
//                .build();
//
<<<<<<< HEAD
//        FileDto fileDto = FileDto.builder()
=======
//        FileResponse fileDto = FileResponse.builder()
>>>>>>> main
//                .uploadName("파일1")
//                .path("../")
//                .type(FileType.JPEG)
//                .size(236L)
//                .build();
//
//        List<PostTagDto> postTagDto = List.of(
//                PostTagDto.builder()
//                        .tagName("태그1")
//                        .build(),
//                PostTagDto.builder()
//                        .tagName("태그2")
//                        .build());
//
//        List<BalanceOptionDto> balanceOptionDto = List.of(
//                BalanceOptionDto.builder()
//                        .title("제목1")
//                        .description("섦명 내용")
//                        .file(fileDto)
//                        .build(),
//                BalanceOptionDto.builder()
//                        .title("제목1")
//                        .description("섦명 내용")
//                        .file(fileDto)
//                        .build());
//
//        PostRequestDto postRequestDto = PostRequestDto.builder()
//                .memberId(member.getId())
//                .title("게시글 생성 테스트")
//                .balanceOptions(balanceOptionDto)
//                .tags(postTagDto)
//                .build();
//
//        Post post = postRequestDto.toEntity(member);
//
//
//        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
//        // 생성된 post 객체를 반환하게 설정
//        when(postRepository.save(any())).thenReturn(post);
//
//        // when
//        Post result = postService.save(postRequestDto);
//
//        // then
//        assertThat(result.getMember().getId()).isEqualTo(member.getId());
//
//    }
//
//    @Test
//    @DisplayName("모든 게시글 조회")
//    void readAllPosts_Success() {
//        // given
//        List<Post> posts = List.of(
//                Post.builder()
//                    .id(1L)
//                    .options(Collections.emptyList())
//                    .postTags(Collections.emptyList())
//                    .build(),
//                Post.builder()
//                    .id(2L)
//                    .options(Collections.emptyList())
//                    .postTags(Collections.emptyList())
//                    .build()
//        );
//
//        when(postRepository.findAll()).thenReturn(posts);
//
//        // when
<<<<<<< HEAD
//        List<PostResponseDto> result = postService.findAll();
=======
//        List<PostResponse> result = postService.findAll();
>>>>>>> main
//
//        // then
//        assertEquals(result.get(0).getId(), 1L);
//        assertEquals(result.get(1).getId(), 2L);
//    }
//
//    @Test
//    @DisplayName("게시글 단건 조회")
//    void readSinglePost_Success()  {
//        // given
//        Post post = Post.builder()
//                .id(1L)
//                .title("게시글_단건_조회_테스트")
//                .options(Collections.emptyList())
//                .postTags(Collections.emptyList())
//                .build();
//
//        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
//
//        // when
<<<<<<< HEAD
//        PostResponseDto result = postService.findById(post.getId());
=======
//        PostResponse result = postService.findById(post.getId());
>>>>>>> main
//
//        // then
//        assertEquals(post.getId() , result.getId());
//        assertEquals(post.getTitle() , result.getTitle());
//    }
//
//    @Test
//    @DisplayName("게시글 id로 게시글 삭제")
//    void deletePostById_Success() {
//        // given
//        Post post = Post.builder()
//                .id(1L)
//                .title("게시글_삭제_테스트")
//                .options(Collections.emptyList())
//                .postTags(Collections.emptyList())
//                .build();
//
//        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
//
<<<<<<< HEAD
//        PostResponseDto result = postService.findById(post.getId());
=======
//        PostResponse result = postService.findById(post.getId());
>>>>>>> main
//
//        // when
//        postService.deleteById(result.getId());
//
//        // then
//        assertFalse(postService.findAll().contains(result));
//    }
//
//    @Test
//    @DisplayName("게시글 id가 다르면 게시글 삭제 실패")
//    void deletePostById_Fail() {
//        // given
//        Post post = Post.builder()
//                .id(1L)
//                .title("게시글_삭제_테스트")
//                .options(Collections.emptyList())
//                .postTags(Collections.emptyList())
//                .build();
//
//        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
//
<<<<<<< HEAD
//        PostResponseDto result = postService.findById(post.getId());
=======
//        PostResponse result = postService.findById(post.getId());
>>>>>>> main
//
//        // when
//        assertThatThrownBy(() -> postService.deleteById(2L))
//                .isInstanceOf(BalanceTalkException.class);
//    }
//
//    public Member createMember(Long id) {
//        return Member.builder().id(id).build();
//    }
//
//    public File createFile() {
//        return File.builder().originalName("파일1").path("../").type(FileType.JPEG).size(236L).build();
//    }
//
//    public PostTag createPostTag() {
//        return PostTag.builder().tag(Tag.builder().name("태그1").build()).build();
//    }
//
//    public BalanceOption createBalanceOption() {
//        return BalanceOption.builder().title("밸런스_선택지").description("설명").file(createFile()).build();
//    }
//
//    public Post createPost(Long id) {
//        return Post.builder()
//                .id(id)
//                .member(createMember(id))
//                .title("제목1")
//                .options(List.of(createBalanceOption()))
//                .postTags(List.of(createPostTag()))
//                .build();
//    }
//
//    @Test
//    @DisplayName("사용자가 특정 게시글에 추천을 누르면 해당 게시글 id가 반환된다.")
//    void createPostLike_Success() {
//        // given
//        Post post = Post.builder()
//                .id(1L)
//                .build();
//        Member member = Member.builder()
//                .id(1L)
//                .build();
//
//        when(postRepository.findById(any())).thenReturn(Optional.of(post));
//        when(memberRepository.findById(any())).thenReturn(Optional.of(member));
//
//        // when
//        Long likedPostId = postService.likePost(post.getId(), member.getId());
//
//        // then
//        assertThat(likedPostId).isEqualTo(post.getId());
//    }
//
//    @Test
//    @DisplayName("게시글 추천 시 해당 게시글이 존재하지 않는 경우 예외 발생")
//    void createPostLike_Fail_ByNotFoundPost() {
//        // given
//        Member member = Member.builder()
//                .id(1L)
//                .build();
//
//        when(postRepository.findById(any())).thenThrow(new BalanceTalkException(NOT_FOUND_POST));
//
//        // when, then
//        assertThatThrownBy(() -> postService.likePost(1L, member.getId()))
//                .isInstanceOf(BalanceTalkException.class)
//                .hasMessageContaining(NOT_FOUND_POST.getMessage());
//    }
//
//    @Test
//    @DisplayName("게시글 중복 추천 시 예외 발생")
//    void createPostLike_Fail_ByAlreadyLikePost() {
//        // given
//        Post post = Post.builder()
//                .id(1L)
//                .build();
//        Member member = Member.builder()
//                .id(1L)
//                .build();
//
//        when(postRepository.findById(any())).thenReturn(Optional.of(post));
//        when(memberRepository.findById(any())).thenReturn(Optional.of(member));
//        when(postLikeRepository.existsByMemberAndPost(member, post))
//                .thenThrow(new BalanceTalkException(ALREADY_LIKE_POST));
//
//        // when, then
//        assertThatThrownBy(() -> postService.likePost(post.getId(), member.getId()))
//                .isInstanceOf(BalanceTalkException.class)
//                .hasMessageContaining(ALREADY_LIKE_POST.getMessage());
//    }
//}