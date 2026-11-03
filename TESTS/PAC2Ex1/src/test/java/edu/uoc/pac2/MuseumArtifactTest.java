package edu.uoc.pac2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class MuseumArtifactTest {

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
        artifact = new MuseumArtifact("ART-2026-0042", "  Lady with a Fan  ", "  Gustav Klimt  ",
                "at", "painting", "good", acquisitionDate, inspectionDate, 120_000, true, false);
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
    void testFieldsFollowUml() throws Exception {
        assertEquals(21, MuseumArtifact.class.getDeclaredFields().length);
        String[] instanceNames = {"inventoryCode", "title", "creator", "originCountry", "category",
                "conservationStatus", "acquisitionDate", "lastInspectionDate", "appraisedValue",
                "fragile", "onLoan"};
        Class<?>[] instanceTypes = {String.class, String.class, String.class, String.class,
                String.class, String.class, LocalDate.class, LocalDate.class, double.class,
                boolean.class, boolean.class};
        for (int i = 0; i < instanceNames.length; i++) {
            Field field = MuseumArtifact.class.getDeclaredField(instanceNames[i]);
            assertEquals(instanceTypes[i], field.getType(), instanceNames[i]);
            assertTrue(Modifier.isPrivate(field.getModifiers()), instanceNames[i]);
            assertFalse(Modifier.isStatic(field.getModifiers()), instanceNames[i]);
        }
        String[] constantNames = {"INVENTORY_CODE_REGEX", "VALID_CATEGORIES", "VALID_STATUSES",
                "HIGH_VALUE_THRESHOLD", "INSPECTION_INTERVAL_MONTHS", "MIN_LOAN_DAYS",
                "MAX_LOAN_DAYS", "DAILY_INSURANCE_RATE", "FRAGILE_SURCHARGE",
                "HIGH_VALUE_SURCHARGE"};
        Class<?>[] constantTypes = {String.class, String[].class, String[].class, double.class,
                int.class, int.class, int.class, double.class, double.class, double.class};
        for (int i = 0; i < constantNames.length; i++) {
            Field field = MuseumArtifact.class.getDeclaredField(constantNames[i]);
            assertEquals(constantTypes[i], field.getType(), constantNames[i]);
            assertTrue(Modifier.isPrivate(field.getModifiers()), constantNames[i]);
            assertTrue(Modifier.isStatic(field.getModifiers()), constantNames[i]);
        }
    }

    @Test
    void testConstructorAndGetters() {
        assertAll(
                () -> assertEquals("ART-2026-0042", artifact.getInventoryCode()),
                () -> assertEquals("Lady with a Fan", artifact.getTitle()),
                () -> assertEquals("Gustav Klimt", artifact.getCreator()),
                () -> assertEquals("AT", artifact.getOriginCountry()),
                () -> assertEquals("PAINTING", artifact.getCategory()),
                () -> assertEquals("GOOD", artifact.getConservationStatus()),
                () -> assertEquals(acquisitionDate, artifact.getAcquisitionDate()),
                () -> assertEquals(inspectionDate, artifact.getLastInspectionDate()),
                () -> assertEquals(120_000, artifact.getAppraisedValue()),
                () -> assertTrue(artifact.isFragile()),
                () -> assertFalse(artifact.isOnLoan()));
    }

    @Test
    void testConstructorUsesSetterValidation() {
        String[] messages = {
                "Inventory code must follow the format ART-YYYY-NNNN.",
                "Title cannot be null, empty or contain only whitespace.",
                "Creator cannot be null, empty or contain only whitespace.",
                "Origin country must be a two-letter code.",
                "Category must be one of: PAINTING, SCULPTURE, MANUSCRIPT, TEXTILE, CERAMIC, OTHER.",
                "Conservation status must be one of: EXCELLENT, GOOD, FAIR, POOR.",
                "Acquisition date cannot be null, after the last inspection date or in the future."
                        + System.lineSeparator()
                        + "[ERROR] Last inspection date cannot be null, before the acquisition date or in the future.",
                "Last inspection date cannot be null, before the acquisition date or in the future.",
                "Appraised value must be greater than 0."
        };
        for (int index = 0; index < messages.length; index++) {
            int invalid = index;
            assertRejectedSetter(() -> new MuseumArtifact(
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
    void testObjectsHaveIndependentState() {
        MuseumArtifact other = new MuseumArtifact("ART-0000-0000", "Other", "Unknown",
                "es", "other", "excellent", acquisitionDate, inspectionDate, 1, false, true);
        other.setTitle("Changed");
        assertEquals("Lady with a Fan", artifact.getTitle());
        assertEquals("Changed", other.getTitle());
        assertTrue(artifact.isFragile());
        assertFalse(other.isFragile());
        assertFalse(artifact.isOnLoan());
        assertTrue(other.isOnLoan());
    }

    @Test
    void testInventoryCodeValidation() {
        for (String valid : new String[]{"ART-0000-0000", "ART-9999-9999"}) {
            artifact.setInventoryCode(valid);
            assertEquals(valid, artifact.getInventoryCode());
        }
        for (String invalid : new String[]{null, "", "art-2026-0042", "ART-2026-042",
                "ART-2026-0042 "}) {
            String previous = artifact.getInventoryCode();
            assertRejectedSetter(() -> artifact.setInventoryCode(invalid),
                    "Inventory code must follow the format ART-YYYY-NNNN.");
            assertEquals(previous, artifact.getInventoryCode());
        }
    }

    @Test
    void testTitleAndCreatorValidation() {
        artifact.setTitle(" \tNew  title\n ");
        artifact.setCreator(" \nNew  creator\t ");
        assertEquals("New  title", artifact.getTitle());
        assertEquals("New  creator", artifact.getCreator());
        for (String invalid : new String[]{null, "", " \t "}) {
            assertRejectedSetter(() -> artifact.setTitle(invalid),
                    "Title cannot be null, empty or contain only whitespace.");
            assertRejectedSetter(() -> artifact.setCreator(invalid),
                    "Creator cannot be null, empty or contain only whitespace.");
            assertEquals("New  title", artifact.getTitle());
            assertEquals("New  creator", artifact.getCreator());
        }
    }

    @Test
    void testOriginCountryValidation() {
        artifact.setOriginCountry("es");
        assertEquals("ES", artifact.getOriginCountry());
        for (String invalid : new String[]{null, "", "ESP", "E1", " ES"}) {
            assertRejectedSetter(() -> artifact.setOriginCountry(invalid),
                    "Origin country must be a two-letter code.");
            assertEquals("ES", artifact.getOriginCountry());
        }
    }

    @Test
    void testCategoryAndConservationStatusValidation() throws Exception {
        for (String category : new String[]{"PAINTING", "SCULPTURE", "MANUSCRIPT",
                "TEXTILE", "CERAMIC", "OTHER"}) {
            assertTrue(invokeValidator("isValidCategory", new Class<?>[]{String.class},
                    category.toLowerCase(Locale.ROOT)));
            artifact.setCategory(category.toLowerCase(Locale.ROOT));
            assertEquals(category, artifact.getCategory());
        }
        for (String status : new String[]{"EXCELLENT", "GOOD", "FAIR", "POOR"}) {
            assertTrue(invokeValidator("isValidConservationStatus", new Class<?>[]{String.class},
                    status.toLowerCase(Locale.ROOT)));
            artifact.setConservationStatus(status.toLowerCase(Locale.ROOT));
            assertEquals(status, artifact.getConservationStatus());
        }
        assertFalse(invokeValidator("isValidCategory", new Class<?>[]{String.class}, (Object) null));
        assertFalse(invokeValidator("isValidConservationStatus", new Class<?>[]{String.class}, "BAD"));
        assertRejectedSetter(() -> artifact.setCategory(" OTHER"),
                "Category must be one of: PAINTING, SCULPTURE, MANUSCRIPT, TEXTILE, CERAMIC, OTHER.");
        assertRejectedSetter(() -> artifact.setConservationStatus("BAD"),
                "Conservation status must be one of: EXCELLENT, GOOD, FAIR, POOR.");
        assertEquals("OTHER", artifact.getCategory());
        assertEquals("POOR", artifact.getConservationStatus());
    }

    @Test
    void testAcquisitionDateValidation() {
        artifact.setAcquisitionDate(inspectionDate);
        assertEquals(inspectionDate, artifact.getAcquisitionDate());
        for (LocalDate invalid : new LocalDate[]{null, inspectionDate.plusDays(1), today.plusDays(1)}) {
            assertRejectedSetter(() -> artifact.setAcquisitionDate(invalid),
                    "Acquisition date cannot be null, after the last inspection date or in the future.");
            assertEquals(inspectionDate, artifact.getAcquisitionDate());
        }
    }

    @Test
    void testInspectionDateValidation() {
        artifact.setLastInspectionDate(acquisitionDate);
        assertEquals(acquisitionDate, artifact.getLastInspectionDate());
        for (LocalDate invalid : new LocalDate[]{null, acquisitionDate.minusDays(1), today.plusDays(1)}) {
            assertRejectedSetter(() -> artifact.setLastInspectionDate(invalid),
                    "Last inspection date cannot be null, before the acquisition date or in the future.");
            assertEquals(acquisitionDate, artifact.getLastInspectionDate());
        }
    }

    @Test
    void testValueAndBooleanSetters() {
        artifact.setAppraisedValue(0.01);
        assertEquals(0.01, artifact.getAppraisedValue());
        for (double invalid : new double[]{-1, 0, Double.NaN}) {
            assertRejectedSetter(() -> artifact.setAppraisedValue(invalid),
                    "Appraised value must be greater than 0.");
            assertEquals(0.01, artifact.getAppraisedValue());
        }
        artifact.setFragile(false);
        artifact.setOnLoan(true);
        assertFalse(artifact.isFragile());
        assertTrue(artifact.isOnLoan());
        artifact.setFragile(true);
        artifact.setOnLoan(false);
        assertTrue(artifact.isFragile());
        assertFalse(artifact.isOnLoan());
    }

    @Test
    void testReferenceDateAndLoanDayValidators() throws Exception {
        assertFalse(invokeValidator("isValidReferenceDate", new Class<?>[]{LocalDate.class},
                (Object) null));
        assertFalse(invokeValidator("isValidReferenceDate", new Class<?>[]{LocalDate.class},
                inspectionDate.minusDays(1)));
        assertTrue(invokeValidator("isValidReferenceDate", new Class<?>[]{LocalDate.class},
                inspectionDate));
        for (int invalid : new int[]{0, -1, 366}) {
            assertFalse(invokeValidator("isValidLoanDays", new Class<?>[]{int.class}, invalid));
        }
        for (int valid : new int[]{1, 365}) {
            assertTrue(invokeValidator("isValidLoanDays", new Class<?>[]{int.class}, valid));
        }
    }

    @Test
    void testInspectionTimingAndInvalidDates() {
        assertFalse(artifact.needsInspection(inspectionDate.plusMonths(12).minusDays(1)));
        assertTrue(artifact.needsInspection(inspectionDate.plusMonths(12)));
        artifact.setAcquisitionDate(LocalDate.of(2020, 1, 1));
        artifact.setLastInspectionDate(LocalDate.of(2024, 2, 29));
        assertFalse(artifact.needsInspection(LocalDate.of(2025, 2, 27)));
        assertTrue(artifact.needsInspection(LocalDate.of(2025, 2, 28)));
        for (LocalDate invalid : new LocalDate[]{null, LocalDate.of(2024, 2, 28)}) {
            assertRejected(() -> artifact.needsInspection(invalid), false,
                    "Reference date cannot be null or before the last inspection date.");
        }
    }

    @Test
    void testHighValueAndRisk() {
        artifact.setAppraisedValue(99_999.99);
        assertFalse(artifact.isHighValue());
        artifact.setAppraisedValue(100_000);
        assertTrue(artifact.isHighValue());
        artifact.setFragile(false);
        artifact.setConservationStatus("POOR");
        assertFalse(artifact.isHighRisk(inspectionDate));
        artifact.setFragile(true);
        assertTrue(artifact.isHighRisk(inspectionDate));
        artifact.setConservationStatus("GOOD");
        assertFalse(artifact.isHighRisk(inspectionDate));
        assertTrue(artifact.isHighRisk(inspectionDate.plusMonths(12)));
        assertRejected(() -> artifact.isHighRisk(null), false,
                "Reference date cannot be null or before the last inspection date.");
    }

    @Test
    void testLoanConditionsAndInvalidDates() {
        assertTrue(artifact.canBeLoaned(inspectionDate));
        artifact.setOnLoan(true);
        assertFalse(artifact.canBeLoaned(inspectionDate));
        artifact.setOnLoan(false);
        artifact.setConservationStatus("FAIR");
        assertFalse(artifact.canBeLoaned(inspectionDate));
        artifact.setConservationStatus("EXCELLENT");
        assertFalse(artifact.canBeLoaned(inspectionDate.plusMonths(12)));
        assertRejected(() -> artifact.canBeLoaned(null), false,
                "Reference date cannot be null or before the last inspection date.");
    }

    @Test
    void testInsuranceCostAndInvalidDuration() {
        assertEquals(172.5, artifact.calculateInsuranceCost(10), 0.000001);
        artifact.setFragile(false);
        artifact.setAppraisedValue(50_000);
        assertEquals(5, artifact.calculateInsuranceCost(1), 0.000001);
        assertEquals(1_825, artifact.calculateInsuranceCost(365), 0.000001);
        for (int invalid : new int[]{0, -1, 366}) {
            assertRejected(() -> artifact.calculateInsuranceCost(invalid), 0.0,
                    "Loan days must be between 1 and 365.");
        }
    }

    @Test
    void testLoanRequestArrayValidator() throws Exception {
        String[] categories = {"painting", "OTHER"};
        double[] values = {120_000, 1};
        int[] days = {1, 365};
        assertTrue(validRequests(categories, values, days, inspectionDate));
        assertTrue(validRequests(new String[0], new double[0], new int[0], inspectionDate));
        assertFalse(validRequests(null, values, days, inspectionDate));
        assertFalse(validRequests(categories, null, days, inspectionDate));
        assertFalse(validRequests(categories, values, null, inspectionDate));
        assertFalse(validRequests(new String[0], values, days, inspectionDate));
        assertFalse(validRequests(categories, values, days, null));
        assertFalse(validRequests(categories, values, days, inspectionDate.minusDays(1)));
        categories[1] = "UNKNOWN";
        assertFalse(validRequests(categories, values, days, inspectionDate));
        categories[1] = "OTHER";
        values[1] = Double.NaN;
        assertFalse(validRequests(categories, values, days, inspectionDate));
        values[1] = 1;
        days[1] = 366;
        assertFalse(validRequests(categories, values, days, inspectionDate));
    }

    private boolean validRequests(String[] categories, double[] values, int[] days,
                                  LocalDate date) throws Exception {
        return invokeValidator("areLoanRequestsValid",
                new Class<?>[]{String[].class, double[].class, int[].class, LocalDate.class},
                categories, values, days, date);
    }

    private boolean invokeValidator(String name, Class<?>[] types, Object... arguments) throws Exception {
        Method method = MuseumArtifact.class.getDeclaredMethod(name, types);
        assertEquals(boolean.class, method.getReturnType());
        method.setAccessible(true);
        return (boolean) method.invoke(artifact, arguments);
    }

    private void assertRejectedSetter(Runnable action, String message) {
        assertRejected(() -> {
            action.run();
            return null;
        }, null, message);
    }

    private void assertRejected(Supplier<?> action, Object neutralValue, String message) {
        output.reset();
        assertEquals(neutralValue, action.get());
        assertEquals("[ERROR] " + message + System.lineSeparator(), output.toString());
        output.reset();
    }
}
