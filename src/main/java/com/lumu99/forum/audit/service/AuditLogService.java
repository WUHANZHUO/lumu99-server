package com.lumu99.forum.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumu99.forum.domain.AuditLog;
import com.lumu99.forum.mapper.AuditLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private static final String MASKED_VALUE = "******";

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogMapper auditLogMapper, ObjectMapper objectMapper) {
        this.auditLogMapper = auditLogMapper;
        this.objectMapper = objectMapper;
    }

    public void record(String operatorUuid,
                       String operatorRole,
                       String action,
                       String targetType,
                       String targetId,
                       Object requestPayload,
                       String result,
                       String ip,
                       String userAgent) {
        String maskedPayload = serializeWithMask(requestPayload);
        AuditLog log = new AuditLog();
        log.setOperatorUuid(operatorUuid);
        log.setOperatorRole(operatorRole);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setRequestPayload(maskedPayload);
        log.setResult(result);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        auditLogMapper.insert(log);
        AuditLogService.log.info("audit action={} result={} operatorUuid={} role={} targetType={} targetId={} payload={}",
                action, result, operatorUuid, operatorRole, targetType, targetId, maskedPayload);
    }

    public String serializeWithMask(Object payload) {
        if (payload == null) return null;
        Object masked = maskNode(toGenericNode(payload));
        try {
            return objectMapper.writeValueAsString(masked);
        } catch (JsonProcessingException ex) {
            return String.valueOf(masked);
        }
    }

    private Object toGenericNode(Object payload) {
        try {
            return objectMapper.convertValue(payload, Object.class);
        } catch (IllegalArgumentException ex) {
            return payload;
        }
    }

    private Object maskNode(Object node) {
        if (node instanceof Map<?, ?> mapNode) {
            Map<String, Object> masked = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapNode.entrySet()) {
                String key = String.valueOf(entry.getKey());
                masked.put(key, isPasswordField(key) ? MASKED_VALUE : maskNode(entry.getValue()));
            }
            return masked;
        }
        if (node instanceof List<?> listNode) {
            List<Object> masked = new ArrayList<>(listNode.size());
            for (Object item : listNode) masked.add(maskNode(item));
            return masked;
        }
        return node;
    }

    private boolean isPasswordField(String key) {
        return key.toLowerCase(Locale.ROOT).contains("password");
    }
}
