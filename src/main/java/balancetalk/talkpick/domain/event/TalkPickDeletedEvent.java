package balancetalk.talkpick.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TalkPickDeletedEvent {

    private Long talkPickId;
}
