package quest.tools;

import java.util.List;

/**
 * R3. A snapshot of the player's numbers and items: what gets saved to, and loaded from, the database.
 * A record is a small immutable class: Java writes the constructor, the getters (hp(), gold(), ...),
 * equals() and toString() for you, so two snapshots with the same values are equal.
 */
public record PlayerState(int hp, int maxHp, int strength, int spellSlots, int gold, List<String> inventory) {

    /** The state of a brand-new character. */
    public static PlayerState newCharacter() {
        return new PlayerState(20, 20, 3, 3, 10, List.of("rusty sword", "leather armor"));
    }
}
