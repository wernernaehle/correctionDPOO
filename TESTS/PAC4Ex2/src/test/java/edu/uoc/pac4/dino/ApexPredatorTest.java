package edu.uoc.pac4.dino;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApexPredatorTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields and Class definition")
    public void checkFieldsAndClass() {
        assertEquals(1, ApexPredator.class.getDeclaredFields().length, "The class must have exactly 1 field.");
        assertTrue(CarnivoreDinosaur.class.isAssignableFrom(ApexPredator.class), "ApexPredator must inherit from CarnivoreDinosaur");

        try {
            assertTrue(Modifier.isPrivate(ApexPredator.class.getDeclaredField("biteForce").getModifiers()), "biteForce must be private.");
            assertEquals(int.class, ApexPredator.class.getDeclaredField("biteForce").getType(), "biteForce must be int.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] Field definition: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("minimum")
    @DisplayName("Minimum - Exception handling for biteForce")
    public void testBiteForceException() {
        DinosaurException exception0 = assertThrows(DinosaurException.class, () -> {
            new ApexPredator("DINO-TREX", 10, 8000.0, 0.9, 0);
        });
        assertEquals(DinosaurException.INVALID_BITE_FORCE, exception0.getMessage());

        DinosaurException exceptionNeg = assertThrows(DinosaurException.class, () -> {
            new ApexPredator("DINO-TREX", 10, 8000.0, 0.9, -100);
        });
        assertEquals(DinosaurException.INVALID_BITE_FORCE, exceptionNeg.getMessage());
    }

    @Test
    @Order(3)
    @Tag("minimum")
    @DisplayName("Minimum - Setters and Getters")
    public void testLogic() {
        try {
            ApexPredator dino = new ApexPredator("DINO-TREX", 10, 8000.0, 0.9, 30000);
            assertEquals(30000, dino.getBiteForce(), "Bite force should be 30000");

            dino.setBiteForce(40000);
            assertEquals(40000, dino.getBiteForce(), "Bite force should be updated to 40000");
        } catch (DinosaurException e) {
            fail("Exception thrown for valid arguments: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @Tag("minimum")
    @DisplayName("Minimum - toString")
    public void testToString() {
        try {
            ApexPredator dino = new ApexPredator("DINO-TREX", 10, 8000.0, 0.95, 35000);
            String expected = "{ \"type\": \"ApexPredator\", \"carnivoreDinosaurDetails\": { \"dinosaurDetails\": { \"id\": \"DINO-TREX\", \"age\": 10, \"weight\": 8000.0, \"dietType\": \"Carnivore\" }, \"aggressionLevel\": 0.95 }, \"biteForce\": 35000 }";
            assertEquals(expected, dino.toString());
        } catch (DinosaurException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}