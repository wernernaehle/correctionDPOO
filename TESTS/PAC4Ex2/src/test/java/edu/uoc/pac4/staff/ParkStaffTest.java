package edu.uoc.pac4.staff;

import edu.uoc.pac4.DinoParkException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParkStaffTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields and Class definition")
    public void checkFieldsAndClass() {
        assertEquals(2, ParkStaff.class.getDeclaredFields().length, "The class must have exactly 2 fields.");
        assertTrue(Modifier.isAbstract(ParkStaff.class.getModifiers()), "ParkStaff must be an abstract class");

        try {
            assertTrue(Modifier.isPrivate(ParkStaff.class.getDeclaredField("name").getModifiers()), "name must be private.");
            assertEquals(String.class, ParkStaff.class.getDeclaredField("name").getType(), "name must be String.");
            assertTrue(Modifier.isPrivate(ParkStaff.class.getDeclaredField("staffId").getModifiers()), "staffId must be private.");
            assertEquals(String.class, ParkStaff.class.getDeclaredField("staffId").getType(), "staffId must be String.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] Field definition: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("minimum")
    @DisplayName("Minimum - Exception handling for staffId")
    public void testStaffIdException() {
        DinoParkException exceptionNull = assertThrows(DinoParkException.class, () -> {
            new ParkStaff("John Doe", null) {
                @Override
                public void performDuties() {}
            };
        });
        assertEquals(DinoParkException.ERROR_STAFF_ID_EMPTY, exceptionNull.getMessage());

        DinoParkException exceptionEmpty = assertThrows(DinoParkException.class, () -> {
            new ParkStaff("John Doe", "  ") {
                @Override
                public void performDuties() {}
            };
        });
        assertEquals(DinoParkException.ERROR_STAFF_ID_EMPTY, exceptionEmpty.getMessage());
    }

    @Test
    @Order(3)
    @Tag("minimum")
    @DisplayName("Minimum - Getters")
    public void testGetters() {
        try {
            ParkStaff staff = new ParkStaff("John Doe", "S001") {
                @Override
                public void performDuties() {}
            };
            assertEquals("John Doe", staff.getName());
            assertEquals("S001", staff.getStaffId());
        } catch (DinoParkException e) {
            fail("Exception thrown for valid arguments: " + e.getMessage());
        }
    }
}
