package edu.uoc.pac4.dino;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarnivoreDinosaurTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields definition")
    public void checkFields() {
        int numFields = 1;

        assertEquals(numFields, CarnivoreDinosaur.class.getDeclaredFields().length, "The class must have exactly " + numFields + " fields.");

        try {
            assertTrue(Modifier.isPrivate(CarnivoreDinosaur.class.getDeclaredField("aggressionLevel").getModifiers()), "The attribute aggressionLevel must be private.");
            assertEquals(double.class, CarnivoreDinosaur.class.getDeclaredField("aggressionLevel").getType(), "The attribute aggressionLevel must be double.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] There is some problem with the definition of the attributes: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("sanity")
    @DisplayName("Sanity - Class and Inheritance definition")
    public void checkClassAndInheritance() {
        assertTrue(Modifier.isAbstract(CarnivoreDinosaur.class.getModifiers()), "The class CarnivoreDinosaur must be abstract");
        assertTrue(Dinosaur.class.isAssignableFrom(CarnivoreDinosaur.class), "CarnivoreDinosaur must inherit from Dinosaur");
    }

    @Test
    @Order(3)
    @Tag("sanity")
    @DisplayName("Sanity - Methods definition")
    public void checkMethods() {
        try {
            assertTrue(Modifier.isPublic(CarnivoreDinosaur.class.getDeclaredMethod("getAggressionLevel").getModifiers()), "getAggressionLevel() must be public");
            assertTrue(Modifier.isPublic(CarnivoreDinosaur.class.getDeclaredMethod("setAggressionLevel", double.class).getModifiers()), "setAggressionLevel() must be public");
        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of methods: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @Tag("advanced")
    @DisplayName("Advanced - Setters, Getters, and toString Logic")
    public void testLogicAndToString() {
        try {
            CarnivoreDinosaur dino1 = new CarnivoreDinosaur("DINO-001", 10, 500.0, 0.5) {};
            assertEquals(0.5, dino1.getAggressionLevel(), "Aggression level should be 0.5");

            // Test boundaries
            dino1.setAggressionLevel(-0.5);
            assertEquals(0.0, dino1.getAggressionLevel(), "Aggression level below 0.0 must be set to 0.0");

            dino1.setAggressionLevel(1.5);
            assertEquals(1.0, dino1.getAggressionLevel(), "Aggression level above 1.0 must be set to 1.0");

            // Test toString
            dino1.setAggressionLevel(0.85);
            String expected = "{ \"dinosaurDetails\": { \"id\": \"DINO-001\", \"age\": 10, \"weight\": 500.0, \"dietType\": \"Carnivore\" }, \"aggressionLevel\": 0.85 }";
            assertEquals(normalizeJson(expected), normalizeJson(dino1.toString()), "toString does not match expected JSON format.");
        } catch (DinosaurException e) {
            fail("Exception should not be thrown for valid constructor arguments: " + e.getMessage());
        }
    }

    private String normalizeJson(String json) {
        boolean insideQuotes = false;
        StringBuilder result = new StringBuilder();
        for (char c : json.toCharArray()) {
            if (c == '\"') {
                insideQuotes = !insideQuotes;
                result.append(c);
                continue;
            }
            if (!insideQuotes && Character.isWhitespace(c)) {
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }
}