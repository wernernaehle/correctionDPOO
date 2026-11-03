package edu.uoc.pac4.dino;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StealthRaptorTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Class definition")
    public void checkClass() {
        assertTrue(CarnivoreDinosaur.class.isAssignableFrom(StealthRaptor.class), "Must inherit from CarnivoreDinosaur");
        assertTrue(Roarable.class.isAssignableFrom(StealthRaptor.class), "Must implement Roarable");
    }

    @Test
    @Order(2)
    @Tag("advanced")
    @DisplayName("Advanced - Exceptions for Stealth Level")
    public void testStealthException() {
        DinosaurException ex1 = assertThrows(DinosaurException.class, () -> {
            new StealthRaptor("DINO-RAPT", 5, 150.0, 0.8, -1);
        });
        assertEquals(DinosaurException.INVALID_STEALTH, ex1.getMessage());

        DinosaurException ex2 = assertThrows(DinosaurException.class, () -> {
            new StealthRaptor("DINO-RAPT", 5, 150.0, 0.8, 101);
        });
        assertEquals(DinosaurException.INVALID_STEALTH, ex2.getMessage());
    }

    @Test
    @Order(3)
    @Tag("advanced")
    @DisplayName("Advanced - Interface method changeId()")
    public void testChangeId() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            StealthRaptor dino = new StealthRaptor("DINO-BLUE", 5, 150.0, 0.8, 90);
            dino.changeId();

            String printed = outContent.toString().replaceAll("\r\n", "\n").replace("\n", "");
            assertEquals("Roaring...", printed, "The changeId() method did not print the expected message.");
            assertEquals("DINO-BLUE (Alpha)", dino.getId(), "The ID was not changed correctly.");
        } catch (DinosaurException e) {
            fail("Exception thrown: " + e.getMessage());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @Order(4)
    @Tag("advanced")
    @DisplayName("Advanced - Setters and Getters")
    public void testLogic() {
        try {
            StealthRaptor dino = new StealthRaptor("DINO-RAPT", 5, 150.0,  0.8, 50);
            assertEquals(50, dino.getPackStealthLevel(), "Stealth level should be 50.");

            dino.setPackStealthLevel(100);
            assertEquals(100, dino.getPackStealthLevel(), "Stealth level should be 100.");
            
            dino.setPackStealthLevel(0);
            assertEquals(0, dino.getPackStealthLevel(), "Stealth level should be 0.");
        } catch (DinosaurException e) {
            fail("Exception thrown for valid arguments: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @Tag("advanced")
    @DisplayName("Advanced - toString")
    public void testToString() {
        try {
            StealthRaptor dino = new StealthRaptor("DINO-RAPT", 5, 150.0,  0.85, 95);
            String expected = "{ \"type\": \"StealthRaptor\", \"carnivoreDinosaurDetails\": { \"dinosaurDetails\": { \"id\": \"DINO-RAPT\", \"age\": 5, \"weight\": 150.0, \"dietType\": \"Carnivore\" }, \"aggressionLevel\": 0.85 }, \"packStealthLevel\": 95 }";
            assertEquals(expected, dino.toString());
        } catch (DinosaurException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}