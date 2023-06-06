package betbonanza.app.generator;

import betbonanza.app.dto.EventDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;

public class HtmlGenerator implements FormatGenerator {
    @Override
    public String generate (List<EventDTO> events) {
        Document doc;
        try {
            doc = Jsoup.parse(HtmlGenerator.class.getResourceAsStream("/template.html"), null, "/");
        }
        catch (IOException e) {
            System.err.println("Couldn't open file template.html.");
            return null;
        }

        Element table = doc.body().selectFirst(".ui > tbody");

        if (table == null) {
            System.err.println("Couldn't find table.");
            return null;
        }
        events.forEach(event ->
            table.append(String.format("""
                        <tr>
                            <td class="collapsing">%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%s</td>
                            <td><a href="%s" class="right aligned collapsing">%s</a></td>
                        </tr>
                    """, event.getSportsKind(), event.getTournament(), event.getFirstTeam(),
                    event.getSecondTeam(), event.getLink(), event.getStartTime()))
        );

        return doc.toString();
    }
}
