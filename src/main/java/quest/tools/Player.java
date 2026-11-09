package quest.tools;

/**
 * Tools 3 and 4: read and update the player's character. The model can't remember numbers reliably,
 * so the state lives here, in Java, and the model asks for it.
 *
 * Step 1c. Make PlayerTest pass.
 */
public class Player {

    // TODO 1: private fields. A new player is:
    //   name "Adventurer", hp 20, maxHp 20, strength 3, spellSlots 3, gold 10,
    //   inventory: a List<String> holding "rusty sword" and "leather armor".
    //   Hint: new ArrayList<>(List.of(...)) gives a list you can change; List.of alone can't be changed.

    /** "Adventurer: HP 20/20, strength +3, spell slots 3, gold 10, inventory: rusty sword, leather armor"
     *  (an empty inventory prints "empty"). */
    public String state() {
        // TODO 2
        return "";
    }

    /**
     * Apply one event's changes. Each change adds a phrase to a List<String>; join them with "; ".
     *
     *   hpChange    "HP -4 (now 16/20)"   HP stays between 0 and maxHp (Math.max / Math.min)
     *   goldChange  "gold -5 (now 5)"     or "not enough gold (has 3)" if it would go below 0
     *   addItem     "gained 'torch'"      (skip if blank: addItem.isBlank())
     *   removeItem  "lost 'torch'"        or "'torch' is not in the inventory"   (list.remove returns true/false)
     *   useSpellSlot "used a spell slot (2 left)"  or "no spell slots left: the spell fails"
     *   then, if hp is 0: "the player is down (0 HP)"
     *
     * Hint: String.format("HP %+d (now %d/%d)", hpChange, hp, maxHp) prints the + or - sign for you.
     *
     * @return "Updated: " + the phrases joined with "; ", or "No changes made." if nothing changed.
     */
    public String update(int hpChange, int goldChange, String addItem, String removeItem, boolean useSpellSlot) {
        // TODO 3
        return "";
    }
}
