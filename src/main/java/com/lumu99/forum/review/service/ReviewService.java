package com.lumu99.forum.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumu99.forum.common.enums.ReviewStatus;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.domain.ForumPost;
import com.lumu99.forum.dto.response.ReviewPostResponse;
import com.lumu99.forum.mapper.ForumPostMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ForumPostMapper forumPostMapper;

    public ReviewService(ForumPostMapper forumPostMapper) {
        this.forumPostMapper = forumPostMapper;
    }

    public List<ReviewPostResponse> listPendingPosts() {
        return forumPostMapper.selectList(
                new LambdaQueryWrapper<ForumPost>()
                        .eq(ForumPost::getReviewStatus, ReviewStatus.PENDING)
                        .orderByDesc(ForumPost::getId)
        ).stream().map(ReviewPostResponse::from).toList();
    }

    public Optional<ReviewPostResponse> getPost(Long postId) {
        return Optional.ofNullable(forumPostMapper.selectById(postId)).map(ReviewPostResponse::from);
    }

    public ReviewPostResponse approve(Long postId) {
        ForumPost post = requirePending(postId);
        post.setReviewStatus(ReviewStatus.APPROVED);
        post.setRejectReason(null);
        post.setPublishedAt(LocalDateTime.now());
        forumPostMapper.updateById(post);
        return ReviewPostResponse.from(post);
    }

    public ReviewPostResponse reject(Long postId, String reason) {
        ForumPost post = requirePending(postId);
        post.setReviewStatus(ReviewStatus.REJECTED);
        post.setRejectReason(reason);
        forumPostMapper.updateById(post);
        return ReviewPostResponse.from(post);
    }

    private ForumPost requirePending(Long postId) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null || post.getReviewStatus() != ReviewStatus.PENDING) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REVIEW_404_POST_NOT_PENDING", "Post is not pending review");
        }
        return post;
    }
}
