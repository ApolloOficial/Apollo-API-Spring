package org.apollo.api.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.EmployeePhoto;
import org.apollo.api.repository.EmployeePhotoRepository;
import org.apollo.api.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    @Mock
    private TenantContext tenantContext;

    @Mock
    private EmployeePhotoRepository photoRepository;

    @InjectMocks
    private PhotoService photoService;

    @Test
    void shouldDetectImageTypesByContent() {
        assertEquals("image/jpeg", PhotoService.detectContentType(JPEG));
        assertEquals("image/png", PhotoService.detectContentType(PNG));
        assertNull(PhotoService.detectContentType(new byte[] {1, 2, 3, 4, 5, 6, 7, 8}));
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);

        assertThrows(BusinessRuleException.class, () -> photoService.upload(file));
        verify(photoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectFileLargerThanOneMegabyte() {
        byte[] big = new byte[(int) PhotoService.MAX_BYTES + 1];
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", big);

        assertThrows(BusinessRuleException.class, () -> photoService.upload(file));
    }

    @Test
    void shouldRejectFileThatIsNotAnImage() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg",
                new byte[] {1, 2, 3, 4, 5, 6, 7, 8});

        assertThrows(BusinessRuleException.class, () -> photoService.upload(file));
    }

    @Test
    void shouldSaveValidPhotoForTheLoggedUser() {
        when(tenantContext.getUserId()).thenReturn(USER_ID);
        when(photoRepository.findById(USER_ID)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", realPng(800, 600));

        photoService.upload(file);

        ArgumentCaptor<EmployeePhoto> captor = ArgumentCaptor.forClass(EmployeePhoto.class);
        verify(photoRepository).save(captor.capture());
        assertEquals(USER_ID, captor.getValue().getEmployeeId());
        assertEquals("image/jpeg", captor.getValue().getContentType());
        java.awt.image.BufferedImage saved = javax.imageio.ImageIO.read(
                new java.io.ByteArrayInputStream(captor.getValue().getData()));
        assertEquals(512, saved.getWidth());
        assertEquals(512, saved.getHeight());
    }

    @Test
    void shouldRejectImageLargerThanMaxDimension() {
        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png",
                realPng(PhotoService.MAX_INPUT_SIDE + 1, 10));

        assertThrows(BusinessRuleException.class, () -> photoService.upload(file));
    }

    @Test
    void shouldRejectTruncatedImage() {
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", PNG);

        assertThrows(BusinessRuleException.class, () -> photoService.upload(file));
    }

    private static byte[] realPng(int w, int h) {
        try {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "png", out);
            return out.toByteArray();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void shouldFailWhenThereIsNoPhoto() {
        when(tenantContext.getUserId()).thenReturn(USER_ID);
        when(photoRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> photoService.current());
    }
}
