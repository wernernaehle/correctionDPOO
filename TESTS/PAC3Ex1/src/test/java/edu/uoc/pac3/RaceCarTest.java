
package edu.uoc.pac3;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RaceCarTest {

    // -------------------------------------------------------------------------
    // SECTION 1: SANITY CHECKS (Architecture & Reflection)
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields definition")
    public void checkFieldsSanity() {
        assertEquals(15, RaceCar.class.getDeclaredFields().length);

        try {
            // --- Private static final constants ---
            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("MIN_MODEL_LENGTH").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("MIN_MODEL_LENGTH").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("MIN_MODEL_LENGTH").getModifiers()));
            assertEquals(int.class, RaceCar.class.getDeclaredField("MIN_MODEL_LENGTH").getType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("MIN_AERODYNAMICS").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("MIN_AERODYNAMICS").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("MIN_AERODYNAMICS").getModifiers()));
            assertEquals(double.class, RaceCar.class.getDeclaredField("MIN_AERODYNAMICS").getType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("MIN_BUILD_YEAR").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("MIN_BUILD_YEAR").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("MIN_BUILD_YEAR").getModifiers()));
            assertEquals(int.class, RaceCar.class.getDeclaredField("MIN_BUILD_YEAR").getType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("MAX_BUILD_YEAR").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("MAX_BUILD_YEAR").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("MAX_BUILD_YEAR").getModifiers()));
            assertEquals(int.class, RaceCar.class.getDeclaredField("MAX_BUILD_YEAR").getType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("MIN_MAIN_SPONSOR_LENGTH").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("MIN_MAIN_SPONSOR_LENGTH").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("MIN_MAIN_SPONSOR_LENGTH").getModifiers()));
            assertEquals(int.class, RaceCar.class.getDeclaredField("MIN_MAIN_SPONSOR_LENGTH").getType());

            // --- Public static final error messages ---
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredField("INVALID_MODEL").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("INVALID_MODEL").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("INVALID_MODEL").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredField("INVALID_MODEL").getType());
            assertEquals("[ERROR] Model must have at least 3 characters", RaceCar.INVALID_MODEL);

            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredField("INVALID_BUILD_YEAR").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("INVALID_BUILD_YEAR").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("INVALID_BUILD_YEAR").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredField("INVALID_BUILD_YEAR").getType());
            assertEquals("[ERROR] Build year must be between 1950 and 2026", RaceCar.INVALID_BUILD_YEAR);

            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredField("INVALID_AERODYNAMICS").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("INVALID_AERODYNAMICS").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("INVALID_AERODYNAMICS").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredField("INVALID_AERODYNAMICS").getType());
            assertEquals("[ERROR] Aerodynamics must be at least 100.0", RaceCar.INVALID_AERODYNAMICS);

            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredField("INVALID_MAIN_SPONSOR").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("INVALID_MAIN_SPONSOR").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("INVALID_MAIN_SPONSOR").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredField("INVALID_MAIN_SPONSOR").getType());
            assertEquals("[ERROR] Main sponsor cannot be null, formed by only whitespace and must have at least 2 characters", RaceCar.INVALID_MAIN_SPONSOR);

            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredField("NO_ENGINE_INSTALLED").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("NO_ENGINE_INSTALLED").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("NO_ENGINE_INSTALLED").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredField("NO_ENGINE_INSTALLED").getType());
            assertEquals("[ERROR] Cannot race without an installed engine", RaceCar.NO_ENGINE_INSTALLED);

            // --- Private instance fields ---
            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("model").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredField("model").getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredField("model").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredField("model").getType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("buildYear").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredField("buildYear").getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredField("buildYear").getModifiers()));
            assertEquals(int.class, RaceCar.class.getDeclaredField("buildYear").getType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("aerodynamics").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredField("aerodynamics").getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredField("aerodynamics").getModifiers()));
            assertEquals(double.class, RaceCar.class.getDeclaredField("aerodynamics").getType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("mainSponsor").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredField("mainSponsor").getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredField("mainSponsor").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredField("mainSponsor").getType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("engine").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredField("engine").getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredField("engine").getModifiers()));
            assertEquals(Engine.class, RaceCar.class.getDeclaredField("engine").getType());

        } catch (NoSuchFieldException e) {
            fail("[ERROR] There is some problem with the definition of the fields: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("sanity")
    @DisplayName("Sanity - Constructor definition")
    public void checkConstructorSanity() {
        assertEquals(2, RaceCar.class.getDeclaredConstructors().length);

        try {
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredConstructor(
                    String.class, int.class, double.class, String.class).getModifiers()));
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredConstructor(
                    String.class, int.class, double.class, String.class,
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
        long publicMethods = Arrays.stream(RaceCar.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers())).count();
        long privateMethods = Arrays.stream(RaceCar.class.getDeclaredMethods())
                .filter(m -> Modifier.isPrivate(m.getModifiers())).count();

        assertEquals(6, publicMethods);  // 5 getters + race()
        assertEquals(5, privateMethods); // 5 setters

        try {
            // --- Private setters ---
            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredMethod("setModel", String.class).getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("setModel", String.class).getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredMethod("setModel", String.class).getModifiers()));
            assertEquals(void.class, RaceCar.class.getDeclaredMethod("setModel", String.class).getReturnType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredMethod("setBuildYear", int.class).getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("setBuildYear", int.class).getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredMethod("setBuildYear", int.class).getModifiers()));
            assertEquals(void.class, RaceCar.class.getDeclaredMethod("setBuildYear", int.class).getReturnType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredMethod("setAerodynamics", double.class).getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("setAerodynamics", double.class).getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredMethod("setAerodynamics", double.class).getModifiers()));
            assertEquals(void.class, RaceCar.class.getDeclaredMethod("setAerodynamics", double.class).getReturnType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredMethod("setMainSponsor", String.class).getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("setMainSponsor", String.class).getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredMethod("setMainSponsor", String.class).getModifiers()));
            assertEquals(void.class, RaceCar.class.getDeclaredMethod("setMainSponsor", String.class).getReturnType());

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredMethod("setEngine", String.class, int.class, int.class, double.class, boolean.class).getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("setEngine", String.class, int.class, int.class, double.class, boolean.class).getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredMethod("setEngine", String.class, int.class, int.class, double.class, boolean.class).getModifiers()));
            assertEquals(void.class, RaceCar.class.getDeclaredMethod("setEngine", String.class, int.class, int.class, double.class, boolean.class).getReturnType());

            // --- Public getters ---
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredMethod("getModel").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("getModel").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredMethod("getModel").getReturnType());

            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredMethod("getBuildYear").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("getBuildYear").getModifiers()));
            assertEquals(int.class, RaceCar.class.getDeclaredMethod("getBuildYear").getReturnType());

            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredMethod("getAerodynamics").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("getAerodynamics").getModifiers()));
            assertEquals(double.class, RaceCar.class.getDeclaredMethod("getAerodynamics").getReturnType());

            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredMethod("getMainSponsor").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("getMainSponsor").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredMethod("getMainSponsor").getReturnType());

            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredMethod("getEngine").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("getEngine").getModifiers()));
            assertEquals(Engine.class, RaceCar.class.getDeclaredMethod("getEngine").getReturnType());

            // --- Public race() ---
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredMethod("race").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredMethod("race").getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredMethod("race").getModifiers()));
            assertEquals(double.class, RaceCar.class.getDeclaredMethod("race").getReturnType());

        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of getter or setter methods: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // SECTION 2: MINIMUM (Constructor & Basic Validation)
    // -------------------------------------------------------------------------

    @Test
    @Order(4)
    @Tag("minimum")
    @DisplayName("Minimum - Valid parameters (both constructors)")
    public void testConstructorsValidParameters() {
        // ===== Constructor without engine =====
        RaceCar car1 = new RaceCar("RB19", 2023, 120.5, "Oracle");
        assertNotNull(car1);
        assertEquals("RB19", car1.getModel());
        assertEquals(2023, car1.getBuildYear());
        assertEquals(120.5, car1.getAerodynamics());
        assertEquals("Oracle", car1.getMainSponsor());
        assertNull(car1.getEngine());

        // ===== Constructor with engine =====
        RaceCar car2 = new RaceCar("W14", 2023, 115.0, "Petronas", "Mercedes", 6, 1000, 15.0, true);
        assertNotNull(car2);
        assertEquals("W14", car2.getModel());
        assertEquals(2023, car2.getBuildYear());
        assertEquals(115.0, car2.getAerodynamics());
        assertEquals("Petronas", car2.getMainSponsor());
        assertNotNull(car2.getEngine());
        assertEquals("Mercedes", car2.getEngine().getManufacturer());
        assertEquals(6, car2.getEngine().getCylinders());
        assertEquals(1000, car2.getEngine().getHorsepower());
        assertEquals(15.0, car2.getEngine().getUsageHours());
        assertTrue(car2.getEngine().isHybrid());
    }

    @Test
    @Order(5)
    @Tag("minimum")
    @DisplayName("Minimum - Model (Massive Validation)")
    public void testModel() {
        // ===== INVALID: null =====
        Exception e1 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar(null, 2023, 120.5, "Oracle"));
        assertEquals("[ERROR] Model must have at least 3 characters", e1.getMessage());

        // ===== INVALID: empty string =====
        Exception e2 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("", 2023, 120.5, "Oracle"));
        assertEquals("[ERROR] Model must have at least 3 characters", e2.getMessage());

        // ===== INVALID: only whitespace =====
        Exception e3 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("   ", 2023, 120.5, "Oracle"));
        assertEquals("[ERROR] Model must have at least 3 characters", e3.getMessage());

        // ===== INVALID: 1 char after trim =====
        Exception e4 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar(" F ", 2023, 120.5, "Oracle"));
        assertEquals("[ERROR] Model must have at least 3 characters", e4.getMessage());

        // ===== INVALID: 2 chars after trim =====
        Exception e5 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("AB", 2023, 120.5, "Oracle"));
        assertEquals("[ERROR] Model must have at least 3 characters", e5.getMessage());

        // ===== INVALID: 2 chars with surrounding spaces =====
        Exception e6 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("  AB  ", 2023, 120.5, "Oracle"));
        assertEquals("[ERROR] Model must have at least 3 characters", e6.getMessage());

        // ===== INVALID: tabs only =====
        Exception e7 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("\t\t", 2023, 120.5, "Oracle"));
        assertEquals("[ERROR] Model must have at least 3 characters", e7.getMessage());

        // ===== VALID: exactly 3 chars =====
        RaceCar car1 = new RaceCar("RB9", 2013, 130.0, "Infiniti");
        assertEquals("RB9", car1.getModel());

        // ===== VALID: trim works properly =====
        RaceCar car2 = new RaceCar("  F1-75  ", 2022, 125.0, "Shell");
        assertEquals("F1-75", car2.getModel());

        // ===== VALID: longer model name =====
        RaceCar car3 = new RaceCar("Mercedes-AMG W14", 2023, 120.0, "Petronas");
        assertEquals("Mercedes-AMG W14", car3.getModel());

        // ===== VALID: with special characters =====
        RaceCar car4 = new RaceCar("___RB___", 2023, 120.0, "Oracle");
        assertEquals("___RB___", car4.getModel());
    }

    @Test
    @Order(6)
    @Tag("minimum")
    @DisplayName("Minimum - Build Year (Boundary Value Analysis)")
    public void testBuildYear() {
        // ===== INVALID: below minimum =====
        Exception e1 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 1949, 120.5, "Petronas"));
        assertEquals("[ERROR] Build year must be between 1950 and 2026", e1.getMessage());

        // ===== INVALID: far below minimum =====
        Exception e2 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 0, 120.5, "Petronas"));
        assertEquals("[ERROR] Build year must be between 1950 and 2026", e2.getMessage());

        // ===== INVALID: negative =====
        Exception e3 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", -100, 120.5, "Petronas"));
        assertEquals("[ERROR] Build year must be between 1950 and 2026", e3.getMessage());

        // ===== INVALID: above maximum =====
        Exception e4 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2027, 120.5, "Petronas"));
        assertEquals("[ERROR] Build year must be between 1950 and 2026", e4.getMessage());

        // ===== INVALID: far above maximum =====
        Exception e5 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 3000, 120.5, "Petronas"));
        assertEquals("[ERROR] Build year must be between 1950 and 2026", e5.getMessage());

        // ===== VALID: minimum boundary =====
        RaceCar car1 = new RaceCar("Alfetta", 1950, 100.0, "Pirelli");
        assertEquals(1950, car1.getBuildYear());

        // ===== VALID: maximum boundary =====
        RaceCar car2 = new RaceCar("RB22", 2026, 160.0, "Oracle");
        assertEquals(2026, car2.getBuildYear());

        // ===== VALID: middle values =====
        RaceCar car3 = new RaceCar("F2004", 2004, 140.0, "Marlboro");
        assertEquals(2004, car3.getBuildYear());

        RaceCar car4 = new RaceCar("FW14B", 1992, 130.0, "Canon");
        assertEquals(1992, car4.getBuildYear());
    }

    @Test
    @Order(7)
    @Tag("minimum")
    @DisplayName("Minimum - Aerodynamics (Boundary Value Analysis)")
    public void testAerodynamics() {
        // ===== INVALID: below minimum =====
        Exception e1 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 99.9, "Petronas"));
        assertEquals("[ERROR] Aerodynamics must be at least 100.0", e1.getMessage());

        // ===== INVALID: zero =====
        Exception e2 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 0.0, "Petronas"));
        assertEquals("[ERROR] Aerodynamics must be at least 100.0", e2.getMessage());

        // ===== INVALID: negative =====
        Exception e3 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, -50.0, "Petronas"));
        assertEquals("[ERROR] Aerodynamics must be at least 100.0", e3.getMessage());

        // ===== INVALID: just below boundary =====
        Exception e4 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 99.99, "Petronas"));
        assertEquals("[ERROR] Aerodynamics must be at least 100.0", e4.getMessage());

        // ===== VALID: exact minimum =====
        RaceCar car1 = new RaceCar("W14", 2023, 100.0, "Petronas");
        assertEquals(100.0, car1.getAerodynamics());

        // ===== VALID: above minimum =====
        RaceCar car2 = new RaceCar("W14", 2023, 100.1, "Petronas");
        assertEquals(100.1, car2.getAerodynamics(), 0.01);

        // ===== VALID: high value =====
        RaceCar car3 = new RaceCar("RB19", 2023, 200.0, "Oracle");
        assertEquals(200.0, car3.getAerodynamics());

        // ===== VALID: very high value =====
        RaceCar car4 = new RaceCar("RB19", 2023, 999.99, "Oracle");
        assertEquals(999.99, car4.getAerodynamics(), 0.01);
    }

    @Test
    @Order(8)
    @Tag("minimum")
    @DisplayName("Minimum - Main Sponsor (Normalization & Validation)")
    public void testMainSponsor() {
        // ===== INVALID: null =====
        Exception e1 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 120.0, null));
        assertEquals("[ERROR] Main sponsor cannot be null, formed by only whitespace and must have at least 2 characters", e1.getMessage());

        // ===== INVALID: empty string =====
        Exception e2 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 120.0, ""));
        assertEquals("[ERROR] Main sponsor cannot be null, formed by only whitespace and must have at least 2 characters", e2.getMessage());

        // ===== INVALID: only whitespace =====
        Exception e3 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 120.0, "   "));
        assertEquals("[ERROR] Main sponsor cannot be null, formed by only whitespace and must have at least 2 characters", e3.getMessage());

        // ===== INVALID: 1 char after trim =====
        Exception e4 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 120.0, " A "));
        assertEquals("[ERROR] Main sponsor cannot be null, formed by only whitespace and must have at least 2 characters", e4.getMessage());

        // ===== INVALID: single char no spaces =====
        Exception e5 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 120.0, "X"));
        assertEquals("[ERROR] Main sponsor cannot be null, formed by only whitespace and must have at least 2 characters", e5.getMessage());

        // ===== INVALID: tabs only =====
        Exception e6 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 120.0, "\t\t"));
        assertEquals("[ERROR] Main sponsor cannot be null, formed by only whitespace and must have at least 2 characters", e6.getMessage());

        // ===== INVALID: newlines only =====
        Exception e7 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("W14", 2023, 120.0, "\n\n"));
        assertEquals("[ERROR] Main sponsor cannot be null, formed by only whitespace and must have at least 2 characters", e7.getMessage());

        // ===== VALID: lowercase → capitalized =====
        RaceCar car1 = new RaceCar("W14", 2023, 120.0, "  petronas  ");
        assertEquals("Petronas", car1.getMainSponsor());

        // ===== VALID: all uppercase → capitalized =====
        RaceCar car2 = new RaceCar("W14", 2023, 120.0, "ORACLE");
        assertEquals("Oracle", car2.getMainSponsor());

        // ===== VALID: mixed case → capitalized =====
        RaceCar car3 = new RaceCar("W14", 2023, 120.0, "rEd BuLL");
        assertEquals("Red bull", car3.getMainSponsor());

        // ===== VALID: already properly capitalized =====
        RaceCar car4 = new RaceCar("W14", 2023, 120.0, "Shell");
        assertEquals("Shell", car4.getMainSponsor());

        // ===== VALID: exactly 2 chars =====
        RaceCar car5 = new RaceCar("W14", 2023, 120.0, "HP");
        assertEquals("Hp", car5.getMainSponsor());

        // ===== VALID: with trailing/leading spaces and mixed case =====
        RaceCar car6 = new RaceCar("W14", 2023, 120.0, "   gOOGLE   ");
        assertEquals("Google", car6.getMainSponsor());
    }

    // -------------------------------------------------------------------------
    // SECTION 3: ADVANCED (Behavior & Logic)
    // -------------------------------------------------------------------------

    @Test
    @Order(9)
    @Tag("advanced")
    @DisplayName("Advanced - Constructor errors cascade (first invalid param triggers)")
    public void testConstructorErrorCascade() {
        // Invalid model → should fail on model before reaching buildYear
        Exception e1 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("AB", -1, 50.0, "A"));
        assertEquals("[ERROR] Model must have at least 3 characters", e1.getMessage());

        // Valid model, invalid buildYear
        Exception e2 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("RB19", 1900, 50.0, "A"));
        assertEquals("[ERROR] Build year must be between 1950 and 2026", e2.getMessage());

        // Valid model+year, invalid aerodynamics
        Exception e3 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("RB19", 2023, 50.0, "A"));
        assertEquals("[ERROR] Aerodynamics must be at least 100.0", e3.getMessage());

        // Valid model+year+aero, invalid sponsor
        Exception e4 = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("RB19", 2023, 120.0, "A"));
        assertEquals("[ERROR] Main sponsor cannot be null, formed by only whitespace and must have at least 2 characters", e4.getMessage());
    }

    @Test
    @Order(10)
    @Tag("advanced")
    @DisplayName("Advanced - Race Method (State Dependency)")
    public void testRaceMethod() {
        // ===== Without engine → IllegalStateException =====
        RaceCar carWithoutEngine = new RaceCar("RB19", 2023, 150.0, "Oracle");
        IllegalStateException ex = assertThrows(IllegalStateException.class, carWithoutEngine::race);
        assertEquals("[ERROR] Cannot race without an installed engine", ex.getMessage());

        // ===== With valid engine: (800 * 150.0) / 100.0 = 1200.0 =====
        RaceCar car1 = new RaceCar("RB19", 2023, 150.0, "Oracle", "Honda", 6, 800, 10.0, true);
        assertEquals(1200.0, car1.race(), 0.01);

        // ===== With valid engine: (1000 * 100.0) / 100.0 = 1000.0 =====
        RaceCar car2 = new RaceCar("W14", 2023, 100.0, "Petronas", "Mercedes", 8, 1000, 5.0, false);
        assertEquals(1000.0, car2.race(), 0.01);

        // ===== With valid engine: (500 * 200.0) / 100.0 = 1000.0 =====
        RaceCar car3 = new RaceCar("MCL60", 2023, 200.0, "Google", "Renault", 6, 500, 0.0, true);
        assertEquals(1000.0, car3.race(), 0.01);

        // ===== With valid engine: (500 * 100.0) / 100.0 = 500.0 (minimum possible) =====
        RaceCar car4 = new RaceCar("FW45", 2023, 100.0, "Duracell", "Renault", 4, 500, 0.0, false);
        assertEquals(500.0, car4.race(), 0.01);

        // ===== High horsepower + high aerodynamics: (1500 * 250.0) / 100.0 = 3750.0 =====
        RaceCar car5 = new RaceCar("RB19", 2023, 250.0, "Oracle", "Honda", 10, 1500, 20.0, true);
        assertEquals(3750.0, car5.race(), 0.01);
    }

    // -------------------------------------------------------------------------
    // SECTION 4: SPECIAL (Composition & Edge Cases)
    // -------------------------------------------------------------------------

    @Test
    @Order(11)
    @Tag("special")
    @DisplayName("Special - Engine Composition (Invalid engine → null)")
    public void testEngineComposition() {
        // ===== Valid engine =====
        RaceCar car1 = new RaceCar("W14", 2023, 115.0, "Petronas", "Mercedes", 6, 1000, 15.0, true);
        assertNotNull(car1);
        assertNotNull(car1.getEngine());
        assertEquals("Mercedes", car1.getEngine().getManufacturer());
        assertEquals(6, car1.getEngine().getCylinders());
        assertEquals(1000, car1.getEngine().getHorsepower());
        assertEquals(15.0, car1.getEngine().getUsageHours());
        assertTrue(car1.getEngine().isHybrid());

        // ===== Invalid: Horsepower < 500 → engine is null =====
        RaceCar car2 = new RaceCar("MCL60", 2023, 110.0, "Google", "Renault", 6, 400, 15.0, true);
        assertNotNull(car2);
        assertNull(car2.getEngine());

        // ===== Invalid: Horsepower = 499 → engine is null =====
        RaceCar car3 = new RaceCar("MCL60", 2023, 110.0, "Google", "Honda", 4, 499, 10.0, false);
        assertNotNull(car3);
        assertNull(car3.getEngine());

        // ===== Invalid: Horsepower = 0 → engine is null =====
        RaceCar car4 = new RaceCar("MCL60", 2023, 110.0, "Google", "Honda", 4, 0, 10.0, false);
        assertNotNull(car4);
        assertNull(car4.getEngine());

        // ===== Invalid: Negative horsepower → engine is null =====
        RaceCar car5 = new RaceCar("MCL60", 2023, 110.0, "Google", "Honda", 4, -100, 10.0, false);
        assertNotNull(car5);
        assertNull(car5.getEngine());

        // ===== Invalid: Cylinders = 0 → engine is null =====
        RaceCar car6 = new RaceCar("MCL60", 2023, 110.0, "Google", "Honda", 0, 800, 10.0, false);
        assertNotNull(car6);
        assertNull(car6.getEngine());

        // ===== Invalid: Negative cylinders → engine is null =====
        RaceCar car7 = new RaceCar("MCL60", 2023, 110.0, "Google", "Honda", -2, 800, 10.0, false);
        assertNotNull(car7);
        assertNull(car7.getEngine());

        // ===== Invalid: null manufacturer → engine is null =====
        RaceCar car8 = new RaceCar("MCL60", 2023, 110.0, "Google", null, 6, 800, 15.0, true);
        assertNotNull(car8);
        assertNull(car8.getEngine());

        // ===== Invalid: empty manufacturer → engine is null =====
        RaceCar car9 = new RaceCar("MCL60", 2023, 110.0, "Google", "", 6, 800, 15.0, true);
        assertNotNull(car9);
        assertNull(car9.getEngine());

        // ===== Invalid: whitespace manufacturer → engine is null =====
        RaceCar car10 = new RaceCar("MCL60", 2023, 110.0, "Google", "   ", 6, 800, 15.0, true);
        assertNotNull(car10);
        assertNull(car10.getEngine());

        // ===== Valid: Horsepower = 500 (minimum boundary) → engine exists =====
        RaceCar car11 = new RaceCar("MCL60", 2023, 110.0, "Google", "Renault", 6, 500, 0.0, false);
        assertNotNull(car11);
        assertNotNull(car11.getEngine());
        assertEquals(500, car11.getEngine().getHorsepower());

        // ===== Valid: negative usageHours → clamped to 0.0 =====
        RaceCar car12 = new RaceCar("MCL60", 2023, 110.0, "Google", "Renault", 6, 800, -50.0, false);
        assertNotNull(car12);
        assertNotNull(car12.getEngine());
        assertEquals(0.0, car12.getEngine().getUsageHours());

        // ===== Valid: manufacturer trimming =====
        RaceCar car13 = new RaceCar("MCL60", 2023, 110.0, "Google", "  Honda  ", 6, 800, 10.0, true);
        assertNotNull(car13);
        assertNotNull(car13.getEngine());
        assertEquals("Honda", car13.getEngine().getManufacturer());

        // ===== Valid: hybrid false =====
        RaceCar car14 = new RaceCar("FW45", 2023, 120.0, "Duracell", "Renault", 4, 600, 5.0, false);
        assertNotNull(car14);
        assertNotNull(car14.getEngine());
        assertFalse(car14.getEngine().isHybrid());

        // ===== Valid: hybrid true =====
        RaceCar car15 = new RaceCar("RB19", 2023, 150.0, "Oracle", "Honda", 6, 1000, 20.0, true);
        assertNotNull(car15);
        assertNotNull(car15.getEngine());
        assertTrue(car15.getEngine().isHybrid());
    }

    @Test
    @Order(12)
    @Tag("special")
    @DisplayName("Special - Race with invalid engine (no engine → cannot race)")
    public void testRaceWithInvalidEngine() {
        // Car created with invalid engine data (horsepower too low) → engine is null
        RaceCar car = new RaceCar("MCL60", 2023, 110.0, "Google", "Renault", 6, 400, 15.0, true);
        assertNull(car.getEngine());

        // Trying to race should throw IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class, car::race);
        assertEquals("[ERROR] Cannot race without an installed engine", ex.getMessage());

        // Car without engine constructor
        RaceCar car2 = new RaceCar("FW45", 2023, 120.0, "Duracell");
        assertNull(car2.getEngine());

        IllegalStateException ex2 = assertThrows(IllegalStateException.class, car2::race);
        assertEquals("[ERROR] Cannot race without an installed engine", ex2.getMessage());
    }
}