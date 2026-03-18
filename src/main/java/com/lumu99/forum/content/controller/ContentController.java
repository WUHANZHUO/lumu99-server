package com.lumu99.forum.content.controller;

import com.lumu99.forum.content.service.ContentService;
import com.lumu99.forum.dto.request.ContentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/stories")
    public ResponseEntity<Map<String, Object>> listStories() {
        return list(ContentService.Module.STORY);
    }

    @PostMapping("/stories")
    public ResponseEntity<Map<String, Object>> createStory(@Valid @RequestBody ContentRequest request) {
        return create(ContentService.Module.STORY, request);
    }

    @PutMapping("/stories/{id}")
    public ResponseEntity<Map<String, Object>> updateStory(@PathVariable Long id, @Valid @RequestBody ContentRequest request) {
        return update(ContentService.Module.STORY, id, request);
    }

    @DeleteMapping("/stories/{id}")
    public ResponseEntity<Map<String, Object>> deleteStory(@PathVariable Long id) {
        return delete(ContentService.Module.STORY, id);
    }

    @PostMapping("/stories/{id}/pin")
    public ResponseEntity<Map<String, Object>> pinStory(@PathVariable Long id) {
        return pin(ContentService.Module.STORY, id, true);
    }

    @DeleteMapping("/stories/{id}/pin")
    public ResponseEntity<Map<String, Object>> unpinStory(@PathVariable Long id) {
        return pin(ContentService.Module.STORY, id, false);
    }

    @GetMapping("/timelines")
    public ResponseEntity<Map<String, Object>> listTimelines() {
        return list(ContentService.Module.TIMELINE);
    }

    @PostMapping("/timelines")
    public ResponseEntity<Map<String, Object>> createTimeline(@Valid @RequestBody ContentRequest request) {
        return create(ContentService.Module.TIMELINE, request);
    }

    @PutMapping("/timelines/{id}")
    public ResponseEntity<Map<String, Object>> updateTimeline(@PathVariable Long id, @Valid @RequestBody ContentRequest request) {
        return update(ContentService.Module.TIMELINE, id, request);
    }

    @DeleteMapping("/timelines/{id}")
    public ResponseEntity<Map<String, Object>> deleteTimeline(@PathVariable Long id) {
        return delete(ContentService.Module.TIMELINE, id);
    }

    @PostMapping("/timelines/{id}/pin")
    public ResponseEntity<Map<String, Object>> pinTimeline(@PathVariable Long id) {
        return pin(ContentService.Module.TIMELINE, id, true);
    }

    @DeleteMapping("/timelines/{id}/pin")
    public ResponseEntity<Map<String, Object>> unpinTimeline(@PathVariable Long id) {
        return pin(ContentService.Module.TIMELINE, id, false);
    }

    @GetMapping("/photos")
    public ResponseEntity<Map<String, Object>> listPhotos() {
        return list(ContentService.Module.PHOTO);
    }

    @PostMapping("/photos")
    public ResponseEntity<Map<String, Object>> createPhoto(@Valid @RequestBody ContentRequest request) {
        return create(ContentService.Module.PHOTO, request);
    }

    @PutMapping("/photos/{id}")
    public ResponseEntity<Map<String, Object>> updatePhoto(@PathVariable Long id, @Valid @RequestBody ContentRequest request) {
        return update(ContentService.Module.PHOTO, id, request);
    }

    @DeleteMapping("/photos/{id}")
    public ResponseEntity<Map<String, Object>> deletePhoto(@PathVariable Long id) {
        return delete(ContentService.Module.PHOTO, id);
    }

    @PostMapping("/photos/{id}/pin")
    public ResponseEntity<Map<String, Object>> pinPhoto(@PathVariable Long id) {
        return pin(ContentService.Module.PHOTO, id, true);
    }

    @DeleteMapping("/photos/{id}/pin")
    public ResponseEntity<Map<String, Object>> unpinPhoto(@PathVariable Long id) {
        return pin(ContentService.Module.PHOTO, id, false);
    }

    @GetMapping("/videos")
    public ResponseEntity<Map<String, Object>> listVideos() {
        return list(ContentService.Module.VIDEO);
    }

    @PostMapping("/videos")
    public ResponseEntity<Map<String, Object>> createVideo(@Valid @RequestBody ContentRequest request) {
        return create(ContentService.Module.VIDEO, request);
    }

    @PutMapping("/videos/{id}")
    public ResponseEntity<Map<String, Object>> updateVideo(@PathVariable Long id, @Valid @RequestBody ContentRequest request) {
        return update(ContentService.Module.VIDEO, id, request);
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<Map<String, Object>> deleteVideo(@PathVariable Long id) {
        return delete(ContentService.Module.VIDEO, id);
    }

    @PostMapping("/videos/{id}/pin")
    public ResponseEntity<Map<String, Object>> pinVideo(@PathVariable Long id) {
        return pin(ContentService.Module.VIDEO, id, true);
    }

    @DeleteMapping("/videos/{id}/pin")
    public ResponseEntity<Map<String, Object>> unpinVideo(@PathVariable Long id) {
        return pin(ContentService.Module.VIDEO, id, false);
    }

    @GetMapping("/worlds")
    public ResponseEntity<Map<String, Object>> listWorlds() {
        return list(ContentService.Module.WORLD);
    }

    @PostMapping("/worlds")
    public ResponseEntity<Map<String, Object>> createWorld(@Valid @RequestBody ContentRequest request) {
        return create(ContentService.Module.WORLD, request);
    }

    @PutMapping("/worlds/{id}")
    public ResponseEntity<Map<String, Object>> updateWorld(@PathVariable Long id, @Valid @RequestBody ContentRequest request) {
        return update(ContentService.Module.WORLD, id, request);
    }

    @DeleteMapping("/worlds/{id}")
    public ResponseEntity<Map<String, Object>> deleteWorld(@PathVariable Long id) {
        return delete(ContentService.Module.WORLD, id);
    }

    @PostMapping("/worlds/{id}/pin")
    public ResponseEntity<Map<String, Object>> pinWorld(@PathVariable Long id) {
        return pin(ContentService.Module.WORLD, id, true);
    }

    @DeleteMapping("/worlds/{id}/pin")
    public ResponseEntity<Map<String, Object>> unpinWorld(@PathVariable Long id) {
        return pin(ContentService.Module.WORLD, id, false);
    }

    @GetMapping("/events")
    public ResponseEntity<Map<String, Object>> listEvents() {
        return list(ContentService.Module.EVENT);
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> createEvent(@Valid @RequestBody ContentRequest request) {
        return create(ContentService.Module.EVENT, request);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<Map<String, Object>> updateEvent(@PathVariable Long id, @Valid @RequestBody ContentRequest request) {
        return update(ContentService.Module.EVENT, id, request);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Map<String, Object>> deleteEvent(@PathVariable Long id) {
        return delete(ContentService.Module.EVENT, id);
    }

    @PostMapping("/events/{id}/pin")
    public ResponseEntity<Map<String, Object>> pinEvent(@PathVariable Long id) {
        return pin(ContentService.Module.EVENT, id, true);
    }

    @DeleteMapping("/events/{id}/pin")
    public ResponseEntity<Map<String, Object>> unpinEvent(@PathVariable Long id) {
        return pin(ContentService.Module.EVENT, id, false);
    }

    private ResponseEntity<Map<String, Object>> list(ContentService.Module module) {
        return ResponseEntity.ok(Map.of("data", contentService.list(module)));
    }

    private ResponseEntity<Map<String, Object>> create(ContentService.Module module, ContentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", contentService.create(module, request)));
    }

    private ResponseEntity<Map<String, Object>> update(ContentService.Module module, Long id, ContentRequest request) {
        return ResponseEntity.ok(Map.of("data", contentService.update(module, id, request)));
    }

    private ResponseEntity<Map<String, Object>> delete(ContentService.Module module, Long id) {
        contentService.delete(module, id);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    private ResponseEntity<Map<String, Object>> pin(ContentService.Module module, Long id, boolean pinned) {
        return ResponseEntity.ok(Map.of("data", contentService.pin(module, id, pinned)));
    }

}
