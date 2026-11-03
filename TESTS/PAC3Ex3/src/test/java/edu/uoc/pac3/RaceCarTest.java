package edu.uoc.pac3;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RaceCarTest {

    @Test
    @Order(1)
    @DisplayName("Sanity - Fields definitions")
    public void checkFieldsDefinition() {
        assertEquals(19, RaceCar.class.getDeclaredFields().length, "Field count mismatch in RaceCar");

        try {
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredField("INVALID_TYRE_COMPOUND").getModifiers()));
            assertTrue(Modifier.isStatic(RaceCar.class.getDeclaredField("INVALID_TYRE_COMPOUND").getModifiers()));
            assertTrue(Modifier.isFinal(RaceCar.class.getDeclaredField("INVALID_TYRE_COMPOUND").getModifiers()));
            assertEquals(String.class, RaceCar.class.getDeclaredField("INVALID_TYRE_COMPOUND").getType());
            assertEquals("[ERROR] Tire compound cannot be null", RaceCar.INVALID_TYRE_COMPOUND);

            assertTrue(Modifier.isPrivate(RaceCar.class.getDeclaredField("tyreCompound").getModifiers()));
            assertFalse(Modifier.isStatic(RaceCar.class.getDeclaredField("tyreCompound").getModifiers()));
            assertFalse(Modifier.isFinal(RaceCar.class.getDeclaredField("tyreCompound").getModifiers()));
            assertEquals(TyreCompound.class, RaceCar.class.getDeclaredField("tyreCompound").getType());

        } catch (NoSuchFieldException e) {
            fail("[ERROR] Field definition error: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Sanity - Constructors definition")
    public void checkConstructorsSanity() {
        // Verificamos que existen exactamente 2 constructores
        assertEquals(2, RaceCar.class.getDeclaredConstructors().length, "RaceCar must have exactly 2 constructors");

        try {
            // Verificamos el constructor corto (ahora con 5 parámetros, incluyendo el TyreCompound)
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredConstructor(
                    String.class, int.class, double.class, String.class, TyreCompound.class
            ).getModifiers()), "Short constructor definition is incorrect");

            // Verificamos el constructor largo (con 10 parámetros, incluyendo el TyreCompound)
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredConstructor(
                    String.class, int.class, double.class, String.class, TyreCompound.class,
                    String.class, int.class, int.class, double.class, boolean.class
            ).getModifiers()), "Long constructor definition is incorrect");

        } catch (NoSuchMethodException e) {
            fail("[ERROR] Constructor definition error. Did you add TyreCompound to the parameters?: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("Sanity - Methods definition")
    public void checkMethodsSanity() {
        try {
            // Verificamos que se han creado el getter y el setter para el neumático
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredMethod("getTyreCompound").getModifiers()), "getTyreCompound must be public");

            // Nota: En RaceCar, el setTyreCompound lo definimos público para poder cambiar ruedas en boxes,
            // pero si en tu código final lo pusiste private, cambia el isPublic por isPrivate aquí abajo.
            assertTrue(Modifier.isPublic(RaceCar.class.getDeclaredMethod("setTyreCompound", TyreCompound.class).getModifiers()), "setTyreCompound must be public");

        } catch (NoSuchMethodException e) {
            fail("[ERROR] Method definition error. Missing getter or setter for TyreCompound: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("RaceCar - Tyre Compound Logic")
    public void testTyreCompound() {
        // Excepciones (nulos)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("AMR24", 2024, 110.0, "Aramco", null));
        assertEquals(RaceCar.INVALID_TYRE_COMPOUND, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> new RaceCar("AMR24", 2024, 110.0, "Aramco", null, "Mercedes", 6, 1000, 15.5, true));
        assertEquals(RaceCar.INVALID_TYRE_COMPOUND, exception.getMessage());

        // Asignaciones correctas (Hard)
        RaceCar car = new RaceCar("AMR24", 2024, 110.0, "Aramco", TyreCompound.HARD);
        assertEquals(TyreCompound.HARD, car.getTyreCompound());

        car = new RaceCar("AMR24", 2024, 110.0, "Aramco", TyreCompound.HARD, "Mercedes", 6, 1000, 15.5, true);
        assertEquals(TyreCompound.HARD, car.getTyreCompound());

        // Asignaciones correctas (Medium)
        car = new RaceCar("AMR24", 2024, 110.0, "Aramco", TyreCompound.MEDIUM);
        assertEquals(TyreCompound.MEDIUM, car.getTyreCompound());

        car = new RaceCar("AMR24", 2024, 110.0, "Aramco", TyreCompound.MEDIUM, "Mercedes", 6, 1000, 15.5, true);
        assertEquals(TyreCompound.MEDIUM, car.getTyreCompound());

        // Asignaciones correctas (Soft)
        car = new RaceCar("AMR24", 2024, 110.0, "Aramco", TyreCompound.SOFT);
        assertEquals(TyreCompound.SOFT, car.getTyreCompound());

        car = new RaceCar("AMR24", 2024, 110.0, "Aramco", TyreCompound.SOFT, "Mercedes", 6, 1000, 15.5, true);
        assertEquals(TyreCompound.SOFT, car.getTyreCompound());

        // Asignaciones correctas (Qualifying)
        car = new RaceCar("AMR24", 2024, 110.0, "Aramco", TyreCompound.QUALIFYING);
        assertEquals(TyreCompound.QUALIFYING, car.getTyreCompound());

        car = new RaceCar("AMR24", 2024, 110.0, "Aramco", TyreCompound.QUALIFYING, "Mercedes", 6, 1000, 15.5, true);
        assertEquals(TyreCompound.QUALIFYING, car.getTyreCompound());
    }
}