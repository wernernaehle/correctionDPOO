package edu.uoc.pac3;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RacingTeamTest {

    @BeforeEach
    public void resetNextId() throws Exception {
        // Reiniciamos el contador estático nextId de RaceCar antes de cada test
        // Esto es crucial para que los test de add/remove/find funcionen de manera predecible.
        Field field = RaceCar.class.getDeclaredField("nextId");
        field.setAccessible(true);
        field.setInt(null, 1);
    }

    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Sanity - Fields definition")
    public void checkFieldsSanity() {
        // En total debemos tener 19 campos: 4 constantes numéricas, 8 mensajes de error,
        // 4 atributos privados, MAX_CARS, numCars y el array de cars.
        assertEquals(19, RacingTeam.class.getDeclaredFields().length);

        try {
            // Constantes numéricas
            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("MIN_NAME_LENGTH").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("MIN_NAME_LENGTH").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("MIN_NAME_LENGTH").getModifiers()));
            assertEquals(int.class, RacingTeam.class.getDeclaredField("MIN_NAME_LENGTH").getType());

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("MAX_NAME_LENGTH").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("MAX_NAME_LENGTH").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("MAX_NAME_LENGTH").getModifiers()));
            assertEquals(int.class, RacingTeam.class.getDeclaredField("MAX_NAME_LENGTH").getType());

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("CODE_LENGTH").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("CODE_LENGTH").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("CODE_LENGTH").getModifiers()));
            assertEquals(int.class, RacingTeam.class.getDeclaredField("CODE_LENGTH").getType());

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("MIN_BUDGET").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("MIN_BUDGET").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("MIN_BUDGET").getModifiers()));
            assertEquals(double.class, RacingTeam.class.getDeclaredField("MIN_BUDGET").getType());

            // Mensajes de error (Constantes públicas)
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredField("INVALID_NAME").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("INVALID_NAME").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("INVALID_NAME").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("INVALID_NAME").getType());

            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredField("INVALID_CODE").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("INVALID_CODE").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("INVALID_CODE").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("INVALID_CODE").getType());

            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredField("INVALID_LAST_RACE_DATE").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("INVALID_LAST_RACE_DATE").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("INVALID_LAST_RACE_DATE").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("INVALID_LAST_RACE_DATE").getType());

            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredField("INVALID_BUDGET").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("INVALID_BUDGET").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("INVALID_BUDGET").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("INVALID_BUDGET").getType());

            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredField("CAR_NULL").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("CAR_NULL").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("CAR_NULL").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("CAR_NULL").getType());

            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredField("TEAM_FULL").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("TEAM_FULL").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("TEAM_FULL").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("TEAM_FULL").getType());

            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredField("CAR_ALREADY_EXISTS").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("CAR_ALREADY_EXISTS").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("CAR_ALREADY_EXISTS").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("CAR_ALREADY_EXISTS").getType());

            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredField("CAR_NOT_FOUND").getModifiers()));
            assertTrue(Modifier.isStatic(RacingTeam.class.getDeclaredField("CAR_NOT_FOUND").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("CAR_NOT_FOUND").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("CAR_NOT_FOUND").getType());

            // Atributos de clase
            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("name").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("name").getType());

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("code").getModifiers()));
            assertEquals(String.class, RacingTeam.class.getDeclaredField("code").getType());

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("lastRaceDate").getModifiers()));
            assertEquals(LocalDate.class, RacingTeam.class.getDeclaredField("lastRaceDate").getType());

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("budget").getModifiers()));
            assertEquals(double.class, RacingTeam.class.getDeclaredField("budget").getType());

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("MAX_CARS").getModifiers()));
            assertTrue(Modifier.isFinal(RacingTeam.class.getDeclaredField("MAX_CARS").getModifiers()));
            assertEquals(int.class, RacingTeam.class.getDeclaredField("MAX_CARS").getType());

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("numCars").getModifiers()));
            assertEquals(int.class, RacingTeam.class.getDeclaredField("numCars").getType());

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredField("cars").getModifiers()));
            assertEquals(RaceCar[].class, RacingTeam.class.getDeclaredField("cars").getType());

        } catch (NoSuchFieldException e) {
            fail("[ERROR] There is some problem with the definition of the fields: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @Tag("sanity")
    @DisplayName("Sanity - Methods definition")
    public void checkMethodsSanity() {
        long publicMethods = Arrays.stream(RacingTeam.class.getDeclaredMethods()).filter(m -> Modifier.isPublic(m.getModifiers())).count();
        long privateMethods = Arrays.stream(RacingTeam.class.getDeclaredMethods()).filter(m -> Modifier.isPrivate(m.getModifiers())).count();

        // 14 public methods (getters + arrays/business logic)
        assertEquals(14, publicMethods);
        // 4 private methods (setters: setName, setCode, setLastRaceDate, setBudget)
        assertEquals(4, privateMethods);

        try {
            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredMethod("setName", String.class).getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("getName").getModifiers()));

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredMethod("setCode", String.class).getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("getCode").getModifiers()));

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredMethod("setLastRaceDate", LocalDate.class).getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("getLastRaceDate").getModifiers()));

            assertTrue(Modifier.isPrivate(RacingTeam.class.getDeclaredMethod("setBudget", double.class).getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("getBudget").getModifiers()));

            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("getNumCars").getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("getMaxCars").getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("getCars").getModifiers()));

            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("getDaysSinceLastRace").getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("findCar", RaceCar.class).getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("findFirstEmptySlot").getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("containsCar", RaceCar.class).getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("addCar", RaceCar.class).getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("removeCar", RaceCar.class).getModifiers()));
            assertTrue(Modifier.isPublic(RacingTeam.class.getDeclaredMethod("getAverageAerodynamics").getModifiers()));

        } catch (NoSuchMethodException e) {
            fail("[ERROR] There is some problem with the definition of the methods: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @Tag("minimum")
    @DisplayName("Minimum - Name constraints and formatting")
    public void testCreateRacingTeamName() {
        // Null
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new RacingTeam(null, "RBR", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_NAME, exception.getMessage());

        // Empty & Whitespace only
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam("", "RBR", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_NAME, exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam("   ", "RBR", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_NAME, exception.getMessage());

        // Too short (< 3)
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam("AB", "RBR", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_NAME, exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam(" AB ", "RBR", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_NAME, exception.getMessage());

        // Too long (> 50)
        String longName = "Red Bull Racing Honda Red Bull Racing Honda Red Bul"; // 51 characters
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam(longName, "RBR", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_NAME, exception.getMessage());

        // Valid lengths and trim verification
        RacingTeam team1 = new RacingTeam("Red Bull Racing", "RBR", LocalDate.now(), 1000000.0, 5);
        assertEquals("Red Bull Racing", team1.getName());

        RacingTeam team2 = new RacingTeam("   McLaren   ", "MCL", LocalDate.now(), 1000000.0, 5);
        assertEquals("McLaren", team2.getName());

        String exactly50 = "Red Bull Racing Honda Red Bull Racing Honda Red Bu"; // 50 chars exactly
        RacingTeam team3 = new RacingTeam(exactly50, "RBR", LocalDate.now(), 1000000.0, 5);
        assertEquals(exactly50, team3.getName());
    }

    @Test
    @Order(4)
    @Tag("minimum")
    @DisplayName("Minimum - Code constraints and formatting")
    public void testCreateRacingTeamCode() {
        // Null
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new RacingTeam("Ferrari", null, LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_CODE, exception.getMessage());

        // Wrong length
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam("Ferrari", "FE", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_CODE, exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam("Ferrari", "FERR", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_CODE, exception.getMessage());

        // Wrong characters (numbers, symbols, spaces, lowercase)
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam("Ferrari", "F1R", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_CODE, exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam("Ferrari", "F-R", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_CODE, exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam("Ferrari", "fer", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_CODE, exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> new RacingTeam("Ferrari", " FE ", LocalDate.now(), 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_CODE, exception.getMessage());

        // Valid code
        RacingTeam team = new RacingTeam("Ferrari", "FER", LocalDate.now(), 1000000.0, 5);
        assertEquals("FER", team.getCode());
    }

    @Test
    @Order(5)
    @Tag("minimum")
    @DisplayName("Minimum - Last Race Date constraints")
    public void testCreateRacingTeamDate() {
        // Future date
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new RacingTeam("Aston Martin", "AST", tomorrow, 1000000.0, 5));
        assertEquals(RacingTeam.INVALID_LAST_RACE_DATE, exception.getMessage());

        // Null date (Valid, never raced)
        RacingTeam team1 = new RacingTeam("Aston Martin", "AST", null, 1000000.0, 5);
        assertNull(team1.getLastRaceDate());

        // Past date and Today (Valid)
        LocalDate yesterday = LocalDate.now().minusDays(1);
        RacingTeam team2 = new RacingTeam("Aston Martin", "AST", yesterday, 1000000.0, 5);
        assertEquals(yesterday, team2.getLastRaceDate());

        RacingTeam team3 = new RacingTeam("Aston Martin", "AST", LocalDate.now(), 1000000.0, 5);
        assertEquals(LocalDate.now(), team3.getLastRaceDate());
    }

    @Test
    @Order(6)
    @Tag("minimum")
    @DisplayName("Minimum - Budget constraints")
    public void testCreateRacingTeamBudget() {
        // Less than minimum
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new RacingTeam("Williams", "WIL", LocalDate.now(), 499999.9, 5));
        assertEquals(RacingTeam.INVALID_BUDGET, exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> new RacingTeam("Williams", "WIL", LocalDate.now(), -100.0, 5));
        assertEquals(RacingTeam.INVALID_BUDGET, exception.getMessage());

        // Valid bounds
        RacingTeam team1 = new RacingTeam("Williams", "WIL", LocalDate.now(), 500000.0, 5);
        assertEquals(500000.0, team1.getBudget(), 0.001);

        RacingTeam team2 = new RacingTeam("Williams", "WIL", LocalDate.now(), 10000000.5, 5);
        assertEquals(10000000.5, team2.getBudget(), 0.001);
    }

    @Test
    @Order(7)
    @Tag("minimum")
    @DisplayName("Minimum - Days Since Last Race")
    public void testGetDaysSinceLastRace() {
        RacingTeam newTeam = new RacingTeam("Haas", "HAA", null, 1000000.0, 5);
        assertEquals(Integer.MAX_VALUE, newTeam.getDaysSinceLastRace());

        RacingTeam teamToday = new RacingTeam("Haas", "HAA", LocalDate.now(), 1000000.0, 5);
        assertEquals(0, teamToday.getDaysSinceLastRace());

        RacingTeam teamPast = new RacingTeam("Haas", "HAA", LocalDate.now().minusDays(15), 1000000.0, 5);
        assertEquals(15, teamPast.getDaysSinceLastRace());
    }

    @Test
    @Order(8)
    @Tag("advanced")
    @DisplayName("Advanced - Initial State and Array slots")
    public void testInitialStateAndSlots() {
        RacingTeam team = new RacingTeam("AlphaTauri", "ALP", LocalDate.now(), 1000000.0, 3);

        assertEquals(0, team.getNumCars());
        assertEquals(3, team.getMaxCars());
        assertEquals(3, team.getCars().length);

        // Todas las posiciones deberian ser null inicialmente
        for (int i = 0; i < team.getMaxCars(); i++) {
            assertNull(team.getCars()[i]);
        }

        // Primer hueco libre debe ser el índice 0
        assertEquals(0, team.findFirstEmptySlot());
    }

    @Test
    @Order(9)
    @Tag("advanced")
    @DisplayName("Advanced - Contains Car and Find Car")
    public void testContainsAndFindCar() {
        RacingTeam team = new RacingTeam("Kick Sauber", "KIK", LocalDate.now(), 1000000.0, 3);
        RaceCar car1 = new RaceCar("C44", 2024, 110.0, "Stake");
        RaceCar car2 = new RaceCar("C43", 2023, 105.0, "Stake");

        // Excepciones por null
        NullPointerException exception = assertThrows(NullPointerException.class, () -> team.containsCar(null));
        assertEquals(RacingTeam.CAR_NULL, exception.getMessage());
        exception = assertThrows(NullPointerException.class, () -> team.findCar(null));
        assertEquals(RacingTeam.CAR_NULL, exception.getMessage());

        // Coche no añadido todavía
        assertFalse(team.containsCar(car1));
        assertEquals(-1, team.findCar(car1));

        // Añadimos
        team.addCar(car1);
        assertTrue(team.containsCar(car1));
        assertEquals(0, team.findCar(car1));

        // Añadimos segundo coche y comprobamos
        team.addCar(car2);
        assertTrue(team.containsCar(car2));
        assertEquals(1, team.findCar(car2));
    }

    @Test
    @Order(10)
    @Tag("advanced")
    @DisplayName("Advanced - Add Car (Priorities and Exceptions)")
    public void testAddCar() {
        RacingTeam team = new RacingTeam("McLaren", "MCL", LocalDate.now(), 1000000.0, 2);
        RaceCar car1 = new RaceCar("MCL38", 2024, 115.0, "Google");
        RaceCar car2 = new RaceCar("MCL60", 2023, 110.0, "Google");
        RaceCar car3 = new RaceCar("MCL36", 2022, 100.0, "Google");

        // 1. Inserciones correctas
        team.addCar(car1);
        assertEquals(1, team.getNumCars());
        assertEquals(car1, team.getCars()[0]);
        assertEquals(1, team.findFirstEmptySlot());

        team.addCar(car2);
        assertEquals(2, team.getNumCars());
        assertEquals(car2, team.getCars()[1]);
        assertEquals(-1, team.findFirstEmptySlot()); // Array lleno

        // 2. Exception: Equipo Lleno (Prioridad 1)
        IllegalStateException fullException = assertThrows(IllegalStateException.class, () -> team.addCar(car3));
        assertEquals(RacingTeam.TEAM_FULL, fullException.getMessage());

        // Creamos otro equipo con hueco para probar prioridades 2 y 3
        RacingTeam team2 = new RacingTeam("McLaren", "MCL", LocalDate.now(), 1000000.0, 5);
        team2.addCar(car1);

        // 3. Exception: Null param (Prioridad 2)
        NullPointerException nullException = assertThrows(NullPointerException.class, () -> team2.addCar(null));
        assertEquals(RacingTeam.CAR_NULL, nullException.getMessage());

        // 4. Exception: Coche duplicado (Prioridad 3)
        IllegalArgumentException duplicateException = assertThrows(IllegalArgumentException.class, () -> team2.addCar(car1));
        assertEquals(RacingTeam.CAR_ALREADY_EXISTS, duplicateException.getMessage());
    }

    @Test
    @Order(11)
    @Tag("advanced")
    @DisplayName("Advanced - Remove Car and Slot Management")
    public void testRemoveCar() {
        RacingTeam team = new RacingTeam("Mercedes", "MER", LocalDate.now(), 5000000.0, 3);
        RaceCar car1 = new RaceCar("W15", 2024, 120.0, "Petronas");
        RaceCar car2 = new RaceCar("W14", 2023, 115.0, "Petronas");
        RaceCar car3 = new RaceCar("W13", 2022, 110.0, "Petronas");

        // Null parameter
        NullPointerException nullException = assertThrows(NullPointerException.class, () -> team.removeCar(null));
        assertEquals(RacingTeam.CAR_NULL, nullException.getMessage());

        // Remove from empty team
        IllegalArgumentException notFoundException = assertThrows(IllegalArgumentException.class, () -> team.removeCar(car1));
        assertEquals(RacingTeam.CAR_NOT_FOUND, notFoundException.getMessage());

        team.addCar(car1);
        team.addCar(car2);
        team.addCar(car3);
        assertEquals(3, team.getNumCars());

        // Remove car in the middle (car2 at index 1)
        team.removeCar(car2);
        assertEquals(2, team.getNumCars());
        assertFalse(team.containsCar(car2));
        assertNull(team.getCars()[1]);
        assertEquals(1, team.findFirstEmptySlot()); // The empty slot must be index 1 now

        // Remove car not found anymore
        assertThrows(IllegalArgumentException.class, () -> team.removeCar(car2));

        // Add a new car, should go to index 1 (the empty slot)
        RaceCar car4 = new RaceCar("W12", 2021, 105.0, "Petronas");
        team.addCar(car4);
        assertEquals(3, team.getNumCars());
        assertEquals(car4, team.getCars()[1]);
        assertEquals(-1, team.findFirstEmptySlot());
    }

    @Test
    @Order(12)
    @Tag("advanced")
    @DisplayName("Advanced - Average Aerodynamics")
    public void testAverageAerodynamics() {
        RacingTeam team = new RacingTeam("Red Bull", "RBR", LocalDate.now(), 5000000.0, 5);

        // Zero cars
        assertEquals(0.0, team.getAverageAerodynamics(), 0.001);

        RaceCar car1 = new RaceCar("RB20", 2024, 150.0, "Oracle");
        RaceCar car2 = new RaceCar("RB19", 2023, 140.0, "Oracle");
        RaceCar car3 = new RaceCar("RB18", 2022, 130.0, "Oracle");

        team.addCar(car1);
        assertEquals(150.0, team.getAverageAerodynamics(), 0.001);

        team.addCar(car2);
        // (150 + 140) / 2 = 145.0
        assertEquals(145.0, team.getAverageAerodynamics(), 0.001);

        team.addCar(car3);
        // (150 + 140 + 130) / 3 = 140.0
        assertEquals(140.0, team.getAverageAerodynamics(), 0.001);

        // Test with a hole in the array
        team.removeCar(car2);
        // Now array has car1 and car3, numCars is 2. (150 + 130) / 2 = 140.0
        assertEquals(140.0, team.getAverageAerodynamics(), 0.001);
    }
}