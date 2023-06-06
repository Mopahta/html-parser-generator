package betbonanza.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class EventDTO {
    @Getter
    String startTime;
    @Getter
    String tournament;
    @Getter
    String sportsKind;
    @Getter
    String link;
    @Getter
    String firstTeam;
    @Getter
    String secondTeam;
}
