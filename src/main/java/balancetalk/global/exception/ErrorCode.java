package balancetalk.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    FILE_SIZE_EXCEEDED(BAD_REQUEST, "파일 크기가 초과되었습니다."),
    EXCEED_MAX_DEPTH(BAD_REQUEST, "답글에 답글을 달 수 없습니다."),
    AUTHORIZATION_CODE_MISMATCH(BAD_REQUEST, "인증 번호가 일치하지 않습니다."),
    EMPTY_JWT_TOKEN(BAD_REQUEST, "토큰 값이 존재하지 않습니다."),
    INVALID_VOTE_OPTION(BAD_REQUEST, "존재하지 않는 선택지입니다."),
    NOT_ATTACH_IMAGE(BAD_REQUEST, "이미지를 첨부하지 않아 문제가 발생했습니다."),
    EXCEEDED_IMAGES_SIZE(BAD_REQUEST, "첨부 가능한 이미지 개수를 초과했습니다."),
    NOT_SUPPORTED_MIME_TYPE(BAD_REQUEST, "지원하지 않는 MIME 타입입니다."),
    MISSING_MIME_TYPE(BAD_REQUEST, "MIME 타입이 존재하지 않습니다."),
    FILE_ID_GAME_OPTION_ID_MISMATCH(BAD_REQUEST, "File id에 매핑되는 GameOption Id가 없습니다."),
    REPORT_MY_COMMENT(BAD_REQUEST, "본인의 댓글을 신고할 수 없습니다"),
    INVALID_REPORT_REASON(BAD_REQUEST, "신고 사유가 올바르지 않습니다."),
    INVALID_REPORT_TYPE(BAD_REQUEST, "신고 타입이 올바르지 않습니다."),
    CANNOT_BOOKMARK_MY_RESOURCE(BAD_REQUEST, "본인이 생성한 자원에는 북마크가 불가능합니다."),
    ALREADY_BOOKMARKED(BAD_REQUEST, "이미 북마크한 게시글입니다."),
    ALREADY_DELETED_BOOKMARK(BAD_REQUEST, "이미 북마크가 취소된 상태입니다."),
    SORT_REQUIRED(BAD_REQUEST, "정렬 조건은 필수입니다."),
    BALANCE_GAME_SIZE_TEN(BAD_REQUEST, "밸런스 게임의 갯수는 10개여야 합니다."),
    BALANCE_GAME_SEARCH_BLANK(BAD_REQUEST, "검색어는 공백이어선 안 됩니다."),
    BALANCE_GAME_SEARCH_LENGTH(BAD_REQUEST, "검색어는 공백 제외 2자 이상이어야 합니다."),
    PASSWORD_MISMATCH(BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    CACHE_NOT_FOUND(BAD_REQUEST, "캐시를 찾을 수 없습니다."),
    INVALID_SOURCE_TYPE(BAD_REQUEST, "올바르지 않은 소스 타입입니다."),

    // 401
    MISMATCHED_EMAIL_OR_PASSWORD(UNAUTHORIZED, "이메일 또는 비밀번호가 잘못되었습니다."),
    AUTHENTICATION_REQUIRED(UNAUTHORIZED, "인증이 필요합니다."),
    EXPIRED_JWT_TOKEN(UNAUTHORIZED, "만료된 토큰 입니다."),
    INVALID_JWT_TOKEN(UNAUTHORIZED, "유효하지 않은 토큰입니다"),

    // 403
    FORBIDDEN_COMMENT_MODIFY(FORBIDDEN, "해당 댓글은 수정 권한이 없습니다."),
    FORBIDDEN_COMMENT_DELETE(FORBIDDEN, "해당 댓글은 삭제 권한이 없습니다."),
    FORBIDDEN_MEMBER_DELETE(FORBIDDEN, "사용자 탈퇴 권한이 없습니다."),
    FORBIDDEN_LIKE_OWN_COMMENT(FORBIDDEN, "본인 댓글에는 좋아요를 누를 수 없습니다."),
    FORBIDDEN_MAIN_TAG_CREATE(FORBIDDEN, "메인 태그 작성 권한이 없습니다."),
    FORBIDDEN_PICK_O_FRIENDS_OPERATION(FORBIDDEN, "PICK-O 프렌즈 작업을 수행할 권한이 없습니다."),

    // 404
    NOT_FOUND_TALK_PICK(NOT_FOUND, "존재하지 않는 톡픽입니다."),
    NOT_FOUND_VOTE_OPTION(NOT_FOUND, "존재하지 않는 선택지입니다."),
    NOT_FOUND_MEMBER(NOT_FOUND, "존재하지 않는 회원입니다."),
    NOT_FOUND_VOTE(NOT_FOUND, "해당 게시글에서 투표한 기록이 존재하지 않습니다."),
    NOT_FOUND_OPTION_VOTE(NOT_FOUND, "해당 선택지에 투표한 기록이 존재하지 않습니다"),
    NOT_FOUND_BOOKMARK(NOT_FOUND, "해당 게시글에서 북마크한 기록이 존재하지 않습니다."),
    NOT_FOUND_COMMENT(NOT_FOUND, "존재하지 않는 댓글입니다."),
    NOT_FOUND_FILE(NOT_FOUND, "존재하지 않는 파일입니다."),
    NOT_FOUND_PARENT_COMMENT_AT_THAT_TALK_PICK(NOT_FOUND, "해당 톡픽에 존재하지 않는 원 댓글입니다."),
    NOT_FOUND_COMMENT_AT_THAT_TALK_PICK(NOT_FOUND, "해당 게시글에 존재하지 않는 댓글입니다."),
    NOT_LIKED_COMMENT(NOT_FOUND, "해당 댓글을 좋아요한 기록이 존재하지 않습니다."),
    NOT_FOUND_LIKE(NOT_FOUND, "해당 좋아요가 존재하지 않습니다."),
    NOT_FOUND_BALANCE_GAME_SET(NOT_FOUND, "존재하지 않는 밸런스 게임 세트 입니다."),
    NOT_FOUND_BALANCE_GAME(NOT_FOUND, "존재하지 않는 밸런스 게임입니다."),
    NOT_FOUND_BALANCE_GAME_THAT_GAME_SET(NOT_FOUND, "해당 밸런스 게임 세트에 존재하지 않는 밸런스 게임입니다."),
    NOT_FOUND_MAIN_TAG(NOT_FOUND, "존재하지 않는 메인 태그입니다."),
    NOT_FOUND_TALK_PICK_THAT_MEMBER(NOT_FOUND, "해당 회원이 작성하지 않은 톡픽입니다."),
    NOT_FOUND_TEMP_TALK_PICK(NOT_FOUND, "임시 저장한 톡픽이 존재하지 않습니다."),
    NOT_FOUND_NOTIFICATION(NOT_FOUND, "존재하지 않는 알림입니다."),
    NOT_FOUND_GAME_OPTION(NOT_FOUND, "게임 선택지가 존재하지 않습니다."),
    NOT_FOUND_CACHE_VALUE(NOT_FOUND, "캐시 값이 존재하지 않습니다."),

    // 409
    ALREADY_VOTE(CONFLICT, "이미 투표한 게시글입니다."),
    ALREADY_LIKED_COMMENT(CONFLICT, "이미 추천을 누른 댓글입니다."),
    ALREADY_REGISTERED_NICKNAME(CONFLICT, "이미 등록된 닉네임입니다."),
    ALREADY_REGISTERED_EMAIL(CONFLICT, "이미 등록된 이메일입니다."),
    ALREADY_REGISTERED_TAG(CONFLICT, "이미 등록된 태그 입니다."),
    SAME_NICKNAME(CONFLICT, "변경하려는 닉네임이 현재와 동일합니다. 다른 닉네임을 입력해주세요."),
    ALREADY_REPORTED_COMMENT(CONFLICT, "이미 신고한 댓글 입니다."),
    SAME_VOTE(CONFLICT, "변경하려는 선택지가 이전과 동일합니다."),

    // 500
    FAIL_SEND_EMAIL(INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
    FAIL_UPLOAD_FILE(INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    SEND_NOTIFICATION_FAIL(INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다."),
    FAIL_PARSE_NOTIFICATION_HISTORY(INTERNAL_SERVER_ERROR, "알림 내역 파싱에 실패했습니다."),
    FAIL_SERIALIZE_NOTIFICATION_HISTORY(INTERNAL_SERVER_ERROR, "알림 내역 직렬화에 실패했습니다."),
    FAIL_SORT(INTERNAL_SERVER_ERROR, "정렬에 실패했습니다."),
    TALK_PICK_SUMMARY_SIZE_IS_OVER(INTERNAL_SERVER_ERROR, "요약 내용의 길이가 적정 기준을 초과했습니다."),
    TALK_PICK_SUMMARY_FAILED(INTERNAL_SERVER_ERROR, "톡픽 요약에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
