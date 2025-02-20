package balancetalk.game.domain.repository;

import static balancetalk.game.domain.QGameSet.gameSet;

import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameSetRepositoryCustomImpl implements GameSetRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Long findRandomGameSetId() {
        // 1. MIN, MAX ID 조회
        NumberTemplate<Long> maxId = Expressions.numberTemplate(Long.class,
                "MAX({0})", gameSet.id);
        NumberTemplate<Long> minId = Expressions.numberTemplate(Long.class,
                "MIN({0})", gameSet.id);

        Tuple minMaxResult = queryFactory
                .select(minId, maxId)
                .from(gameSet)
                .fetchOne();

        if (minMaxResult == null) {
            throw new BalanceTalkException(ErrorCode.NOT_FOUND_BALANCE_GAME);
        }

        Long min = minMaxResult.get(minId);
        Long max = minMaxResult.get(maxId);

        if (min == null || max == null) {
            throw new BalanceTalkException(ErrorCode.NOT_FOUND_BALANCE_GAME);
        }

        // 2. 랜덤 ID 생성
        long randomId = min + (long)(Math.random() * (max - min + 1));

        // 3. randomId 이상의 첫 번째 ID 조회
        Long result = queryFactory
                .select(gameSet.id)
                .from(gameSet)
                .where(gameSet.id.goe(randomId))
                .orderBy(gameSet.id.asc())
                .limit(1)
                .fetchFirst();

        if (result == null) {
            throw new BalanceTalkException(ErrorCode.NOT_FOUND_BALANCE_GAME);
        }

        return result;
    }
}
