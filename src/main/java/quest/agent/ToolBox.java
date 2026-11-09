package quest.agent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import quest.tools.Dice;
import quest.tools.Player;
import quest.tools.RuleBook;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.bedrockruntime.model.Tool;
import software.amazon.awssdk.services.bedrockruntime.model.ToolConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.ToolInputSchema;
import software.amazon.awssdk.services.bedrockruntime.model.ToolSpecification;

/**
 * The agent's hands. Two jobs:
 *   1. describe the tools to the model (name, description, JSON schema of the arguments): done for you below;
 *   2. run a tool when the model asks for one, by name: that's {@link #run}, your Step 3a.
 *
 * The model never runs anything itself. It only sends back "please call roll_dice with {sides: 20}";
 * this class decides whether and how that happens.
 */
public class ToolBox {

    private final Dice dice;
    private final RuleBook ruleBook;
    private final Player player;

    public ToolBox(Dice dice, RuleBook ruleBook, Player player) {
        this.dice = dice;
        this.ruleBook = ruleBook;
        this.player = player;
    }

    /** The real tools: random dice, the rule book from resources, a new player. */
    public static ToolBox standard() {
        return new ToolBox(new Dice(new Random()), RuleBook.fromResource("/rulebook.txt"), new Player());
    }

    /**
     * Step 3a. Run the tool the model asked for and return its result as text.
     *
     * The tool names are the ones in toolConfiguration() below:
     *   "roll_dice"           -> dice.roll(sides, count)              count is optional: default 1
     *   "lookup_rule"         -> ruleBook.lookup(topic)
     *   "get_player_state"    -> player.state()                        no arguments
     *   "update_player_state" -> player.update(hpChange, goldChange, addItem, removeItem, useSpellSlot)
     *                            every argument is optional: defaults 0, 0, "", "", false
     *   anything else         -> "Unknown tool: " + name               (never throw: the model may make a name up)
     *
     * Read the arguments with the helpers at the bottom, e.g. intArg(input, "sides", 6).
     * Hint: a switch on a String works:  switch (name) { case "roll_dice": return ...; ... default: return ...; }
     */
    public String run(String name, Document input) {
        // TODO 3a

        switch (name) {
        case "roll_dice":
        return dice.roll(intArg(input, "sides", 6), intArg(input, "count", 1));
    // lookup_rule, get_player_state, update_player_state ...
        case "lookup_rule":
        return ruleBook.lookup(stringArg(input, "topic", ""));
        case "get_player_state":
        return player.state();
        case "update_player_state":
        return player.update(
            intArg(input, "hpChange", 0),
            intArg(input, "goldChange", 0),
            stringArg(input, "addItem", ""),
            stringArg(input, "removeItem", ""),
            boolArg(input, "useSpellSlot", false));     
        default:
        return "Unknown tool: " + name;
        }   
    }   

    /** What the model is told about each tool. The descriptions matter: they're how it decides when to call one. */
    public ToolConfiguration toolConfiguration() {
        return ToolConfiguration.builder().tools(List.of(
                tool("roll_dice", "Roll dice. Use it for any action whose outcome depends on chance.",
                        schema(List.of("sides"),
                                "sides", "integer", "Number of sides on each die, e.g. 20 for a d20",
                                "count", "integer", "How many dice to roll (default 1)")),
                tool("lookup_rule", "Look up a game rule in the rule book, e.g. 'combat', 'magic', 'arm wrestling'.",
                        schema(List.of("topic"),
                                "topic", "string", "The topic to search for")),
                tool("get_player_state", "Get the player's HP, strength, spell slots, gold and inventory.",
                        schema(List.of())),
                tool("update_player_state", "Change the player's state after an event. Leave out anything that doesn't change.",
                        schema(List.of(),
                                "hpChange", "integer", "HP change: negative for damage, positive for healing",
                                "goldChange", "integer", "Gold change: negative to spend or lose, positive to gain",
                                "addItem", "string", "An item to add to the inventory",
                                "removeItem", "string", "An item to remove from the inventory",
                                "useSpellSlot", "boolean", "true to use up one spell slot"))
        )).build();
    }

    // ---- argument helpers: a missing argument gets the default ----

    static int intArg(Document input, String key, int fallback) {
        Document value = field(input, key);
        return value != null && value.isNumber() ? value.asNumber().intValue() : fallback;
    }

    static String stringArg(Document input, String key, String fallback) {
        Document value = field(input, key);
        return value != null && value.isString() ? value.asString() : fallback;
    }

    static boolean boolArg(Document input, String key, boolean fallback) {
        Document value = field(input, key);
        return value != null && value.isBoolean() ? value.asBoolean() : fallback;
    }

    private static Document field(Document input, String key) {
        return input != null && input.isMap() ? input.asMap().get(key) : null;
    }

    // ---- building the JSON schemas ----

    private static Tool tool(String name, String description, Document schema) {
        return Tool.builder().toolSpec(ToolSpecification.builder()
                .name(name)
                .description(description)
                .inputSchema(ToolInputSchema.builder().json(schema).build())
                .build()).build();
    }

    /** {"type":"object","properties":{name:{"type":..,"description":..},...},"required":[...]} from (name, type, description) triples. */
    private static Document schema(List<String> required, String... triples) {
        Map<String, Document> properties = new LinkedHashMap<>();
        for (int i = 0; i < triples.length; i += 3) {
            properties.put(triples[i], Document.fromMap(Map.of(
                    "type", Document.fromString(triples[i + 1]),
                    "description", Document.fromString(triples[i + 2]))));
        }
        return Document.fromMap(Map.of(
                "type", Document.fromString("object"),
                "properties", Document.fromMap(properties),
                "required", Document.fromList(required.stream().map(Document::fromString).toList())));
    }
}
