package org.apollo.api.sungrow;

import java.util.List;
import java.util.Map;

public interface SungrowRealTimeSource {
    List<Map<String, Object>> realTime(List<String> serialNumbers, List<String> pointIds);
}
