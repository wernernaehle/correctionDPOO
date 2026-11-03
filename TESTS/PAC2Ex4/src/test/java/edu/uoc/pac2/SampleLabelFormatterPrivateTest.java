package edu.uoc.pac2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class SampleLabelFormatterPrivateTest {

    @Test
    @Order(1)
    @DisplayName("calculateCheckDigit")
    void checkDigitUsesPositionsFromLeftToRight() {
        assertEquals(0, SampleLabelFormatter.calculateCheckDigit("BIO-001234"));
        assertEquals(1, SampleLabelFormatter.calculateCheckDigit("BIO-100000"));
        assertEquals(6, SampleLabelFormatter.calculateCheckDigit("BIO-000001"));
    }

    @Test
    @Order(2)
    @DisplayName("buildLabel")
    void labelValidationKeepsCodePriority() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> SampleLabelFormatter.buildLabel("bio-123456", null));
        assertEquals("Invalid sample code.", error.getMessage());
        assertEquals("BIO-000001|SALIVA|6",
                SampleLabelFormatter.buildLabel("BIO-000001", "sAlIvA"));
    }

    @Test
    @Order(3)
    @DisplayName("buildBatchLabels")
    void invalidElementInvalidatesEntireBatch() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> SampleLabelFormatter.buildBatchLabels(
                        new String[]{"BIO-123456", "BIO-12345A"},
                        new String[]{"DNA", "BLOOD"}));
        assertEquals("Invalid batch data.", error.getMessage());
        error = assertThrows(IllegalArgumentException.class,
                () -> SampleLabelFormatter.buildBatchLabels(
                        new String[]{"BIO-123456"}, new String[]{null}));
        assertEquals("Invalid batch data.", error.getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("buildBatchLabels")
    void singleLabelBatchHasNoNewline() {
        assertEquals("BIO-100000|TISSUE|1",
                SampleLabelFormatter.buildBatchLabels(
                        new String[]{"BIO-100000"}, new String[]{"tissue"}));
    }
}
