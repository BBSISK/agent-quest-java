package quest.agent;

import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;

/**
 * The one thing the agent needs from a model: send a conversation, get a response.
 * Bedrock's client fits it as a method reference (bedrock::converse); the tests use a scripted fake,
 * so the loop can be tested without calling AWS.
 */
@FunctionalInterface
public interface ModelClient {
    ConverseResponse converse(ConverseRequest request);
}
