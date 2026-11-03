package edu.uoc.pac2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class MuseumArtifactPrivateTest {

    private MuseumArtifact artifact;
    private LocalDate today;
    private LocalDate acquisitionDate;
    private LocalDate inspectionDate;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream output;
    private ByteArrayOutputStream errors;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        output = new ByteArrayOutputStream();
        errors = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        System.setErr(new PrintStream(errors));
        today = LocalDate.now();
        acquisitionDate = today.minusYears(3);
        inspectionDate = today.minusMonths(6);
        artifact = new MuseumArtifact("ART-2026-0042", "  Lady with a Fan  ",
                "  Gustav Klimt  ", "at", "painting", "good",
                acquisitionDate, inspectionDate, 120_000.0, true, false);
    }

    @AfterEach
    void restoreStreams() {
        try {
            assertEquals("", output.toString(), "Unexpected console output");
            assertEquals("", errors.toString(), "Unexpected error output");
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }


    @ParameterizedTest
    @CsvSource({"PAINTING,120000,1,true", "painting,120000,365,true",
            "PaInTiNg,120001,30,true", "PAINTING,119999.99,30,false",
            "OTHER,150000,30,false", "PAINTING,1,30,false"})
    void testSingleLoanRequest(String category, double value, int days, boolean expected) {
        assertEquals(expected, artifact.isEligibleForLoan(category, value, days, inspectionDate));
        assertFalse(artifact.isOnLoan());
        assertEquals(120_000, artifact.getAppraisedValue());
    }

    @Test
    void testLoanEligibilityForEveryCategory() {
        for (String category : new String[]{"PAINTING", "SCULPTURE", "MANUSCRIPT", "TEXTILE", "CERAMIC", "OTHER"}) {
            artifact.setCategory(category);
            assertTrue(artifact.isEligibleForLoan(category.toLowerCase(Locale.ROOT),
                    120_000, 10, inspectionDate));
        }
    }

    @Test
    void testSingleRequestRequiresLoanableArtifact() {
        artifact.setOnLoan(true);
        assertFalse(artifact.isEligibleForLoan("PAINTING", 120_000, 10, inspectionDate));
        artifact.setOnLoan(false);
        for (String status : new String[]{"FAIR", "POOR"}) {
            artifact.setConservationStatus(status);
            assertFalse(artifact.isEligibleForLoan("PAINTING", 120_000, 10, inspectionDate));
        }
        for (String status : new String[]{"EXCELLENT", "GOOD"}) {
            artifact.setConservationStatus(status);
            assertTrue(artifact.isEligibleForLoan("PAINTING", 120_000, 10, inspectionDate));
            assertFalse(artifact.isEligibleForLoan("PAINTING", 120_000, 10,
                    inspectionDate.plusMonths(12)));
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "UNKNOWN", " PAINTING", "PAINTING "})
    void testSingleRequestRejectsInvalidCategories(String category) {
        reject(() -> artifact.isEligibleForLoan(category, 120_000, 10, inspectionDate),
                false, "Invalid loan request data.");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 0, Double.NaN})
    void testSingleRequestRejectsInvalidInsuredValues(double value) {
        reject(() -> artifact.isEligibleForLoan("PAINTING", value, 10, inspectionDate),
                false, "Invalid loan request data.");
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 366, Integer.MAX_VALUE})
    void testSingleRequestRejectsInvalidDurations(int days) {
        reject(() -> artifact.isEligibleForLoan("PAINTING", 120_000, days, inspectionDate),
                false, "Invalid loan request data.");
    }

    @Test
    void testSingleRequestRejectsInvalidDatesBeforeCheckingEligibility() {
        artifact.setOnLoan(true);
        for (LocalDate date : new LocalDate[]{null, inspectionDate.minusDays(1)}) {
            reject(() -> artifact.isEligibleForLoan("OTHER", 1, 10, date), false,
                    "Reference date cannot be null or before the last inspection date.");
        }
    }

    @Test
    void testMixedRequestsAndFractionalPercentage() {
        String[] categories = {"PAINTING", "OTHER", "painting"};
        double[] values = {120_000, 200_000, 119_999};
        int[] days = {1, 365, 10};
        assertStatistics(1, 100.0 / 3, categories, values, days, inspectionDate);
        values[2] = 120_001;
        assertStatistics(2, 200.0 / 3, categories, values, days, inspectionDate);
        assertArrayEquals(new String[]{"PAINTING", "OTHER", "painting"}, categories);
        assertArrayEquals(new double[]{120_000, 200_000, 120_001}, values);
        assertArrayEquals(new int[]{1, 365, 10}, days);
        assertFalse(artifact.isOnLoan());
        assertEquals("PAINTING", artifact.getCategory());
        assertEquals(120_000, artifact.getAppraisedValue());
    }

    @Test
    void testAllNoneSingleAndEmptyRequests() {
        assertStatistics(3, 100, new String[]{"PAINTING", "painting", "PAINTING"},
                new double[]{120_000, 130_000, 140_000}, new int[]{1, 365, 30}, inspectionDate);
        assertStatistics(0, 0, new String[]{"OTHER", "PAINTING"},
                new double[]{200_000, 119_999}, new int[]{1, 365}, inspectionDate);
        assertStatistics(1, 100, new String[]{"PAINTING"}, new double[]{120_000},
                new int[]{1}, inspectionDate);
        assertStatistics(0, 0, new String[]{"OTHER"}, new double[]{120_000},
                new int[]{1}, inspectionDate);
        assertStatistics(0, 0, new String[0], new double[0], new int[0], inspectionDate);
    }

    @Test
    void testStatisticsReflectLoanConditions() {
        String[] categories = {"PAINTING", "painting"};
        double[] values = {120_000, 120_001};
        int[] days = {1, 365};
        artifact.setOnLoan(true);
        assertStatistics(0, 0, categories, values, days, inspectionDate);
        artifact.setOnLoan(false);
        for (String status : new String[]{"FAIR", "POOR"}) {
            artifact.setConservationStatus(status);
            assertStatistics(0, 0, categories, values, days, inspectionDate);
        }
        artifact.setConservationStatus("GOOD");
        assertStatistics(0, 0, categories, values, days, inspectionDate.plusMonths(12));
        assertStatistics(2, 100, categories, values, days,
                inspectionDate.plusMonths(12).minusDays(1));
    }

    @Test
    void testStatisticsRejectEachNullArrayAndLengthMismatch() {
        String[] categories = {"PAINTING"};
        double[] values = {120_000};
        int[] days = {10};
        rejectStatistics(null, values, days, inspectionDate);
        rejectStatistics(categories, null, days, inspectionDate);
        rejectStatistics(categories, values, null, inspectionDate);
        rejectStatistics(new String[0], values, days, inspectionDate);
        rejectStatistics(categories, new double[0], days, inspectionDate);
        rejectStatistics(categories, values, new int[0], inspectionDate);
        rejectStatistics(categories, new double[]{120_000, 120_000}, days, inspectionDate);
        rejectStatistics(categories, values, new int[]{1, 365}, inspectionDate);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void testStatisticsRejectInvalidElementsAtEveryPosition(int index) {
        for (String invalid : new String[]{null, "", "UNKNOWN", " PAINTING"}) {
            String[] categories = {"PAINTING", "PAINTING", "PAINTING"};
            categories[index] = invalid;
            rejectStatistics(categories, new double[]{120_000, 120_000, 120_000},
                    new int[]{1, 1, 1}, inspectionDate);
        }
        for (double invalid : new double[]{-1, 0, Double.NaN}) {
            double[] values = {120_000, 120_000, 120_000};
            values[index] = invalid;
            rejectStatistics(new String[]{"PAINTING", "PAINTING", "PAINTING"},
                    values, new int[]{1, 1, 1}, inspectionDate);
        }
        for (int invalid : new int[]{-1, 0, 366}) {
            int[] days = {1, 1, 1};
            days[index] = invalid;
            rejectStatistics(new String[]{"PAINTING", "PAINTING", "PAINTING"},
                    new double[]{120_000, 120_000, 120_000}, days, inspectionDate);
        }
    }

    @Test
    void testStatisticsValidateDatesIncludingEmptyArrays() {
        for (LocalDate date : new LocalDate[]{null, inspectionDate.minusDays(1)}) {
            rejectStatistics(new String[]{"PAINTING"}, new double[]{120_000}, new int[]{1}, date);
            rejectStatistics(new String[0], new double[0], new int[0], date);
        }
    }

    private void assertStatistics(int count, double percentage, String[] categories,
                                  double[] values, int[] days, LocalDate date) {
        assertEquals(count, artifact.countEligibleLoanRequests(categories, values, days, date));
        assertEquals(percentage,
                artifact.calculateEligibleLoanRequestPercentage(categories, values, days, date), 0.000001);
    }

    private void rejectStatistics(String[] categories, double[] values, int[] days, LocalDate date) {
        reject(() -> artifact.countEligibleLoanRequests(categories, values, days, date), 0,
                "Invalid input data for loan request calculation.");
        reject(() -> artifact.calculateEligibleLoanRequestPercentage(categories, values, days, date), 0.0,
                "Invalid input data for loan request calculation.");
    }

    private void reject(Supplier<?> action, Object neutralValue, String message) {
        output.reset();
        assertEquals(neutralValue, action.get());
        assertEquals("[ERROR] " + message + System.lineSeparator(), output.toString());
        output.reset();
    }
}
