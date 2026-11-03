package edu.uoc.pac3;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TyreCompoundTest {

    @Test
    @Order(1)
    @DisplayName("Sanity - Fields definitions")
    public void checkFieldsSanity() {
        assertEquals(8, TyreCompound.class.getDeclaredFields().length);

        try {
            assertTrue(Modifier.isPublic(TyreCompound.class.getDeclaredField("HARD").getModifiers()));
            assertTrue(Modifier.isStatic(TyreCompound.class.getDeclaredField("HARD").getModifiers()));
            assertTrue(Modifier.isFinal(TyreCompound.class.getDeclaredField("HARD").getModifiers()));
            assertEquals(TyreCompound.class, TyreCompound.class.getDeclaredField("HARD").getType());

            assertTrue(Modifier.isPublic(TyreCompound.class.getDeclaredField("MEDIUM").getModifiers()));
            assertTrue(Modifier.isStatic(TyreCompound.class.getDeclaredField("MEDIUM").getModifiers()));
            assertTrue(Modifier.isFinal(TyreCompound.class.getDeclaredField("MEDIUM").getModifiers()));
            assertEquals(TyreCompound.class, TyreCompound.class.getDeclaredField("MEDIUM").getType());

            assertTrue(Modifier.isPublic(TyreCompound.class.getDeclaredField("SOFT").getModifiers()));
            assertTrue(Modifier.isStatic(TyreCompound.class.getDeclaredField("SOFT").getModifiers()));
            assertTrue(Modifier.isFinal(TyreCompound.class.getDeclaredField("SOFT").getModifiers()));
            assertEquals(TyreCompound.class, TyreCompound.class.getDeclaredField("SOFT").getType());

            assertTrue(Modifier.isPublic(TyreCompound.class.getDeclaredField("QUALIFYING").getModifiers()));
            assertTrue(Modifier.isStatic(TyreCompound.class.getDeclaredField("QUALIFYING").getModifiers()));
            assertTrue(Modifier.isFinal(TyreCompound.class.getDeclaredField("QUALIFYING").getModifiers()));
            assertEquals(TyreCompound.class, TyreCompound.class.getDeclaredField("QUALIFYING").getType());

            assertTrue(Modifier.isPrivate(TyreCompound.class.getDeclaredField("description").getModifiers()));
            assertTrue(Modifier.isFinal(TyreCompound.class.getDeclaredField("description").getModifiers()));
            assertEquals(String.class, TyreCompound.class.getDeclaredField("description").getType());

            assertTrue(Modifier.isPrivate(TyreCompound.class.getDeclaredField("grip").getModifiers()));
            assertTrue(Modifier.isFinal(TyreCompound.class.getDeclaredField("grip").getModifiers()));
            assertEquals(int.class, TyreCompound.class.getDeclaredField("grip").getType());

            assertTrue(Modifier.isPrivate(TyreCompound.class.getDeclaredField("speedBonus").getModifiers()));
            assertTrue(Modifier.isFinal(TyreCompound.class.getDeclaredField("speedBonus").getModifiers()));
            assertEquals(int.class, TyreCompound.class.getDeclaredField("speedBonus").getType());

        } catch (NoSuchFieldException e) {
            fail("[ERROR] There is some problem with the definition of the fields: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Tyre Compound - Description")
    public void testDescription() {
        assertEquals("Hard", TyreCompound.HARD.getDescription());
        assertEquals("Medium", TyreCompound.MEDIUM.getDescription());
        assertEquals("Soft", TyreCompound.SOFT.getDescription());
        assertEquals("Qualifying", TyreCompound.QUALIFYING.getDescription());
    }

    @Test
    @Order(4)
    @DisplayName("Tyre Compound - Grip")
    public void testGrip() {
        assertEquals(80, TyreCompound.HARD.getGrip());
        assertEquals(100, TyreCompound.MEDIUM.getGrip());
        assertEquals(120, TyreCompound.SOFT.getGrip());
        assertEquals(150, TyreCompound.QUALIFYING.getGrip());
    }

    @Test
    @Order(5)
    @DisplayName("Tyre Compound - Speed Bonus")
    public void testSpeedBonus() {
        assertEquals(10, TyreCompound.HARD.getSpeedBonus());
        assertEquals(15, TyreCompound.MEDIUM.getSpeedBonus());
        assertEquals(25, TyreCompound.SOFT.getSpeedBonus());
        assertEquals(40, TyreCompound.QUALIFYING.getSpeedBonus());
    }

    @Test
    @Order(6)
    @DisplayName("Tyre Compound - Total Performance")
    public void testTotalPerformance() {
        assertEquals(145.0, TyreCompound.HARD.getTotalPerformance(), 0.01);
        assertEquals(187.5, TyreCompound.MEDIUM.getTotalPerformance(), 0.01);
        assertEquals(242.5, TyreCompound.SOFT.getTotalPerformance(), 0.01);
        assertEquals(325.0, TyreCompound.QUALIFYING.getTotalPerformance(), 0.01);
    }

    @Test
    @Order(7)
    @DisplayName("Tyre Compound - Get type")
    public void testGetType() {
        assertEquals(TyreCompound.HARD, TyreCompound.getType("Hard"));
        assertEquals(TyreCompound.MEDIUM, TyreCompound.getType("Medium"));
        assertEquals(TyreCompound.SOFT, TyreCompound.getType("Soft"));
        assertEquals(TyreCompound.QUALIFYING, TyreCompound.getType("Qualifying"));
        assertNull(TyreCompound.getType("Unknown"));
    }

    @Test
    @Order(8)
    @DisplayName("Tyre Compound - Next type")
    public void testNextType() {
        assertEquals(TyreCompound.MEDIUM, TyreCompound.HARD.getNextCompound());
        assertEquals(TyreCompound.SOFT, TyreCompound.MEDIUM.getNextCompound());
        assertEquals(TyreCompound.QUALIFYING, TyreCompound.SOFT.getNextCompound());
        assertEquals(TyreCompound.QUALIFYING, TyreCompound.QUALIFYING.getNextCompound());
    }
}