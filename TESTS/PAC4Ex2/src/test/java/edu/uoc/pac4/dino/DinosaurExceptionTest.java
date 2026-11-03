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
        int numFields = 5;

        // Check attribute fields
        assertEquals(numFields, DinosaurException.class.getDeclaredFields().length, "The class must have exactly " + numFields + " attributes");

        try {
            assertTrue(Modifier.isPublic(DinosaurException.class.getField("INVALID_DINOSAUR_ID").getModifiers()), "The field INVALID_DINOSAUR_ID must be public");
            assertTrue(Modifier.isStatic(DinosaurException.class.getField("INVALID_DINOSAUR_ID").getModifiers()), "The field INVALID_DINOSAUR_ID must be static");
            assertTrue(Modifier.isFinal(DinosaurException.class.getField("INVALID_DINOSAUR_ID").getModifiers()), "The field INVALID_DINOSAUR_ID must be final");
            assertEquals(String.class, DinosaurException.class.getField("INVALID_DINOSAUR_ID").getType(), "The field INVALID_DINOSAUR_ID must be of type String");
            assertEquals("[ERROR] Dinosaur ID cannot be null and have to start with 'DINO-'.", DinosaurException.INVALID_DINOSAUR_ID, "The value of INVALID_DINOSAUR_ID field is incorrect");

            assertTrue(Modifier.isPublic(DinosaurException.class.getField("INVALID_DIET_TYPE").getModifiers()), "The field INVALID_DIET_TYPE must be public");
            assertTrue(Modifier.isStatic(DinosaurException.class.getField("INVALID_DIET_TYPE").getModifiers()), "The field INVALID_DIET_TYPE must be static");
            assertTrue(Modifier.isFinal(DinosaurException.class.getField("INVALID_DIET_TYPE").getModifiers()), "The field INVALID_DIET_TYPE must be final");
            assertEquals(String.class, DinosaurException.class.getField("INVALID_DIET_TYPE").getType(), "The field INVALID_DIET_TYPE must be of type String");
            assertEquals("[ERROR] Diet type cannot be null.", DinosaurException.INVALID_DIET_TYPE, "The value of INVALID_DIET_TYPE field is incorrect");

            assertTrue(Modifier.isPublic(DinosaurException.class.getField("INVALID_BITE_FORCE").getModifiers()), "The field INVALID_BITE_FORCE must be public");
            assertTrue(Modifier.isStatic(DinosaurException.class.getField("INVALID_BITE_FORCE").getModifiers()), "The field INVALID_BITE_FORCE must be static");
            assertTrue(Modifier.isFinal(DinosaurException.class.getField("INVALID_BITE_FORCE").getModifiers()), "The field INVALID_BITE_FORCE must be final");
            assertEquals(String.class, DinosaurException.class.getField("INVALID_BITE_FORCE").getType(), "The field INVALID_BITE_FORCE must be of type String");
            assertEquals("[ERROR] Bite force must be greater than 0.", DinosaurException.INVALID_BITE_FORCE, "The value of INVALID_BITE_FORCE field is incorrect");

            assertTrue(Modifier.isPublic(DinosaurException.class.getField("INVALID_CAMOUFLAGE").getModifiers()), "The field INVALID_CAMOUFLAGE must be public");
            assertTrue(Modifier.isStatic(DinosaurException.class.getField("INVALID_CAMOUFLAGE").getModifiers()), "The field INVALID_CAMOUFLAGE must be static");
            assertTrue(Modifier.isFinal(DinosaurException.class.getField("INVALID_CAMOUFLAGE").getModifiers()), "The field INVALID_CAMOUFLAGE must be final");
            assertEquals(String.class, DinosaurException.class.getField("INVALID_CAMOUFLAGE").getType(), "The field INVALID_CAMOUFLAGE must be of type String");
            assertEquals("[ERROR] Camouflage ability must be between 0.0 and 1.0.", DinosaurException.INVALID_CAMOUFLAGE, "The value of INVALID_CAMOUFLAGE field is incorrect");

            assertTrue(Modifier.isPublic(DinosaurException.class.getField("INVALID_STEALTH").getModifiers()), "The field INVALID_STEALTH must be public");
            assertTrue(Modifier.isStatic(DinosaurException.class.getField("INVALID_STEALTH").getModifiers()), "The field INVALID_STEALTH must be static");
            assertTrue(Modifier.isFinal(DinosaurException.class.getField("INVALID_STEALTH").getModifiers()), "The field INVALID_STEALTH must be final");
            assertEquals(String.class, DinosaurException.class.getField("INVALID_STEALTH").getType(), "The field INVALID_STEALTH must be of type String");
            assertEquals("[ERROR] Pack stealth level must be between 0 and 100.", DinosaurException.INVALID_STEALTH, "The value of INVALID_STEALTH field is incorrect");

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

    @Test
    @Order(5)
    @Tag("sanity")
    @DisplayName("Sanity - Instantiation and Message")
    public void testExceptionMessage() {
        DinosaurException exception = new DinosaurException("Test message");
        assertEquals("Test message", exception.getMessage());
    }
}