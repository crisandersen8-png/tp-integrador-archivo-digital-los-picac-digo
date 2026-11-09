package ar.edu.unsam.ddso.tp.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PhysicalPieceTest {

    @Test
    void addDigitizationLinksBothSides() {
        // Given
        PhysicalPiece piece = new PhysicalPiece();
        Digitization digitization = new Digitization();

        // When
        piece.addDigitization(digitization);

        // Then
        assertEquals(1, piece.getDigitizations().size());
        assertTrue(piece.getDigitizations().contains(digitization));
        assertSame(piece, digitization.getPhysicalPiece());
    }

    @Test
    void removeDigitizationUnlinksBothSides() {
        // Given
        PhysicalPiece piece = new PhysicalPiece();
        Digitization digitization = new Digitization();
        piece.addDigitization(digitization);

        // When
        piece.removeDigitization(digitization);

        // Then
        assertTrue(piece.getDigitizations().isEmpty());
        assertNull(digitization.getPhysicalPiece());
    }

    @Test
    void removeDigitizationOfAnotherPieceDoesNotUnlinkIt() {
        // Given
        PhysicalPiece piece = new PhysicalPiece();
        PhysicalPiece otherPiece = new PhysicalPiece();
        Digitization digitization = new Digitization();
        otherPiece.addDigitization(digitization);

        // When
        piece.removeDigitization(digitization);

        // Then
        assertSame(otherPiece, digitization.getPhysicalPiece());
        assertEquals(1, otherPiece.getDigitizations().size());
    }
}
