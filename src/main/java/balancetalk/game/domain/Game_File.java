package balancetalk.game.domain;

import balancetalk.module.file.domain.FileType;
import balancetalk.module.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Game_File {

    @Id
    @GeneratedValue
    @Column(name = "game_file_id")
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String originalName; // 사용자가 업로드한 파일명

    @Size(max = 100)
    @Column(length = 50, unique = true)
    private String storedName; // 서버 내부에서 관리하는 파일명

    @NotBlank
    @Size(max = 209)
    @Column(nullable = false, length = 209)
    private String path;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private FileType type;

    @NotNull
    @Positive
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;
}
