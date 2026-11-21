package quest.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("J1: Journey")
class JourneyTest {

    @Test
    @DisplayName("starts in the village")
    void startsInVillage() {
        Journey j = new Journey();
        assertEquals("Village", j.place());
        assertEquals("village", j.kind());
        assertEquals(List.of("Village"), j.visited());
    }

    @Test
    @DisplayName("moving changes place and kind, and adds to the trail")
    void moveTo() {
        Journey j = new Journey();
        String result = j.moveTo("The Rusty Flagon", "tavern");
        assertEquals("The Rusty Flagon", j.place());
        assertEquals("tavern", j.kind());
        assertEquals(List.of("Village", "The Rusty Flagon"), j.visited());
        assertTrue(result.contains("The Rusty Flagon"), result);
    }

    @Test
    @DisplayName("an unknown kind becomes 'road' (the model may invent one)")
    void unknownKind() {
        Journey j = new Journey();
        j.moveTo("Sky Palace", "cloud");
        assertEquals("road", j.kind());
    }

    @Test
    @DisplayName("moving to the same place twice does not repeat it in the trail")
    void noRepeat() {
        Journey j = new Journey();
        j.moveTo("Market Square", "market");
        j.moveTo("Market Square", "market");
        assertEquals(List.of("Village", "Market Square"), j.visited());
    }
}
