package edu.uoc.pac4;

import edu.uoc.pac4.dino.DietType;
import edu.uoc.pac4.dino.Dinosaur;
import edu.uoc.pac4.dino.DinosaurException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DinoParkTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields definition")
    public void checkFields() {
        int numFields = 3;

        // Check attribute fields
        assertEquals(numFields, DinoPark.class.getDeclaredFields().length, "The class must have exactly " + numFields + " fields.");

        try {
            assertTrue(Modifier.isPrivate(DinoPark.class.getDeclaredField("MAX_DINOSAURS").getModifiers()), "The attribute MAX_DINOSAURS must be private.");
            assertTrue(Modifier.isStatic(DinoPark.class.getDeclaredField("MAX_DINOSAURS").getModifiers()), "The attribute MAX_DINOSAURS must be static.");
            assertTrue(Modifier.isFinal(DinoPark.class.getDeclaredField("MAX_DINOSAURS").getModifiers()), "The attribute MAX_DINOSAURS must be final.");
            assertEquals(int.class, DinoPark.class.getDeclaredField("MAX_DINOSAURS").getType(), "The attribute MAX_DINOSAURS must be of type int.");

            assertTrue(Modifier.isPrivate(DinoPark.class.getDeclaredField("parkName").getModifiers()), "The attribute parkName must be private.");
            assertFalse(Modifier.isStatic(DinoPark.class.getDeclaredField("parkName").getModifiers()), "The attribute parkName must not be static.");
            assertFalse(Modifier.isFinal(DinoPark.class.getDeclaredField("parkName").getModifiers()), "The attribute parkName must not be final.");
            assertEquals(String.class, DinoPark.class.getDeclaredField("parkName").getType(), "The attribute parkName must be of type String.");

            assertTrue(Modifier.isPrivate(DinoPark.class.getDeclaredField("dinosaurs").getModifiers()), "The attribute dinosaurs must be private.");
            assertFalse(Modifier.isStatic(DinoPark.class.getDeclaredField("dinosaurs").getModifiers()), "The attribute dinosaurs must not be static.");
            assertFalse(Modifier.isFinal(DinoPark.class.getDeclaredField("dinosaurs").getModifiers()), "The attribute dinosaurs must not be final.");
            assertEquals(ArrayList.class, DinoPark.class.getDeclaredField("dinosaurs").getType(), "The attribute dinosaurs must be of type List.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] There is some problem with the definition of the attributes: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("sanity")
    @DisplayName("Sanity - Constructors definition")
    public void checkConstructors() {
        int numConstructors = 1;

        // Check number of constructors
        assertEquals(numConstructors, DinoPark.class.getConstructors().length, "The class must have exactly " + numConstructors + " constructor");

        try {
            assertTrue(Modifier.isPublic(DinoPark.class.getConstructor(String.class).getModifiers()), "The constructor DinoPark(String) must be public");
        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of constructors: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @Tag("sanity")
    @DisplayName("Sanity - Methods definition")
    public void checkMethods() {
        int numMethods = 7;

        // Check number of methods
        assertEquals(numMethods, DinoPark.class.getDeclaredMethods().length, "The class must have exactly " + numMethods + " methods");

        try {
            assertTrue(Modifier.isPublic(DinoPark.class.getDeclaredMethod("getParkName").getModifiers()), "The method getParkName() must be public");
            assertFalse(Modifier.isStatic(DinoPark.class.getDeclaredMethod("getParkName").getModifiers()), "The method getParkName() must not be static");
            assertFalse(Modifier.isFinal(DinoPark.class.getDeclaredMethod("getParkName").getModifiers()), "The method getParkName() must not be final");
            assertEquals(String.class, DinoPark.class.getDeclaredMethod("getParkName").getReturnType(), "The method getParkName() must return a String");

            assertTrue(Modifier.isPublic(DinoPark.class.getDeclaredMethod("setParkName", String.class).getModifiers()), "The method setParkName(String) must be public");
            assertFalse(Modifier.isStatic(DinoPark.class.getDeclaredMethod("setParkName", String.class).getModifiers()), "The method setParkName(String) must not be static");
            assertFalse(Modifier.isFinal(DinoPark.class.getDeclaredMethod("setParkName", String.class).getModifiers()), "The method setParkName(String) must not be final");
            assertEquals(void.class, DinoPark.class.getDeclaredMethod("setParkName", String.class).getReturnType(), "The method setParkName(String) must return void");

            assertTrue(Modifier.isPublic(DinoPark.class.getDeclaredMethod("getDinosaurs").getModifiers()), "The method getDinosaurs() must be public");
            assertFalse(Modifier.isStatic(DinoPark.class.getDeclaredMethod("getDinosaurs").getModifiers()), "The method getDinosaurs() must not be static");
            assertFalse(Modifier.isFinal(DinoPark.class.getDeclaredMethod("getDinosaurs").getModifiers()), "The method getDinosaurs() must not be final");
            assertEquals(List.class, DinoPark.class.getDeclaredMethod("getDinosaurs").getReturnType(), "The method getDinosaurs() must return a List");

            assertTrue(Modifier.isPublic(DinoPark.class.getDeclaredMethod("getMaxDinosaurs").getModifiers()), "The method getMaxDinosaurs() must be public");
            assertFalse(Modifier.isStatic(DinoPark.class.getDeclaredMethod("getMaxDinosaurs").getModifiers()), "The method getMaxDinosaurs() must not be static");
            assertFalse(Modifier.isFinal(DinoPark.class.getDeclaredMethod("getMaxDinosaurs").getModifiers()), "The method getMaxDinosaurs() must not be final");
            assertEquals(int.class, DinoPark.class.getDeclaredMethod("getMaxDinosaurs").getReturnType(), "The method getMaxDinosaurs() must return a int");

            assertTrue(Modifier.isPublic(DinoPark.class.getDeclaredMethod("addDinosaur", Dinosaur.class).getModifiers()), "The method addDinosaur(Dinosaur) must be public");
            assertFalse(Modifier.isStatic(DinoPark.class.getDeclaredMethod("addDinosaur", Dinosaur.class).getModifiers()), "The method addDinosaur(Dinosaur) must not be static");
            assertFalse(Modifier.isFinal(DinoPark.class.getDeclaredMethod("addDinosaur", Dinosaur.class).getModifiers()), "The method addDinosaur(Dinosaur) must not be final");
            assertEquals(boolean.class, DinoPark.class.getDeclaredMethod("addDinosaur", Dinosaur.class).getReturnType(), "The method addDinosaur(Dinosaur) must return a boolean");

            assertTrue(Modifier.isPublic(DinoPark.class.getDeclaredMethod("removeDinosaur", Dinosaur.class).getModifiers()), "The method removeDinosaur(Dinosaur) must be public");
            assertFalse(Modifier.isStatic(DinoPark.class.getDeclaredMethod("removeDinosaur", Dinosaur.class).getModifiers()), "The method removeDinosaur(Dinosaur) must not be static");
            assertFalse(Modifier.isFinal(DinoPark.class.getDeclaredMethod("removeDinosaur", Dinosaur.class).getModifiers()), "The method removeDinosaur(Dinosaur) must not be final");
            assertEquals(boolean.class, DinoPark.class.getDeclaredMethod("removeDinosaur", Dinosaur.class).getReturnType(), "The method removeDinosaur(Dinosaur) must return a boolean");

        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of methods: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @Tag("minimum")
    @DisplayName("Minimum - getParkName")
    public void testGetParkName() {
        try {
            DinoPark park = new DinoPark("Jurassic Park");
            assertEquals("Jurassic Park", park.getParkName());
            assertEquals(10, park.getMaxDinosaurs());
            assertTrue(park.getDinosaurs().isEmpty());
        } catch (DinoParkException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    @Order(5)
    @Tag("minimum")
    @DisplayName("Minimum - add and remove Dinosaur logic")
    public void testAddAndRemoveDinosaur() {
        try {
            DinoPark park = new DinoPark("Jurassic Park");
            Dinosaur dino1 = new Dinosaur("DINO-001", 10, 50.0, DietType.CARNIVORE);
            Dinosaur dino2 = new Dinosaur("DINO-002", 15, 100.0, DietType.HERBIVORE);

            // Add dino
            assertTrue(park.addDinosaur(dino1));
            assertEquals(1, park.getDinosaurs().size());

            // Exception: exists
            Exception e1 = assertThrows(DinoParkException.class, () -> park.addDinosaur(dino1));
            assertEquals(DinoParkException.DINOSAUR_ALREADY_EXISTS, e1.getMessage());

            // Exception: null dino
            Exception e2 = assertThrows(DinoParkException.class, () -> park.addDinosaur(null));
            assertEquals(DinoParkException.NULL_DINOSAUR, e2.getMessage());

            // Exception: dinos correct
            assertTrue(park.removeDinosaur(dino1));
            assertEquals(0, park.getDinosaurs().size());

            // Exception: not tfound
            Exception e3 = assertThrows(DinoParkException.class, () -> park.removeDinosaur(dino2));
            assertEquals(DinoParkException.DINOSAUR_NOT_FOUND, e3.getMessage());

        } catch (DinoParkException | DinosaurException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    @Order(6)
    @Tag("advanced")
    @DisplayName("Advanced - toString")
    public void testToString() {
        try {
            DinoPark park = new DinoPark("Jurassic Park");
            String expectedJsonEmpty = "{ \"parkName\": \"Jurassic Park\", \"dinosaurs\": [] }";
            assertEquals(normalizeJson(expectedJsonEmpty), normalizeJson(park.toString()), "The JSON representation of the empty DinoPark is incorrect.");

            Dinosaur dino1 = new Dinosaur("DINO-1", 10, 50.0, DietType.CARNIVORE);
            Dinosaur dino2 = new Dinosaur("DINO-2", 15, 100.0, DietType.HERBIVORE);
            park.addDinosaur(dino1);
            park.addDinosaur(dino2);

            String expectedJsonWithDinos = "{ \"parkName\": \"Jurassic Park\", \"dinosaurs\": [ " +
                    dino1 + ", " +
                    dino2 +
                    " ] }";
            assertEquals(normalizeJson(expectedJsonWithDinos), normalizeJson(park.toString()), "The JSON representation of the DinoPark with dinosaurs is incorrect.");
        } catch (DinoParkException | DinosaurException e) {
            fail("Exception should not be thrown");
        }
    }

    private String normalizeJson(String json) {
        boolean insideQuotes = false;
        StringBuilder result = new StringBuilder();
        for (char c : json.toCharArray()) {
            if (c == '\"') {
                insideQuotes = !insideQuotes;
                result.append(c);
                continue;
            }
            if (!insideQuotes && Character.isWhitespace(c)) {
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }
}