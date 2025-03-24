package koa.model.player;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class Player implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private Rank rank;

    public Player(String name, Rank rank) {
        this.name = name;
        this.rank = rank;
    }

    @Override
    public String toString() {
        return name + " " + rank.toString();
    }

}
