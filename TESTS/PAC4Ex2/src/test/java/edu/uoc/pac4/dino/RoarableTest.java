package edu.uoc.pac4.dino;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoarableTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Methods definition")
    public void checkMethods() {
        int numMethods = 2;

        assertEquals(numMethods, Roarable.class.getDeclaredMethods().length, "The interface must have exactly " + numMethods + " methods");

        final Class<Roarable> ownClass = Roarable.class;

        try {
            assertTrue(Modifier.isPublic(ownClass.getMethod("changeId").getModifiers()), "The method changeId() must be public");
            assertFalse(Modifier.isStatic(ownClass.getMethod("changeId").getModifiers()), "The method changeId() must not be static");
            assertFalse(Modifier.isFinal(ownClass.getMethod("changeId").getModifiers()), "The method changeId() must not be final");
            assertFalse(ownClass.getMethod("changeId").isDefault(), "The method changeId() must not be a default method");
            assertEquals(void.class, ownClass.getMethod("changeId").getReturnType(), "The method changeId() must return void");

            assertTrue(Modifier.isPublic(ownClass.getMethod("roar").getModifiers()), "The method roar() must be public");
            assertFalse(Modifier.isStatic(ownClass.getMethod("roar").getModifiers()), "The method roar() must not be static");
            assertFalse(Modifier.isFinal(ownClass.getMethod("roar").getModifiers()), "The method roar() must not be final");
            assertTrue(ownClass.getMethod("roar").isDefault(), "The method roar() must be a default method");
            assertEquals(void.class, ownClass.getMethod("roar").getReturnType(), "The method roar() must return void");
        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of methods: " + e.getMessage());
        }
    }
}