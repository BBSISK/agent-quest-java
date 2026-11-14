package quest.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import quest.tools.PlayerState;

/**
 * R4. The game's REST API.
 *   POST /api/games                  start a game            -> {gameId, player}
 *   GET  /api/games/{id}             the player's state      -> PlayerState, or 404
 *   POST /api/games/{id}/turns       {"text": "I attack!"}   -> {reply, toolCalls, player}, 404, or 400
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    static final int MAX_TEXT = 500;

    public record NewGame(String gameId, PlayerState player) { }

    public record TurnRequest(String text) { }

    private final GameService games;

    public GameController(GameService games) {
        this.games = games;
    }

    @PostMapping
    public NewGame newGame() {
        String id = games.newGame();
        return new NewGame(id, games.player(id).orElseThrow());
    }

    @GetMapping("/{id}")
    public PlayerState player(@PathVariable("id") String id) {
        return games.player(id).orElseThrow(() -> notFound(id));
    }

    @PostMapping("/{id}/turns")
    public GameService.TurnResult turn(@PathVariable("id") String id, @RequestBody TurnRequest request) {
        if (request == null || request.text() == null || request.text().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text is required");
        }
        if (request.text().length() > MAX_TEXT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text is limited to " + MAX_TEXT + " characters");
        }
        return games.play(id, request.text().strip()).orElseThrow(() -> notFound(id));
    }

    private static ResponseStatusException notFound(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No game " + id);
    }
}
