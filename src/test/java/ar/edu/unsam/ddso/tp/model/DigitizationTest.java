package ar.edu.unsam.ddso.tp.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DigitizationTest {

    @Test
    void addTagLinksBothSides() {
        // Given
        Digitization digitization = new Digitization();
        Tag tag = new Tag();

        // When
        digitization.addTag(tag);

        // Then
        assertTrue(digitization.getTags().contains(tag));
        assertTrue(tag.getDigitizations().contains(digitization));
    }

    @Test
    void addSameTagTwiceKeepsSingleAssociation() {
        // Given
        Digitization digitization = new Digitization();
        Tag tag = new Tag();

        // When
        digitization.addTag(tag);
        digitization.addTag(tag);

        // Then
        assertEquals(1, digitization.getTags().size());
        assertEquals(1, tag.getDigitizations().size());
    }

    @Test
    void removeTagUnlinksBothSides() {
        // Given
        Digitization digitization = new Digitization();
        Tag tag = new Tag();
        digitization.addTag(tag);

        // When
        digitization.removeTag(tag);

        // Then
        assertTrue(digitization.getTags().isEmpty());
        assertTrue(tag.getDigitizations().isEmpty());
    }
}
