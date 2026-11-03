package edu.uoc.pac2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class SampleLabelFormatterTest {

    @Test
    @Order(1)
    @DisplayName("isValidSampleCode")
    void validatesSampleCodes() {
        assertTrue(SampleLabelFormatter.isValidSampleCode("BIO-123456"));
        assertTrue(SampleLabelFormatter.isValidSampleCode("BIO-000000"));
        assertFalse(SampleLabelFormatter.isValidSampleCode(null));
        assertFalse(SampleLabelFormatter.isValidSampleCode("bio-123456"));
        assertFalse(SampleLabelFormatter.isValidSampleCode("BIO-12345"));
        assertFalse(SampleLabelFormatter.isValidSampleCode("BIO-12345A"));
        assertFalse(SampleLabelFormatter.isValidSampleCode("BIO-１２３４５６"));
    }

    @Test
    @Order(2)
    @DisplayName("isValidSampleType")
    void validatesSampleTypes() {
        assertTrue(SampleLabelFormatter.isValidSampleType("BLOOD"));
        assertTrue(SampleLabelFormatter.isValidSampleType("saliva"));
        assertTrue(SampleLabelFormatter.isValidSampleType("TiSsUe"));
        assertTrue(SampleLabelFormatter.isValidSampleType("DNA"));
        assertFalse(SampleLabelFormatter.isValidSampleType(null));
        assertFalse(SampleLabelFormatter.isValidSampleType(" blood"));
        assertFalse(SampleLabelFormatter.isValidSampleType("PLASMA"));
    }

    @Test
    @Order(3)
    @DisplayName("calculateCheckDigit")
    void calculatesCheckDigit() {
        assertEquals(1, SampleLabelFormatter.calculateCheckDigit("BIO-123456"));
        assertEquals(0, SampleLabelFormatter.calculateCheckDigit("BIO-000000"));
        assertEquals(9, SampleLabelFormatter.calculateCheckDigit("BIO-999999"));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> SampleLabelFormatter.calculateCheckDigit("BIO-12345A"));
        assertEquals("Invalid sample code.", error.getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("buildLabel")
    void buildsCanonicalLabelAndReportsInvalidInputs() {
        assertEquals("BIO-123456|BLOOD|1",
                SampleLabelFormatter.buildLabel("BIO-123456", "blood"));
        assertEquals("BIO-000000|DNA|0",
                SampleLabelFormatter.buildLabel("BIO-000000", "DNA"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> SampleLabelFormatter.buildLabel(null, "DNA"));
        assertEquals("Invalid sample code.", error.getMessage());
        error = assertThrows(IllegalArgumentException.class,
                () -> SampleLabelFormatter.buildLabel("BIO-123456", "PLASMA"));
        assertEquals("Invalid sample type.", error.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("buildBatchLabels")
    void buildsBatchWithoutTrailingNewline() {
        assertEquals("BIO-123456|BLOOD|1\nBIO-000000|DNA|0",
                SampleLabelFormatter.buildBatchLabels(
                        new String[]{"BIO-123456", "BIO-000000"},
                        new String[]{"blood", "DNA"}));
        assertEquals("", SampleLabelFormatter.buildBatchLabels(new String[]{}, new String[]{}));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> SampleLabelFormatter.buildBatchLabels(null, new String[]{}));
        assertEquals("Invalid batch data.", error.getMessage());
        error = assertThrows(IllegalArgumentException.class,
                () -> SampleLabelFormatter.buildBatchLabels(
                        new String[]{"BIO-123456"}, new String[]{}));
        assertEquals("Invalid batch data.", error.getMessage());
    }
}
