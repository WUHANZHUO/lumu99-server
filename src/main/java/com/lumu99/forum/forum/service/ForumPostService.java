package com.lumu99.forum.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumu99.forum.common.enums.ReviewStatus;
import com.lumu99.forum.common.enums.UserRole;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.common.security.SecurityContextHelper;
import com.lumu99.forum.domain.ForumPost;
import com.lumu99.forum.domain.ForumPostTagRel;
import com.lumu99.forum.domain.ForumTag;
import com.lumu99.forum.dto.request.CreatePostRequest;
import com.lumu99.forum.dto.request.UpdatePostRequest;
import com.lumu99.forum.dto.response.PostResponse;
import com.lumu99.forum.mapper.ForumPostMapper;
import com.lumu99.forum.mapper.ForumPostTagRelMapper;
import com.lumu99.forum.mapper.ForumTagMapper;
import com.lumu99.forum.review.service.ReviewDecisionEngine;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ForumPostService {

    private final ForumPostMapper forumPostMapper;
    private final ForumPostTagRelMapper forumPostTagRelMapper;
    private final ForumTagMapper forumTagMapper;
    private final ReviewDecisionEngine reviewDecisionEngine;

    public ForumPostService(ForumPostMapper forumPostMapper,
                            ForumPostTagRelMapper forumPostTagRelMapper,
                            ForumTagMapper forumTagMapper,
                            ReviewDecisionEngine reviewDecisionEngine) {
        this.forumPostMapper = forumPostMapper;
        this.forumPostTagRelMapper = forumPostTagRelMapper;
        this.forumTagMapper = forumTagMapper;
        this.reviewDecisionEngine = reviewDecisionEngine;
    }

    public List<PostResponse> listPosts() {
        return forumPostMapper.selectList(
                new LambdaQueryWrapper<ForumPost>()
                        .orderByDesc(ForumPost::getIsPinned)
                        .orderByDesc(ForumPost::getId)
        ).stream().map(PostResponse::from).toList();
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest request) {
        String userUuid = SecurityContextHelper.currentUserUuid();
        String role = SecurityContextHelper.currentUserRole();
        validateTagPolicy(request.tagIds(), role);

        ReviewStatus reviewStatus = reviewDecisionEngine.decideReviewStatus(role, request.title(), request.content());

        ForumPost post = new ForumPost();
        post.setAuthorUuid(userUuid);
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setReviewStatus(reviewStatus);
        post.setIsPinned(false);
        if (reviewStatus == ReviewStatus.APPROVED) {
            post.setPublishedAt(LocalDateTime.now());
        }
        forumPostMapper.insert(post);

        saveTagRels(post.getId(), request.tagIds());
        return PostResponse.from(forumPostMapper.selectById(post.getId()));
    }

    public Optional<PostResponse> getPost(Long postId) {
        ForumPost post = forumPostMapper.selectById(postId);
        return Optional.ofNullable(post).map(PostResponse::from);
    }

    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request) {
        String role = SecurityContextHelper.currentUserRole();
        validateTagPolicy(request.tagIds(), role);

        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Post not found");
        }
        post.setTitle(request.title());
        post.setContent(request.content());
        forumPostMapper.updateById(post);

        forumPostTagRelMapper.delete(new LambdaQueryWrapper<ForumPostTagRel>().eq(ForumPostTagRel::getPostId, postId));
        saveTagRels(postId, request.tagIds());
        return PostResponse.from(forumPostMapper.selectById(postId));
    }

    @Transactional
    public void deletePost(Long postId) {
        forumPostTagRelMapper.delete(new LambdaQueryWrapper<ForumPostTagRel>().eq(ForumPostTagRel::getPostId, postId));
        forumPostMapper.deleteById(postId);
    }

    public PostResponse pinPost(Long postId) {
        requireAdmin();
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Post not found");
        }
        post.setIsPinned(true);
        forumPostMapper.updateById(post);
        return PostResponse.from(post);
    }

    public PostResponse unpinPost(Long postId) {
        requireAdmin();
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Post not found");
        }
        post.setIsPinned(false);
        forumPostMapper.updateById(post);
        return PostResponse.from(post);
    }

    private void validateTagPolicy(List<Long> tagIds, String role) {
        if (tagIds == null || tagIds.isEmpty() || UserRole.ADMIN.name().equals(role)) {
            return;
        }
        List<ForumTag> tags = forumTagMapper.selectBatchIds(tagIds);
        for (ForumTag tag : tags) {
            if (Boolean.TRUE.equals(tag.getAdminOnly())) {
                throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "FORUM_422_TAG_NOT_ALLOWED", "Tag is not allowed for normal user");
            }
        }
    }

    private void saveTagRels(Long postId, List<Long> tagIds) {
        if (tagIds == null) return;
        for (Long tagId : tagIds) {
            ForumPostTagRel rel = new ForumPostTagRel();
            rel.setPostId(postId);
            rel.setTagId(tagId);
            forumPostTagRelMapper.insert(rel);
        }
    }

    private void requireAdmin() {
        if (!SecurityContextHelper.isAdmin()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ADMIN_403_ONLY_ADMIN", "Only admin can access");
        }
    }
}
