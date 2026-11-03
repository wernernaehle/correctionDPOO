package edu.uoc.pac4.dino;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HybridDinosaurTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Class definition")
    public void checkClass() {
        assertTrue(CarnivoreDinosaur.class.isAssignableFrom(HybridDinosaur.class), "Must inherit from CarnivoreDinosaur");
        assertTrue(Roarable.class.isAssignableFrom(HybridDinosaur.class), "Must implement Roarable");
    }

    @Test
    @Order(2)
    @Tag("advanced")
    @DisplayName("Advanced - Exceptions for Camouflage Ability")
    public void testCamouflageException() {
        DinosaurException ex1 = assertThrows(DinosaurException.class, () -> {
            new HybridDinosaur("DINO-IND", 3, 5000.0, 1.0, -0.1);
        });
        assertEquals(DinosaurException.INVALID_CAMOUFLAGE, ex1.getMessage());

        DinosaurException ex2 = assertThrows(DinosaurException.class, () -> {
            new HybridDinosaur("DINO-IND", 3, 5000.0, 1.0, 1.1);
        });
        assertEquals(DinosaurException.INVALID_CAMOUFLAGE, ex2.getMessage());
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
            HybridDinosaur dino = new HybridDinosaur("DINO-INDOM", 3, 5000.0, 1.0, 0.8);
            dino.changeId();

            String printed = outContent.toString().replaceAll("\r\n", "\n").replace("\n", "");
            assertEquals("Roaring...", printed, "The changeId() method did not print the expected message.");
            assertEquals("DINO-INDOM (Indominus)", dino.getId(), "The ID was not changed correctly.");
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
            HybridDinosaur dino = new HybridDinosaur("DINO-IND", 3, 5000.0, 1.0, 0.5);
            assertEquals(0.5, dino.getCamouflageAbility(), "Camouflage ability should be 0.5.");

            dino.setCamouflageAbility(1.0);
            assertEquals(1.0, dino.getCamouflageAbility(), "Camouflage ability should be 1.0.");
            
            dino.setCamouflageAbility(0.0);
            assertEquals(0.0, dino.getCamouflageAbility(), "Camouflage ability should be 0.0.");
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
            HybridDinosaur dino = new HybridDinosaur("DINO-IND", 3, 5000.0, 0.99, 0.85);
            String expected = "{ \"type\": \"HybridDinosaur\", \"carnivoreDinosaurDetails\": { \"dinosaurDetails\": { \"id\": \"DINO-IND\", \"age\": 3, \"weight\": 5000.0, \"dietType\": \"Carnivore\" }, \"aggressionLevel\": 0.99 }, \"camouflageAbility\": 0.85 }";
            assertEquals(expected, dino.toString());
        } catch (DinosaurException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}