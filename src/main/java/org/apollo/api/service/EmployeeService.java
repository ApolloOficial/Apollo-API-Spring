package org.apollo.api.service;

import org.apollo.api.dto.EmployeeDTO;
import org.apollo.api.model.Employee;
import org.apollo.api.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<EmployeeDTO> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public EmployeeDTO findById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        return toDTO(employee);
    }

    public EmployeeDTO create(EmployeeDTO dto) {

        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        Employee employee = new Employee();

        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPasswordHash(
                passwordEncoder.encode(dto.getPassword())
        );
        employee.setRoleId(dto.getRoleId());
        employee.setCompanyUnitId(dto.getCompanyUnitId());
        employee.setIsActive(
                dto.getIsActive() != null ? dto.getIsActive() : true
        );
        employee.setFcmToken(dto.getFcmToken());

        Employee savedEmployee = employeeRepository.save(employee);

        return toDTO(savedEmployee);
    }

    public EmployeeDTO update(UUID id, EmployeeDTO dto) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setRoleId(dto.getRoleId());
        employee.setCompanyUnitId(dto.getCompanyUnitId());
        employee.setIsActive(dto.getIsActive());
        employee.setFcmToken(dto.getFcmToken());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            employee.setPasswordHash(
                    passwordEncoder.encode(dto.getPassword())
            );
        }

        Employee updatedEmployee = employeeRepository.save(employee);

        return toDTO(updatedEmployee);
    }

    public void delete(UUID id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        employeeRepository.delete(employee);
    }

    public EmployeeDTO deactivate(UUID id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        employee.setIsActive(false);

        Employee updatedEmployee = employeeRepository.save(employee);

        return toDTO(updatedEmployee);
    }

    private EmployeeDTO toDTO(Employee employee) {

        EmployeeDTO dto = new EmployeeDTO();

        dto.setId(employee.getId());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());
        dto.setRoleId(employee.getRoleId());
        dto.setCompanyUnitId(employee.getCompanyUnitId());
        dto.setIsActive(employee.getIsActive());
        dto.setFcmToken(employee.getFcmToken());
        dto.setCreatedAt(employee.getCreatedAt());

        return dto;
    }
}