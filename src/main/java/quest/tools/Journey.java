package quest.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Where the player is, and the places they've been (J1). Used by the set_location tool. */
public class Journey {

    public static final Set<String> KINDS =
            Set.of("village", "tavern", "market", "forest", "road", "cave", "castle", "temple");

    private String place = "Village";
    private String kind = "village";
    private final List<String> visited = new ArrayList<>(List.of("Village"));

    /** Move the player. Returns a short result for the model, e.g. "Now at The Rusty Flagon (tavern)". */
    public String moveTo(String newPlace, String newKind) {
        this.place = newPlace;
        this.kind = KINDS.contains(newKind) ? newKind : "road";
        if (!visited.getLast().equals(newPlace)) visited.add(newPlace);
        return "Now at " + place + " (" + kind + ")";
    }


    public String place() { return place; }
    public String kind() { return kind; }
    public List<String> visited() { return List.copyOf(visited); }
}
