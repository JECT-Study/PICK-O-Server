package balancetalk.talkpick.domain.event;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TalkPickCreatedEvent {

    private Long talkPickId;
    private List<Long> fileIds;
}
