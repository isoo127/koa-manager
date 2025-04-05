package koa.model.player;

import koa.model.tournament.Result;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Rating {

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

    private final double k = 12;
    private final double a = 0.5;
    private final HashMap<Long, PlayerRecord> records = new HashMap<>();

    private double calculateE(double d) {
        return 1 / (1 + Math.pow(10, -d / 400));
    }

    private double getWinAdjustmentVal(double ed, double weight) {
        return (1 - ed) * k * weight + a;
    }

    private double getLoseAdjustmentVal(double ed) {
        return (0 - ed) * k + a;
    }

    private double getDrawAdjustmentVal(double ed, double weight) {
        return (getWinAdjustmentVal(ed, weight) + getLoseAdjustmentVal(ed)) / 2;
    }

    private PlayerRecord getRecord(Player player) {
        return records.computeIfAbsent(player.getId(), k1 -> new PlayerRecord(player.getName()));
    }

    public void updateRating(Player blackPlayer, Player whitePlayer, Result result, double weight) {
        PlayerRecord blackRecord = getRecord(blackPlayer);
        PlayerRecord whiteRecord = getRecord(whitePlayer);

        double bd = blackRecord.getRating() - whiteRecord.getRating();
        double wd = bd * -1;

        double ebd = calculateE(bd);
        double ewd = calculateE(wd);

        switch(result) {
            case BLACK_WIN -> {
                blackRecord.updateRating(getWinAdjustmentVal(ebd, weight));
                whiteRecord.updateRating(getLoseAdjustmentVal(ewd));
                blackRecord.increaseWin();
                whiteRecord.increaseLose();
            }
            case WHITE_WIN -> {
                whiteRecord.updateRating(getWinAdjustmentVal(ewd, weight));
                blackRecord.updateRating(getLoseAdjustmentVal(ebd));
                whiteRecord.increaseWin();
                blackRecord.increaseLose();
            }
            case DRAW -> {
                blackRecord.updateRating(getDrawAdjustmentVal(ebd, weight));
                whiteRecord.updateRating(getDrawAdjustmentVal(ewd, weight));
                blackRecord.increaseDraw();
                whiteRecord.increaseDraw();
            }
        }
    }

    public Object[][] getRanking() {
        ArrayList<Object[]> ranking = new ArrayList<>();

        List<PlayerRecord> sortedRecords = new ArrayList<>(records.values());
        sortedRecords.sort(Comparator.comparingDouble(PlayerRecord::getRating).reversed());
        int rank = 1;
        for (PlayerRecord record : sortedRecords) {
            if (record.winCount + record.drawCount + record.loseCount >= 6) {
                Object[] row = new Object[6];
                row[0] = rank++;
                row[1] = record.name;
                row[2] = Math.round(record.rating * 100.0) / 100.0;;
                row[3] = record.winCount;
                row[4] = record.drawCount;
                row[5] = record.loseCount;
                ranking.add(row);
            }
        }

        return ranking.toArray(new Object[ranking.size()][]);
    }

    public Object[][] getAllRanking() {
        ArrayList<Object[]> ranking = new ArrayList<>();

        List<PlayerRecord> sortedRecords = new ArrayList<>(records.values());
        sortedRecords.sort(Comparator.comparingDouble(PlayerRecord::getRating).reversed());
        int rank = 1;
        for (PlayerRecord record : sortedRecords) {
            Object[] row = new Object[6];
            row[0] = rank++;
            row[1] = record.name;
            row[2] = Math.round(record.rating * 100.0) / 100.0;;
            row[3] = record.winCount;
            row[4] = record.drawCount;
            row[5] = record.loseCount;
            ranking.add(row);
        }

        return ranking.toArray(new Object[ranking.size()][]);
    }

    public void resetRanking() {
        records.clear();
    }

}
