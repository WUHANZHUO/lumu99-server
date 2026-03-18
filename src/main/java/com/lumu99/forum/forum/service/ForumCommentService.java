package com.lumu99.forum.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumu99.forum.common.enums.MuteStatus;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.common.security.SecurityContextHelper;
import com.lumu99.forum.domain.ForumComment;
import com.lumu99.forum.domain.User;
import com.lumu99.forum.dto.response.CommentResponse;
import com.lumu99.forum.mapper.ForumCommentMapper;
import com.lumu99.forum.mapper.ForumPostMapper;
import com.lumu99.forum.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumCommentService {

    private final ForumCommentMapper forumCommentMapper;
    private final ForumPostMapper forumPostMapper;
    private final UserMapper userMapper;

    public ForumCommentService(ForumCommentMapper forumCommentMapper,
                               ForumPostMapper forumPostMapper,
                               UserMapper userMapper) {
        this.forumCommentMapper = forumCommentMapper;
        this.forumPostMapper = forumPostMapper;
        this.userMapper = userMapper;
    }

    public List<CommentResponse> listComments(Long postId) {
        return forumCommentMapper.selectList(
                new LambdaQueryWrapper<ForumComment>()
                        .eq(ForumComment::getPostId, postId)
                        .orderByAsc(ForumComment::getId)
        ).stream().map(CommentResponse::from).toList();
    }

    public CommentResponse createComment(Long postId, String content) {
        String userUuid = SecurityContextHelper.currentUserUuid();
        enforceNotMuted(userUuid);
        ensurePostExists(postId);

        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setAuthorUuid(userUuid);
        comment.setContent(content);
        forumCommentMapper.insert(comment);
        return CommentResponse.from(comment);
    }

    private void ensurePostExists(Long postId) {
        if (forumPostMapper.selectById(postId) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Post not found");
        }
    }

    private void enforceNotMuted(String userUuid) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserUuid, userUuid));
        if (user != null && MuteStatus.MUTED == user.getMuteStatus()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORUM_403_MUTED", "Muted user cannot comment");
        }
    }
}
