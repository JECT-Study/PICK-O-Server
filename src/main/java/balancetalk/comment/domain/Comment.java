package balancetalk.comment.domain;

import balancetalk.global.common.BaseTimeEntity;
import balancetalk.global.notification.domain.NotificationHistory;
import balancetalk.member.domain.Member;
import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.domain.ViewStatus;
import balancetalk.vote.domain.VoteOption;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    private String content;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private ViewStatus viewStatus;

    @NotNull
    private Boolean isBest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talk_pick_id")
    private TalkPick talkPick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> replies = new ArrayList<>();

    @Transient
    private Boolean isNotifiedForFirstReply = false;

    @NotNull
    private LocalDateTime editedAt;

    private boolean isEdited;

    private VoteOption voteOption;

    @Embedded
    private NotificationHistory notificationHistory = new NotificationHistory();

    public void updateContent(String content) {
        this.content = content;
        this.editedAt = LocalDateTime.now();
        this.isEdited = true;
    }

    public void setIsBest(boolean isBest) {
        this.isBest = isBest;
    }

    public void setIsNotifiedForFirstReplyTrue() {
        this.isNotifiedForFirstReply = true;
    }

    public NotificationHistory getNotificationHistory() {
        if (this.notificationHistory == null) {
            this.notificationHistory = new NotificationHistory();
        }
        return this.notificationHistory;
    }

    public void updateVoteOption(VoteOption voteOption) {
        this.voteOption = voteOption;
    }
}