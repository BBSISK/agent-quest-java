package quest;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;

public class GameMaster {

    // The EU version of Nova 2 Lite: my AWS role is only allowed to call EU models.
    static final String MODEL_ID = "eu.amazon.nova-2-lite-v1:0";

    static final String SYSTEM_PROMPT = """
            You are a dramatic and entertaining tabletop RPG games master.
            Narrate scenes vividly, in at most two short paragraphs.""";

    public static void main(String[] args) {
        // 1. A client for Bedrock in Ireland, signed in with my ask-barry profile (no keys in the code).
        try (BedrockRuntimeClient bedrock = BedrockRuntimeClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("ask-barry"))
                .build()) {

            // 2. One message from the user.
            Message userMessage = Message.builder()
                    .role(ConversationRole.USER)
                    .content(ContentBlock.fromText("I kick open the tavern door and demand the finest ale!"))
                    .build();

            // 3. Send it with the system prompt, using the Converse API.
            ConverseResponse response = bedrock.converse(request -> request
                    .modelId(MODEL_ID)
                    .system(SystemContentBlock.fromText(SYSTEM_PROMPT))
                    .messages(userMessage)
                    .inferenceConfig(config -> config.maxTokens(400).temperature(0.7f)));

            // 4. Read the reply, and why the model stopped.
            String reply = response.output().message().content().get(0).text();
            System.out.println(reply);
            System.out.println("\n[stop reason: " + response.stopReason()
                    + ", tokens in/out: " + response.usage().inputTokens() + "/" + response.usage().outputTokens() + "]");
        }
    }
}