package balancetalk.game.domain;

import balancetalk.file.domain.File;
import balancetalk.global.common.BaseTimeEntity;
import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.vote.domain.VoteOption;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
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
public class Game extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private GameSet gameSet;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameOption> gameOptions = new ArrayList<>();

    @Size(max = 100)
    private String description;

    private LocalDateTime editedAt;

    public Long getWriterId() {
        return gameSet.getWriterId();
    }

    public long getVoteCount(VoteOption optionType) {
        GameOption option = gameOptions.stream()
                .filter(gameOption -> gameOption.isTypeEqual(optionType))
                .findFirst()
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_OPTION_VOTE));
        return option.getVotesCount();
    }

    public void assignGameSet(GameSet gameSet) {
        this.gameSet = gameSet;
    }


    public void updateGame(Game newGame) {
        this.description = newGame.getDescription();
        this.editedAt = LocalDateTime.now();

        IntStream.range(0, newGame.getGameOptions().size()).forEach(i -> {
            GameOption currentOption = this.gameOptions.get(i);
            GameOption newOption = newGame.getGameOptions().get(i);
            currentOption.updateGameOption(newOption);
        });
    }

    public List<Long> getGameOptionIds() {
        return gameOptions.stream().map(GameOption::getId).toList();
    }

    public String getImgA(List<File> files) {
        return getImgUrlByIndex(files, 0);
    }

    public String getImgB(List<File> files) {
        return getImgUrlByIndex(files, 1);
    }

    private String getImgUrlByIndex(List<File> files, int index) {
        if (gameOptions.size() <= index) {
            throw new BalanceTalkException(ErrorCode.NOT_FOUND_GAME_OPTION);
        }

        return files.stream()
                .filter(file -> file.getResourceId().equals(gameOptions.get(index).getId()))
                .map(File::getImgUrl)
                .findFirst()
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_FILE));
    }
}
