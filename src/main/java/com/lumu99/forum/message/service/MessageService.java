package com.lumu99.forum.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumu99.forum.common.enums.UserRole;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.common.security.SecurityContextHelper;
import com.lumu99.forum.domain.AdminSettings;
import com.lumu99.forum.domain.DmMessage;
import com.lumu99.forum.domain.DmThread;
import com.lumu99.forum.domain.User;
import com.lumu99.forum.mapper.AdminSettingsMapper;
import com.lumu99.forum.mapper.DmMessageMapper;
import com.lumu99.forum.mapper.DmThreadMapper;
import com.lumu99.forum.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final DmThreadMapper dmThreadMapper;
    private final DmMessageMapper dmMessageMapper;
    private final UserMapper userMapper;
    private final AdminSettingsMapper adminSettingsMapper;

    public MessageService(DmThreadMapper dmThreadMapper,
                          DmMessageMapper dmMessageMapper,
                          UserMapper userMapper,
                          AdminSettingsMapper adminSettingsMapper) {
        this.dmThreadMapper = dmThreadMapper;
        this.dmMessageMapper = dmMessageMapper;
        this.userMapper = userMapper;
        this.adminSettingsMapper = adminSettingsMapper;
    }

    public List<DmThread> listThreads() {
        String userUuid = SecurityContextHelper.currentUserUuid();
        return dmThreadMapper.selectList(
                new LambdaQueryWrapper<DmThread>()
                        .eq(DmThread::getUserAUuid, userUuid)
                        .or()
                        .eq(DmThread::getUserBUuid, userUuid)
                        .orderByDesc(DmThread::getLastMessageAt)
        );
    }

    public List<DmMessage> listThreadMessages(Long threadId) {
        String userUuid = SecurityContextHelper.currentUserUuid();
        ensureThreadOwnedByUser(threadId, userUuid);
        return dmMessageMapper.selectList(
                new LambdaQueryWrapper<DmMessage>()
                        .eq(DmMessage::getThreadId, threadId)
                        .orderByAsc(DmMessage::getId)
        );
    }

    @Transactional
    public DmMessage sendMessage(String toUserUuid, String content) {
        String fromUserUuid = SecurityContextHelper.currentUserUuid();
        User fromUser = requireUser(fromUserUuid);
        User toUser = requireUser(toUserUuid);

        if (fromUser.getRole() == UserRole.USER && toUser.getRole() == UserRole.USER && !isUserDmEnabled()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "MSG_403_USER_DM_DISABLED", "User DM is disabled");
        }

        Long threadId = findOrCreateThread(fromUserUuid, toUserUuid);
        LocalDateTime now = LocalDateTime.now();

        DmMessage message = new DmMessage();
        message.setThreadId(threadId);
        message.setFromUuid(fromUserUuid);
        message.setToUuid(toUserUuid);
        message.setContent(content);
        message.setIsRead(false);
        message.setCreatedAt(now);
        dmMessageMapper.insert(message);

        DmThread thread = dmThreadMapper.selectById(threadId);
        thread.setLastMessageAt(now);
        thread.setLastMessagePreview(preview(content));
        dmThreadMapper.updateById(thread);

        return message;
    }

    private Long findOrCreateThread(String userA, String userB) {
        DmThread existing = dmThreadMapper.selectOne(
                new LambdaQueryWrapper<DmThread>()
                        .and(w -> w.eq(DmThread::getUserAUuid, userA).eq(DmThread::getUserBUuid, userB))
                        .or()
                        .and(w -> w.eq(DmThread::getUserAUuid, userB).eq(DmThread::getUserBUuid, userA))
        );
        if (existing != null) {
            return existing.getId();
        }
        DmThread thread = new DmThread();
        thread.setUserAUuid(userA);
        thread.setUserBUuid(userB);
        dmThreadMapper.insert(thread);
        return thread.getId();
    }

    private void ensureThreadOwnedByUser(Long threadId, String userUuid) {
        DmThread thread = dmThreadMapper.selectOne(
                new LambdaQueryWrapper<DmThread>()
                        .eq(DmThread::getId, threadId)
                        .and(w -> w.eq(DmThread::getUserAUuid, userUuid).or().eq(DmThread::getUserBUuid, userUuid))
        );
        if (thread == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_403_FORBIDDEN", "Forbidden");
        }
    }

    private User requireUser(String userUuid) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserUuid, userUuid));
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "User not found");
        }
        return user;
    }

    private boolean isUserDmEnabled() {
        AdminSettings settings = adminSettingsMapper.selectById(1L);
        return settings != null && Boolean.TRUE.equals(settings.getUserDmEnabled());
    }

    private String preview(String content) {
        if (content == null) return "";
        return content.length() <= 64 ? content : content.substring(0, 64);
    }
}
