package koa.model.tournament;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

@Getter
public class Game implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Setter
    private long id;
    private final long tournamentId;
    private final long blackPlayerId;
    private final long whitePlayerId;
    private final Result result;
    private final String note;

    public Game(long tournamentId, long blackPlayerId, long whitePlayerId, Result result, String note) {
        this.tournamentId = tournamentId;
        this.blackPlayerId = blackPlayerId;
        this.whitePlayerId = whitePlayerId;
        this.result = result;
        this.note = note;
    }

}
