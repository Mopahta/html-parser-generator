package betbonanza.app.parser;

import betbonanza.app.dto.EventDTO;

import java.util.List;

public interface Parser {
    List<EventDTO> parse(String url);
}
