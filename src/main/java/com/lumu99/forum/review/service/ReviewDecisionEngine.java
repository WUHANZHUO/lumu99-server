package com.lumu99.forum.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumu99.forum.common.enums.ReviewStatus;
import com.lumu99.forum.common.enums.UserRole;
import com.lumu99.forum.domain.AdminSettings;
import com.lumu99.forum.domain.ForbiddenWord;
import com.lumu99.forum.mapper.AdminSettingsMapper;
import com.lumu99.forum.mapper.ForbiddenWordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class ReviewDecisionEngine {

    private final ForbiddenWordMapper forbiddenWordMapper;
    private final AdminSettingsMapper adminSettingsMapper;

    public ReviewDecisionEngine(ForbiddenWordMapper forbiddenWordMapper,
                                AdminSettingsMapper adminSettingsMapper) {
        this.forbiddenWordMapper = forbiddenWordMapper;
        this.adminSettingsMapper = adminSettingsMapper;
    }

    public ReviewStatus decideReviewStatus(String operatorRole, String title, String content) {
        if (hitForbiddenWord(title, content)) {
            return ReviewStatus.PENDING;
        }
        if (UserRole.ADMIN.name().equalsIgnoreCase(operatorRole)) {
            return ReviewStatus.APPROVED;
        }
        AdminSettings settings = adminSettingsMapper.selectById(1L);
        if (settings != null && Boolean.TRUE.equals(settings.getForumPostNeedReview())) {
            return ReviewStatus.PENDING;
        }
        return ReviewStatus.APPROVED;
    }

    private boolean hitForbiddenWord(String title, String content) {
        String plain = (safe(title) + " " + safe(content)).toLowerCase(Locale.ROOT);
        List<ForbiddenWord> words = forbiddenWordMapper.selectList(
                new LambdaQueryWrapper<ForbiddenWord>().eq(ForbiddenWord::getEnabled, true)
        );
        for (ForbiddenWord fw : words) {
            String word = fw.getWord();
            if (StringUtils.hasText(word) && plain.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }
}
