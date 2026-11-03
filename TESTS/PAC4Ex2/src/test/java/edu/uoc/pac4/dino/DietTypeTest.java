package edu.uoc.pac4.dino;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DietTypeTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Literals definition")
    public void checkLiterals() {
        DietType[] literalsArray = DietType.values();
        List<DietType> literals = Arrays.asList(literalsArray);

        assertEquals(3, literals.size());

        assertTrue(literals.contains(DietType.CARNIVORE));
        assertTrue(literals.contains(DietType.HERBIVORE));
        assertTrue(literals.contains(DietType.OMNIVORE));
    }

    @Test
    @Order(2)
    @Tag("sanity")
    @DisplayName("Sanity - Methods definition")
    public void checkMethods() {
        int numMethods = 4; // values(), valueOf(), toString(), y el implícito de enum

        try {
            assertTrue(Modifier.isPublic(DietType.class.getMethod("toString").getModifiers()), "The method toString() must be public");
            assertFalse(Modifier.isStatic(DietType.class.getMethod("toString").getModifiers()), "The method toString() must not be static");
            assertFalse(Modifier.isFinal(DietType.class.getMethod("toString").getModifiers()), "The method toString() must not be final");
            assertEquals(String.class, DietType.class.getMethod("toString").getReturnType(), "The method toString() must return a Stringg");
        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of methods: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @Tag("minimum")
    @DisplayName("Minimum - toString method")
    public void testToString() {
        assertEquals("Carnivore", DietType.CARNIVORE.toString());
        assertEquals("Herbivore", DietType.HERBIVORE.toString());
        assertEquals("Omnivore", DietType.OMNIVORE.toString());
    }
}