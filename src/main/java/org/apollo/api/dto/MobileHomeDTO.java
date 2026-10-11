package org.apollo.api.dto;

import java.util.List;

public record MobileHomeDTO(String technicianName, String profileImageUrl, String siteName, String siteCity,
                            String siteState, int totalServiceOrders, List<MobileHomeServiceOrderDTO> serviceOrders,
                            int totalAttentionItems, List<MobileHomeAttentionItemDTO> attentionItems,
                            List<MobileHomePendingRegistrationDTO> pendingRegistrations) {
}
