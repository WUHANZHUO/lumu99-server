package com.lumu99.forum.admin.service;

import com.lumu99.forum.domain.AdminSettings;
import com.lumu99.forum.mapper.AdminSettingsMapper;
import org.springframework.stereotype.Service;

@Service
public class AdminSettingsService {

    private final AdminSettingsMapper adminSettingsMapper;

    public AdminSettingsService(AdminSettingsMapper adminSettingsMapper) {
        this.adminSettingsMapper = adminSettingsMapper;
    }

    public AdminSettings getSettings() {
        return adminSettingsMapper.selectById(1L);
    }

    public AdminSettings updateSettings(boolean worldGuestVisible,
                                        boolean eventsGuestVisible,
                                        boolean forumPostNeedReview,
                                        boolean userDmEnabled) {
        AdminSettings settings = adminSettingsMapper.selectById(1L);
        settings.setWorldGuestVisible(worldGuestVisible);
        settings.setEventsGuestVisible(eventsGuestVisible);
        settings.setForumPostNeedReview(forumPostNeedReview);
        settings.setUserDmEnabled(userDmEnabled);
        adminSettingsMapper.updateById(settings);
        return settings;
    }
}
