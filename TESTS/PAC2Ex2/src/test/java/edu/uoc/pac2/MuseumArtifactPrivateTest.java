package edu.uoc.pac2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MuseumArtifactPrivateTest {

    private MuseumArtifact artifact;
    private final LocalDate inspectionDate = LocalDate.now().minusMonths(6);

    @BeforeEach
    void setUp() {
        artifact = new MuseumArtifact("ART-2026-0001", "Artifact", "Unknown", "ES",
                "OTHER", "EXCELLENT", LocalDate.of(2020, 1, 1), inspectionDate,
                50_000.0, false, false);
    }

    @Test
    void testAllFieldsArePrivate() {
        for (Field field : MuseumArtifact.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName());
        }
    }

    @Test
    void testConstructorPropagatesFirstValidationException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MuseumArtifact("INVALID", "", "", "ESP", "INVALID", "INVALID",
                        null, null, 0, false, false));
        assertEquals("Inventory code must follow the format ART-YYYY-NNNN.", exception.getMessage());
    }

    @Test
    void testFailedSetterPreservesState() {
        assertThrows(IllegalArgumentException.class, () -> artifact.setTitle(" "));
        assertEquals("Artifact", artifact.getTitle());
        assertThrows(IllegalArgumentException.class, () -> artifact.setAppraisedValue(Double.NaN));
        assertEquals(50_000.0, artifact.getAppraisedValue());
    }

    @Test
    void testInspectionAndRiskBoundaries() throws Exception {
        assertFalse(artifact.needsInspection(inspectionDate.plusMonths(12).minusDays(1)));
        assertTrue(artifact.needsInspection(inspectionDate.plusMonths(12)));
        artifact.setFragile(true);
        assertTrue(artifact.isHighRisk(inspectionDate.plusMonths(12)));
    }

    @Test
    void testInsuranceCostVariants() throws Exception {
        assertEquals(50.0, artifact.calculateInsuranceCost(10), 0.0001);
        artifact.setFragile(true);
        artifact.setAppraisedValue(100_000.0);
        assertEquals(143.75, artifact.calculateInsuranceCost(10), 0.0001);
        assertAll(
                () -> assertThrowsExactly(Exception.class,
                        () -> artifact.calculateInsuranceCost(-1)),
                () -> assertThrowsExactly(Exception.class,
                        () -> artifact.calculateInsuranceCost(366))
        );
    }

    @Test
    void testLoanStatisticsAndEmptyArrays() throws Exception {
        String[] categories = {"OTHER", "PAINTING", "other"};
        double[] values = {50_000.0, 100_000.0, 49_999.0};
        int[] durations = {1, 365, 20};
        assertEquals(1, artifact.countEligibleLoanRequests(categories, values, durations, inspectionDate));
        assertEquals(100.0 / 3.0, artifact.calculateEligibleLoanRequestPercentage(
                categories, values, durations, inspectionDate), 0.0001);
        assertEquals(0.0, artifact.calculateEligibleLoanRequestPercentage(
                new String[0], new double[0], new int[0], inspectionDate));
    }

    @Test
    void testInvalidArraysAndNaNAreRejected() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> artifact.countEligibleLoanRequests(null, new double[0], new int[0], inspectionDate)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> artifact.countEligibleLoanRequests(
                                new String[]{"OTHER"}, new double[]{Double.NaN}, new int[]{10}, inspectionDate)),
                () -> assertThrowsExactly(Exception.class,
                        () -> artifact.isEligibleForLoan("OTHER", Double.NaN, 10, inspectionDate))
        );
    }
}
