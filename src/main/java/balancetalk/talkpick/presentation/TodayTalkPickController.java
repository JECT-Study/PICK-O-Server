package balancetalk.talkpick.presentation;

import static balancetalk.talkpick.dto.TodayTalkPickDto.TodayTalkPickResponse;

import balancetalk.talkpick.application.TodayTalkPickService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/talks/today")
@Tag(name = "talk_pick", description = "톡픽 API")
public class TodayTalkPickController {

    private final TodayTalkPickService todayTalkPickService;

    @Operation(summary = "오늘의 톡픽 조회", description = "오늘의 톡픽을 조회합니다.")
    @GetMapping
    public List<TodayTalkPickResponse> findTodayTalkPick() {
        return todayTalkPickService.findTodayTalkPick();
    }
}
