package quest.web;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quest.GameMaster;
import quest.store.GameRepository;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

/**
 * R4. Wires the real parts together when the web app starts: Bedrock (via my ask-barry AWS profile),
 * the RDS database (Spring Boot builds the DataSource from application.properties), and the GameService.
 */
@Configuration
public class QuestConfig {

    @Bean(destroyMethod = "close")
    public BedrockRuntimeClient bedrock() {
        return BedrockRuntimeClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("ask-barry"))
                .build();
    }

    @Bean
    public GameRepository gameRepository(DataSource dataSource) {
        return new GameRepository(dataSource);
    }

    @Bean
    public GameService gameService(BedrockRuntimeClient bedrock, GameRepository repository) {
        return new GameService(bedrock::converse, GameMaster.MODEL_ID, GameMaster.SYSTEM_PROMPT, repository);
    }
}
