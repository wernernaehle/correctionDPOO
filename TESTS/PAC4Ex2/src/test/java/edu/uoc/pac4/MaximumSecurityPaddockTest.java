package edu.uoc.pac4;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaximumSecurityPaddockTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields and Inheritance definition")
    public void checkFields() {
        assertEquals(1, MaximumSecurityPaddock.class.getDeclaredFields().length, "The class must have exactly 1 field.");
        assertTrue(DinoPark.class.isAssignableFrom(MaximumSecurityPaddock.class), "Must inherit from DinoPark");

        try {
            assertTrue(Modifier.isPrivate(MaximumSecurityPaddock.class.getDeclaredField("electricFenceVoltage").getModifiers()), "electricFenceVoltage must be private.");
            assertEquals(double.class, MaximumSecurityPaddock.class.getDeclaredField("electricFenceVoltage").getType(), "electricFenceVoltage must be double.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] Field definition: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("minimum")
    @DisplayName("Minimum - Electric Fence Voltage Exceptions")
    public void testVoltageExceptions() {
        Exception ex1 = assertThrows(DinoParkException.class, () -> {
            new MaximumSecurityPaddock("T-Rex Kingdom", 0);
        });
        assertEquals(DinoParkException.INVALID_FENCE_VOLTAGE, ex1.getMessage());

        Exception ex2 = assertThrows(DinoParkException.class, () -> {
            new MaximumSecurityPaddock("T-Rex Kingdom", -50.5);
        });
        assertEquals(DinoParkException.INVALID_FENCE_VOLTAGE, ex2.getMessage());
    }

    @Test
    @Order(3)
    @Tag("minimum")
    @DisplayName("Minimum - Setters and Getters")
    public void testLogic() {
        try {
            MaximumSecurityPaddock paddock = new MaximumSecurityPaddock("T-Rex Kingdom", 100.7);
            assertEquals(100.7, paddock.getElectricFenceVoltage(), 0.01, "Voltage should be 100.7.");

            paddock.setElectricFenceVoltage(200.5);
            assertEquals(200.5, paddock.getElectricFenceVoltage(), 0.01, "Voltage should be 200.5.");
        } catch (DinoParkException e) {
            fail("Exception thrown for valid arguments: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @Tag("minimum")
    @DisplayName("Minimum - toString format")
    public void testToString() throws DinoParkException {
        MaximumSecurityPaddock paddock = new MaximumSecurityPaddock("T-Rex Kingdom", 100.7);
        String expectedStart = "{ \"type\": \"MaximumSecurityPaddock\"";
        String expectedEnd = "\"electricFenceVoltage\": 100.70 }";

        String result = paddock.toString();
        assertTrue(result.contains(expectedStart));
        assertTrue(result.contains(expectedEnd));
    }
}