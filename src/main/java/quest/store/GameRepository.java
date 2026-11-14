package quest.store;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import quest.agent.Agent;
import quest.tools.PlayerState;

/**
 * R3. Saves and loads games with plain SQL through Spring's JdbcTemplate.
 *
 * JdbcTemplate opens and closes connections for you, and every "?" in the SQL is filled in safely
 * by the driver: user text never becomes part of the SQL itself, so SQL injection can't happen.
 * TransactionTemplate runs several statements as one unit: all succeed, or none are kept.
 */
public class GameRepository {

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public GameRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    /** A new game: one row in games, one in player_state, one per item. Done for you, as the example. */
    public void createGame(String gameId, String modelId, PlayerState state) {
        transaction.executeWithoutResult(status -> {
            jdbc.update("INSERT INTO games (id, model_id) VALUES (?, ?)", gameId, modelId);
            jdbc.update("INSERT INTO player_state (game_id, hp, max_hp, strength, spell_slots, gold) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    gameId, state.hp(), state.maxHp(), state.strength(), state.spellSlots(), state.gold());
            insertItems(gameId, state.inventory());
        });
    }

    /**
     * R3b. Save the player's state after a turn. Inside ONE transaction:
     *   1. UPDATE player_state SET hp = ?, max_hp = ?, strength = ?, spell_slots = ?, gold = ? WHERE game_id = ?
     *   2. DELETE FROM inventory_items WHERE game_id = ?
     *   3. insertItems(gameId, state.inventory())
     * (Deleting and re-inserting the items is the simplest way to make the table match the list.)
     */
    public void savePlayer(String gameId, PlayerState state) {
        transaction.executeWithoutResult(status -> {
            jdbc.update("UPDATE player_state SET hp = ?, max_hp = ?, strength = ?, spell_slots = ?, gold = ? WHERE game_id = ?",
                    state.hp(), state.maxHp(), state.strength(), state.spellSlots(), state.gold(), gameId);
            jdbc.update("DELETE FROM inventory_items WHERE game_id = ?", gameId);

            insertItems(gameId, state.inventory());
        });
    }

    /**
     * R3c. Load a game's player, or Optional.empty() if there's no such game.
     *   1. List<String> items = jdbc.queryForList(
     *          "SELECT item FROM inventory_items WHERE game_id = ? ORDER BY id", String.class, gameId);
     *   2. List<PlayerState> rows = jdbc.query(
     *          "SELECT hp, max_hp, strength, spell_slots, gold FROM player_state WHERE game_id = ?",
     *          (rs, rowNum) -> new PlayerState(rs.getInt("hp"), ... , items),
     *          gameId);
     *   3. return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
     * ORDER BY id keeps the items in the order they were added.
     */
    public Optional<PlayerState> loadPlayer(String gameId) {
  
                    List<String> items = jdbc.queryForList(
                    "SELECT item FROM inventory_items WHERE game_id = ? ORDER BY id", String.class, gameId);
                    List<PlayerState> rows = jdbc.query(
                            "SELECT hp, max_hp, strength, spell_slots, gold FROM player_state WHERE game_id = ?",
                            (rs, rowNum) -> new PlayerState(rs.getInt("hp"), rs.getInt("max_hp"), rs.getInt("strength"),
                                    rs.getInt("spell_slots"), rs.getInt("gold"), items),
                            gameId);
                    return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
                }

    /**
     * R4. Save a finished turn: the player's state, the turn, and every tool the agent ran in it,
     * all in ONE transaction. Done for you. (savePlayer joins this transaction rather than starting its own.)
     */
    public void saveTurn(String gameId, int turnNo, String playerText, String reply,
                         List<Agent.ToolCall> calls, PlayerState state) {
        transaction.executeWithoutResult(status -> {
            savePlayer(gameId, state);
            jdbc.update("INSERT INTO turns (game_id, turn_no, player_text, gm_reply) VALUES (?, ?, ?, ?)",
                    gameId, turnNo, playerText, reply);
            Integer turnId = jdbc.queryForObject(
                    "SELECT id FROM turns WHERE game_id = ? AND turn_no = ?", Integer.class, gameId, turnNo);
            int seq = 1;
            for (Agent.ToolCall call : calls) {
                jdbc.update("INSERT INTO tool_calls (turn_id, seq, tool_name, input, result) VALUES (?, ?, ?, ?, ?)",
                        turnId, seq++, call.name(), call.input(), call.result());
            }
        });
    }

    /**
     * R4b. A game's turns, oldest first, so a restarted server can remind the agent of the story so far.
     *   SELECT turn_no, player_text, gm_reply FROM turns WHERE game_id = ? ORDER BY turn_no
     * mapped to new TurnRecord(rs.getInt("turn_no"), rs.getString("player_text"), rs.getString("gm_reply")).
     * Same shape as the second query in loadPlayer.
     */
    public List<TurnRecord> loadTurns(String gameId) {
        return jdbc.query(
                "SELECT turn_no, player_text, gm_reply FROM turns WHERE game_id = ? ORDER BY turn_no",
                (rs, rowNum) -> new TurnRecord(rs.getInt("turn_no"), rs.getString("player_text"), rs.getString("gm_reply")),
                gameId);

    }

    private void insertItems(String gameId, List<String> items) {
        for (String item : items) {
            jdbc.update("INSERT INTO inventory_items (game_id, item) VALUES (?, ?)", gameId, item);
        }
    }
}
