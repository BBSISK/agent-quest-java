package quest.tools;

import java.util.ArrayList;
import java.util.List;

/*
 * Tools 3 and 4: read and update the player's character. The model can't remember numbers reliably,
 * so the state lives here, in Java, and the model asks for it.
 *
 * Step 1c. Make PlayerTest pass.
 */
public class Player {

    private final String name = "Adventurer";
    private final int maxHp;
    private final int strength;
    private int hp;
    private int spellSlots;
    private int gold;
    private final List<String> inventory;



    /**
     * "Adventurer: HP 20/20, strength +3, spell slots 3, gold 10, inventory: rusty
     * sword, leather armor"
     * (an empty inventory prints "empty").
     */
    public String state() {
        // TODO 2
        return String.format("%s: HP %d/%d, strength +%d, spell slots %d, gold %d, inventory: %s",
                name, hp, maxHp, strength, spellSlots, gold,
                inventory.isEmpty() ? "empty" : String.join(", ", inventory));
    }

    /** A brand-new character. */
    public Player() {
        this(PlayerState.newCharacter());     // calls the other constructor
    }

    /** A character rebuilt from a saved snapshot. */
    public Player(PlayerState saved) {
        this.maxHp = saved.maxHp();
        this.strength = saved.strength();
        this.hp = saved.hp();
        this.spellSlots = saved.spellSlots();
        this.gold = saved.gold();
        this.inventory = new ArrayList<>(saved.inventory());   // a copy you can change
    }

    public PlayerState snapshot() {
        return new PlayerState(hp, maxHp, strength, spellSlots, gold, List.copyOf(inventory));
    }
    /**
     * Apply one event's changes. Each change adds a phrase to a List<String>; join
     * them with "; ".
     *
     * hpChange "HP -4 (now 16/20)" HP stays between 0 and maxHp (Math.max /
     * Math.min)
     * goldChange "gold -5 (now 5)" or "not enough gold (has 3)" if it would go
     * below 0
     * addItem "gained 'torch'" (skip if blank: addItem.isBlank())
     * removeItem "lost 'torch'" or "'torch' is not in the inventory" (list.remove
     * returns true/false)
     * useSpellSlot "used a spell slot (2 left)" or "no spell slots left: the spell
     * fails"
     * then, if hp is 0: "the player is down (0 HP)"
     *
     * Hint: String.format("HP %+d (now %d/%d)", hpChange, hp, maxHp) prints the +
     * or - sign for you.
     *
     * @return "Updated: " + the phrases joined with "; ", or "No changes made." if
     *         nothing changed.
     */

    public String update(int hpChange, int goldChange, String addItem, String removeItem, boolean useSpellSlot) {
        List<String> changes = new ArrayList<>();

        if (hpChange != 0) {
            hp = Math.max(0, Math.min(maxHp, hp + hpChange));
            changes.add(String.format("HP %+d (now %d/%d)", hpChange, hp, maxHp));
        }

        if (goldChange != 0) {
            if (gold + goldChange < 0) {
                changes.add("not enough gold (has " + gold + ")");
            } else {
                gold += goldChange;
                changes.add(String.format("gold %+d (now %d)", goldChange, gold));
            }
        }

        if (!addItem.isBlank()) {
            inventory.add(addItem);
            changes.add("gained '" + addItem + "'");
        }

        if (!removeItem.isBlank()) {
            if (inventory.remove(removeItem)) {
                changes.add("lost '" + removeItem + "'");
            } else {
                changes.add("'" + removeItem + "' is not in the inventory");
            }
        }

        if (useSpellSlot) {
            if (spellSlots > 0) {
                spellSlots--;
                changes.add("used a spell slot (" + spellSlots + " left)");
            } else {
                changes.add("no spell slots left: the spell fails");
            }
        }
        // gold: if (goldChange != 0) { if (gold + goldChange < 0) ...not enough gold...
        // else ...change it... }

        // add item: if (!addItem.isBlank()) { ... }

        // remove item: if (!removeItem.isBlank()) { if (inventory.remove(removeItem))
        // ... else ... }

        // spell slot: if (useSpellSlot) { if (spellSlots > 0) ... else ... }

        if (hp == 0) {
            changes.add("the player is down (0 HP)");
        }

        if (changes.isEmpty()) {
            return "No changes made.";
        }
        return "Updated: " + String.join("; ", changes);
    }

}
