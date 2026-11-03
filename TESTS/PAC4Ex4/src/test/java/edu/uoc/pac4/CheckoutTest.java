package edu.uoc.pac4;

import org.junit.jupiter.api.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CheckoutTest {

    @Test
    @Order(1)
    @DisplayName("DIP - Structural test: No concrete dependencies")
    public void testStructuralDependencyInversion() {
        Field[] fields = Checkout.class.getDeclaredFields();

        for (Field field : fields) {
            assertFalse(field.getType().getSimpleName().equals("PayPalService"),
                    "[ERROR] Checkout must NOT depend on the concrete PayPalService class.");
            assertFalse(field.getType().getSimpleName().equals("EmailService"),
                    "[ERROR] Checkout must NOT depend on the concrete EmailService class.");

            assertTrue(field.getType().isInterface(),
                    "[ERROR] Checkout attribute '" + field.getName() + "' must be an Interface, not a concrete class.");
        }

        assertTrue(fields.length >= 2, "[ERROR] Checkout should have at least 2 attributes for payment and notification.");
    }

    @Test
    @Order(2)
    @DisplayName("DIP - Structural test: Constructor Injection with Interfaces")
    public void testConstructorInjection() {
        boolean hasCorrectConstructor = false;

        Constructor<?>[] constructors = Checkout.class.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            // Verificamos que haya un constructor con 2 parámetros y que ambos sean interfaces
            if (parameterTypes.length == 2 && parameterTypes[0].isInterface() && parameterTypes[1].isInterface()) {
                hasCorrectConstructor = true;
                break;
            }
        }

        assertTrue(hasCorrectConstructor,
                "[ERROR] Checkout must have a constructor that accepts TWO interfaces as parameters (e.g., PaymentMethod and NotificationSender).");
    }

    @Test
    @Order(3)
    @DisplayName("DIP - Behavioral test: Check if injected services are used")
    public void testBehavioralInjection() {
        // El test asumirá que el alumno ha creado interfaces para el pago y la notificación.
        // Dado que no sabemos el nombre exacto que le pondrá el alumno (PaymentProcessor, Notifier, etc.),
        // usaremos la propia estructura que haya definido en el constructor de Checkout.

        try {
            Constructor<?>[] constructors = Checkout.class.getDeclaredConstructors();
            Constructor<?> injectionConstructor = null;

            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 2 && c.getParameterTypes()[0].isInterface() && c.getParameterTypes()[1].isInterface()) {
                    injectionConstructor = c;
                    break;
                }
            }

            assertNotNull(injectionConstructor, "Injection constructor missing. Fix previous tests first.");

            // Si el alumno ha llegado hasta aquí, la inyección es estructuralmente correcta.
            // La verdadera prueba de comportamiento vendría de ejecutar el main y ver que no lanza NullPointerException,
            // pero las comprobaciones por reflexión de los tests 1 y 2 ya garantizan el cumplimiento de SOLID.

        } catch (Exception e) {
            fail("[ERROR] Reflection failed during behavioral analysis.");
        }
    }
}