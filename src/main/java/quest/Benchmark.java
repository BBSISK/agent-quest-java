package quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import quest.agent.Agent;
import quest.agent.ModelClient;
import quest.agent.ToolBox;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;

/**
 * Step 5: the same scripted game on each model, scored on whether the agent DID what the story needs,
 * judged only from the tool log (what ran), never from the narration (what it says).
 *
 * Run:  mvn -q compile exec:java -Dexec.mainClass=quest.Benchmark
 *       mvn -q compile exec:java -Dexec.mainClass=quest.Benchmark -Dexec.args="3"     (runs per model, default 3)
 */
public class Benchmark {

    /** One scripted turn and the tools it must use. */
    record Turn(String label, String text, List<String> mustCall) { }

    static final List<Turn> SCRIPT = List.of(
            new Turn("ale", "I walk into the tavern and buy an ale.",
                    List.of("update_player_state")),
            new Turn("bet", "I bet 5 gold that I can beat the blacksmith at arm wrestling.",
                    List.of("lookup_rule", "roll_dice", "update_player_state")),
            new Turn("fireball", "Goblins burst in! I cast a fireball at them.",
                    List.of("roll_dice", "update_player_state")),
            new Turn("state", "What's my current state?",
                    List.of("get_player_state")));

    static final Map<String, String> MODELS = Map.of(
            "nova", "eu.amazon.nova-2-lite-v1:0",
            "haiku", "eu.anthropic.claude-haiku-4-5-20251001-v1:0");

    public static void main(String[] args) {
        int runs = args.length > 0 ? Integer.parseInt(args[0]) : 3;
        try (BedrockRuntimeClient bedrock = BedrockRuntimeClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("ask-barry"))
                .build()) {

            List<String> summary = new ArrayList<>();
            for (String name : List.of("nova", "haiku")) {
                int[] tokens = {0, 0};
                ModelClient model = request -> {
                    ConverseResponse response = bedrock.converse(request);
                    tokens[0] += response.usage().inputTokens();
                    tokens[1] += response.usage().outputTokens();
                    return response;
                };

                int needed = 0, done = 0, stateNeeded = 0, stateDone = 0;
                long millis = 0;
                System.out.println("\n=== " + name + " (" + MODELS.get(name) + ") ===");
                for (int run = 1; run <= runs; run++) {
                    Agent agent = new Agent(model, MODELS.get(name), GameMaster.SYSTEM_PROMPT, ToolBox.standard());
                    StringBuilder line = new StringBuilder("run " + run + ":");
                    for (Turn turn : SCRIPT) {
                        int before = agent.toolLog().size();
                        long start = System.currentTimeMillis();
                        agent.ask(turn.text());
                        millis += System.currentTimeMillis() - start;
                        List<String> calls = agent.toolLog().subList(before, agent.toolLog().size());

                        line.append("  ").append(turn.label()).append(" ");
                        for (String tool : turn.mustCall()) {
                            boolean ok = called(calls, tool);
                            needed++;
                            if (ok) done++;
                            if (tool.equals("update_player_state")) {
                                stateNeeded++;
                                if (ok) stateDone++;
                            }
                            line.append(ok ? "✓" : "✗");
                        }
                    }
                    System.out.println(line);
                }
                int turns = runs * SCRIPT.size();
                summary.add(String.format("| %s | %d/%d | %d/%d | %.1f s | %d / %d |", name,
                        stateDone, stateNeeded, done, needed, millis / 1000.0 / turns,
                        tokens[0] / runs, tokens[1] / runs));
            }

            System.out.println("\n| Model | State changes recorded | All required tools | Avg per turn | Tokens in / out per game |");
            System.out.println("|---|---|---|---|---|");
            summary.forEach(System.out::println);
        }
    }

    /** True if any log line in this turn is a call to {@code tool}. Log lines look like "roll_dice {...} -> ...". */
    static boolean called(List<String> calls, String tool) {
        for (String call : calls) {
            if (call.startsWith(tool + " ")) {
                return true;
            }
        }
        return false;
    }
}
