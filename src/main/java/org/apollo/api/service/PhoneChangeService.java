package org.apollo.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.MeDTO;
import org.apollo.api.dto.PhoneChangeRequestCreateDTO;
import org.apollo.api.dto.PhoneChangeRequestDTO;
import org.apollo.api.enums.PhoneChangeStatusEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Employee;
import org.apollo.api.model.PhoneChangeRequest;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.repository.PhoneChangeRequestRepository;
import org.apollo.api.security.TenantContext;
import org.apollo.api.util.PhoneMask;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhoneChangeService {

    private final TenantContext tenantContext;
    private final ProfileService profileService;
    private final SettingsService settingsService;
    private final PhoneChangeRequestRepository requestRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public PhoneChangeRequestDTO request(PhoneChangeRequestCreateDTO dto) {
        MeDTO me = profileService.me();
        String phone = PhoneMask.digitsOnly(dto.getNewPhone());
        if (!PhoneMask.isValid(phone)) {
            throw new BusinessRuleException("Invalid phone number");
        }
        if (phone.equals(settingsService.currentPhone(me.getUserId()))) {
            throw new BusinessRuleException("The new phone is the same as the current one");
        }
        if (requestRepository.existsByEmployeeIdAndStatus(me.getUserId(), PhoneChangeStatusEnum.PENDING)) {
            throw new BusinessRuleException("There is already a pending phone change request");
        }
        PhoneChangeRequest request = new PhoneChangeRequest();
        request.setEmployeeId(me.getUserId());
        request.setNewPhone(phone);
        PhoneChangeRequest saved = requestRepository.save(request);
        return toDto(saved, me.getFullName(), me.getEmail());
    }

    @Transactional(readOnly = true)
    public List<PhoneChangeRequestDTO> listPending() {
        return requestRepository.findByScopeAndStatus(tenantContext.getCompanyId(), requireUnit(),
                PhoneChangeStatusEnum.PENDING);
    }

    @Transactional
    public PhoneChangeRequestDTO approve(UUID id) {
        PhoneChangeRequest request = review(id, PhoneChangeStatusEnum.APPROVED);
        settingsService.setPhone(request.getEmployeeId(), request.getNewPhone());
        return toDto(request);
    }

    @Transactional
    public PhoneChangeRequestDTO reject(UUID id) {
        return toDto(review(id, PhoneChangeStatusEnum.REJECTED));
    }

    private PhoneChangeRequest review(UUID id, PhoneChangeStatusEnum decision) {
        PhoneChangeRequest request = requestRepository
                .findByIdInScope(id, tenantContext.getCompanyId(), requireUnit())
                .orElseThrow(() -> new ResourceNotFoundException("Phone change request not found"));
        if (request.getStatus() != PhoneChangeStatusEnum.PENDING) {
            throw new BusinessRuleException("This request has already been reviewed");
        }
        request.setStatus(decision);
        request.setReviewedBy(tenantContext.getUserId());
        request.setReviewedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    private UUID requireUnit() {
        UUID unitId = tenantContext.getCompanyUnitId();
        if (unitId == null) {
            throw new AccessDeniedException("Operation requires a branch scope");
        }
        return unitId;
    }

    private PhoneChangeRequestDTO toDto(PhoneChangeRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId()).orElse(null);
        return toDto(request, employee == null ? null : employee.getFullName(),
                employee == null ? null : employee.getEmail());
    }

    private PhoneChangeRequestDTO toDto(PhoneChangeRequest request, String name, String email) {
        return new PhoneChangeRequestDTO(request.getId(), request.getEmployeeId(), name, email,
                request.getNewPhone(), request.getStatus(), request.getCreatedAt(), request.getReviewedAt());
    }
}
