package balancetalk.vote.domain;

import balancetalk.game.domain.GameOption;
import balancetalk.global.common.BaseTimeEntity;
import balancetalk.member.domain.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameVote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_option_id")
    private GameOption gameOption;

    private boolean isActive;

    public VoteOption getVoteOption() {
        return gameOption.getOptionType();
    }

    public boolean matchesGameOption(GameOption gameOption) {
        return this.gameOption.equals(gameOption);
    }


    public void updateGameOption(GameOption gameOption) {
        this.gameOption = gameOption;
    }

    public void updateActive(boolean isActive) {
        this.isActive = isActive;
    }
}
