package quest.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("R3a: Player <-> PlayerState")
class PlayerSnapshotTest {

    @Test
    @DisplayName("a new player's snapshot is the new-character state")
    void newPlayerSnapshot() {
        assertEquals(PlayerState.newCharacter(), new Player().snapshot());
    }

    @Test
    @DisplayName("save and restore: a player rebuilt from a snapshot is identical")
    void roundTrip() {
        Player original = new Player();
        original.update(-4, 5, "torch", "rusty sword", true);
        Player restored = new Player(original.snapshot());
        assertEquals(original.state(), restored.state());
        assertEquals(original.snapshot(), restored.snapshot());
    }

    @Test
    @DisplayName("a snapshot is a copy: later changes to the player don't change it")
    void snapshotIsACopy() {
        Player player = new Player();
        PlayerState before = player.snapshot();
        player.update(0, 0, "torch", "", false);
        assertEquals(List.of("rusty sword", "leather armor"), before.inventory());
    }

    @Test
    @DisplayName("a restored player can still gain items (its inventory isn't read-only)")
    void restoredInventoryCanChange() {
        Player restored = new Player(PlayerState.newCharacter());
        assertTrue(restored.update(0, 0, "torch", "", false).contains("gained 'torch'"));
    }
}
