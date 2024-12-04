package org.kiru.user.admin.adapter;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.user.entity.QUserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.QUserPortfolioImg;
import org.kiru.core.user.userlike.entity.QUserLike;
import org.kiru.user.admin.dto.AdminLikeUserResponse.AdminLikeUserDto;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
import org.kiru.user.admin.service.out.AdminUserQuery;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.userlike.repository.UserLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AdminAdapter implements AdminUserQuery {
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findSimpleUsers(pageable);
    }

    @Override
    public List<UserDto> findUserByName(String name) {
        return userRepository.findSimpleUserByName(name);
    }

    @Override
    public Page<AdminLikeUserDto> findUserLikes(Pageable pageable, Long userId) {
        QUserJpaEntity qUserJpaEntity = QUserJpaEntity.userJpaEntity;
        QUserLike qUserLike = QUserLike.userLike;
        QUserPortfolioImg qUserPortfolioImg = QUserPortfolioImg.userPortfolioImg;
        List<AdminLikeUserDto> likes = queryFactory.select(Projections.constructor(AdminLikeUserDto.class,
                        qUserJpaEntity.id,
                        qUserJpaEntity.username,
                        qUserPortfolioImg.portfolioImageUrl,
                        qUserLike.createdAt))
                .from(qUserLike)
                .innerJoin(qUserJpaEntity).on(qUserLike.likedUserId.eq(qUserJpaEntity.id))
                .leftJoin(qUserPortfolioImg).on(qUserJpaEntity.id.eq(qUserPortfolioImg.userId).and(qUserPortfolioImg.sequence.eq(1)))
                .where(qUserLike.userId.eq(userId))
                .fetch();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), likes.size());
        return new PageImpl<>(likes.subList(start, end), pageable, likes.size());
    }

    @Override
    public Page<AdminLikeUserDto> findUserLiked(Pageable pageable, Long userId) {
        QUserJpaEntity qUserJpaEntity = QUserJpaEntity.userJpaEntity;
        QUserLike qUserLike = QUserLike.userLike;
        QUserPortfolioImg qUserPortfolioImg = QUserPortfolioImg.userPortfolioImg;
        List<AdminLikeUserDto> likes = queryFactory.select(Projections.constructor(AdminLikeUserDto.class,
                        qUserJpaEntity.id,
                        qUserJpaEntity.username,
                        qUserPortfolioImg.portfolioImageUrl,
                        qUserLike.createdAt))
                .from(qUserLike)
                .innerJoin(qUserJpaEntity).on(qUserLike.userId.eq(qUserJpaEntity.id))
                .leftJoin(qUserPortfolioImg).on(qUserJpaEntity.id.eq(qUserPortfolioImg.userId).and(qUserPortfolioImg.sequence.eq(1)))
                .where(qUserLike.likedUserId.eq(userId))
                .fetch();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), likes.size());
        return new PageImpl<>(likes.subList(start, end), pageable, likes.size());
    }
}