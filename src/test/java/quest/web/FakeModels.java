package quest.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import quest.agent.ModelClient;
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

/** Test helpers: a scripted model that plays back responses in order and records every request. */
final class FakeModels {

    static final class Scripted implements ModelClient {
        final List<ConverseResponse> script;
        final List<ConverseRequest> requests = new ArrayList<>();

        Scripted(List<ConverseResponse> script) {
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

    /** The model asks for update_player_state with a gold change. */
    static ConverseResponse askToChangeGold(String id, int goldChange) {
        Document input = Document.fromMap(Map.of("goldChange", Document.fromNumber(goldChange)));
        return response(StopReason.TOOL_USE, ContentBlock.builder().toolUse(
                ToolUseBlock.builder().toolUseId(id).name("update_player_state").input(input).build()).build());
    }

    static ConverseResponse response(StopReason stop, ContentBlock... content) {
        Message message = Message.builder().role(ConversationRole.ASSISTANT).content(content).build();
        return ConverseResponse.builder()
                .stopReason(stop)
                .output(ConverseOutput.builder().message(message).build())
                .usage(TokenUsage.builder().inputTokens(10).outputTokens(5).totalTokens(15).build())
                .build();
    }

    private FakeModels() { }
}
