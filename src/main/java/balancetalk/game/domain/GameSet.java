package balancetalk.game.domain;

import balancetalk.global.common.BaseTimeEntity;
import balancetalk.global.notification.domain.NotificationHistory;
import balancetalk.member.domain.Member;
import balancetalk.vote.domain.VoteOption;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
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
public class GameSet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(mappedBy = "gameSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Game> games = new ArrayList<>();

    @NotBlank
    @Size(max = 50)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_tag_id")
    private MainTag mainTag;

    @PositiveOrZero
    @ColumnDefault("0")
    private long views;

    @Size(max = 32)
    private String subTag;

    @PositiveOrZero
    @ColumnDefault("0")
    private Long bookmarks;

    private LocalDateTime editedAt;

    @Embedded
    private NotificationHistory notificationHistory = new NotificationHistory();

    public void increaseViews() {
        this.views++;
    }

    public boolean matchesId(long id) {
        return this.id == id;
    }

    public void increaseBookmarks() {
        this.bookmarks++;
    }

    public void decreaseBookmarks() {
        this.bookmarks--;
    }

    public boolean hasGame(Long gameId) {
        return games.stream().anyMatch(game -> game.getId().equals(gameId));
    }

    public Long getWriterId() {
        return member.getId();
    }

    public void addGames(List<Game> games) {
        this.games = games;
        games.forEach(game -> {
            game.assignGameSet(this);
            game.getGameOptions().forEach(option -> option.addGame(game));
        });
    }

    public long getVotesCount() {
        return games.get(0).getVoteCount(VoteOption.A) + games.get(0).getVoteCount(VoteOption.B);
    }

    public NotificationHistory getNotificationHistory() {
        if (this.notificationHistory == null) {
            this.notificationHistory = new NotificationHistory();
        }
        return this.notificationHistory;
    }

    public void updateGameSetRequest(
            String title,
            MainTag mainTag,
            String subTag,
            List<Game> newGames
    ) {
        this.title = title;
        this.editedAt = LocalDateTime.now();
        this.mainTag = mainTag;
        this.subTag = subTag;

        IntStream.range(0, this.games.size()).forEach(i -> {
            Game existingGame = this.games.get(i);
            Game newGame = newGames.get(i);
            existingGame.updateGame(newGame);
        });
    }

    public List<Long> getGameOptionIds() {
        return games.stream().flatMap(game -> game.getGameOptionIds().stream()).toList();
    }
}
