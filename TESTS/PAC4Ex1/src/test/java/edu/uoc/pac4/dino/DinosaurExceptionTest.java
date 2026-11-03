package edu.uoc.pac4.dino;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DinosaurExceptionTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields definition")
    public void checkExceptions() {
        int numFields = 2;

        // Check attribute fields
        assertEquals(numFields, DinosaurException.class.getDeclaredFields().length, "The class must have exactly " + numFields + " attributes");

        try {
            assertTrue(Modifier.isPublic(DinosaurException.class.getField("INVALID_DINOSAUR_ID").getModifiers()), "The field INVALID_DINOSAUR_ID must be public");
            assertTrue(Modifier.isStatic(DinosaurException.class.getField("INVALID_DINOSAUR_ID").getModifiers()), "The field INVALID_DINOSAUR_ID must be static");
            assertTrue(Modifier.isFinal(DinosaurException.class.getField("INVALID_DINOSAUR_ID").getModifiers()), "The field INVALID_DINOSAUR_ID must be final");
            assertEquals(String.class, DinosaurException.class.getField("INVALID_DINOSAUR_ID").getType(), "The field INVALID_DINOSAUR_ID must be of type String");
            assertEquals("[ERROR] Dinosaur ID cannot be null and have to start with 'DINO-'.", DinosaurException.INVALID_DINOSAUR_ID, "The value of INVALID_DINOSAUR_ID is incorrect.");

            assertTrue(Modifier.isPublic(DinosaurException.class.getField("INVALID_DIET_TYPE").getModifiers()), "The field INVALID_DIET_TYPE must be public");
            assertTrue(Modifier.isStatic(DinosaurException.class.getField("INVALID_DIET_TYPE").getModifiers()), "The field INVALID_DIET_TYPE must be static");
            assertTrue(Modifier.isFinal(DinosaurException.class.getField("INVALID_DIET_TYPE").getModifiers()), "The field INVALID_DIET_TYPE must be final");
            assertEquals(String.class, DinosaurException.class.getField("INVALID_DIET_TYPE").getType(), "The field INVALID_DIET_TYPE must be of type String");
            assertEquals("[ERROR] Diet type cannot be null.", DinosaurException.INVALID_DIET_TYPE, "The value of INVALID_DIET_TYPE is incorrect.");
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
        assertEquals(numConstructors, DinosaurException.class.getDeclaredConstructors().length, "The class must have exactly " + numConstructors + " constructors");

        try {
            assertTrue(Modifier.isPublic(DinosaurException.class.getConstructor(String.class).getModifiers()), "The constructor DinosaurException(String) must be public");
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
        assertEquals(numMethods, DinosaurException.class.getDeclaredMethods().length, "The class must have exactly " + numMethods + " methods");
    }

    @Test
    @Order(4)
    @Tag("sanity")
    @DisplayName("Sanity - Inheritance definition")
    void checkInheritance() {
        assertTrue(Exception.class.isAssignableFrom(DinosaurException.class), "DinosaurException must inherit from Exception");
    }
}