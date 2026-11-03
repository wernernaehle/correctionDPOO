package edu.uoc.pac4.staff;

import edu.uoc.pac4.DinoParkException;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RangerTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields and Class definition")
    public void checkFieldsAndClass() {
        assertEquals(1, Ranger.class.getDeclaredFields().length, "The class must have exactly 1 field.");
        assertTrue(ParkStaff.class.isAssignableFrom(Ranger.class), "Ranger must inherit from ParkStaff");

        try {
            assertTrue(Modifier.isPrivate(Ranger.class.getDeclaredField("enclosureName").getModifiers()), "enclosureName must be private.");
            assertEquals(String.class, Ranger.class.getDeclaredField("enclosureName").getType(), "enclosureName must be String.");
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
            Ranger ranger = new Ranger("Bob", "R001", "Raptor Paddock");
            
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            
            ranger.performDuties();
            
            assertEquals("Ranger Bob is patrolling the Raptor Paddock enclosure." + System.lineSeparator(), outContent.toString());
            
            System.setOut(System.out);
        } catch (DinoParkException e) {
            fail("Exception thrown for valid arguments: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @Tag("minimum")
    @DisplayName("Minimum - toString")
    public void testToString() {
        try {
            Ranger ranger = new Ranger("Bob", "R001", "Raptor Paddock");
            String expected = "{ \"type\": \"Ranger\", \"name\": \"Bob\", \"staffId\": \"R001\", \"enclosureName\": \"Raptor Paddock\" }";
            assertEquals(expected, ranger.toString());
        } catch (DinoParkException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
