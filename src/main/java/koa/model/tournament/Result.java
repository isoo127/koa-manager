package koa.model.tournament;

public enum Result {

    BLACK_WIN("흑승"), WHITE_WIN("백승"), DRAW("무승부");

    private final String name;

    Result(String str) {
        this.name = str;
    }

    @Override
    public String toString() {
        return name;
    }

    public static String[] getResultListName() {
        Result[] results = values();
        String[] resultNames = new String[results.length];
        for (int i = 0; i < results.length; i++) {
            resultNames[i] = results[i].toString();
        }
        return resultNames;
    }

    public static Result of(String str) {
        for (Result result : values()) {
            if (result.toString().equals(str)) {
                return result;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 결과: " + str);
    }

}
