package quest.tools;

/**
 * Tool 2: look up a rule. The rule book is a text file split into sections that start with "## ".
 * A simple keyword search: in a real system this would be retrieval (RAG), like Ask Barry.
 *
 * Step 1b. Make RuleBookTest pass.
 */
public class RuleBook {

    // TODO 1: a private final String[] sections field.

    /** @param text the whole rule book. Split it on "## ": sections[0] is the title, the rest are sections. */
    public RuleBook(String text) {
        // TODO 1
    }

    /**
     * Load the rule book from src/main/resources, e.g. fromResource("/rulebook.txt").
     * Hint: RuleBook.class.getResourceAsStream(name), then new String(in.readAllBytes(), StandardCharsets.UTF_8).
     * Use try-with-resources, and throw an IllegalArgumentException if the stream is null (file not found).
     */
    public static RuleBook fromResource(String name) {
        // TODO 2
        return null;
    }

    /**
     * @return the first section (from index 1) whose text contains the topic, ignoring case:
     *         "Rule book: " + that section, with strip() to remove the blank lines around it.
     *         No match: "No rules found for 'dragons'. Sections: Skill checks, Combat, Magic, Tavern"
     *         (a section's title is its first line: section.lines().findFirst()).
     */
    public String lookup(String topic) {
        // TODO 3: loop over the sections, compare in lower case.
        // TODO 4: no match: collect the titles in a List<String> and String.join(", ", titles).
        return "";
    }
}
