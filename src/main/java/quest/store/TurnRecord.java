package quest.store;

/** R4. One saved turn: what the player said and what the games master answered. */
public record TurnRecord(int turnNo, String playerText, String gmReply) { }
