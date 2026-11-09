package quest.agent;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.StopReason;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ToolResultBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ToolResultContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ToolUseBlock;

/**
 * The agentic loop, written by hand: what a framework (Strands, LangGraph,
 * Agent Framework) does for you.
 *
 * send the conversation -> model answers? -> return the text
 * -> model asks for tools? -> run them, add the results, send again
 * ...at most MAX_ROUNDS times, so a confused model can't loop (and spend)
 * forever.
 */
public class Agent {

    static final int MAX_ROUNDS = 8;

    private final ModelClient model;
    private final String modelId;
    private final String systemPrompt;
    private final ToolBox tools;
    private final List<Message> history = new ArrayList<>(); // the whole conversation, sent every time
    private final List<String> toolLog = new ArrayList<>(); // "roll_dice {sides=20} -> Rolled a d20: 14"

    public Agent(ModelClient model, String modelId, String systemPrompt, ToolBox tools) {
        this.model = model;
        this.modelId = modelId;
        this.systemPrompt = systemPrompt;
        this.tools = tools;
    }

    /**
     * Step 3b. One turn of the game: the player says something, the agent may use
     * tools, then it answers.
     *
     * 1. Add the player's text to history as a USER message. (use userText(...)
     * below)
     * 2. Loop for round = 1 .. MAX_ROUNDS:
     * a. response = model.converse(request());
     * b. Message reply = response.output().message(); add it to history (ALWAYS,
     * before anything else)
     * c. if response.stopReason() != StopReason.TOOL_USE: return textOf(reply)
     * d. otherwise, for each ContentBlock in reply.content() where block.toolUse()
     * != null:
     * run it with tools.run(use.name(), use.input()),
     * log it: toolLog.add(use.name() + " " + use.input() + " -> " + result)
     * and collect toolResult(use, result) (helper below)
     * e. add ONE USER message holding all the collected results: Message.builder()
     * .role(ConversationRole.USER).content(results).build()
     * 3. If the loop ends without an answer: return "(The games master stopped
     * after " + MAX_ROUNDS + " rounds of tool calls.)"
     *
     * Why the order matters: Bedrock checks that every toolResult answers a toolUse
     * in the message just
     * before it, with the same toolUseId. Skip step b, or send results without the
     * ids, and it refuses the request.
     */

    public String ask(String userText) {
        history.add(userText(userText));

        for (int round = 1; round <= MAX_ROUNDS; round++) {
            ConverseResponse response = model.converse(request());
            Message reply = response.output().message();
            history.add(reply); // always keep the model's turn

            if (response.stopReason() != StopReason.TOOL_USE) {
                return textOf(reply); // the model has answered: done
            }

            List<ContentBlock> results = new ArrayList<>();
            for (ContentBlock block : reply.content()) {
                ToolUseBlock use = block.toolUse();
                if (use != null) {
                    // TODO: run the tool, add a line to toolLog, add toolResult(use, result) to
                    // results
                    String result = tools.run(use.name(), use.input());
                    toolLog.add(use.name() + " " + use.input() + " -> " + result);
                    results.add(toolResult(use, result));
                }
            }
            // TODO: add ONE user message holding all the results to history
            history.add(Message.builder().role(ConversationRole.USER).content(results).build());
        }
        return "(The games master stopped after " + MAX_ROUNDS + " rounds of tool calls.)";

    }

    /** The tool calls made so far, oldest first (for printing, and for tests). */
    public List<String> toolLog() {
        return toolLog;
    }

    // ---- helpers: these are done for you ----

    private ConverseRequest request() {
        return ConverseRequest.builder()
                .modelId(modelId)
                .system(SystemContentBlock.fromText(systemPrompt))
                .messages(history)
                .toolConfig(tools.toolConfiguration())
                .inferenceConfig(config -> config.maxTokens(600).temperature(0.7f))
                .build();
    }

    static Message userText(String text) {
        return Message.builder().role(ConversationRole.USER).content(ContentBlock.fromText(text)).build();
    }

    static ContentBlock toolResult(ToolUseBlock use, String result) {
        ToolResultBlock block = ToolResultBlock.builder()
                .toolUseId(use.toolUseId()) // which request this answers
                .content(ToolResultContentBlock.builder().text(result).build())
                .build();
        return ContentBlock.builder().toolResult(block).build();
    }

    /**
     * All the text blocks of a message joined together (a reply can mix text and
     * tool requests).
     */
    static String textOf(Message message) {
        StringBuilder text = new StringBuilder();
        for (ContentBlock block : message.content()) {
            if (block.text() != null) {
                text.append(block.text());
            }
        }
        return text.toString().strip();
    }
}
