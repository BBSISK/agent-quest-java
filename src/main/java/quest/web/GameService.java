package quest.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import quest.agent.Agent;
import quest.agent.ModelClient;
import quest.agent.ToolBox;
import quest.store.GameRepository;
import quest.store.TurnRecord;
import quest.tools.Dice;
import quest.tools.Player;
import quest.tools.PlayerState;
import quest.tools.RuleBook;

/**
 * R4. Runs many games at once. Each game id has its own Agent and Player in memory; the database is the
 * source of truth, so after a restart a game is rebuilt from it: the player from player_state, and the
 * story so far from turns (fed back to the agent with remember()).
 */
public class GameService {

    /** What one turn returns to the browser. */
    public record TurnResult(String reply, List<Agent.ToolCall> toolCalls, PlayerState player) { }

    private static final class Session {
        final Agent agent;
        final Player player;
        int turns;

        Session(Agent agent, Player player, int turns) {
            this.agent = agent;
            this.player = player;
            this.turns = turns;
        }
    }

    private final ModelClient model;
    private final String modelId;
    private final String systemPrompt;
    private final GameRepository repository;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public GameService(ModelClient model, String modelId, String systemPrompt, GameRepository repository) {
        this.model = model;
        this.modelId = modelId;
        this.systemPrompt = systemPrompt;
        this.repository = repository;
    }

    /** Start a game: saved to the database first, then kept in memory. Returns its id. */
    public String newGame() {
        String id = UUID.randomUUID().toString();
        PlayerState start = PlayerState.newCharacter();
        repository.createGame(id, modelId, start);
        sessions.put(id, open(start, List.of()));
        return id;
    }

    public Optional<PlayerState> player(String gameId) {
        return session(gameId).map(s -> s.player.snapshot());
    }

    /** Play one turn and save it (player state, turn, tool calls) in one transaction. Empty if no such game. */
    public Optional<TurnResult> play(String gameId, String playerText) {
        Optional<Session> found = session(gameId);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Session s = found.get();
        synchronized (s) {                                   // one turn at a time per game
            int before = s.agent.toolCalls().size();
            String reply = s.agent.ask(playerText);
            List<Agent.ToolCall> calls = List.copyOf(s.agent.toolCalls().subList(before, s.agent.toolCalls().size()));
            s.turns++;
            PlayerState state = s.player.snapshot();
            repository.saveTurn(gameId, s.turns, playerText, reply, calls, state);
            return Optional.of(new TurnResult(reply, calls, state));
        }
    }

    /** From memory if we have it; otherwise rebuilt from the database (e.g. after a restart). */
    private Optional<Session> session(String gameId) {
        Session cached = sessions.get(gameId);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<PlayerState> saved = repository.loadPlayer(gameId);
        if (saved.isEmpty()) {
            return Optional.empty();
        }
        Session rebuilt = open(saved.get(), repository.loadTurns(gameId));
        Session existing = sessions.putIfAbsent(gameId, rebuilt);
        return Optional.of(existing != null ? existing : rebuilt);
    }

    private Session open(PlayerState state, List<TurnRecord> turns) {
        Player player = new Player(state);
        ToolBox tools = new ToolBox(new Dice(new Random()), RuleBook.fromResource("/rulebook.txt"), player);
        Agent agent = new Agent(model, modelId, systemPrompt, tools);
        for (TurnRecord turn : turns) {
            agent.remember(turn.playerText(), turn.gmReply());
        }
        return new Session(agent, player, turns.size());
    }
}
