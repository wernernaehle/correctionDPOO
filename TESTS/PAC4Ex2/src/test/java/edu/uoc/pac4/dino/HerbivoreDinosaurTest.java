package edu.uoc.pac4.dino;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HerbivoreDinosaurTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields and Class definition")
    public void checkFieldsAndClass() {
        assertEquals(1, HerbivoreDinosaur.class.getDeclaredFields().length, "The class must have exactly 1 field.");
        assertTrue(Dinosaur.class.isAssignableFrom(HerbivoreDinosaur.class), "HerbivoreDinosaur must inherit from Dinosaur");

        try {
            assertTrue(Modifier.isPrivate(HerbivoreDinosaur.class.getDeclaredField("foragingCapacity").getModifiers()), "foragingCapacity must be private.");
            assertEquals(int.class, HerbivoreDinosaur.class.getDeclaredField("foragingCapacity").getType(), "foragingCapacity must be int.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] Field definition: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("advanced")
    @DisplayName("Advanced - Setters and Getters Logic")
    public void testSettersAndGetters() {
        try {
            HerbivoreDinosaur dino = new HerbivoreDinosaur("DINO-H01", 10, 5000.0, 50);
            assertEquals(50, dino.getForagingCapacity(), "Foraging capacity should be 50.");

            dino.setForagingCapacity(-10);
            assertEquals(0, dino.getForagingCapacity(), "Foraging capacity below 0 must be set to 0.");

            dino.setForagingCapacity(150);
            assertEquals(100, dino.getForagingCapacity(), "Foraging capacity above 100 must be set to 100.");
        } catch (DinosaurException e) {
            fail("Exception should not be thrown for valid constructor arguments.");
        }
    }

    @Test
    @Order(3)
    @Tag("advanced")
    @DisplayName("Advanced - toString")
    public void testToString() {
        try {
            HerbivoreDinosaur dino = new HerbivoreDinosaur("DINO-TRIC", 15, 6000.0, 80);
            String expected = "{ \"type\": \"HerbivoreDinosaur\", \"dinosaurDetails\": { \"id\": \"DINO-TRIC\", \"age\": 15, \"weight\": 6000.0, \"dietType\": \"Herbivore\" }, \"foragingCapacity\": 80 }";
            assertEquals(expected, dino.toString(), "toString does not match expected JSON format.");
        } catch (DinosaurException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}