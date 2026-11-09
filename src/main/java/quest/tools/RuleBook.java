package quest.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Tool 2: look up a rule. The rule book is a text file split into sections that start with "## ".
 * A simple keyword search: in a real system this would be retrieval (RAG), like Ask Barry.
 *
 * Step 1b. Make RuleBookTest pass.
 */
public class RuleBook {

    // TODO 1: a private final String[] sections field.
    private final String[] sections;

    /** @param text the whole rule book. Split it on "## ": sections[0] is the title, the rest are sections. */
    public RuleBook(String text) {
        // TODO 1
        this.sections = text.split("## ");  
    }

    /**
     * Load the rule book from src/main/resources, e.g. fromResource("/rulebook.txt").
     * Hint: RuleBook.class.getResourceAsStream(name), then new String(in.readAllBytes(), StandardCharsets.UTF_8).
     * Use try-with-resources, and throw an IllegalArgumentException if the stream is null (file not found).
     */
    public static RuleBook fromResource(String name) {
        // TODO 2

        try (InputStream in = RuleBook.class.getResourceAsStream(name)) {
    if (in == null) {
        throw new IllegalArgumentException("No rule book at " + name);
    }
        return new RuleBook(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    } catch (IOException e) {
        throw new UncheckedIOException(e);
    }
    }

    /**
     * @return the first section (from index 1) whose text contains the topic, ignoring case:
     *         "Rule book: " + that section, with strip() to remove the blank lines around it.
     *         No match: "No rules found for 'dragons'. Sections: Skill checks, Combat, Magic, Tavern"
     *         (a section's title is its first line: section.lines().findFirst()).
     */
    public String lookup(String topic) {
        // TODO 3: loop over the sections, compare in lower case.

       
            for (int i = 1; i < sections.length; i++) {
                String section = sections[i];
                if (section.toLowerCase().contains(topic.toLowerCase())) {
                    return "Rule book: " + section.strip();
                }
            }


        // TODO 4: no match: collect the titles in a List<String> and String.join(", ", titles).

       
            StringBuilder titles = new StringBuilder();
            for (int i = 1; i < sections.length; i++) {
                String section = sections[i];
                String title = section.lines().findFirst().orElse("");
                titles.append(title);
                if (i < sections.length - 1) {
                    titles.append(", ");
                }
            }
            return "No rules found for '" + topic + "'. Sections: " + titles.toString();
        
    }
}
