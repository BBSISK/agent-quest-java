package quest.agent;

import java.util.Arrays;
import java.util.List;

/** J3. A games-master reply split into the story and up to three suggested next actions. */
public record Suggestions(String story, List<String> options) {

    static final String MARKER = "suggestions:";   // compared in lower case

    public static Suggestions split(String reply) {
        // YOUR CODE:
        int at = reply.toLowerCase().lastIndexOf(MARKER);
        if(at == -1) {
                return new Suggestions(reply.strip(), List.of());
        }
    
        String story = reply.substring(0, at).strip();
        String text = reply.substring(at + MARKER.length());
      
        Arrays.stream(text.split("\\|")).map(String::strip).filter(s -> !s.isEmpty()).limit(3).toList();

                List<String> options = Arrays.stream(text.split("\\|"))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .limit(3)
                .toList();
        return new Suggestions(story, options);
    }
}
