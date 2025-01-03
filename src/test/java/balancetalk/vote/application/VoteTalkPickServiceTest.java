package balancetalk.vote.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import balancetalk.global.exception.BalanceTalkException;
import balancetalk.member.domain.Member;
import balancetalk.member.dto.ApiMember;
import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.domain.TalkPickReader;
import balancetalk.vote.domain.TalkPickVote;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VoteTalkPickServiceTest {

    @InjectMocks
    VoteTalkPickService voteTalkPickService;

    @Mock
    TalkPickReader talkPickReader;

    @Mock
    ApiMember apiMember;

    @Test
    @DisplayName("회원이 이미 투표한 톡픽일 경우 투표 생성은 실패한다.")
    void createVote_Fail_ByAlreadyVote() {
        // given
        TalkPick talkPick = TalkPick.builder().id(1L).build();
        TalkPickVote vote = TalkPickVote.builder().talkPick(talkPick).build();
        Member member = Member.builder().talkPickVotes(List.of(vote)).build();

        when(talkPickReader.readById(any())).thenReturn(talkPick);
        when(apiMember.toMember(any())).thenReturn(member);

        // when, then
        assertThatThrownBy(() -> voteTalkPickService.createVote(1L, any(), apiMember))
                .isInstanceOf(BalanceTalkException.class);
    }

    @Test
    @DisplayName("투표한 이력이 없을 경우 투표 수정은 실패한다.")
    void updateVote_Fail_ByNotFoundVote() {
        // given
        TalkPick talkPick = TalkPick.builder().id(1L).build();
        Member member = Member.builder().talkPickVotes(List.of()).build();

        when(talkPickReader.readById(any())).thenReturn(talkPick);
        when(apiMember.toMember(any())).thenReturn(member);

        // when, then
        assertThatThrownBy(() -> voteTalkPickService.updateVote(1L, any(), apiMember))
                .isInstanceOf(BalanceTalkException.class);
    }

    @Test
    @DisplayName("투표한 이력이 없을 경우 투표 삭제는 실패한다.")
    void deleteVote_Fail_ByNotFoundVote() {
        // given
        TalkPick talkPick = TalkPick.builder().id(1L).build();
        Member member = Member.builder().talkPickVotes(List.of()).build();

        when(talkPickReader.readById(any())).thenReturn(talkPick);
        when(apiMember.toMember(any())).thenReturn(member);

        // when, then
        assertThatThrownBy(() -> voteTalkPickService.deleteVote(1L, apiMember))
                .isInstanceOf(BalanceTalkException.class);
    }
}