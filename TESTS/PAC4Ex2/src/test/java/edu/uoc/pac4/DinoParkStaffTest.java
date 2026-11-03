package edu.uoc.pac4;

import edu.uoc.pac4.staff.ParkStaff;
import edu.uoc.pac4.staff.Ranger;
import edu.uoc.pac4.staff.Veterinarian;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DinoParkStaffTest {

    @Test
    @Order(1)
    @Tag("minimum")
    @DisplayName("Minimum - hireStaff and getStaff")
    public void testHireStaff() {
        try {
            DinoPark park = new DinoPark("Isla Nublar") {};
            
            assertEquals(0, park.getStaff().size());
            
            ParkStaff vet = new Veterinarian("Alice", "V001", "Theropod");
            ParkStaff ranger = new Ranger("Bob", "R001", "Raptor Paddock");
            
            park.hireStaff(vet);
            assertEquals(1, park.getStaff().size());
            assertTrue(park.getStaff().contains(vet));
            
            park.hireStaff(ranger);
            assertEquals(2, park.getStaff().size());
            assertTrue(park.getStaff().contains(ranger));
            
            // Should not add null
            park.hireStaff(null);
            assertEquals(2, park.getStaff().size());
            
            // Should not add duplicate (if implemented via list.contains as seen in DinoPark)
            park.hireStaff(vet);
            assertEquals(2, park.getStaff().size());
            
        } catch (DinoParkException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
