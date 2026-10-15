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
    // dimensao maxima aceita na entrada (anti "decompression bomb") e tamanho final do avatar
    static final int MAX_INPUT_SIDE = 4096;
    static final int AVATAR_SIDE = 512;

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
        byte[] original = read(file);
        if (detectContentType(original) == null) {
            throw new BusinessRuleException("Photo must be a JPEG or PNG image");
        }
        byte[] data = normalize(original);
        String contentType = "image/jpeg";
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

    // Decodifica, recorta ao centro em quadrado, reduz para AVATAR_SIDE e regrava como JPEG.
    static byte[] normalize(byte[] data) {
        try (javax.imageio.stream.ImageInputStream in = javax.imageio.ImageIO.createImageInputStream(
                new java.io.ByteArrayInputStream(data))) {
            java.util.Iterator<javax.imageio.ImageReader> readers = javax.imageio.ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new BusinessRuleException("Photo is not a valid image");
            }
            javax.imageio.ImageReader reader = readers.next();
            java.awt.image.BufferedImage source;
            try {
                reader.setInput(in);
                int w = reader.getWidth(0);
                int h = reader.getHeight(0);
                if (w < 1 || h < 1 || w > MAX_INPUT_SIDE || h > MAX_INPUT_SIDE) {
                    throw new BusinessRuleException("Photo dimensions must be at most " + MAX_INPUT_SIDE + "x" + MAX_INPUT_SIDE);
                }
                source = reader.read(0);
            } finally {
                reader.dispose();
            }
            int side = Math.min(source.getWidth(), source.getHeight());
            java.awt.image.BufferedImage square = source.getSubimage(
                    (source.getWidth() - side) / 2, (source.getHeight() - side) / 2, side, side);
            int target = Math.min(side, AVATAR_SIDE);
            java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(
                    target, target, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = out.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, target, target);
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(square, 0, 0, target, target, null);
            g.dispose();
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(out, "jpg", bytes);
            return bytes.toByteArray();
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof BusinessRuleException business) {
                throw business;
            }
            throw new BusinessRuleException("Photo is not a valid image");
        }
    }

    private byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessRuleException("Could not read the photo file");
        }
    }
}
