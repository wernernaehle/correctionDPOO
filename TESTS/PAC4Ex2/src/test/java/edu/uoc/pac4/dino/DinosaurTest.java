package edu.uoc.pac4.dino;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DinosaurTest {

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields definition")
    public void checkFields() {
        int numFields = 4;

        // Check attribute fields
        assertEquals(numFields, Dinosaur.class.getDeclaredFields().length, "The class must have exactly " + numFields + " fields.");
        assertTrue(Modifier.isAbstract(Dinosaur.class.getModifiers()), "The class Dinosaur must be abstract.");
        try {
            assertTrue(Modifier.isPrivate(Dinosaur.class.getDeclaredField("id").getModifiers()), "The attribute id must be private.");
            assertFalse(Modifier.isStatic(Dinosaur.class.getDeclaredField("id").getModifiers()), "The attribute id must not be static.");
            assertFalse(Modifier.isFinal(Dinosaur.class.getDeclaredField("id").getModifiers()), "The attribute id must not be final.");
            assertEquals(String.class, Dinosaur.class.getDeclaredField("id").getType(), "The attribute id must be of type String.");

            assertTrue(Modifier.isPrivate(Dinosaur.class.getDeclaredField("age").getModifiers()), "The attribute age must be private.");
            assertFalse(Modifier.isStatic(Dinosaur.class.getDeclaredField("age").getModifiers()), "The attribute age must not be static.");
            assertFalse(Modifier.isFinal(Dinosaur.class.getDeclaredField("age").getModifiers()), "The attribute age must not be final.");
            assertEquals(int.class, Dinosaur.class.getDeclaredField("age").getType(), "The attribute age must be of type int.");

            assertTrue(Modifier.isPrivate(Dinosaur.class.getDeclaredField("weight").getModifiers()), "The attribute weight must be private.");
            assertFalse(Modifier.isStatic(Dinosaur.class.getDeclaredField("weight").getModifiers()), "The attribute weight must not be static.");
            assertFalse(Modifier.isFinal(Dinosaur.class.getDeclaredField("weight").getModifiers()), "The attribute weight must not be final.");
            assertEquals(double.class, Dinosaur.class.getDeclaredField("weight").getType(), "The attribute weight must be of type double.");

            assertTrue(Modifier.isPrivate(Dinosaur.class.getDeclaredField("dietType").getModifiers()), "The attribute dietType must be private.");
            assertFalse(Modifier.isStatic(Dinosaur.class.getDeclaredField("dietType").getModifiers()), "The attribute dietType must not be static.");
            assertFalse(Modifier.isFinal(Dinosaur.class.getDeclaredField("dietType").getModifiers()), "The attribute dietType must not be final.");
            assertEquals(DietType.class, Dinosaur.class.getDeclaredField("dietType").getType(), "The attribute dietType must be of type DietType.");
        } catch (NoSuchFieldException e) {
            fail("[ERROR] There is some problem with the definition of the attributes: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("sanity")
    @DisplayName("Sanity - Constructors definition")
    public void checkConstructor() {
        int numConstructors = 0;

        // Check no constructors, it's a abstact class
        assertEquals(numConstructors, Dinosaur.class.getConstructors().length, "The class must have exactly " + numConstructors + " constructor because it's an abstract class.");

        try {
            assertTrue(Modifier.isProtected(Dinosaur.class.getDeclaredConstructor(String.class, int.class, double.class, DietType.class).getModifiers()), "The constructor must be protected.");        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of constructors: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @Tag("sanity")
    @DisplayName("Sanity - Methods definition")
    public void checkMethods() {
        int numMethods = 9;

        // Check number of methods
        assertEquals(numMethods, Dinosaur.class.getDeclaredMethods().length, "The class must have exactly " + numMethods + " method");

        try {
            assertTrue(Modifier.isPublic(Dinosaur.class.getDeclaredMethod("getId").getModifiers()), "The method getId must be public.");            assertFalse(Modifier.isStatic(Dinosaur.class.getMethod("getId").getModifiers()), "The method getId() must not be static");
            assertFalse(Modifier.isFinal(Dinosaur.class.getMethod("getId").getModifiers()), "The method getId() must not be final");
            assertEquals(String.class, Dinosaur.class.getMethod("getId").getReturnType(), "The method getId() must return a String");

            assertTrue(Modifier.isProtected(Dinosaur.class.getDeclaredMethod("setId", String.class).getModifiers()), "The method setId must be protected.");            assertFalse(Modifier.isStatic(Dinosaur.class.getDeclaredMethod("setId", String.class).getModifiers()), "The method setId(String) must not be static");
            assertFalse(Modifier.isFinal(Dinosaur.class.getDeclaredMethod("setId", String.class).getModifiers()), "The method setId(String) must not be final");
            assertEquals(void.class, Dinosaur.class.getDeclaredMethod("setId", String.class).getReturnType(), "The method setId(String) must return void");

            assertTrue(Modifier.isPublic(Dinosaur.class.getMethod("getAge").getModifiers()), "The method getAge() must be public");
            assertFalse(Modifier.isStatic(Dinosaur.class.getMethod("getAge").getModifiers()), "The method getAge() must not be static");
            assertFalse(Modifier.isFinal(Dinosaur.class.getMethod("getAge").getModifiers()), "The method getAge() must not be final");
            assertEquals(int.class, Dinosaur.class.getMethod("getAge").getReturnType(), "The method getAge() must return a int");

            assertTrue(Modifier.isPrivate(Dinosaur.class.getDeclaredMethod("setAge", int.class).getModifiers()), "The method setAge(int) must be private");
            assertFalse(Modifier.isStatic(Dinosaur.class.getDeclaredMethod("setAge", int.class).getModifiers()), "The method setAge(int) must not be static");
            assertFalse(Modifier.isFinal(Dinosaur.class.getDeclaredMethod("setAge", int.class).getModifiers()), "The method setAge(int) must not be final");
            assertEquals(void.class, Dinosaur.class.getDeclaredMethod("setAge", int.class).getReturnType(), "The method setAge(int) must return void");

            assertTrue(Modifier.isPublic(Dinosaur.class.getMethod("getWeight").getModifiers()), "The method getWeight() must be public");
            assertFalse(Modifier.isStatic(Dinosaur.class.getMethod("getWeight").getModifiers()), "The method getWeight() must not be static");
            assertFalse(Modifier.isFinal(Dinosaur.class.getMethod("getWeight").getModifiers()), "The method getWeight() must not be final");
            assertEquals(double.class, Dinosaur.class.getMethod("getWeight").getReturnType(), "The method getWeight() must return a double");

            assertTrue(Modifier.isPrivate(Dinosaur.class.getDeclaredMethod("setWeight", double.class).getModifiers()), "The method setWeight(double) must be private");
            assertFalse(Modifier.isStatic(Dinosaur.class.getDeclaredMethod("setWeight", double.class).getModifiers()), "The method setWeight(double) must not be static");
            assertFalse(Modifier.isFinal(Dinosaur.class.getDeclaredMethod("setWeight", double.class).getModifiers()), "The method setWeight(double) must not be final");
            assertEquals(void.class, Dinosaur.class.getDeclaredMethod("setWeight", double.class).getReturnType(), "The method setWeight(double) must return void");

            assertTrue(Modifier.isPublic(Dinosaur.class.getMethod("getDietType").getModifiers()), "The method getDietType() must be public");
            assertFalse(Modifier.isStatic(Dinosaur.class.getMethod("getDietType").getModifiers()), "The method getDietType() must not be static");
            assertFalse(Modifier.isFinal(Dinosaur.class.getMethod("getDietType").getModifiers()), "The method getDietType() must not be final");
            assertEquals(DietType.class, Dinosaur.class.getMethod("getDietType").getReturnType(), "The method getDietType() must return a DietType");

            assertTrue(Modifier.isPublic(Dinosaur.class.getDeclaredMethod("setDietType", DietType.class).getModifiers()), "The method setDietType(DietType) must be public");
            assertFalse(Modifier.isStatic(Dinosaur.class.getDeclaredMethod("setDietType", DietType.class).getModifiers()), "The method setDietType(DietType) must not be static");
            assertFalse(Modifier.isFinal(Dinosaur.class.getDeclaredMethod("setDietType", DietType.class).getModifiers()), "The method setDietType(DietType) must not be final");
            assertEquals(void.class, Dinosaur.class.getDeclaredMethod("setDietType", DietType.class).getReturnType(), "The method setDietType(DietType) must return void");

        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of methods: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @Tag("minimum")
    @DisplayName("Minimum - getId")
    public void testId() {
        try {
            Dinosaur dino = new Dinosaur("DINO-01", 10, 100.0, DietType.CARNIVORE){};
            assertEquals("DINO-01", dino.getId());
            assertThrows(DinosaurException.class, () -> new Dinosaur(null, 10, 2500.0, DietType.CARNIVORE) {});
            assertThrows(DinosaurException.class, () -> new Dinosaur("REX-001", 10, 2500.0, DietType.CARNIVORE) {});
        } catch (DinosaurException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    @Order(5)
    @Tag("minimum")
    @DisplayName("Minimum - getAge")
    public void testAge() {
        try {
            // ¡NUEVO!: Usamos {} para instanciar una clase anónima
            Dinosaur dino1 = new Dinosaur("DINO-001", 15, 2500.0, DietType.CARNIVORE) {};
            assertEquals(15, dino1.getAge());

            Dinosaur dino2 = new Dinosaur("DINO-002", -5, 2500.0, DietType.CARNIVORE) {};
            assertEquals(0, dino2.getAge(), "Negative age should be set to 0");

        } catch (DinosaurException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    @Order(6)
    @Tag("minimum")
    @DisplayName("Minimum - getWeight")
    public void testWeight() {
        try {
            Dinosaur dino1 = new Dinosaur("DINO-001", 10, 5000.5, DietType.CARNIVORE) {};
            assertEquals(5000.5, dino1.getWeight());

            Dinosaur dino2 = new Dinosaur("DINO-002", 10, -150.0, DietType.CARNIVORE) {};
            assertEquals(0.0, dino2.getWeight(), "Negative weight should be set to 0.0");
        } catch (DinosaurException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    @Order(7)
    @Tag("minimum")
    @DisplayName("Minimum - testDietType validation")
    public void testDietType() {
        try {
            Dinosaur dino1 = new Dinosaur("DINO-001", 10, 2500.0, DietType.HERBIVORE) {};
            assertEquals(DietType.HERBIVORE, dino1.getDietType());
        } catch (DinosaurException e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    @Order(8)
    @Tag("advanced")
    @DisplayName("Advanced - Exceptions")
    public void testExceptions() {
        Exception exception = assertThrows(DinosaurException.class, () -> new Dinosaur(null, 10, 100.0, DietType.CARNIVORE){});
        assertEquals(DinosaurException.INVALID_DINOSAUR_ID, exception.getMessage());

        exception = assertThrows(DinosaurException.class, () -> new Dinosaur("INVALID", 10, 100.0, DietType.CARNIVORE){});
        assertEquals(DinosaurException.INVALID_DINOSAUR_ID, exception.getMessage());

        exception = assertThrows(DinosaurException.class, () -> new Dinosaur("DINO-01", 10, 100.0, null){});
        assertEquals(DinosaurException.INVALID_DIET_TYPE, exception.getMessage());
    }

    @Test
    @Order(9)
    @Tag("advanced")
    @DisplayName("Advanced - toString")
    public void testToString() {
        try {
            // Caso 1: Valores normales
            Dinosaur dino = new Dinosaur("DINO-001", 10, 2500.0, DietType.CARNIVORE){};
            String expected = "{ \"id\": \"DINO-001\", \"age\": 10, \"weight\": 2500.0, \"dietType\": \"Carnivore\" }";
            assertEquals(
                    normalizeJson(expected),
                    normalizeJson(dino.toString()),
                    "The toString() method output is incorrect."
            );

            // Caso 2: Edad negativa (debe corregirse a 0)
            dino = new Dinosaur("DINO-100", -5, 100.0, DietType.HERBIVORE){};
            expected = "{ \"id\": \"DINO-100\", \"age\": 0, \"weight\": 100.0, \"dietType\": \"Herbivore\" }";
            assertEquals(
                    normalizeJson(expected),
                    normalizeJson(dino.toString()),
                    "The toString() method output is incorrect."
            );

            // Caso 3: Peso negativo (debe corregirse a 0.0)
            dino = new Dinosaur("DINO-200", 15, -50.0, DietType.OMNIVORE){};
            expected = "{ \"id\": \"DINO-200\", \"age\": 15, \"weight\": 0.0, \"dietType\": \"Omnivore\" }";
            assertEquals(
                    normalizeJson(expected),
                    normalizeJson(dino.toString()),
                    "The toString() method output is incorrect."
            );
        } catch (DinosaurException e) {
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