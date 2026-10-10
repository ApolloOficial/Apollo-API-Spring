package org.apollo.api.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.EmployeePhoto;
import org.apollo.api.repository.EmployeePhotoRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PhotoService {

    public static final long MAX_BYTES = 1024 * 1024;

    private final TenantContext tenantContext;
    private final EmployeePhotoRepository photoRepository;

    @Transactional
    public void upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Photo file is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessRuleException("Photo must be at most 1 MB");
        }
        byte[] data = read(file);
        String contentType = detectContentType(data);
        if (contentType == null) {
            throw new BusinessRuleException("Photo must be a JPEG or PNG image");
        }
        UUID userId = tenantContext.getUserId();
        EmployeePhoto photo = photoRepository.findById(userId)
                .orElseGet(() -> new EmployeePhoto(userId, contentType, data));
        photo.setContentType(contentType);
        photo.setData(data);
        photo.setUpdatedAt(LocalDateTime.now());
        photoRepository.save(photo);
    }

    @Transactional(readOnly = true)
    public EmployeePhoto current() {
        return photoRepository.findById(tenantContext.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));
    }

    @Transactional
    public void remove() {
        UUID userId = tenantContext.getUserId();
        if (photoRepository.existsById(userId)) {
            photoRepository.deleteById(userId);
        }
    }

    static String detectContentType(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (data.length >= 8 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
            return "image/png";
        }
        return null;
    }

    private byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessRuleException("Could not read the photo file");
        }
    }
}
