package betbonanza.app.generator;

import betbonanza.app.dto.EventDTO;

import java.util.List;

public interface FormatGenerator {
    String generate(List<EventDTO> events);
}
