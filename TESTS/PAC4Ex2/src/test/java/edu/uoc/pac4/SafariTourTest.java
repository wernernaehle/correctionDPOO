package edu.uoc.pac4;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SafariTourTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields and Inheritance definition")
    public void checkFields() {
        assertEquals(1, SafariTour.class.getDeclaredFields().length, "The class must have exactly 1 field.");
        assertTrue(DinoPark.class.isAssignableFrom(SafariTour.class), "Must inherit from DinoPark");

        try {
            assertTrue(Modifier.isPrivate(SafariTour.class.getDeclaredField("numberOfVehicles").getModifiers()), "numberOfVehicles must be private.");
            assertEquals(int.class, SafariTour.class.getDeclaredField("numberOfVehicles").getType(), "numberOfVehicles must be int.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] Field definition: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("minimum")
    @DisplayName("Minimum - Number Of Vehicles Exceptions")
    public void testVehiclesExceptions() {
        Exception ex = assertThrows(DinoParkException.class, () -> {
            new SafariTour("Jurassic Safari", -1);
        });
        assertEquals(DinoParkException.INVALID_VEHICLES_NUMBER, ex.getMessage());
    }

    @Test
    @Order(3)
    @Tag("minimum")
    @DisplayName("Minimum - Setters and Getters")
    public void testLogic() {
        try {
            SafariTour tour = new SafariTour("Jurassic Safari", 10);
            assertEquals(10, tour.getNumberOfVehicles(), "Number of vehicles should be 10.");

            tour.setNumberOfVehicles(20);
            assertEquals(20, tour.getNumberOfVehicles(), "Number of vehicles should be 20.");
        } catch (DinoParkException e) {
            fail("Exception thrown for valid arguments: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @Tag("minimum")
    @DisplayName("Minimum - toString format")
    public void testToString() throws DinoParkException {
        SafariTour tour = new SafariTour("Jurassic Safari", 10);
        String result = tour.toString();
        assertTrue(result.contains("\"type\": \"SafariTour\""));
        assertTrue(result.contains("\"numberOfVehicles\": 10"));
    }
}