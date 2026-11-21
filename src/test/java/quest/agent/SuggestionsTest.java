package quest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("J3: Suggestions.split")
class SuggestionsTest {

    @Test
    @DisplayName("cuts the SUGGESTIONS line off the story and splits it into options")
    void normal() {
        Suggestions s = Suggestions.split("You enter the tavern.\nSUGGESTIONS: Order an ale | Talk to the barkeep | Leave");
        assertEquals("You enter the tavern.", s.story());
        assertEquals(List.of("Order an ale", "Talk to the barkeep", "Leave"), s.options());
    }

    @Test
    @DisplayName("no SUGGESTIONS line: the story is unchanged and there are no options")
    void missing() {
        Suggestions s = Suggestions.split("The night is quiet.");
        assertEquals("The night is quiet.", s.story());
        assertEquals(List.of(), s.options());
    }

    @Test
    @DisplayName("any capitals, extra spaces and empty items are tidied")
    void messy() {
        Suggestions s = Suggestions.split("Rain falls.\nSuggestions:  Run |  | Hide  ");
        assertEquals("Rain falls.", s.story());
        assertEquals(List.of("Run", "Hide"), s.options());
    }

    @Test
    @DisplayName("keeps at most three options")
    void atMostThree() {
        Suggestions s = Suggestions.split("Hm.\nSUGGESTIONS: a | b | c | d");
        assertEquals(List.of("a", "b", "c"), s.options());
    }
}
