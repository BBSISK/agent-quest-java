package quest.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import quest.store.GameRepository;

/** R4: the REST API, with MockMvc (no real server), a scripted model and an in-memory database. */
@DisplayName("R4: /api/games")
class GameControllerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(h2);
        GameService games = new GameService(new FakeModels.Scripted(List.of(FakeModels.answer("The tavern falls silent."))),
                "test-model", "You are a games master.", new GameRepository(h2));
        mvc = MockMvcBuilders.standaloneSetup(new GameController(games)).build();
    }

    private String newGameId() throws Exception {
        String body = mvc.perform(post("/api/games")).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.gameId");
    }

    private static String text(String s) {
        return "{\"text\": \"" + s + "\"}";
    }

    @Test
    @DisplayName("POST /api/games starts a game with a new character")
    void newGame() throws Exception {
        mvc.perform(post("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isNotEmpty())
                .andExpect(jsonPath("$.player.gold").value(10));
    }

    @Test
    @DisplayName("POST /api/games/{id}/turns returns the reply and the player")
    void playTurn() throws Exception {
        String id = newGameId();
        mvc.perform(post("/api/games/" + id + "/turns").contentType(MediaType.APPLICATION_JSON).content(text("I walk in.")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("The tavern falls silent."))
                .andExpect(jsonPath("$.player.hp").value(20));
    }

    @Test
    @DisplayName("an unknown game is 404")
    void unknownGame() throws Exception {
        mvc.perform(post("/api/games/no-such-game/turns").contentType(MediaType.APPLICATION_JSON).content(text("hello")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("blank text is 400")
    void blankText() throws Exception {
        String id = newGameId();
        mvc.perform(post("/api/games/" + id + "/turns").contentType(MediaType.APPLICATION_JSON).content(text("  ")))
                .andExpect(status().isBadRequest());
    }
}
