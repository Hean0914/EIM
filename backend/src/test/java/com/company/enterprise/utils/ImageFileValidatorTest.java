package com.company.enterprise.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageFileValidatorTest {

    @Test
    void acceptsAllowedExtensionsIgnoringCase() {
        assertTrue(ImageFileValidator.isAllowedExtension("avatar.png"));
        assertTrue(ImageFileValidator.isAllowedExtension("avatar.JPG"));
        assertTrue(ImageFileValidator.isAllowedExtension("my.photo.webp"));
    }

    @Test
    void rejectsUnsupportedOrMissingExtension() {
        assertFalse(ImageFileValidator.isAllowedExtension("payload.exe"));
        assertFalse(ImageFileValidator.isAllowedExtension("payload.svg"));
        assertFalse(ImageFileValidator.isAllowedExtension("noextension"));
        assertFalse(ImageFileValidator.isAllowedExtension("trailingdot."));
        assertFalse(ImageFileValidator.isAllowedExtension(null));
    }

    @Test
    void extensionOfReturnsLowercaseSuffixOrNull() {
        assertEquals("png", ImageFileValidator.extensionOf("Avatar.PNG"));
        assertEquals("jpeg", ImageFileValidator.extensionOf("a.b.jpeg"));
        assertNull(ImageFileValidator.extensionOf("noextension"));
        assertNull(ImageFileValidator.extensionOf("trailingdot."));
        assertNull(ImageFileValidator.extensionOf(null));
    }

    @Test
    void acceptsMissingContentTypeButRejectsUnsupportedOne() {
        assertTrue(ImageFileValidator.isAllowedContentType(null));
        assertTrue(ImageFileValidator.isAllowedContentType("  "));
        assertTrue(ImageFileValidator.isAllowedContentType("image/png"));
        assertTrue(ImageFileValidator.isAllowedContentType("IMAGE/PNG"));
        assertFalse(ImageFileValidator.isAllowedContentType("image/svg+xml"));
        assertFalse(ImageFileValidator.isAllowedContentType("application/octet-stream"));
    }

    @Test
    void resolvesAndNormalizesAllowedContentType() {
        assertEquals("image/png", ImageFileValidator.resolveContentType("avatar.png", " IMAGE/PNG "));
        assertEquals("image/jpeg", ImageFileValidator.resolveContentType("avatar.JPG", "image/jpeg"));
    }

    @Test
    void infersContentTypeFromExtensionWhenMissing() {
        assertEquals("image/jpeg", ImageFileValidator.resolveContentType("avatar.jpeg", null));
        assertEquals("image/png", ImageFileValidator.resolveContentType("avatar.png", " "));
        assertEquals("image/gif", ImageFileValidator.resolveContentType("avatar.gif", null));
        assertEquals("image/webp", ImageFileValidator.resolveContentType("avatar.webp", null));
        assertNull(ImageFileValidator.resolveContentType("avatar.svg", null));
    }

    @Test
    void enforcesSizeBoundary() {
        assertFalse(ImageFileValidator.isAllowedSize(0));
        assertTrue(ImageFileValidator.isAllowedSize(1));
        assertTrue(ImageFileValidator.isAllowedSize(ImageFileValidator.MAX_IMAGE_SIZE_BYTES));
        assertFalse(ImageFileValidator.isAllowedSize(ImageFileValidator.MAX_IMAGE_SIZE_BYTES + 1));
    }

    @Test
    void combinesExtensionContentTypeAndSize() {
        assertTrue(ImageFileValidator.isAllowed("avatar.png", "image/png", 1024));
        assertFalse(ImageFileValidator.isAllowed("avatar.png", "image/png", 0));
        assertFalse(ImageFileValidator.isAllowed("avatar.exe", "image/png", 1024));
        assertFalse(ImageFileValidator.isAllowed("avatar.png", "text/html", 1024));
    }
}
