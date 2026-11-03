package edu.uoc.pac4;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DinoParkExceptionTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields definition")
    public void checkExceptions() {
        int numFields = 9;

        // Check attribute fields
        assertEquals(numFields, DinoParkException.class.getDeclaredFields().length, "The class must have exactly " + numFields + " attributes");

        try {
            assertTrue(Modifier.isPublic(DinoParkException.class.getField("INVALID_PARK_NAME").getModifiers()), "The field INVALID_PARK_NAME must be public");
            assertTrue(Modifier.isStatic(DinoParkException.class.getField("INVALID_PARK_NAME").getModifiers()), "The field INVALID_PARK_NAME must be static");
            assertTrue(Modifier.isFinal(DinoParkException.class.getField("INVALID_PARK_NAME").getModifiers()), "The field INVALID_PARK_NAME must be final");
            assertEquals(String.class, DinoParkException.class.getField("INVALID_PARK_NAME").getType(), "The field INVALID_PARK_NAME must be of type String");
            assertEquals("[ERROR] Park name cannot be null or empty.", DinoParkException.INVALID_PARK_NAME, "The value of INVALID_PARK_NAME field is incorrect");

            assertTrue(Modifier.isPublic(DinoParkException.class.getField("NULL_DINOSAUR").getModifiers()), "The field NULL_DINOSAUR must be public");
            assertTrue(Modifier.isStatic(DinoParkException.class.getField("NULL_DINOSAUR").getModifiers()), "The field NULL_DINOSAUR must be static");
            assertTrue(Modifier.isFinal(DinoParkException.class.getField("NULL_DINOSAUR").getModifiers()), "The field NULL_DINOSAUR must be final");
            assertEquals(String.class, DinoParkException.class.getField("NULL_DINOSAUR").getType(), "The field NULL_DINOSAUR must be of type String");
            assertEquals("[ERROR] Dinosaur cannot be null.", DinoParkException.NULL_DINOSAUR, "The value of NULL_DINOSAUR field is incorrect");

            assertTrue(Modifier.isPublic(DinoParkException.class.getField("MAX_DINOSAURS_REACHED").getModifiers()), "The field MAX_DINOSAURS_REACHED must be public");
            assertTrue(Modifier.isStatic(DinoParkException.class.getField("MAX_DINOSAURS_REACHED").getModifiers()), "The field MAX_DINOSAURS_REACHED must be static");
            assertTrue(Modifier.isFinal(DinoParkException.class.getField("MAX_DINOSAURS_REACHED").getModifiers()), "The field MAX_DINOSAURS_REACHED must be final");
            assertEquals(String.class, DinoParkException.class.getField("MAX_DINOSAURS_REACHED").getType(), "The field MAX_DINOSAURS_REACHED must be of type String");
            assertEquals("[ERROR] Maximum number of dinosaurs reached.", DinoParkException.MAX_DINOSAURS_REACHED, "The value of MAX_DINOSAURS_REACHED field is incorrect");

            assertTrue(Modifier.isPublic(DinoParkException.class.getField("DINOSAUR_ALREADY_EXISTS").getModifiers()), "The field DINOSAUR_ALREADY_EXISTS must be public");
            assertTrue(Modifier.isStatic(DinoParkException.class.getField("DINOSAUR_ALREADY_EXISTS").getModifiers()), "The field DINOSAUR_ALREADY_EXISTS must be static");
            assertTrue(Modifier.isFinal(DinoParkException.class.getField("DINOSAUR_ALREADY_EXISTS").getModifiers()), "The field DINOSAUR_ALREADY_EXISTS must be final");
            assertEquals(String.class, DinoParkException.class.getField("DINOSAUR_ALREADY_EXISTS").getType(), "The field DINOSAUR_ALREADY_EXISTS must be of type String");
            assertEquals("[ERROR] Dinosaur already exists in the park.", DinoParkException.DINOSAUR_ALREADY_EXISTS, "The value of DINOSAUR_ALREADY_EXISTS field is incorrect");

            assertTrue(Modifier.isPublic(DinoParkException.class.getField("DINOSAUR_NOT_FOUND").getModifiers()), "The field DINOSAUR_NOT_FOUND must be public");
            assertTrue(Modifier.isStatic(DinoParkException.class.getField("DINOSAUR_NOT_FOUND").getModifiers()), "The field DINOSAUR_NOT_FOUND must be static");
            assertTrue(Modifier.isFinal(DinoParkException.class.getField("DINOSAUR_NOT_FOUND").getModifiers()), "The field DINOSAUR_NOT_FOUND must be final");
            assertEquals(String.class, DinoParkException.class.getField("DINOSAUR_NOT_FOUND").getType(), "The field DINOSAUR_NOT_FOUND must be of type String");
            assertEquals("[ERROR] Dinosaur not found in the park.", DinoParkException.DINOSAUR_NOT_FOUND, "The value of DINOSAUR_NOT_FOUND field is incorrect");

            assertTrue(Modifier.isPublic(DinoParkException.class.getField("INVALID_VEHICLES_NUMBER").getModifiers()), "The field INVALID_VEHICLES_NUMBER must be public");
            assertTrue(Modifier.isStatic(DinoParkException.class.getField("INVALID_VEHICLES_NUMBER").getModifiers()),  "The field INVALID_VEHICLES_NUMBER must be static");
            assertTrue(Modifier.isFinal(DinoParkException.class.getField("INVALID_VEHICLES_NUMBER").getModifiers()),  "The field INVALID_VEHICLES_NUMBER must be final");
            assertEquals(String.class, DinoParkException.class.getField("INVALID_VEHICLES_NUMBER").getType(),  "The field INVALID_VEHICLES_NUMBER must be of type String");
            assertEquals("[ERROR] Number of vehicles cannot be negative.", DinoParkException.INVALID_VEHICLES_NUMBER,  "The value of INVALID_VEHICLES_NUMBER field is incorrect");

            assertTrue(Modifier.isPublic(DinoParkException.class.getField("INVALID_FENCE_VOLTAGE").getModifiers()),  "The field INVALID_FENCE_VOLTAGE must be public");
            assertTrue(Modifier.isStatic(DinoParkException.class.getField("INVALID_FENCE_VOLTAGE").getModifiers()), "The field INVALID_FENCE_VOLTAGE must be static");
            assertTrue(Modifier.isFinal(DinoParkException.class.getField("INVALID_FENCE_VOLTAGE").getModifiers()), "The field INVALID_FENCE_VOLTAGE must be final");
            assertEquals(String.class, DinoParkException.class.getField("INVALID_FENCE_VOLTAGE").getType(), "The field INVALID_FENCE_VOLTAGE must be of type String");
            assertEquals("[ERROR] Electric fence voltage must be greater than 0.", DinoParkException.INVALID_FENCE_VOLTAGE, "The value of INVALID_FENCE_VOLTAGE field is incorrect");

            assertTrue(Modifier.isPublic(DinoParkException.class.getField("ERROR_STAFF_ID_EMPTY").getModifiers()),  "The field ERROR_STAFF_ID_EMPTY must be public");
            assertTrue(Modifier.isStatic(DinoParkException.class.getField("ERROR_STAFF_ID_EMPTY").getModifiers()), "The field ERROR_STAFF_ID_EMPTY must be static");
            assertTrue(Modifier.isFinal(DinoParkException.class.getField("ERROR_STAFF_ID_EMPTY").getModifiers()), "The field ERROR_STAFF_ID_EMPTY must be final");
            assertEquals(String.class, DinoParkException.class.getField("ERROR_STAFF_ID_EMPTY").getType(), "The field ERROR_STAFF_ID_EMPTY must be of type String");
            assertEquals("[ERROR] Staff ID cannot be empty.", DinoParkException.ERROR_STAFF_ID_EMPTY, "The value of ERROR_STAFF_ID_EMPTY field is incorrect");

            assertTrue(Modifier.isPublic(DinoParkException.class.getField("ERROR_MEDICINE_EMPTY").getModifiers()),  "The field ERROR_MEDICINE_EMPTY must be public");
            assertTrue(Modifier.isStatic(DinoParkException.class.getField("ERROR_MEDICINE_EMPTY").getModifiers()), "The field ERROR_MEDICINE_EMPTY must be static");
            assertTrue(Modifier.isFinal(DinoParkException.class.getField("ERROR_MEDICINE_EMPTY").getModifiers()), "The field ERROR_MEDICINE_EMPTY must be final");
            assertEquals(String.class, DinoParkException.class.getField("ERROR_MEDICINE_EMPTY").getType(), "The field ERROR_MEDICINE_EMPTY must be of type String");
            assertEquals("[ERROR] Medicine name must be provided.", DinoParkException.ERROR_MEDICINE_EMPTY, "The value of ERROR_MEDICINE_EMPTY field is incorrect");

        } catch (NoSuchFieldException e) {
            fail("[ERROR] There is some problem with the definition of the field: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("sanity")
    @DisplayName("Sanity - Constructor definition")
    public void checkConstructor() {
        int numConstructors = 1;

        // Check constructors
        assertEquals(numConstructors, DinoParkException.class.getDeclaredConstructors().length, "The class must have exactly " + numConstructors + " constructors");


        try {
            assertTrue(Modifier.isPublic(DinoParkException.class.getConstructor(String.class).getModifiers()), "The constructor DinoParkException(String) must be public");
        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of constructors: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @Tag("sanity")
    @DisplayName("Sanity - Methods definition")
    void checkMethods() {
        int numMethods = 0;

        // Check number of methods
        assertEquals(numMethods, DinoParkException.class.getDeclaredMethods().length, "The class must have exactly " + numMethods + " methods");
    }

    @Test
    @Order(4)
    @Tag("sanity")
    @DisplayName("Sanity - Inheritance definition")
    void checkInheritance() {
        assertTrue(Exception.class.isAssignableFrom(DinoParkException.class), "DinoParkException must inherit from Exception");
    }

    @Test
    @Order(5)
    @Tag("sanity")
    @DisplayName("Sanity - Instantiation and Message")
    public void testExceptionMessage() {
        DinoParkException exception = new DinoParkException("Test message");
        assertEquals("Test message", exception.getMessage());
    }
}