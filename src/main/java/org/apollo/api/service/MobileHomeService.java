package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.MobileHomeAttentionItemDTO;
import org.apollo.api.dto.MobileHomeDTO;
import org.apollo.api.dto.MobileHomePendingRegistrationDTO;
import org.apollo.api.dto.MobileHomeServiceOrderDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.repository.MobileHomeRepository;
import org.apollo.api.repository.MobileHomeRepository.AttentionRow;
import org.apollo.api.repository.MobileHomeRepository.HeaderRow;
import org.apollo.api.repository.MobileHomeRepository.OrderRow;
import org.apollo.api.repository.MobileHomeRepository.PendingRow;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MobileHomeService {

    static final int ORDERS_LIMIT = 20;
    static final int ATTENTION_LIMIT = 10;
    static final int PENDING_LIMIT = 10;
    static final String PHOTO_PATH = "api/v1/settings/photo";
    static final String HEALTH_LABEL = "Saúde da string";
    static final String NO_MEASUREMENT_LABEL = "Sem medição";

    private final MobileHomeRepository repository;
    private final TenantContext tenantContext;

    public MobileHomeDTO home() {
        UUID userId = tenantContext.getUserId();
        UUID unitId = tenantContext.getCompanyUnitId();
        HeaderRow header = repository.findHeader(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UUID technicianId = "TECHNICIAN".equals(tenantContext.getRoleName()) ? userId : null;

        List<MobileHomeServiceOrderDTO> orders = List.of();
        List<MobileHomeAttentionItemDTO> attention = List.of();
        List<MobileHomePendingRegistrationDTO> pending = List.of();
        int totalOrders = 0;
        int totalAttention = 0;

        if (unitId != null) {
            var ordersPage = repository.findOpenOrders(unitId, technicianId, ORDERS_LIMIT);
            var attentionPage = repository.findAttention(unitId, technicianId, ATTENTION_LIMIT);
            orders = ordersPage.items().stream().map(this::toOrder).toList();
            attention = attentionPage.items().stream().map(this::toAttention).toList();
            pending = repository.findPendingRegistrations(unitId, PENDING_LIMIT).items().stream()
                    .map(this::toPending).toList();
            totalOrders = ordersPage.total();
            totalAttention = attentionPage.total();
        }

        return new MobileHomeDTO(header.fullName(), header.hasPhoto() ? PHOTO_PATH : null, header.unitName(),
                header.city(), header.state(), totalOrders, orders, totalAttention, attention, pending);
    }

    private MobileHomeServiceOrderDTO toOrder(OrderRow row) {
        return new MobileHomeServiceOrderDTO(row.id(), String.format("OS-%04d", row.id()), row.assetCode(),
                status(row.status()), priority(row.severity()),
                row.dueDate() == null ? null : row.dueDate().toString());
    }

    private MobileHomeAttentionItemDTO toAttention(AttentionRow row) {
        boolean measured = row.healthScore() != null;
        return new MobileHomeAttentionItemDTO(row.id(), priority(row.severity()), row.message(),
                measured ? HEALTH_LABEL : NO_MEASUREMENT_LABEL, measured ? row.healthScore() : 0d,
                row.serviceOrderId() == null ? 0L : row.serviceOrderId());
    }

    private MobileHomePendingRegistrationDTO toPending(PendingRow row) {
        return new MobileHomePendingRegistrationDTO(row.stringId().getMostSignificantBits() & Long.MAX_VALUE,
                row.code(), row.registered(), row.total());
    }

    static String status(String value) {
        if (value == null) {
            return "PENDING";
        }
        return switch (value) {
            case "EM_ANDAMENTO" -> "IN_PROGRESS";
            case "CONCLUIDA" -> "COMPLETED";
            case "CANCELADA" -> "CANCELLED";
            default -> "PENDING";
        };
    }

    static String priority(String value) {
        if (value == null) {
            return "MEDIUM";
        }
        return switch (value) {
            case "BAIXA" -> "LOW";
            case "ALTA" -> "HIGH";
            case "CRITICA" -> "CRITICAL";
            default -> "MEDIUM";
        };
    }
}
