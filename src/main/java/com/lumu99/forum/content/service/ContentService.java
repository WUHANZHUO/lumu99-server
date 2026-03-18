package com.lumu99.forum.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumu99.forum.common.enums.ContentStatus;
import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.common.security.SecurityContextHelper;
import com.lumu99.forum.domain.AdminSettings;
import com.lumu99.forum.domain.content.BaseContent;
import com.lumu99.forum.domain.content.ContentEvent;
import com.lumu99.forum.domain.content.ContentPhoto;
import com.lumu99.forum.domain.content.ContentStory;
import com.lumu99.forum.domain.content.ContentTimeline;
import com.lumu99.forum.domain.content.ContentVideo;
import com.lumu99.forum.domain.content.ContentWorld;
import com.lumu99.forum.dto.request.ContentRequest;
import com.lumu99.forum.dto.response.ContentResponse;
import com.lumu99.forum.mapper.AdminSettingsMapper;
import com.lumu99.forum.mapper.content.ContentEventMapper;
import com.lumu99.forum.mapper.content.ContentPhotoMapper;
import com.lumu99.forum.mapper.content.ContentStoryMapper;
import com.lumu99.forum.mapper.content.ContentTimelineMapper;
import com.lumu99.forum.mapper.content.ContentVideoMapper;
import com.lumu99.forum.mapper.content.ContentWorldMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class ContentService {

    private final ContentStoryMapper storyMapper;
    private final ContentTimelineMapper timelineMapper;
    private final ContentPhotoMapper photoMapper;
    private final ContentVideoMapper videoMapper;
    private final ContentWorldMapper worldMapper;
    private final ContentEventMapper eventMapper;
    private final AdminSettingsMapper adminSettingsMapper;

    public ContentService(ContentStoryMapper storyMapper,
                          ContentTimelineMapper timelineMapper,
                          ContentPhotoMapper photoMapper,
                          ContentVideoMapper videoMapper,
                          ContentWorldMapper worldMapper,
                          ContentEventMapper eventMapper,
                          AdminSettingsMapper adminSettingsMapper) {
        this.storyMapper = storyMapper;
        this.timelineMapper = timelineMapper;
        this.photoMapper = photoMapper;
        this.videoMapper = videoMapper;
        this.worldMapper = worldMapper;
        this.eventMapper = eventMapper;
        this.adminSettingsMapper = adminSettingsMapper;
    }

    public List<ContentResponse> list(Module module) {
        enforceGuestVisibility(module);
        return getMapper(module).selectList(
                new LambdaQueryWrapper<BaseContent>()
                        .orderByDesc(BaseContent::getIsPinned)
                        .orderByDesc(BaseContent::getId)
        ).stream().map(ContentResponse::from).toList();
    }

    public ContentResponse create(Module module, ContentRequest request) {
        requireAdmin();
        String userUuid = SecurityContextHelper.currentUserUuid();
        BaseContent entity = newEntity(module);
        entity.setTitle(request.title());
        entity.setBody(request.body());
        entity.setResourceUrl(request.resourceUrl());
        entity.setStatus(parseStatus(request.status()));
        entity.setIsPinned(false);
        entity.setCreatedBy(userUuid);
        entity.setUpdatedBy(userUuid);
        getMapper(module).insert(entity);
        return ContentResponse.from(entity);
    }

    public ContentResponse update(Module module, Long id, ContentRequest request) {
        requireAdmin();
        String userUuid = SecurityContextHelper.currentUserUuid();
        BaseContent entity = requireContent(module, id);
        entity.setTitle(request.title());
        entity.setBody(request.body());
        entity.setResourceUrl(request.resourceUrl());
        entity.setStatus(parseStatus(request.status()));
        entity.setUpdatedBy(userUuid);
        getMapper(module).updateById(entity);
        return ContentResponse.from(entity);
    }

    public void delete(Module module, Long id) {
        requireAdmin();
        getMapper(module).deleteById(id);
    }

    public ContentResponse pin(Module module, Long id, boolean pinned) {
        requireAdmin();
        BaseContent entity = requireContent(module, id);
        entity.setIsPinned(pinned);
        entity.setUpdatedBy(SecurityContextHelper.currentUserUuid());
        getMapper(module).updateById(entity);
        return ContentResponse.from(entity);
    }

    private BaseContent requireContent(Module module, Long id) {
        BaseContent entity = getMapper(module).selectById(id);
        if (entity == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "Content not found");
        }
        return entity;
    }

    private void enforceGuestVisibility(Module module) {
        if (!SecurityContextHelper.isGuest()) return;
        AdminSettings settings = adminSettingsMapper.selectById(1L);
        if (module == Module.WORLD && (settings == null || !Boolean.TRUE.equals(settings.getWorldGuestVisible()))) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_403_FORBIDDEN", "World is not visible for guest");
        }
        if (module == Module.EVENT && (settings == null || !Boolean.TRUE.equals(settings.getEventsGuestVisible()))) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_403_FORBIDDEN", "Event is not visible for guest");
        }
    }

    private void requireAdmin() {
        if (!SecurityContextHelper.isAdmin()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ADMIN_403_ONLY_ADMIN", "Only admin can access");
        }
    }

    private ContentStatus parseStatus(String status) {
        if (status == null) return ContentStatus.PUBLISHED;
        try {
            return ContentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ContentStatus.PUBLISHED;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseContent> BaseMapper<T> getMapper(Module module) {
        return (BaseMapper<T>) switch (module) {
            case STORY -> storyMapper;
            case TIMELINE -> timelineMapper;
            case PHOTO -> photoMapper;
            case VIDEO -> videoMapper;
            case WORLD -> worldMapper;
            case EVENT -> eventMapper;
        };
    }

    private BaseContent newEntity(Module module) {
        return switch (module) {
            case STORY -> new ContentStory();
            case TIMELINE -> new ContentTimeline();
            case PHOTO -> new ContentPhoto();
            case VIDEO -> new ContentVideo();
            case WORLD -> new ContentWorld();
            case EVENT -> new ContentEvent();
        };
    }

    public enum Module {
        STORY, TIMELINE, PHOTO, VIDEO, WORLD, EVENT
    }
}
