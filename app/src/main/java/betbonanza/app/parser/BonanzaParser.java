package betbonanza.app.parser;

import betbonanza.app.dto.EventDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BonanzaParser implements Parser {
    private String modifyStartTime(String startTime) throws ParseException {

        startTime = startTime.substring(startTime.indexOf(" ") + 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy dd MMM HH:mm", Locale.ENGLISH);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        Date parsedDate = dateFormat.parse(year + " " + startTime);
        Timestamp timestamp = new Timestamp(parsedDate.getTime());

        startTime = timestamp.toString();
        startTime = startTime.substring(0, startTime.lastIndexOf(":"));

        return startTime;
    }
    @Override
    public List<EventDTO> parse (String url) {
        ArrayList<EventDTO> events = new ArrayList<>();

        try {
            Document bets = Jsoup.connect(url).get();
            Elements elements = bets.select("table.highlights--item");

            elements.forEach(x -> {
                String startTime = x.select("tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > a:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1)").text();

                try {
                    startTime = modifyStartTime(startTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                    System.err.printf("Error during parsing time %s\n Current year %d\n", startTime, Calendar.getInstance().get(Calendar.YEAR));
                    return;
                }

                String eventInfo = x.select("tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > a:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(3) > td:nth-child(1) > span:nth-child(1)").text();
                int dividerIndex = eventInfo.indexOf("/");

                String tournament = eventInfo.substring(dividerIndex + 2);
                String sportsKind = eventInfo.substring(0, dividerIndex);

                String link = x.select("tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > a:nth-child(1)").attr("abs:href");
                String firstTeam = x.select("tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > a:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > p:nth-child(1) > span:nth-child(1)")
                        .text();
                String secondTeam = x.select("tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > a:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > p:nth-child(2) > span:nth-child(1)")
                        .text();

                events.add(new EventDTO(startTime, tournament, sportsKind, link, firstTeam, secondTeam));
            });
        }
        catch (IOException e) {
            System.err.printf("Error while fetching site %s.\n", url);
        }

        return events;
    }
}
