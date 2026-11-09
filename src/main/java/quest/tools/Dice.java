package quest.tools;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tool 1: roll dice. The model will call this whenever an action involves chance.
 *
 * Step 1a. Make DiceTest pass.
 */
public class Dice {

    // TODO 1: a private final Random field, set by the constructor.
    //         (Passing the Random in, rather than creating it here, lets the tests use a fixed seed.)

    private final Random random;

    public Dice(Random random) {
        // TODO 1
        this.random = random;
    }

    /**
     * Roll {@code count} dice with {@code sides} sides each.
     *
     * @return one die:      "Rolled a d20: 14"
     *         several dice: "Rolled 3d6: [2, 5, 1] (total 8)"
     *         bad input:    "Invalid roll: use 2 or more sides and 1 to 20 dice."  (sides < 2, or count outside 1..20)
     */
    public String roll(int sides, int count) {

        if (sides < 2 || count < 1 || count > 20) {
            return "Invalid roll: use 2 or more sides and 1 to 20 dice.";
        }

        // TODO 2: check the input first and return the "Invalid roll" message if it's bad.


        List<Integer> rolls = new ArrayList<>();
        int total = 0;
        for(int i = 0; i < count; i++) {
            int roll = random.nextInt(sides) + 1;
            total += roll;
            rolls.add(roll);
        }

        if (count == 1) {
        return "Rolled a d" + sides + ": " + rolls.get(0);
        }
        return "Rolled " + count + "d" + sides + ": " + rolls + " (total " + total + ")";
        // TODO 3: roll each die with random.nextInt(sides) + 1, keep them in a List<Integer>, add up the total.
        //         Hint: a List's toString() already prints "[2, 5, 1]".
        // TODO 4: return the right message for one die or several.

    }
}
