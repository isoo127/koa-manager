package koa.model.player;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Rank implements Serializable, Comparable<Rank> {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum RankType {
        KYU("급"), DAN("단"), UNRANKED("무급");

        private final String str;

        RankType(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    private final RankType type;
    private final int level;
    @Setter
    private int point;

    private Rank(int level, RankType type) {
        if(validLevel(type, level))
            throw new RuntimeException();
        this.type = type;
        this.level = level;
        this.point = 0;
    }

    private Rank(int level, RankType type, int point) {
        if(validLevel(type, level))
            throw new RuntimeException();
        this.type = type;
        this.level = level;
        this.point = point;
    }

    public static Rank of(int level, RankType type) {
        return new Rank(level, type);
    }

    public static Rank of(int level, RankType type, int point) {
        return new Rank(level, type, point);
    }

    public static Rank of(String rankStr) {
        if (rankStr == null || rankStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Rank 문자열이 null이거나 비어있습니다.");
        }

        rankStr = rankStr.trim();

        if (rankStr.equals("무급")) {
            return new Rank(0, RankType.UNRANKED);
        }

        Pattern pattern = Pattern.compile("^(\\d+)([급단])$");
        Matcher matcher = pattern.matcher(rankStr);
        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String typeStr = matcher.group(2);
            RankType type;
            if ("급".equals(typeStr)) {
                type = RankType.KYU;
            } else if ("단".equals(typeStr)) {
                type = RankType.DAN;
            } else {
                throw new IllegalArgumentException("알 수 없는 랭크 타입: " + typeStr);
            }
            return new Rank(value, type);
        } else {
            throw new IllegalArgumentException("올바르지 않은 랭크 형식: " + rankStr);
        }
    }

    public static List<String> getRankOptions() {
        ArrayList<String> result = new ArrayList<>();
        result.add(RankType.UNRANKED.toString());
        for (int i = 15; i >= 1; i--) {
            result.add(i + RankType.KYU.toString());
        }
        for (int i = 1; i <= 9; i++) {
            result.add(i + RankType.DAN.toString());
        }
        return result;
    }

    public static int getRequirePoint(Rank rank) {
        if (rank.type == RankType.KYU)
            return 300 - rank.point;
        if (rank.type == RankType.UNRANKED)
            return 300 - rank.point;

        // case of 'dan'
        int requirePoint = 0;
        switch (rank.level) {
            case 1:
                requirePoint = 200;
                break;
            case 2:
                requirePoint = 300;
                break;
            case 3:
                requirePoint = 400;
                break;
            case 4:
                requirePoint = 500;
                break;
            case 5:
                requirePoint = 700;
                break;
            case 6:
                requirePoint = 1000;
                break;
            case 7:
                requirePoint = 1500;
                break;
            case 8:
                requirePoint = 2500;
        }
        return requirePoint - rank.point;
    }

    private boolean validLevel(RankType type, int level) {
        switch (type) {
            case KYU -> {
                if (level <= 15 && level >= 1) return false;
            }
            case DAN -> {
                if (level <= 9 && level >= 1) return false;
            }
            case UNRANKED -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(Rank o) {
        if (this.type == RankType.UNRANKED) { // UNRANKED
            if (o.type == RankType.UNRANKED) return this.point - o.point;
            else return -1;
        }
        else if (this.type == RankType.DAN) { // DAN
            if (o.type == RankType.KYU || o.type == RankType.UNRANKED) return 1;
            else if (this.level == o.level) return this.point - o.point;
            else return this.level - o.level;
        } else { // KYU
            if (o.type == RankType.DAN) return -1;
            else if (o.type == RankType.UNRANKED) return 1;
            else if (this.level == o.level) return this.point - o.point;
            else return o.level - this.level;
        }
    }

    @Override
    public String toString() {
        if (type == RankType.UNRANKED) return type.toString();
        return level + type.toString();
    }

}
