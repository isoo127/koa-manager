package koa.model.player;

import koa.model.tournament.Result;
import koa.model.tournament.Game;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class Rating {

    private record RatingAdjustment(double blackAdjustment, double whiteAdjustment) { }

    @Getter
    private static class PlayerRecord {

        private final String name;
        private double rating;
        private int winCount;
        private int drawCount;
        private int loseCount;

        PlayerRecord(String name) {
            this.name = name;
            rating = 1200.0;
            winCount = 0;
            drawCount = 0;
            loseCount = 0;
        }

        public void updateRating(double val) {
            rating += val;
        }

        public void increaseWin() {
            winCount++;
        }

        public void increaseDraw() {
            drawCount++;
        }

        public void increaseLose() {
            loseCount++;
        }

    }

    private static final double K = 12;
    private static final double A = 0.5;
    private final HashMap<Long, PlayerRecord> records = new HashMap<>();

    private double calculateE(double d) {
        return 1 / (1 + Math.pow(10, -d / 400));
    }

    private double getWinAdjustmentVal(double ed, double weight) {
        return (1 - ed) * K * weight + A;
    }

    private double getLoseAdjustmentVal(double ed) {
        return (0 - ed) * K + A;
    }

    private double getDrawAdjustmentVal(double ed, double weight) {
        return (getWinAdjustmentVal(ed, weight) + getLoseAdjustmentVal(ed)) / 2;
    }

    private PlayerRecord getRecord(Player player) {
        return records.computeIfAbsent(player.getId(), k1 -> new PlayerRecord(player.getName()));
    }

    private RatingAdjustment calculateRatingAdjustment(PlayerRecord blackRecord, PlayerRecord whiteRecord,
                                                        Result result, double weight) {
        double bd = blackRecord.getRating() - whiteRecord.getRating();
        double wd = bd * -1;

        double ebd = calculateE(bd);
        double ewd = calculateE(wd);

        return switch(result) {
            case BLACK_WIN -> new RatingAdjustment(
                    getWinAdjustmentVal(ebd, weight),
                    getLoseAdjustmentVal(ewd));
            case WHITE_WIN -> new RatingAdjustment(
                    getLoseAdjustmentVal(ebd),
                    getWinAdjustmentVal(ewd, weight));
            case DRAW -> new RatingAdjustment(
                    getDrawAdjustmentVal(ebd, weight),
                    getDrawAdjustmentVal(ewd, weight));
        };
    }

    private void recordGameResult(PlayerRecord blackRecord, PlayerRecord whiteRecord, Result result) {
        switch (result) {
            case BLACK_WIN -> {
                blackRecord.increaseWin();
                whiteRecord.increaseLose();
            }
            case WHITE_WIN -> {
                whiteRecord.increaseWin();
                blackRecord.increaseLose();
            }
            case DRAW -> {
                blackRecord.increaseDraw();
                whiteRecord.increaseDraw();
            }
        }
    }

    public void updateTournamentRatings(List<Game> games, LongFunction<Player> playerFinder, double weight) {
        Map<PlayerRecord, Double> ratingAdjustments = new HashMap<>();

        for (Game game : games) {
            PlayerRecord blackRecord = getRecord(playerFinder.apply(game.getBlackPlayerId()));
            PlayerRecord whiteRecord = getRecord(playerFinder.apply(game.getWhitePlayerId()));
            RatingAdjustment adjustment = calculateRatingAdjustment(
                    blackRecord, whiteRecord, game.getResult(), weight);

            ratingAdjustments.merge(blackRecord, adjustment.blackAdjustment, Double::sum);
            ratingAdjustments.merge(whiteRecord, adjustment.whiteAdjustment, Double::sum);
            recordGameResult(blackRecord, whiteRecord, game.getResult());
        }

        for (Map.Entry<PlayerRecord, Double> entry : ratingAdjustments.entrySet()) {
            entry.getKey().updateRating(entry.getValue());
        }
    }

    public Object[][] getRanking() {
        ArrayList<Object[]> ranking = new ArrayList<>();

        List<PlayerRecord> sortedRecords = new ArrayList<>(records.values());
        sortedRecords.sort(Comparator.comparingDouble(PlayerRecord::getRating).reversed());
        int rank = 1;
        for (PlayerRecord rec : sortedRecords) {
            if (rec.winCount + rec.drawCount + rec.loseCount >= 6) {
                rank = getRank(ranking, rank, rec);
            }
        }

        return ranking.toArray(new Object[ranking.size()][]);
    }

    public Object[][] getAllRanking() {
        ArrayList<Object[]> ranking = new ArrayList<>();

        List<PlayerRecord> sortedRecords = new ArrayList<>(records.values());
        sortedRecords.sort(Comparator.comparingDouble(PlayerRecord::getRating).reversed());
        int rank = 1;
        for (PlayerRecord rec : sortedRecords) {
            rank = getRank(ranking, rank, rec);
        }

        return ranking.toArray(new Object[ranking.size()][]);
    }

    private int getRank(ArrayList<Object[]> ranking, int rank, PlayerRecord rec) {
        Object[] row = new Object[6];
        row[0] = rank++;
        row[1] = rec.name;
        row[2] = Math.round(rec.rating * 100.0) / 100.0;
        row[3] = rec.winCount;
        row[4] = rec.drawCount;
        row[5] = rec.loseCount;
        ranking.add(row);
        return rank;
    }

    public void resetRanking() {
        records.clear();
    }

}
