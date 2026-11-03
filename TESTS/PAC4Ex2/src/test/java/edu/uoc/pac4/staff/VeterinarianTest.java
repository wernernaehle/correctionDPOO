package edu.uoc.pac4.staff;

import edu.uoc.pac4.DinoParkException;
import edu.uoc.pac4.dino.Dinosaur;
import edu.uoc.pac4.dino.DietType;
import edu.uoc.pac4.dino.ApexPredator;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VeterinarianTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields and Class definition")
    public void checkFieldsAndClass() {
        assertEquals(1, Veterinarian.class.getDeclaredFields().length, "The class must have exactly 1 field.");
        assertTrue(ParkStaff.class.isAssignableFrom(Veterinarian.class), "Veterinarian must inherit from ParkStaff");

        try {
            assertTrue(Modifier.isPrivate(Veterinarian.class.getDeclaredField("specialization").getModifiers()), "specialization must be private.");
            assertEquals(String.class, Veterinarian.class.getDeclaredField("specialization").getType(), "specialization must be String.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] Field definition: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("minimum")
    @DisplayName("Minimum - performDuties")
    public void testPerformDuties() {
        try {
            Veterinarian vet = new Veterinarian("Alice", "V001", "Theropod");
            
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            
            vet.performDuties();
            
            assertEquals("Veterinarian Alice is conducting health checks in the Theropod unit." + System.lineSeparator(), outContent.toString());
            
            System.setOut(System.out);
        } catch (DinoParkException e) {
            fail("Exception thrown for valid arguments: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @Tag("minimum")
    @DisplayName("Minimum - Heal methods")
    public void testHeal() {
        try {
            Veterinarian vet = new Veterinarian("Alice", "V001", "Theropod");
            Dinosaur dino = new Dinosaur("DINO-TREX", 5, 5000.0, DietType.CARNIVORE) {};
            
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outContent));
            
            try {
                // Basic heal
                vet.heal(dino);
                assertTrue(outContent.toString().contains("Applying basic treatment to dinosaur: DINO-TREX"), "[DEBUG_LOG] Output was: " + outContent.toString());
                
                outContent.reset();
                
                // Advanced heal
                vet.heal(dino, "Antibiotics");
                assertTrue(outContent.toString().contains("Applying advanced treatment to DINO-TREX using Antibiotics."), "[DEBUG_LOG] Output was: " + outContent.toString());
            } finally {
                System.setOut(originalOut);
            }
        } catch (Exception e) {
            fail("Exception thrown: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @Tag("minimum")
    @DisplayName("Minimum - Exception in advanced heal")
    public void testHealException() {
        try {
            Veterinarian vet = new Veterinarian("Alice", "V001", "Theropod");
            Dinosaur dino = new Dinosaur("DINO-TREX", 5, 5000.0, DietType.CARNIVORE) {};
            
            DinoParkException exNull = assertThrows(DinoParkException.class, () -> vet.heal(dino, null));
            assertEquals(DinoParkException.ERROR_MEDICINE_EMPTY, exNull.getMessage());
            
            DinoParkException exEmpty = assertThrows(DinoParkException.class, () -> vet.heal(dino, "  "));
            assertEquals(DinoParkException.ERROR_MEDICINE_EMPTY, exEmpty.getMessage());
        } catch (Exception e) {
            fail("Exception thrown for valid arguments: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @Tag("minimum")
    @DisplayName("Minimum - toString")
    public void testToString() {
        try {
            Veterinarian vet = new Veterinarian("Alice", "V001", "Theropod");
            String expected = "{ \"type\": \"Veterinarian\", \"name\": \"Alice\", \"staffId\": \"V001\", \"specialization\": \"Theropod\" }";
            assertEquals(expected, vet.toString());
        } catch (DinoParkException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
