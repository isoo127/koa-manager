package koa.model.tournament;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class Game implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Setter
    private long id;
    private long tournamentId;
    private long blackPlayerId;
    private long whitePlayerId;
    private Result result;
    private String note;

    public Game(long tournamentId, long blackPlayerId, long whitePlayerId, Result result, String note) {
        this.tournamentId = tournamentId;
        this.blackPlayerId = blackPlayerId;
        this.whitePlayerId = whitePlayerId;
        this.result = result;
        this.note = note;
    }

}
