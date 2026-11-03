package edu.uoc.pac2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MuseumArtifactTest {

    private static final String INVENTORY_ERROR = "Inventory code must follow the format ART-YYYY-NNNN.";
    private static final String TITLE_ERROR = "Title cannot be null, empty or contain only whitespace.";
    private static final String CREATOR_ERROR = "Creator cannot be null, empty or contain only whitespace.";
    private static final String ORIGIN_ERROR = "Origin country must be a two-letter code.";
    private static final String CATEGORY_ERROR = "Category must be one of: PAINTING, SCULPTURE, MANUSCRIPT, TEXTILE, CERAMIC, OTHER.";
    private static final String STATUS_ERROR = "Conservation status must be one of: EXCELLENT, GOOD, FAIR, POOR.";
    private static final String ACQUISITION_ERROR = "Acquisition date cannot be null, after the last inspection date or in the future.";
    private static final String INSPECTION_ERROR = "Last inspection date cannot be null, before the acquisition date or in the future.";
    private static final String VALUE_ERROR = "Appraised value must be greater than 0.";
    private static final String REFERENCE_ERROR = "Reference date cannot be null or before the last inspection date.";
    private static final String DAYS_ERROR = "Loan days must be between 1 and 365.";
    private static final String REQUEST_ERROR = "Invalid loan request data.";
    private static final String STATISTICS_ERROR = "Invalid input data for loan request calculation.";

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
        artifact = new MuseumArtifact("ART-2026-0042", "Lady with a Fan", "Gustav Klimt",
                "AT", "PAINTING", "GOOD", acquisitionDate, inspectionDate, 120_000, true, false);
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

    @Test
    void testConstructorExceptions() {
        String[] messages = {INVENTORY_ERROR, TITLE_ERROR, CREATOR_ERROR, ORIGIN_ERROR,
                CATEGORY_ERROR, STATUS_ERROR, ACQUISITION_ERROR, INSPECTION_ERROR, VALUE_ERROR};
        for (int index = 0; index < messages.length; index++) {
            int invalid = index;
            assertInvalid(() -> new MuseumArtifact(
                    invalid == 0 ? null : "ART-2026-0001",
                    invalid == 1 ? null : "Title",
                    invalid == 2 ? null : "Creator",
                    invalid == 3 ? null : "ES",
                    invalid == 4 ? null : "OTHER",
                    invalid == 5 ? null : "GOOD",
                    invalid == 6 ? null : acquisitionDate,
                    invalid == 7 ? null : inspectionDate,
                    invalid == 8 ? 0 : 1, false, false), messages[index]);
        }
    }

    @Test
    void testTextSetterExceptions() {
        for (String invalid : new String[]{null, "", " \t "}) {
            assertInvalid(() -> artifact.setTitle(invalid), TITLE_ERROR);
            assertInvalid(() -> artifact.setCreator(invalid), CREATOR_ERROR);
        }
        assertEquals("Lady with a Fan", artifact.getTitle());
        assertEquals("Gustav Klimt", artifact.getCreator());
    }

    @Test
    void testCodeAndCountrySetterExceptions() {
        for (String invalid : new String[]{null, "", "art-2026-0042", "ART-2026-042"}) {
            assertInvalid(() -> artifact.setInventoryCode(invalid), INVENTORY_ERROR);
        }
        for (String invalid : new String[]{null, "", "ESP", "E1"}) {
            assertInvalid(() -> artifact.setOriginCountry(invalid), ORIGIN_ERROR);
        }
        assertEquals("ART-2026-0042", artifact.getInventoryCode());
        assertEquals("AT", artifact.getOriginCountry());
    }

    @Test
    void testCategoryAndStatusSetterExceptions() {
        for (String invalid : new String[]{null, "", "UNKNOWN", " PAINTING"}) {
            assertInvalid(() -> artifact.setCategory(invalid), CATEGORY_ERROR);
        }
        for (String invalid : new String[]{null, "", "BAD", " GOOD"}) {
            assertInvalid(() -> artifact.setConservationStatus(invalid), STATUS_ERROR);
        }
        assertEquals("PAINTING", artifact.getCategory());
        assertEquals("GOOD", artifact.getConservationStatus());
    }

    @Test
    void testDateAndValueSetterExceptions() {
        for (LocalDate invalid : new LocalDate[]{null, inspectionDate.plusDays(1), today.plusDays(1)}) {
            assertInvalid(() -> artifact.setAcquisitionDate(invalid), ACQUISITION_ERROR);
        }
        for (LocalDate invalid : new LocalDate[]{null, acquisitionDate.minusDays(1), today.plusDays(1)}) {
            assertInvalid(() -> artifact.setLastInspectionDate(invalid), INSPECTION_ERROR);
        }
        for (double invalid : new double[]{-1, 0, Double.NaN}) {
            assertInvalid(() -> artifact.setAppraisedValue(invalid), VALUE_ERROR);
        }
        assertEquals(acquisitionDate, artifact.getAcquisitionDate());
        assertEquals(inspectionDate, artifact.getLastInspectionDate());
        assertEquals(120_000, artifact.getAppraisedValue());
    }

    @Test
    void testReferenceDateExceptions() {
        artifact.setFragile(false);
        artifact.setOnLoan(true);
        for (LocalDate invalid : new LocalDate[]{null, inspectionDate.minusDays(1)}) {
            assertInvalidChecked(() -> artifact.needsInspection(invalid), REFERENCE_ERROR);
            assertInvalidChecked(() -> artifact.isHighRisk(invalid), REFERENCE_ERROR);
            assertInvalidChecked(() -> artifact.canBeLoaned(invalid), REFERENCE_ERROR);
        }
    }

    @Test
    void testInsuranceDurationExceptions() {
        for (int invalid : new int[]{Integer.MIN_VALUE, -1, 0, 366, Integer.MAX_VALUE}) {
            assertInvalidChecked(() -> artifact.calculateInsuranceCost(invalid), DAYS_ERROR);
        }
    }

    @Test
    void testSingleLoanRequestExceptions() {
        for (String invalid : new String[]{null, "", "UNKNOWN", " PAINTING"}) {
            assertInvalidChecked(() -> artifact.isEligibleForLoan(invalid, 120_000, 10, inspectionDate),
                    REQUEST_ERROR);
        }
        for (double invalid : new double[]{-1, 0, Double.NaN}) {
            assertInvalidChecked(() -> artifact.isEligibleForLoan("PAINTING", invalid, 10, inspectionDate),
                    REQUEST_ERROR);
        }
        for (int invalid : new int[]{0, -1, 366}) {
            assertInvalidChecked(() -> artifact.isEligibleForLoan("PAINTING", 120_000, invalid, inspectionDate),
                    REQUEST_ERROR);
        }
        artifact.setOnLoan(true);
        for (LocalDate invalid : new LocalDate[]{null, inspectionDate.minusDays(1)}) {
            assertInvalidChecked(() -> artifact.isEligibleForLoan("OTHER", 1, 10, invalid), REFERENCE_ERROR);
        }
    }

    @Test
    void testStatisticsArrayExceptions() {
        String[] categories = {"PAINTING"};
        double[] values = {120_000};
        int[] days = {10};
        assertInvalidStatistics(null, values, days, inspectionDate);
        assertInvalidStatistics(categories, null, days, inspectionDate);
        assertInvalidStatistics(categories, values, null, inspectionDate);
        assertInvalidStatistics(new String[0], values, days, inspectionDate);
        assertInvalidStatistics(categories, new double[0], days, inspectionDate);
        assertInvalidStatistics(categories, values, new int[0], inspectionDate);
    }

    @Test
    void testStatisticsElementAndDateExceptions() {
        String[] categories = {"PAINTING", "PAINTING", "PAINTING"};
        double[] values = {120_000, 120_000, 120_000};
        int[] days = {1, 1, 1};
        for (int index = 0; index < categories.length; index++) {
            categories[index] = "UNKNOWN";
            assertInvalidStatistics(categories, values, days, inspectionDate);
            categories[index] = "PAINTING";
            values[index] = Double.NaN;
            assertInvalidStatistics(categories, values, days, inspectionDate);
            values[index] = 120_000;
            days[index] = 366;
            assertInvalidStatistics(categories, values, days, inspectionDate);
            days[index] = 1;
        }
        for (LocalDate invalid : new LocalDate[]{null, inspectionDate.minusDays(1)}) {
            assertInvalidStatistics(categories, values, days, invalid);
            assertInvalidStatistics(new String[0], new double[0], new int[0], invalid);
        }
    }

    private void assertInvalidStatistics(String[] categories, double[] values, int[] days, LocalDate date) {
        assertInvalid(() -> artifact.countEligibleLoanRequests(categories, values, days, date),
                STATISTICS_ERROR);
        assertInvalid(() -> artifact.calculateEligibleLoanRequestPercentage(categories, values, days, date),
                STATISTICS_ERROR);
    }

    private void assertInvalid(Executable action, String message) {
        assertInvalid(action, message, IllegalArgumentException.class);
    }

    private void assertInvalidChecked(Executable action, String message) {
        assertInvalid(action, message, Exception.class);
    }

    private void assertInvalid(Executable action, String message, Class<? extends Exception> exceptionType) {
        Exception exception = assertThrowsExactly(exceptionType, action);
        assertEquals(message, exception.getMessage());
        assertEquals("", output.toString(), "An exception must not print an error message");
        assertEquals("", errors.toString(), "An exception must not print an error message");
    }
}
