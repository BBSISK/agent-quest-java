package quest.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Step 1c: Player")
class PlayerTest {

    @Test
    @DisplayName("a new player: 20/20 HP, strength +3, 3 spell slots, 10 gold, sword and armour")
    void newPlayer() {
        assertEquals("Adventurer: HP 20/20, strength +3, spell slots 3, gold 10, inventory: rusty sword, leather armor",
                new Player().state());
    }

    @Test
    @DisplayName("HP stays between 0 and max, and 0 HP is reported")
    void hpClamped() {
        Player p = new Player();
        assertTrue(p.update(5, 0, "", "", false).contains("now 20/20"));
        String out = p.update(-50, 0, "", "", false);
        assertTrue(out.contains("now 0/20") && out.contains("down"), out);
    }

    @Test
    @DisplayName("gold can't go below zero")
    void gold() {
        Player p = new Player();
        assertTrue(p.update(0, -11, "", "", false).contains("not enough gold"));
        assertTrue(p.update(0, -5, "", "", false).contains("now 5"));
    }

    @Test
    @DisplayName("items are added and removed; removing one you don't have says so")
    void items() {
        Player p = new Player();
        assertTrue(p.update(0, 0, "torch", "", false).contains("gained 'torch'"));
        assertTrue(p.update(0, 0, "", "dragon egg", false).contains("not in the inventory"));
        assertTrue(p.update(0, 0, "", "rusty sword", false).contains("lost 'rusty sword'"));
        assertTrue(p.state().endsWith("inventory: leather armor, torch"), p.state());
    }

    @Test
    @DisplayName("spell slots run out, then the spell fails")
    void spellSlots() {
        Player p = new Player();
        for (int i = 0; i < 3; i++) {
            p.update(0, 0, "", "", true);
        }
        assertTrue(p.update(0, 0, "", "", true).contains("spell fails"));
    }

    @Test
    @DisplayName("nothing to change: \"No changes made.\"")
    void noChange() {
        assertEquals("No changes made.", new Player().update(0, 0, "", "", false));
    }
}
