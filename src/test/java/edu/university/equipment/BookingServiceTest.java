package edu.university.equipment;
import static org.junit.jupiter.api.Assertions.*; import java.util.NoSuchElementException; import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test;
class BookingServiceTest {
 private BookingService service; @BeforeEach void setup(){service=new BookingService();}
//  @Test void successfulBookingReducesInventory(){service.create("STU1001","LAPTOP01",2);assertEquals(8,service.getEquipment("LAPTOP01").getAvailableQuantity());}
//  @Test void zeroQuantityRejected(){assertThrows(IllegalArgumentException.class,()->service.create("STU1001","LAPTOP01",0));assertEquals(10,service.getEquipment("LAPTOP01").getAvailableQuantity());}
//  @Test void blankBorrowerRejected(){assertThrows(IllegalArgumentException.class,()->service.create(" ","LAPTOP01",1));}
//  @Test void unknownEquipmentRejected(){assertThrows(NoSuchElementException.class,()->service.create("STU1001","UNKNOWN",1));}
//  @Test void ordinaryOverbookingRejected(){assertThrows(IllegalStateException.class,()->service.create("STU1001","PROJECTOR01",6));assertEquals(4,service.getEquipment("PROJECTOR01").getAvailableQuantity());}
//  @Test void cancellationRestoresInventoryOnce(){Booking b=service.create("STU1001","TABLET01",3);service.cancel(b.getId());assertEquals(8,service.getEquipment("TABLET01").getAvailableQuantity());assertThrows(IllegalStateException.class,()->service.cancel(b.getId()));}

@Test
void successfulBookingReducesInventory() {
    service.create("STU1001", "LAPTOP01", 2);
    assertEquals(8, service.getEquipment("LAPTOP01").getAvailableQuantity());
}

@Test
void zeroQuantityRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> service.create("STU1001", "LAPTOP01", 0));
    assertEquals(10, service.getEquipment("LAPTOP01").getAvailableQuantity());
}

@Test
void blankBorrowerRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> service.create(" ", "LAPTOP01", 1));
}

@Test
void unknownEquipmentRejected() {
    assertThrows(NoSuchElementException.class,
        () -> service.create("STU1001", "UNKNOWN", 1));
}

@Test
void ordinaryOverbookingRejected() {
    service.create("STU1001", "PROJECTOR01", 2);
    service.create("STU1001", "PROJECTOR01", 1);

    assertThrows(IllegalStateException.class,
        () -> service.create("STU1001", "PROJECTOR01", 2));

    assertEquals(1, service.getEquipment("PROJECTOR01").getAvailableQuantity());
}

@Test
void cancellationRestoresInventoryOnce() {
    Booking b = service.create("STU1001", "TABLET01", 2);
    service.cancel(b.getId());

    assertEquals(8, service.getEquipment("TABLET01").getAvailableQuantity());

    assertThrows(IllegalStateException.class,
        () -> service.cancel(b.getId()));
}

//  @Test
// void bookingExceedingAvailableInventoryIsRejectedWithoutChangingInventory() {
//     int initialQuantity = service.getEquipment("PROJECTOR01").getAvailableQuantity();

//     assertThrows(IllegalStateException.class,
//         () -> service.create("STU1001", "PROJECTOR01", initialQuantity + 1));

//     assertEquals(initialQuantity,
//         service.getEquipment("PROJECTOR01").getAvailableQuantity());
// }

// @Test
// void bookingExactlyMatchingAvailableInventoryIsAccepted() {
//     int availableQuantity = service.getEquipment("PROJECTOR01").getAvailableQuantity();

//     Booking booking = service.create("STU1001", "PROJECTOR01", availableQuantity);

//     assertEquals("CONFIRMED", booking.getStatus());
//     assertEquals(0, service.getEquipment("PROJECTOR01").getAvailableQuantity());
// }

// @Test
// void rejectedBookingDoesNotChangeInventory() {
//     int initialQuantity = service.getEquipment("PROJECTOR01").getAvailableQuantity();

//     assertThrows(IllegalStateException.class,
//         () -> service.create("STU1001", "PROJECTOR01", initialQuantity + 1));

//     assertEquals(initialQuantity,
//         service.getEquipment("PROJECTOR01").getAvailableQuantity());
// }

@Test
void bookingExceedingAvailableInventoryIsRejectedWithoutChangingInventory() {
    service.create("STU1001", "PROJECTOR01", 2);
    service.create("STU1001", "PROJECTOR01", 1);

    assertThrows(IllegalStateException.class,
        () -> service.create("STU1001", "PROJECTOR01", 2));

    assertEquals(1,
        service.getEquipment("PROJECTOR01").getAvailableQuantity());
}

@Test
void bookingExactlyMatchingAvailableInventoryIsAccepted() {
    service.create("STU1001", "PROJECTOR01", 2);

    int availableQuantity =
        service.getEquipment("PROJECTOR01").getAvailableQuantity();

    Booking booking =
        service.create("STU1001", "PROJECTOR01", availableQuantity);

    assertEquals("CONFIRMED", booking.getStatus());
    assertEquals(0,
        service.getEquipment("PROJECTOR01").getAvailableQuantity());
}

@Test
void rejectedBookingDoesNotChangeInventory() {
    service.create("STU1001", "PROJECTOR01", 2);
    service.create("STU1001", "PROJECTOR01", 1);

    int initialQuantity =
        service.getEquipment("PROJECTOR01").getAvailableQuantity();

    assertThrows(IllegalStateException.class,
        () -> service.create("STU1001", "PROJECTOR01", 2));

    assertEquals(initialQuantity,
        service.getEquipment("PROJECTOR01").getAvailableQuantity());
}

@Test
void bookingExactlyTwoUnitsIsAccepted() {
    int initialQuantity =
        service.getEquipment("LAPTOP01").getAvailableQuantity();

    Booking booking = service.create("STU1001", "LAPTOP01", 2);

    assertEquals("CONFIRMED", booking.getStatus());
    assertEquals(initialQuantity - 2,
        service.getEquipment("LAPTOP01").getAvailableQuantity());
}

@Test
void bookingThreeUnitsIsRejectedWithoutChangingInventory() {
    int initialQuantity =
        service.getEquipment("LAPTOP01").getAvailableQuantity();

    assertThrows(IllegalArgumentException.class,
        () -> service.create("STU1001", "LAPTOP01", 3));

    assertEquals(initialQuantity,
        service.getEquipment("LAPTOP01").getAvailableQuantity());
}

}