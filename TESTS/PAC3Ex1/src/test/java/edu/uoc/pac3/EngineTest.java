package edu.uoc.pac3;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EngineTest {

    // -------------------------------------------------------------------------
    // SECTION 1: SANITY CHECKS (Architecture & Reflection)
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields definition")
    public void checkFieldsSanity() {
        assertEquals(9, Engine.class.getDeclaredFields().length);

        try {
            // --- Private static final constants ---
            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredField("MIN_HORSEPOWER").getModifiers()));
            assertTrue(Modifier.isStatic(Engine.class.getDeclaredField("MIN_HORSEPOWER").getModifiers()));
            assertTrue(Modifier.isFinal(Engine.class.getDeclaredField("MIN_HORSEPOWER").getModifiers()));
            assertEquals(int.class, Engine.class.getDeclaredField("MIN_HORSEPOWER").getType());

            // --- Public static final error messages ---
            assertTrue(Modifier.isPublic(Engine.class.getDeclaredField("INVALID_MANUFACTURER").getModifiers()));
            assertTrue(Modifier.isStatic(Engine.class.getDeclaredField("INVALID_MANUFACTURER").getModifiers()));
            assertTrue(Modifier.isFinal(Engine.class.getDeclaredField("INVALID_MANUFACTURER").getModifiers()));
            assertEquals(String.class, Engine.class.getDeclaredField("INVALID_MANUFACTURER").getType());
            assertEquals("[ERROR] Manufacturer cannot be null, empty or formed by only whitespace", Engine.INVALID_MANUFACTURER);

            assertTrue(Modifier.isPublic(Engine.class.getDeclaredField("INVALID_CYLINDERS").getModifiers()));
            assertTrue(Modifier.isStatic(Engine.class.getDeclaredField("INVALID_CYLINDERS").getModifiers()));
            assertTrue(Modifier.isFinal(Engine.class.getDeclaredField("INVALID_CYLINDERS").getModifiers()));
            assertEquals(String.class, Engine.class.getDeclaredField("INVALID_CYLINDERS").getType());
            assertEquals("[ERROR] Engine must have at least one cylinder", Engine.INVALID_CYLINDERS);

            assertTrue(Modifier.isPublic(Engine.class.getDeclaredField("INVALID_HORSEPOWER").getModifiers()));
            assertTrue(Modifier.isStatic(Engine.class.getDeclaredField("INVALID_HORSEPOWER").getModifiers()));
            assertTrue(Modifier.isFinal(Engine.class.getDeclaredField("INVALID_HORSEPOWER").getModifiers()));
            assertEquals(String.class, Engine.class.getDeclaredField("INVALID_HORSEPOWER").getType());
            assertEquals("[ERROR] Horsepower must be at least 500", Engine.INVALID_HORSEPOWER);

            // --- Private instance fields ---
            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredField("manufacturer").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredField("manufacturer").getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredField("manufacturer").getModifiers()));
            assertEquals(String.class, Engine.class.getDeclaredField("manufacturer").getType());

            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredField("cylinders").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredField("cylinders").getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredField("cylinders").getModifiers()));
            assertEquals(int.class, Engine.class.getDeclaredField("cylinders").getType());

            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredField("horsepower").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredField("horsepower").getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredField("horsepower").getModifiers()));
            assertEquals(int.class, Engine.class.getDeclaredField("horsepower").getType());

            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredField("usageHours").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredField("usageHours").getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredField("usageHours").getModifiers()));
            assertEquals(double.class, Engine.class.getDeclaredField("usageHours").getType());

            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredField("isHybrid").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredField("isHybrid").getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredField("isHybrid").getModifiers()));
            assertEquals(boolean.class, Engine.class.getDeclaredField("isHybrid").getType());

        } catch (NoSuchFieldException e) {
            fail("[ERROR] There is some problem with the definition of the fields: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("sanity")
    @DisplayName("Sanity - Constructor definition")
    public void checkConstructorSanity() {
        assertEquals(1, Engine.class.getDeclaredConstructors().length);

        try {
            assertTrue(Modifier.isPublic(Engine.class.getDeclaredConstructor(
                    String.class, int.class, int.class, double.class, boolean.class).getModifiers()));
        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of constructors: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @Tag("sanity")
    @DisplayName("Sanity - Methods definition")
    public void checkMethodsSanity() {
        assertEquals(10, Engine.class.getDeclaredMethods().length);

        try {
            // --- Private setters ---
            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredMethod("setManufacturer", String.class).getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("setManufacturer", String.class).getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredMethod("setManufacturer", String.class).getModifiers()));
            assertEquals(void.class, Engine.class.getDeclaredMethod("setManufacturer", String.class).getReturnType());

            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredMethod("setCylinders", int.class).getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("setCylinders", int.class).getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredMethod("setCylinders", int.class).getModifiers()));
            assertEquals(void.class, Engine.class.getDeclaredMethod("setCylinders", int.class).getReturnType());

            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredMethod("setHorsepower", int.class).getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("setHorsepower", int.class).getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredMethod("setHorsepower", int.class).getModifiers()));
            assertEquals(void.class, Engine.class.getDeclaredMethod("setHorsepower", int.class).getReturnType());

            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredMethod("setUsageHours", double.class).getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("setUsageHours", double.class).getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredMethod("setUsageHours", double.class).getModifiers()));
            assertEquals(void.class, Engine.class.getDeclaredMethod("setUsageHours", double.class).getReturnType());

            assertTrue(Modifier.isPrivate(Engine.class.getDeclaredMethod("setHybrid", boolean.class).getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("setHybrid", boolean.class).getModifiers()));
            assertFalse(Modifier.isFinal(Engine.class.getDeclaredMethod("setHybrid", boolean.class).getModifiers()));
            assertEquals(void.class, Engine.class.getDeclaredMethod("setHybrid", boolean.class).getReturnType());

            // --- Public getters ---
            assertTrue(Modifier.isPublic(Engine.class.getDeclaredMethod("getManufacturer").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("getManufacturer").getModifiers()));
            assertEquals(String.class, Engine.class.getDeclaredMethod("getManufacturer").getReturnType());

            assertTrue(Modifier.isPublic(Engine.class.getDeclaredMethod("getCylinders").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("getCylinders").getModifiers()));
            assertEquals(int.class, Engine.class.getDeclaredMethod("getCylinders").getReturnType());

            assertTrue(Modifier.isPublic(Engine.class.getDeclaredMethod("getHorsepower").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("getHorsepower").getModifiers()));
            assertEquals(int.class, Engine.class.getDeclaredMethod("getHorsepower").getReturnType());

            assertTrue(Modifier.isPublic(Engine.class.getDeclaredMethod("getUsageHours").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("getUsageHours").getModifiers()));
            assertEquals(double.class, Engine.class.getDeclaredMethod("getUsageHours").getReturnType());

            assertTrue(Modifier.isPublic(Engine.class.getDeclaredMethod("isHybrid").getModifiers()));
            assertFalse(Modifier.isStatic(Engine.class.getDeclaredMethod("isHybrid").getModifiers()));
            assertEquals(boolean.class, Engine.class.getDeclaredMethod("isHybrid").getReturnType());

        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of the methods: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // SECTION 2: MINIMUM (One test per field)
    // -------------------------------------------------------------------------

    @Test
    @Order(4)
    @Tag("minimum")
    @DisplayName("Minimum - Manufacturer")
    public void testCreateEngineManufacturer() {
        // ===== INVALID: null =====
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine(null, 6, 600, 10.5, true));
        assertEquals(Engine.INVALID_MANUFACTURER, exception.getMessage());

        // ===== INVALID: empty string =====
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("", 6, 600, 10.5, true));
        assertEquals(Engine.INVALID_MANUFACTURER, exception.getMessage());

        // ===== INVALID: only whitespace =====
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("   ", 6, 600, 10.5, true));
        assertEquals(Engine.INVALID_MANUFACTURER, exception.getMessage());

        // ===== INVALID: tabs only =====
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("\t\t", 6, 600, 10.5, true));
        assertEquals(Engine.INVALID_MANUFACTURER, exception.getMessage());

        // ===== INVALID: newlines only =====
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("\n\n", 6, 600, 10.5, true));
        assertEquals(Engine.INVALID_MANUFACTURER, exception.getMessage());

        // ===== VALID: with surrounding spaces → trimmed =====
        Engine engine = new Engine(" Ferrari ", 6, 600, 10.5, true);
        assertNotNull(engine);
        assertEquals("Ferrari", engine.getManufacturer());

        // ===== VALID: no trimming needed =====
        engine = new Engine("Honda", 4, 500, 0.0, false);
        assertNotNull(engine);
        assertEquals("Honda", engine.getManufacturer());

        // ===== VALID: single char (valid, no min length) =====
        engine = new Engine("M", 6, 600, 10.5, true);
        assertNotNull(engine);
        assertEquals("M", engine.getManufacturer());
    }

    @Test
    @Order(5)
    @Tag("minimum")
    @DisplayName("Minimum - Cylinders")
    public void testCreateEngineCylinders() {
        // ===== INVALID: zero =====
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("Ferrari", 0, 600, 10.5, true));
        assertEquals(Engine.INVALID_CYLINDERS, exception.getMessage());

        // ===== INVALID: negative =====
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("Ferrari", -2, 600, 10.5, true));
        assertEquals(Engine.INVALID_CYLINDERS, exception.getMessage());

        // ===== INVALID: far below minimum =====
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("Ferrari", -100, 600, 10.5, true));
        assertEquals(Engine.INVALID_CYLINDERS, exception.getMessage());

        // ===== VALID: minimum boundary (1 cylinder) =====
        Engine engine = new Engine("Ferrari", 1, 600, 10.5, true);
        assertNotNull(engine);
        assertEquals(1, engine.getCylinders());

        // ===== VALID: typical value =====
        engine = new Engine("Ferrari", 8, 600, 10.5, true);
        assertNotNull(engine);
        assertEquals(8, engine.getCylinders());

        // ===== VALID: high value =====
        engine = new Engine("Ferrari", 12, 800, 5.0, false);
        assertNotNull(engine);
        assertEquals(12, engine.getCylinders());
    }

    @Test
    @Order(6)
    @Tag("minimum")
    @DisplayName("Minimum - Horsepower")
    public void testCreateEngineHorsepower() {
        // ===== INVALID: just below boundary =====
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("Ferrari", 8, 499, 10.5, true));
        assertEquals(Engine.INVALID_HORSEPOWER, exception.getMessage());

        // ===== INVALID: zero =====
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("Ferrari", 8, 0, 10.5, true));
        assertEquals(Engine.INVALID_HORSEPOWER, exception.getMessage());

        // ===== INVALID: negative =====
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("Ferrari", 8, -100, 10.5, true));
        assertEquals(Engine.INVALID_HORSEPOWER, exception.getMessage());

        // ===== VALID: exact minimum (boundary) =====
        Engine engine = new Engine("Ferrari", 8, 500, 10.5, true);
        assertNotNull(engine);
        assertEquals(500, engine.getHorsepower());

        // ===== VALID: above minimum =====
        engine = new Engine("Ferrari", 8, 501, 10.5, true);
        assertNotNull(engine);
        assertEquals(501, engine.getHorsepower());

        // ===== VALID: high value =====
        engine = new Engine("Ferrari", 8, 1500, 5.0, false);
        assertNotNull(engine);
        assertEquals(1500, engine.getHorsepower());
    }

    @Test
    @Order(7)
    @Tag("minimum")
    @DisplayName("Minimum - Usage Hours")
    public void testCreateEngineUsageHours() {
        // ===== Negative → clamped to 0.0 =====
        Engine engine = new Engine("Ferrari", 8, 800, -5.5, true);
        assertNotNull(engine);
        assertEquals(0.0, engine.getUsageHours());

        // ===== Negative far below → clamped to 0.0 =====
        engine = new Engine("Ferrari", 8, 800, -1000.0, true);
        assertNotNull(engine);
        assertEquals(0.0, engine.getUsageHours());

        // ===== Zero → stays 0.0 =====
        engine = new Engine("Ferrari", 8, 800, 0.0, true);
        assertNotNull(engine);
        assertEquals(0.0, engine.getUsageHours());

        // ===== Positive value =====
        engine = new Engine("Ferrari", 8, 800, 25.4, true);
        assertNotNull(engine);
        assertEquals(25.4, engine.getUsageHours());

        // ===== High value =====
        engine = new Engine("Ferrari", 8, 800, 9999.99, false);
        assertNotNull(engine);
        assertEquals(9999.99, engine.getUsageHours(), 0.01);
    }

    @Test
    @Order(8)
    @Tag("minimum")
    @DisplayName("Minimum - Hybrid")
    public void testCreateEngineHybrid() {
        // ===== Hybrid = true =====
        Engine engine = new Engine("Ferrari", 8, 800, 10.0, true);
        assertNotNull(engine);
        assertTrue(engine.isHybrid());

        // ===== Hybrid = false =====
        engine = new Engine("Ferrari", 8, 800, 10.0, false);
        assertNotNull(engine);
        assertFalse(engine.isHybrid());
    }

    @Test
    @Order(9)
    @Tag("minimum")
    @DisplayName("Minimum - Valid parameters (full constructor)")
    public void testConstructorValidParameters() {
        // ===== Full valid constructor =====
        Engine engine = new Engine("Mercedes", 6, 1000, 15.0, true);
        assertNotNull(engine);
        assertEquals("Mercedes", engine.getManufacturer());
        assertEquals(6, engine.getCylinders());
        assertEquals(1000, engine.getHorsepower());
        assertEquals(15.0, engine.getUsageHours());
        assertTrue(engine.isHybrid());

        // ===== Another valid combination =====
        engine = new Engine("  Honda  ", 4, 500, 0.0, false);
        assertNotNull(engine);
        assertEquals("Honda", engine.getManufacturer());
        assertEquals(4, engine.getCylinders());
        assertEquals(500, engine.getHorsepower());
        assertEquals(0.0, engine.getUsageHours());
        assertFalse(engine.isHybrid());

        // ===== High values =====
        engine = new Engine("Renault", 12, 1500, 200.0, true);
        assertNotNull(engine);
        assertEquals("Renault", engine.getManufacturer());
        assertEquals(12, engine.getCylinders());
        assertEquals(1500, engine.getHorsepower());
        assertEquals(200.0, engine.getUsageHours());
        assertTrue(engine.isHybrid());
    }

    // -------------------------------------------------------------------------
    // SECTION 3: ADVANCED (Behavior & Logic)
    // -------------------------------------------------------------------------

    @Test
    @Order(10)
    @Tag("advanced")
    @DisplayName("Advanced - Constructor errors cascade (first invalid param triggers)")
    public void testConstructorErrorCascade() {
        // Invalid manufacturer → should fail on manufacturer before reaching cylinders
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine(null, 0, 100, -5.0, true));
        assertEquals(Engine.INVALID_MANUFACTURER, exception.getMessage());

        // Valid manufacturer, invalid cylinders
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("Ferrari", 0, 100, -5.0, true));
        assertEquals(Engine.INVALID_CYLINDERS, exception.getMessage());

        // Valid manufacturer+cylinders, invalid horsepower
        exception = assertThrows(IllegalArgumentException.class,
                () -> new Engine("Ferrari", 6, 100, -5.0, true));
        assertEquals(Engine.INVALID_HORSEPOWER, exception.getMessage());

        // Valid manufacturer+cylinders+horsepower, negative usageHours → no exception, clamped to 0.0
        Engine engine = new Engine("Ferrari", 6, 600, -5.0, true);
        assertNotNull(engine);
        assertEquals(0.0, engine.getUsageHours());
    }
}