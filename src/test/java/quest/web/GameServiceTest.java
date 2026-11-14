package quest.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import quest.store.GameRepository;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

/** R4: games, turns and restarts, with a scripted model and an in-memory database. No AWS. */
@DisplayName("R4: GameService")
class GameServiceTest {

    private DataSource dataSource;

    @BeforeEach
    void freshDatabase() {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(h2);
        dataSource = h2;
    }

    private GameService service(FakeModels.Scripted model) {
        return new GameService(model, "test-model", "You are a games master.", new GameRepository(dataSource));
    }

    private int count(String table) {
        return new JdbcTemplate(dataSource).queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    @Test
    @DisplayName("a new game is saved straight away, with a new character")
    void newGame() {
        GameService games = service(new FakeModels.Scripted(List.of(FakeModels.answer("Welcome."))));
        String id = games.newGame();
        assertEquals(1, count("games"));
        assertEquals(10, games.player(id).orElseThrow().gold());
    }

    @Test
    @DisplayName("a turn runs the tools, and the player, the turn and the tool call are all saved")
    void turnIsSaved() {
        GameService games = service(new FakeModels.Scripted(List.of(
                FakeModels.askToChangeGold("t1", 5), FakeModels.answer("You win the bet!"))));
        String id = games.newGame();

        GameService.TurnResult result = games.play(id, "I bet 5 gold on arm wrestling.").orElseThrow();

        assertEquals("You win the bet!", result.reply());
        assertEquals(15, result.player().gold());
        assertEquals(1, result.toolCalls().size());
        assertEquals("update_player_state", result.toolCalls().get(0).name());
        assertEquals(15, new GameRepository(dataSource).loadPlayer(id).orElseThrow().gold(), "saved to the database");
        assertEquals(1, count("turns"));
        assertEquals(1, count("tool_calls"));
    }

    @Test
    @DisplayName("after a restart, the game comes back from the database, story included")
    void survivesRestart() {
        String id;
        {
            GameService before = service(new FakeModels.Scripted(List.of(
                    FakeModels.askToChangeGold("t1", 5), FakeModels.answer("You win the bet!"))));
            id = before.newGame();
            before.play(id, "I bet 5 gold on arm wrestling.");
        }
        // "restart": a brand-new service with an empty memory, the same database
        FakeModels.Scripted model = new FakeModels.Scripted(List.of(FakeModels.answer("Welcome back.")));
        GameService after = service(model);

        assertEquals(15, after.player(id).orElseThrow().gold(), "the player came back from player_state");
        after.play(id, "What happened last time?");

        List<Message> sent = model.requests.get(0).messages();
        assertEquals(3, sent.size(), "the remembered turn (player + games master), then the new question");
        assertEquals(ConversationRole.USER, sent.get(0).role());
        assertEquals("I bet 5 gold on arm wrestling.", sent.get(0).content().get(0).text());
        assertEquals("You win the bet!", sent.get(1).content().get(0).text());
        assertEquals(2, count("turns"));
    }

    @Test
    @DisplayName("an unknown game id is empty, not an error")
    void unknownGame() {
        GameService games = service(new FakeModels.Scripted(List.of(FakeModels.answer("x"))));
        assertTrue(games.player("no-such-game").isEmpty());
        assertTrue(games.play("no-such-game", "hello").isEmpty());
    }
}
