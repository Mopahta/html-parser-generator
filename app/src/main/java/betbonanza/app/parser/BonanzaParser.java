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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BonanzaParser implements Parser {

    private final ThreadPoolExecutor executorService;
    private final Object parseStarted = new Object();
    private final Object parseEnded = new Object();
    private final LinkedList<LinkedList<LinkedList<LinkedList<EventDTO>>>> matches;

    public BonanzaParser() {
        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executorService.setKeepAliveTime(5, TimeUnit.SECONDS);
        executorService.allowCoreThreadTimeOut(true);
        matches = new LinkedList<>();
    }

    @Override
    public List<EventDTO> parse(String url) {
        ArrayList<EventDTO> events = new ArrayList<>();

        executorService.submit(() -> {
            try {
                parseSportsKind(url, matches);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });

        try {
            synchronized (parseStarted) {
                parseStarted.wait(2000);
            }
            synchronized (parseEnded) {
                parseEnded.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        matches.forEach(sportsKind ->
                sportsKind.forEach(region ->
                        region.forEach(tournament ->
                                tournament.forEach(event -> events.add(event)))));

        System.out.println("Parsed events amount: " + events.size());
        return events;
    }

    private void parseSportsKind(String url, LinkedList<LinkedList<LinkedList<LinkedList<EventDTO>>>> sportsKinds) throws IOException {
        synchronized (parseStarted) {
            parseStarted.notify();
        }

        Document doc = Jsoup.connect(url).get();

        Elements elements = doc.select("tr.menu > td > table");

        elements.forEach(element -> {
            String link = element.select("tbody:nth-child(1) > tr:nth-child(1) > td > a").attr("abs:href");

            if (link.length() != 0) {
                LinkedList<LinkedList<LinkedList<EventDTO>>> regions = new LinkedList<>();
                sportsKinds.add(regions);

                executorService.submit(() -> {
                    try {
                        parseRegions(link, regions);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void parseRegions(String url, LinkedList<LinkedList<LinkedList<EventDTO>>> regions) throws IOException {
        Document doc = Jsoup.connect(url).get();

        Elements elements = doc.select("tr.menu > td > table");

        elements.forEach(element -> {
            String link = element.select("tbody:nth-child(1) > tr:nth-child(1) > td > a").attr("abs:href");

            if (link.length() != 0) {
                LinkedList<LinkedList<EventDTO>> tournaments = new LinkedList<>();
                regions.add(tournaments);

                executorService.submit(() -> {
                    try {
                        parseTournament(link, tournaments);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }

    private void parseTournament(String url, LinkedList<LinkedList<EventDTO>> tournaments) throws IOException {
        Document doc = Jsoup.connect(url).get();

        Elements elements = doc.select("tr.menu > td > table");

        elements.forEach(element -> {
            String link = element.select("tbody:nth-child(1) > tr:nth-child(1) > td > a").attr("abs:href");

            if (link.length() != 0) {
                LinkedList<EventDTO> matches = new LinkedList<>();
                tournaments.add(matches);

                executorService.submit(() -> {
                    try {
                        parseMatchInfo(link, matches);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }
    private void parseMatchInfo(String url, LinkedList<EventDTO> matches) throws IOException {
        Document doc = Jsoup.connect(url).get();

        Elements elements = doc.select("table.highlights--item");

        elements.forEach(x -> {
            String startTime = x.select("tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > a:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1)").text();

            if (startTime.length() == 0) {
                return;
            }

            try {
                startTime = modifyStartTime(startTime);
            } catch (ParseException e) {
                e.printStackTrace();
                System.err.printf("Error during parsing time %s\nCurrent year %d\n%s\n", startTime, Calendar.getInstance().get(Calendar.YEAR), url);
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

            matches.add(new EventDTO(startTime, tournament, sportsKind, link, firstTeam, secondTeam));
        });

        if (executorService.getActiveCount() == 1) {
            System.out.println("notifying that parse ended");
            synchronized (parseEnded) {
                parseEnded.notify();
            }
        }
    }

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
}
