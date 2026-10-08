package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.MeDTO;
import org.apollo.api.model.Company;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.security.AuthenticatedUser;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final TenantContext tenantContext;
    private final CompanyRepository companyRepository;
    private final CompanyUnitRepository companyUnitRepository;

    @Transactional(readOnly = true)
    public MeDTO me() {
        AuthenticatedUser user = tenantContext.currentUser();
        String companyName = companyRepository.findById(user.getCompanyId()).map(Company::getName).orElse(null);
        String unitName = user.getCompanyUnitId() == null ? null
                : companyUnitRepository.findByIdAndCompanyId(user.getCompanyUnitId(), user.getCompanyId())
                .map(CompanyUnit::getName).orElse(null);
        return new MeDTO(user.getUserId(), user.getFullName(), user.getEmail(), user.getRoleName(),
                user.getCompanyId(), companyName, user.getCompanyUnitId(), unitName);
    }
}