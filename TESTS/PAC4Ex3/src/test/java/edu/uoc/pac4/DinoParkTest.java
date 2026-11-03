package edu.uoc.pac4;

import edu.uoc.pac4.dino.DietType;
import edu.uoc.pac4.dino.Dinosaur;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DinoParkTest {

    @Test
    @Order(1)
    @DisplayName("getDinosaursByDietType")
    public void testGetDinosaursByDietType() {
        try {
            DinoPark park = new DinoPark("Jurassic Park"){};

            // Creamos los dinosaurios (usando {} para instanciar la clase abstracta)
            Dinosaur dino1 = new Dinosaur("DINO-001", 5, 100.4, DietType.CARNIVORE) {};
            Dinosaur dino2 = new Dinosaur("DINO-002", 10, 200.8, DietType.HERBIVORE) {};
            Dinosaur dino3 = new Dinosaur("DINO-003", 8, 1500.0, DietType.CARNIVORE) {};
            Dinosaur dino4 = new Dinosaur("DINO-004", 15, 2500.0, DietType.CARNIVORE) {};
            Dinosaur dino5 = new Dinosaur("DINO-005", 20, 3000.0, DietType.OMNIVORE) {};
            Dinosaur dino6 = new Dinosaur("DINO-006", 12, 1200.0, DietType.HERBIVORE) {};
            Dinosaur dino7 = new Dinosaur("DINO-007", 3, 15.0, DietType.OMNIVORE) {};

            park.addDinosaur(dino1);
            park.addDinosaur(dino2);
            park.addDinosaur(dino3);
            park.addDinosaur(dino4);
            park.addDinosaur(dino5);
            park.addDinosaur(dino6);
            park.addDinosaur(dino7);

            // Test CARNIVORE
            List<Dinosaur> carnivores = park.getDinosaursByDietType(DietType.CARNIVORE);
            assertEquals(3, carnivores.size(), "There should be 3 CARNIVORE dinosaurs");
            assertTrue(carnivores.contains(dino1), "List should contain dino1");
            assertTrue(carnivores.contains(dino3), "List should contain dino3");
            assertTrue(carnivores.contains(dino4), "List should contain dino4");

            // Test HERBIVORE
            List<Dinosaur> herbivores = park.getDinosaursByDietType(DietType.HERBIVORE);
            assertEquals(2, herbivores.size(), "There should be 2 HERBIVORE dinosaurs");
            assertTrue(herbivores.contains(dino2), "List should contain dino2");
            assertTrue(herbivores.contains(dino6), "List should contain dino6");

            // Test OMNIVORE
            List<Dinosaur> omnivores = park.getDinosaursByDietType(DietType.OMNIVORE);
            assertEquals(2, omnivores.size(), "There should be 2 OMNIVORE dinosaurs");
            assertTrue(omnivores.contains(dino5), "List should contain dino5");
            assertTrue(omnivores.contains(dino7), "List should contain dino7");

            // Test Borrado 1
            park.removeDinosaur(dino4);
            carnivores = park.getDinosaursByDietType(DietType.CARNIVORE);
            assertEquals(2, carnivores.size(), "There should be 2 CARNIVORE dinosaurs after removing dino4");
            assertTrue(carnivores.contains(dino1), "List should contain dino1");
            assertTrue(carnivores.contains(dino3), "List should contain dino3");

            // Test Borrado 2
            park.removeDinosaur(dino1);
            park.removeDinosaur(dino3);
            carnivores = park.getDinosaursByDietType(DietType.CARNIVORE);
            assertEquals(0, carnivores.size(), "There should be 0 CARNIVORE dinosaurs after removing dino1 and dino3");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("getTotalWeight")
    public void testGetTotalWeight() {
        try {
            DinoPark park = new DinoPark("Jurassic Park"){};

            double totalWeight = park.getTotalWeight();
            assertEquals(0.0, totalWeight, 0.001, "Total weight should be 0.0 for an empty park");

            Dinosaur dino1 = new Dinosaur("DINO-001", 5, 100.4, DietType.CARNIVORE) {};
            Dinosaur dino2 = new Dinosaur("DINO-002", 10, 200.8, DietType.HERBIVORE) {};
            Dinosaur dino3 = new Dinosaur("DINO-003", 8, 1500.0, DietType.CARNIVORE) {};
            Dinosaur dino4 = new Dinosaur("DINO-004", 15, 2500.0, DietType.CARNIVORE) {};
            Dinosaur dino5 = new Dinosaur("DINO-005", 20, 3000.0, DietType.OMNIVORE) {};
            Dinosaur dino6 = new Dinosaur("DINO-006", 12, 1200.0, DietType.HERBIVORE) {};
            Dinosaur dino7 = new Dinosaur("DINO-007", 3, 15.0, DietType.OMNIVORE) {};

            park.addDinosaur(dino1);
            park.addDinosaur(dino2);
            park.addDinosaur(dino3);
            park.addDinosaur(dino4);
            park.addDinosaur(dino5);
            park.addDinosaur(dino6);
            park.addDinosaur(dino7);

            // Suma total esperada: 100.4 + 200.8 + 1500.0 + 2500.0 + 3000.0 + 1200.0 + 15.0 = 8516.2
            totalWeight = park.getTotalWeight();
            assertEquals(8516.2, totalWeight, 0.001, "Total weight should be 8516.2");

            // Borramos uno y recalculamos
            park.removeDinosaur(dino1);
            totalWeight = park.getTotalWeight();
            assertEquals(8415.8, totalWeight, 0.001, "Total weight should be 8415.8 after removing dino1");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}