package quest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import quest.tools.Dice;
import quest.tools.Player;
import quest.tools.RuleBook;
import software.amazon.awssdk.core.document.Document;

@DisplayName("Step 3a: ToolBox.run")
class ToolBoxTest {

    private final ToolBox tools = new ToolBox(new Dice(new Random(3)), RuleBook.fromResource("/rulebook.txt"), new Player());

    /** args("sides", 20, "count", 2) -> {"sides": 20, "count": 2} */
    static Document args(Object... pairs) {
        Map<String, Document> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            Object v = pairs[i + 1];
            map.put((String) pairs[i], v instanceof Integer n ? Document.fromNumber(n)
                    : v instanceof Boolean b ? Document.fromBoolean(b) : Document.fromString((String) v));
        }
        return Document.fromMap(map);
    }

    @Test
    @DisplayName("roll_dice uses sides and count; count defaults to 1")
    void rollDice() {
        assertTrue(tools.run("roll_dice", args("sides", 20)).startsWith("Rolled a d20: "));
        assertTrue(tools.run("roll_dice", args("sides", 6, "count", 2))
                .startsWith("Rolled 2d6: "));
    }

    @Test
    @DisplayName("lookup_rule passes the topic to the rule book")
    void lookupRule() {
        assertTrue(tools.run("lookup_rule", args("topic", "fireball")).contains("3d6"));
    }

    @Test
    @DisplayName("get_player_state needs no arguments")
    void getState() {
        assertTrue(tools.run("get_player_state", args()).startsWith("Adventurer: HP 20/20"));
    }

    @Test
    @DisplayName("update_player_state: missing arguments mean 'no change'")
    void updateState() {
        assertEquals("Updated: HP -4 (now 16/20)",
                tools.run("update_player_state", args("hpChange", -4)));
        assertEquals("Updated: gained 'torch'; used a spell slot (2 left)",
                tools.run("update_player_state", args("addItem", "torch", "useSpellSlot", true)));
        assertEquals("No changes made.", tools.run("update_player_state", args()));
    }

    @Test
    @DisplayName("an unknown tool name gets a message, not an exception")
    void unknownTool() {
        assertEquals("Unknown tool: summon_dragon", tools.run("summon_dragon", args()));
    }

    @Test
    @DisplayName("the model is told about exactly four tools")
    void fourTools() {
        assertEquals(4, tools.toolConfiguration().tools().size());
        assertEquals("roll_dice", tools.toolConfiguration().tools().get(0).toolSpec().name());
    }
}
