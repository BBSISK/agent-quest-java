package quest;

import java.util.Scanner;

import quest.agent.Agent;
import quest.agent.ModelClient;
import quest.agent.ToolBox;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;

public class GameMaster {

    static final String MODEL_ID = "eu.amazon.nova-2-lite-v1:0";

    static final String SYSTEM_PROMPT = """
            You are a dramatic and entertaining tabletop RPG games master.
            - Narrate vividly, in at most two short paragraphs.
            - Never say the player's HP, gold, items or spell slots changed unless you called update_player_state in this turn.
            - Use plain text, no markdown.
            - Roll dice for any action involving chance; never invent a roll.
            - Look up the rule book for game mechanics rather than making rules up.
            - Check the player's state before decisions about their abilities, gold or items.
            - Update the player's state after damage, healing, spending, loot or spells.""";

    static int tokensIn = 0;
    static int tokensOut = 0;

    public static void main(String[] args) {
        try (BedrockRuntimeClient bedrock = BedrockRuntimeClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("ask-barry"))
                .build();
             Scanner keyboard = new Scanner(System.in)) {

            // The real model, wrapped so every call adds up the tokens it used.
            ModelClient model = request -> {
                ConverseResponse response = bedrock.converse(request);
                tokensIn += response.usage().inputTokens();
                tokensOut += response.usage().outputTokens();
                return response;
            };

            Agent agent = new Agent(model, MODEL_ID, SYSTEM_PROMPT, ToolBox.standard());
            play(agent, "Set the scene. I'm a wandering adventurer arriving at a small village at dusk.");

            while (true) {
                System.out.print("\nYou > ");
                if (!keyboard.hasNextLine()) break;
                String line = keyboard.nextLine().strip();
                if (line.equalsIgnoreCase("quit")) break;
                if (!line.isEmpty()) play(agent, line);
            }
            System.out.println("\nThanks for playing! Tokens used: " + tokensIn + " in, " + tokensOut + " out");
        }
    }

    /** One turn: the agent's answer, then the tools it used to get there. */
    static void play(Agent agent, String text) {
        int before = agent.toolLog().size();
        System.out.println("\n" + agent.ask(text));
        for (String call : agent.toolLog().subList(before, agent.toolLog().size())) {
            System.out.println("   [tool] " + call);
        }
    }
}