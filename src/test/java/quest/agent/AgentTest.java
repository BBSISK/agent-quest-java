package quest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import quest.tools.Dice;
import quest.tools.Player;
import quest.tools.RuleBook;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseOutput;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.StopReason;
import software.amazon.awssdk.services.bedrockruntime.model.TokenUsage;
import software.amazon.awssdk.services.bedrockruntime.model.ToolUseBlock;

@DisplayName("Step 3b: the agentic loop (with a scripted fake model, no AWS)")
class AgentTest {

    /** Plays back prepared responses in order and records every request it receives. */
    static class ScriptedModel implements ModelClient {
        final List<ConverseResponse> script;
        final List<ConverseRequest> requests = new ArrayList<>();

        ScriptedModel(List<ConverseResponse> script) {
            this.script = script;
        }

        @Override
        public ConverseResponse converse(ConverseRequest request) {
            requests.add(request);
            return script.get(Math.min(requests.size() - 1, script.size() - 1));   // the last one repeats
        }
    }

    static ConverseResponse answer(String text) {
        return response(StopReason.END_TURN, ContentBlock.fromText(text));
    }

    static ConverseResponse askForTool(String id, String name, Document input) {
        return response(StopReason.TOOL_USE, ContentBlock.fromText("Let me roll for that."),
                ContentBlock.builder().toolUse(ToolUseBlock.builder().toolUseId(id).name(name).input(input).build()).build());
    }

    static ConverseResponse response(StopReason stop, ContentBlock... content) {
        Message message = Message.builder().role(ConversationRole.ASSISTANT).content(content).build();
        return ConverseResponse.builder()
                .stopReason(stop)
                .output(ConverseOutput.builder().message(message).build())
                .usage(TokenUsage.builder().inputTokens(10).outputTokens(5).totalTokens(15).build())
                .build();
    }

    static Agent agent(ScriptedModel model) {
        ToolBox tools = new ToolBox(new Dice(new Random(3)), RuleBook.fromResource("/rulebook.txt"), new Player());
        return new Agent(model, "test-model", "You are a games master.", tools);
    }

    static final Document D20 = ToolBoxTest.args("sides", 20);

    @Test
    @DisplayName("no tool needed: one call, the model's text comes back")
    void plainAnswer() {
        ScriptedModel model = new ScriptedModel(List.of(answer("The tavern falls silent.")));
        assertEquals("The tavern falls silent.", agent(model).ask("I walk in."));
        assertEquals(1, model.requests.size());
        assertEquals(ConversationRole.USER, model.requests.get(0).messages().get(0).role());
    }

    @Test
    @DisplayName("tool round: runs the tool, sends the result back with the same toolUseId, then answers")
    void oneToolRound() {
        ScriptedModel model = new ScriptedModel(List.of(askForTool("t1", "roll_dice", D20), answer("A 14: the door flies open!")));
        Agent agent = agent(model);

        assertEquals("A 14: the door flies open!", agent.ask("I kick the door."));
        assertEquals(2, model.requests.size());

        List<Message> sent = model.requests.get(1).messages();
        assertEquals(3, sent.size(), "user text, assistant tool request, user tool result");
        assertEquals(ConversationRole.ASSISTANT, sent.get(1).role());
        Message results = sent.get(2);
        assertEquals(ConversationRole.USER, results.role());
        assertNotNull(results.content().get(0).toolResult(), "the third message must carry the tool result");
        assertEquals("t1", results.content().get(0).toolResult().toolUseId());
        assertTrue(results.content().get(0).toolResult().content().get(0).text().startsWith("Rolled a d20: "));

        assertEquals(1, agent.toolLog().size());
        assertTrue(agent.toolLog().get(0).startsWith("roll_dice "));
    }

    @Test
    @DisplayName("history carries over: the second turn sends the first turn too")
    void historyKept() {
        ScriptedModel model = new ScriptedModel(List.of(answer("One."), answer("Two.")));
        Agent agent = agent(model);
        agent.ask("first");
        agent.ask("second");
        assertEquals(3, model.requests.get(1).messages().size(), "first, the answer, second");
    }

    @Test
    @DisplayName("a model that never stops asking for tools is cut off after MAX_ROUNDS")
    void roundLimit() {
        ScriptedModel model = new ScriptedModel(List.of(askForTool("again", "roll_dice", D20)));
        String out = agent(model).ask("Roll forever.");
        assertEquals(Agent.MAX_ROUNDS, model.requests.size());
        assertTrue(out.contains("stopped after " + Agent.MAX_ROUNDS), out);
    }

    @Test
    @DisplayName("an empty reply from the model never goes into history (Bedrock refuses empty messages)")
    void emptyReplyIsNotSentBack() {
        ScriptedModel model = new ScriptedModel(List.of(response(StopReason.END_TURN), answer("Two.")));
        Agent agent = agent(model);
        assertNotNull(agent.ask("first"));
        agent.ask("second");
        for (Message message : model.requests.get(1).messages()) {
            assertTrue(!message.content().isEmpty(), "a " + message.role() + " message with no content was sent");
        }
    }
}
