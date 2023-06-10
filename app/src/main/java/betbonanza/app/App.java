package betbonanza.app;

import betbonanza.app.dto.EventDTO;
import betbonanza.app.generator.FormatGenerator;
import betbonanza.app.generator.HtmlGenerator;
import betbonanza.app.parser.BonanzaParser;
import betbonanza.app.parser.Parser;
import betbonanza.app.saver.FileSaver;
import betbonanza.app.saver.Saver;

import java.util.List;

public class App {
    public static void main(String[] args) {
        Parser bonanzaParser = new BonanzaParser();

        List<EventDTO> events = bonanzaParser.parse("https://lite.betbonanza.com/sports");

        FormatGenerator htmlGenerator = new HtmlGenerator();

        System.out.println("Generating html...");
        String html = htmlGenerator.generate(events);

        Saver fileSaver = new FileSaver();
        System.out.println("Saving to output.html...");
        fileSaver.save(html);
    }
}
