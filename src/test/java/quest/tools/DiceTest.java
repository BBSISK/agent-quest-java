package quest.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Step 1a: Dice")
class DiceTest {

    @Test
    @DisplayName("one die: \"Rolled a d20: n\" with n between 1 and 20")
    void oneDie() {
        String out = new Dice(new Random(7)).roll(20, 1);
        assertTrue(out.startsWith("Rolled a d20: "), out);
        int n = Integer.parseInt(out.substring("Rolled a d20: ".length()));
        assertTrue(n >= 1 && n <= 20, out);
    }

    @Test
    @DisplayName("several dice: lists each roll and the total")
    void severalDice() {
        String out = new Dice(new Random(7)).roll(6, 3);
        assertTrue(out.startsWith("Rolled 3d6: ["), out);
        String list = out.substring(out.indexOf('[') + 1, out.indexOf(']'));
        int sum = 0;
        for (String part : list.split(", ")) {
            int r = Integer.parseInt(part);
            assertTrue(r >= 1 && r <= 6, out);
            sum += r;
        }
        assertTrue(out.endsWith("(total " + sum + ")"), out);
    }

    @Test
    @DisplayName("the same seed gives the same rolls (so tests are repeatable)")
    void seeded() {
        assertEquals(new Dice(new Random(42)).roll(20, 4), new Dice(new Random(42)).roll(20, 4));
    }

    @Test
    @DisplayName("refuses a die with fewer than 2 sides, or a count outside 1 to 20")
    void invalid() {
        Dice dice = new Dice(new Random(1));
        assertTrue(dice.roll(1, 1).startsWith("Invalid roll"));
        assertTrue(dice.roll(6, 0).startsWith("Invalid roll"));
        assertTrue(dice.roll(6, 21).startsWith("Invalid roll"));
    }
}
