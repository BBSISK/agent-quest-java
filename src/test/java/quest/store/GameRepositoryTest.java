package quest.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import quest.tools.PlayerState;

/** R3: the repository against a fresh in-memory H2 database (MySQL mode) built from schema.sql. No AWS. */
@DisplayName("R3: GameRepository")
class GameRepositoryTest {

    private JdbcDataSource dataSource;
    private GameRepository repository;

    @BeforeEach
    void freshDatabase() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        repository = new GameRepository(dataSource);
    }

    private int count(String table) {
        return new JdbcTemplate(dataSource).queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    @Test
    @DisplayName("createGame writes the game, the player and each item")
    void createGame() {
        repository.createGame("g1", "test-model", PlayerState.newCharacter());
        assertEquals(1, count("games"));
        assertEquals(1, count("player_state"));
        assertEquals(2, count("inventory_items"));
    }

    @Test
    @DisplayName("R3c: what you save is what you load, items in order")
    void loadAfterCreate() {
        repository.createGame("g1", "test-model", PlayerState.newCharacter());
        assertEquals(Optional.of(PlayerState.newCharacter()), repository.loadPlayer("g1"));
    }

    @Test
    @DisplayName("R3c: an unknown game loads as empty, not null and not an exception")
    void unknownGame() {
        assertTrue(repository.loadPlayer("no-such-game").isEmpty());
    }

    @Test
    @DisplayName("R3b: savePlayer updates the numbers and replaces the inventory")
    void saveThenLoad() {
        repository.createGame("g1", "test-model", PlayerState.newCharacter());
        PlayerState later = new PlayerState(16, 20, 3, 2, 15, List.of("leather armor", "torch"));
        repository.savePlayer("g1", later);
        assertEquals(Optional.of(later), repository.loadPlayer("g1"));
        assertEquals(2, count("inventory_items"));
    }

    @Test
    @DisplayName("the database refuses negative gold, and the transaction leaves no half-made game")
    void checkConstraintRollsBack() {
        PlayerState broke = new PlayerState(20, 20, 3, 3, -5, List.of("rusty sword"));
        assertThrows(DataAccessException.class, () -> repository.createGame("g2", "test-model", broke));
        assertEquals(0, count("games"), "the games row was rolled back with the failed player row");
    }
}
