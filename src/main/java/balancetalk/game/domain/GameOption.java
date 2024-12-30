package balancetalk.game.domain;

import balancetalk.global.common.BaseTimeEntity;
import balancetalk.vote.domain.GameVote;
import balancetalk.vote.domain.VoteOption;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Size(max = 30)
    private String name;

    private Long imgId;

    @Size(max = 50)
    private String description;

    @Enumerated(value = EnumType.STRING)
    private VoteOption optionType;

    @PositiveOrZero
    @ColumnDefault("0")
    private long votesCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToMany(mappedBy = "gameOption", cascade = CascadeType.REMOVE)
    private List<GameVote> gameVotes = new ArrayList<>();

    public void addGame(Game game) {
        this.game = game;
    }

    public void updateGameOption(GameOption newGameOption) {
        this.imgId = newGameOption.getImgId();
        this.name = newGameOption.getName();
        this.description = newGameOption.getDescription();
    }

    public boolean isTypeEqual(VoteOption voteOption) {
        return optionType.equals(voteOption);
    }

    public void increaseVotesCount() {
        this.votesCount++;
    }

    public void decreaseVotesCount() {
        this.votesCount--;
    }

    public boolean hasImage() {
        return imgId != null;
    }
}
