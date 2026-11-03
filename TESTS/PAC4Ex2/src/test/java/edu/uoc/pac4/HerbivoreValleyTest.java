package edu.uoc.pac4;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HerbivoreValleyTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields and Inheritance definition")
    public void checkFields() {
        assertEquals(1, HerbivoreValley.class.getDeclaredFields().length, "The class must have exactly 1 field.");
        assertTrue(DinoPark.class.isAssignableFrom(HerbivoreValley.class), "Must inherit from DinoPark");

        try {
            assertTrue(Modifier.isPrivate(HerbivoreValley.class.getDeclaredField("hasWaterLake").getModifiers()), "hasWaterLake must be private.");
            assertEquals(boolean.class, HerbivoreValley.class.getDeclaredField("hasWaterLake").getType(), "hasWaterLake must be int.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] Field definition: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("minimum")
    @DisplayName("Minimum - Logic and toString format")
    public void testLogicAndToString() throws DinoParkException {
        HerbivoreValley valley = new HerbivoreValley("Gallimimus Valley", true);
        assertEquals(true, valley.getHasWaterLake());

        valley.setHasWaterLake(false);
        assertEquals(false, valley.getHasWaterLake());

        String result = valley.toString();
        assertTrue(result.contains("\"type\": \"HerbivoreValley\""));
        assertTrue(result.contains("\"hasWaterLake\": false"));
    }
}