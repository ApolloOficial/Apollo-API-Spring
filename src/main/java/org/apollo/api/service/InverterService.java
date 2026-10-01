package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.InverterCreateDTO;
import org.apollo.api.dto.InverterDTO;
import org.apollo.api.dto.PanelCreateDTO;
import org.apollo.api.dto.StringCreateDTO;
import org.apollo.api.enums.InverterStatusEnum;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Inverter;
import org.apollo.api.repository.InverterModelRepository;
import org.apollo.api.repository.InverterRepository;
import org.apollo.api.security.TenantContext;
import org.apollo.api.util.DbProcedures;
import org.apollo.api.util.Specs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InverterService {

    private final InverterRepository inverterRepository;
    private final InverterModelRepository inverterModelRepository;
    private final DbProcedures procedures;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public Page<InverterDTO> findAll(UUID companyUnitId, InverterStatusEnum status, Long inverterModelId,
                                     String search, Pageable pageable) {
        Specification<Inverter> spec = Specification
                .where(Specs.<Inverter>equalTo(i -> i.get("companyUnit").get("company").get("id"), companyId()))
                .and(Specs.<Inverter>equalTo(i -> i.get("companyUnit").get("id"), companyUnitId))
                .and(Specs.<Inverter>equalTo(i -> i.get("status"), status))
                .and(Specs.<Inverter>equalTo(i -> i.get("inverterModel").get("id"), inverterModelId))
                .and(matchesCodeOrSerial(search));
        return inverterRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public InverterDTO findById(UUID id) {
        return toDTO(findInverter(id));
    }

    /**
     * Cadastra o inversor com suas strings e placas (pr_register_inverter). Quem cadastra e o
     * operador logado e o inversor fica na filial dele; o banco valida cargo, MPPT/entrada etc.
     */
    public InverterDTO create(InverterCreateDTO dto) {
        if (!inverterModelRepository.existsById(dto.inverterModelId())) {
            throw new ResourceNotFoundException("Inverter model not found: " + dto.inverterModelId());
        }
        UUID inverterId = procedures.registerInverter(tenantContext.getUserId(), toPayload(dto));
        return toDTO(findInverter(inverterId));
    }

    private Inverter findInverter(UUID id) {
        return inverterRepository.findByIdAndCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Inverter not found: " + id));
    }

    private Specification<Inverter> matchesCodeOrSerial(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), pattern),
                cb.like(cb.lower(root.get("serialNumber")), pattern));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private InverterDTO toDTO(Inverter i) {
        return new InverterDTO(i.getId(), i.getCompanyUnit().getId(), i.getCompanyUnit().getName(),
                i.getInverterModel().getId(), i.getInverterModel().getBrand(), i.getInverterModel().getModel(),
                i.getCode(), i.getSerialNumber(), i.getStatus(), i.getInstalledAt(), i.getStringsCount());
    }

    // ---- monta o JSON que a procedure espera (sem depender de biblioteca de JSON) -----------

    private String toPayload(InverterCreateDTO dto) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"inverter_model_id\":").append(dto.inverterModelId());
        json.append(",\"code\":").append(quote(dto.code()));
        json.append(",\"serial_number\":").append(quote(dto.serialNumber()));
        json.append(",\"installed_at\":").append(quote(dto.installedAt().toString()));
        json.append(",\"strings\":[");
        boolean firstString = true;
        for (StringCreateDTO s : dto.strings()) {
            if (!firstString) json.append(',');
            firstString = false;
            json.append("{\"mppt_number\":").append(s.mpptNumber());
            json.append(",\"entry_number\":").append(s.entryNumber());
            json.append(",\"panel_model_id\":").append(s.panelModelId());
            json.append(",\"invoice_number\":").append(quote(s.invoiceNumber()));
            json.append(",\"acquisition_dt\":").append(quote(s.acquisitionDt().toString()));
            json.append(",\"unit_cost\":").append(s.unitCost() == null ? "null" : s.unitCost().toPlainString());
            json.append(",\"panels\":[");
            boolean firstPanel = true;
            for (PanelCreateDTO p : s.panels()) {
                if (!firstPanel) json.append(',');
                firstPanel = false;
                json.append("{\"serial_number\":").append(quote(p.serialNumber()));
                json.append(",\"barcode\":").append(quote(p.barcode())).append('}');
            }
            json.append("]}");
        }
        json.append("]}");
        return json.toString();
    }

    private static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
