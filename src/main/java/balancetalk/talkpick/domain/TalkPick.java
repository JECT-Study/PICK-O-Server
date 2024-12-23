package balancetalk.talkpick.domain;

import balancetalk.comment.domain.Comment;
import balancetalk.global.common.BaseTimeEntity;
import balancetalk.global.notification.domain.NotificationHistory;
import balancetalk.member.domain.Member;
import balancetalk.vote.domain.TalkPickVote;
import balancetalk.vote.domain.VoteOption;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class TalkPick extends BaseTimeEntity {

    private static final int MIN_CONTENT_LENGTH_FOR_SUMMARY = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @NotBlank
    @Size(max = 50)
    private String title;

    @Embedded
    private Summary summary;

    @NotNull
    private SummaryStatus summaryStatus;

    @NotBlank
    @Size(max = 2000)
    private String content;

    @NotBlank
    @Size(max = 10)
    @Column(name = "option_a")
    private String optionA;

    @NotBlank
    @Size(max = 10)
    @Column(name = "option_b")
    private String optionB;

    private String sourceUrl;

    @PositiveOrZero
    @ColumnDefault("0")
    private Long views;

    @PositiveOrZero
    @ColumnDefault("0")
    private Long bookmarks;

    private LocalDateTime editedAt;

    private boolean isEdited;

    @Enumerated(value = EnumType.STRING)
    private ViewStatus viewStatus = ViewStatus.NORMAL;

    @OneToMany(mappedBy = "talkPick", cascade = CascadeType.REMOVE)
    private List<TalkPickVote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "talkPick", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @Embedded
    private NotificationHistory notificationHistory = new NotificationHistory();

    public void increaseViews() {
        this.views++;
    }

    public long votesCountOf(VoteOption voteOption) {
        return votes.stream()
                .filter(vote -> vote.isVoteOptionEquals(voteOption))
                .count();
    }

    public String getWriterNickname() {
        return this.member.getNickname();
    }

    public void increaseBookmarks() {
        this.bookmarks++;
    }

    public void decreaseBookmarks() {
        this.bookmarks--;
    }

    public void update(TalkPick newTalkPick) {
        this.title = newTalkPick.getTitle();
        this.content = newTalkPick.getContent();
        this.optionA = newTalkPick.getOptionA();
        this.optionB = newTalkPick.getOptionB();
        this.sourceUrl = newTalkPick.getSourceUrl();
        this.editedAt = LocalDateTime.now();
        this.isEdited = true;
    }

    public boolean matchesId(long id) {
        return this.id == id;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void updateSummary(Summary newSummary) {
        this.summary = newSummary;
    }

    public NotificationHistory getNotificationHistory() {
        if (this.notificationHistory == null) {
            this.notificationHistory = new NotificationHistory();
        }
        return this.notificationHistory;
    }

    public boolean hasShortContent() {
        return content.length() < MIN_CONTENT_LENGTH_FOR_SUMMARY;
    }

    public void updateSummaryStatus(SummaryStatus summaryStatus) {
        this.summaryStatus = summaryStatus;
    }

    public TodayTalkPick toTodayTalkPick() {
        return TodayTalkPick.builder()
                .pickDate(LocalDate.now())
                .talkPick(this)
                .build();
    }
}
