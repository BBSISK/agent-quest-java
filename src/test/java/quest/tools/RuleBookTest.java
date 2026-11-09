package quest.tools;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Step 1b: RuleBook")
class RuleBookTest {

    private static final String TEXT = """
            # Test rules

            ## Combat
            Roll a d20 + strength. 12 or higher hits.

            ## Tavern
            Ale costs 1 gold.
            Arm wrestling: both roll d20 + strength.
            """;

    @Test
    @DisplayName("finds the section whose title or text contains the topic, ignoring case")
    void findsSection() {
        RuleBook book = new RuleBook(TEXT);
        String out = book.lookup("ARM WRESTLING");
        assertTrue(out.startsWith("Rule book: Tavern"), out);
        assertTrue(out.contains("both roll d20"), out);
        assertFalse(out.contains("12 or higher"), "only the matching section: " + out);
    }

    @Test
    @DisplayName("a miss lists the section titles so the model can try again")
    void missListsSections() {
        String out = new RuleBook(TEXT).lookup("dragons");
        assertTrue(out.startsWith("No rules found for 'dragons'"), out);
        assertTrue(out.contains("Combat") && out.contains("Tavern"), out);
    }

    @Test
    @DisplayName("loads the real rule book from src/main/resources")
    void loadsResource() {
        assertTrue(RuleBook.fromResource("/rulebook.txt").lookup("fireball").contains("3d6"));
    }
}
