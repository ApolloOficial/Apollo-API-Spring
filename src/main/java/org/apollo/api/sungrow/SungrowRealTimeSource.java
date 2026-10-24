package org.apollo.api.sungrow;

import java.util.List;
import java.util.Map;

/**
 * De onde vem a leitura em tempo real dos inversores. A leitura devolvida segue o formato da API da Sungrow
 * (device_sn, device_time, dev_fault_status e os pontos p{id}), entao o resto do codigo e o mesmo nos dois casos:
 * <ul>
 *   <li>{@link SungrowLiveSource}: planta real, chama a API da Sungrow;</li>
 *   <li>{@link SungrowSimulatedPlant}: planta simulada dentro da propria API, para demonstracao.</li>
 * </ul>
 */
public interface SungrowRealTimeSource {

    List<Map<String, Object>> realTime(List<String> serialNumbers, List<String> pointIds);
}
