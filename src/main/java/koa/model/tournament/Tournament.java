package koa.model.tournament;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

@Getter
@Setter
public class Tournament implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Setter
    private long id;
    private String name;
    private LocalDate startDate;
    private double weight;
    private ArrayList<Long> gameIds;

    public Tournament(String name, LocalDate startDate, double weight) {
        this.name = name;
        this.startDate = startDate;
        this.weight = weight;
        this.gameIds = new ArrayList<>();
    }

}
