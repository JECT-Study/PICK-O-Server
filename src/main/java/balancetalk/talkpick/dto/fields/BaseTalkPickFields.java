package balancetalk.talkpick.dto.fields;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BaseTalkPickFields {

    @Schema(description = "제목", example = "제목")
    @Size(max = 50, message = "제목은 50자 이하여야 합니다.")
    private String title;

    @Schema(description = "본문 내용", example = "본문 내용")
    @Size(max = 2000, message = "본문은 2,000자 이하여야 합니다.")
    private String content;

    @Schema(description = "선택지 A 이름", example = "선택지 A 이름")
    @Size(max = 30, message = "선택지 이름은 30자 이하여야 합니다.")
    private String optionA;

    @Schema(description = "선택지 B 이름", example = "선택지 B 이름")
    @Size(max = 30, message = "선택지 이름은 30자 이하여야 합니다.")
    private String optionB;

    @Schema(description = "출처 URL", example = "https://github.com/CHZZK-Study/Balance-Talk-Backend/issues/506")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sourceUrl;

    public static BaseTalkPickFields from(String title, String content,
                                          String optionA, String optionB,
                                          String sourceUrl) {
        return BaseTalkPickFields.builder()
                .title(title)
                .content(content)
                .optionA(optionA)
                .optionB(optionB)
                .sourceUrl(sourceUrl)
                .build();
    }

    public static BaseTalkPickFields from(String title, String content,
                                          String optionA, String optionB) {
        return BaseTalkPickFields.builder()
                .title(title)
                .content(content)
                .optionA(optionA)
                .optionB(optionB)
                .build();
    }
}
