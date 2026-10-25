package org.apollo.api.tracking.service;

import java.util.Set;

public final class TrackingCatalog {
    private TrackingCatalog() {}
    public static final Set<String> EVENTS = Set.of("app_opened", "screen_viewed", "work_order_viewed", "work_order_start_clicked", "barcode_scanned", "report_started", "action_failed", "app_foregrounded", "app_backgrounded", "screen_exited", "ui_action", "repeated_action", "form_started", "form_submitted", "form_succeeded", "form_abandoned", "validation_failed", "filter_changed", "search_completed", "empty_state_viewed", "retry_clicked", "feedback_shown", "permission_result", "api_request_completed", "api_request_failed");
    public static final Set<String> KEYS = Set.of("work_order_id", "maintenance_type_id", "action", "error_type", "input_method", "previous_screen", "duration_ms", "exit_reason", "form_id", "field", "rule", "target", "attempts", "filter_name", "filter_value", "query_length", "result_count", "state", "feedback_type", "permission", "granted", "http_status", "method", "endpoint", "error_code");
}
