package koa.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import koa.model.player.Player;
import koa.model.player.Rank;
import koa.model.tournament.Game;
import koa.model.tournament.Result;
import koa.model.tournament.Tournament;
import koa.repository.GameRepository;
import koa.repository.PlayerRepository;
import koa.repository.TournamentRepository;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

public class CsvUtil {

    public static void loadPlayersFromCsv(String filePath) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), Charset.forName("MS949")))) {
            reader.readNext(); // read header
            String[] line;
            while ((line = reader.readNext()) != null) {
                String name = line[0].trim();
                String rankName = line[1].trim();
                int point = Integer.parseInt(line[2].trim());
                //System.out.println(name + "/" + rankName + "/" + point);

                Rank rank = Rank.of(rankName);
                rank.setPoint(point);
                Player player = new Player(name, rank);

                PlayerRepository.getInstance().insert(player);
            }
            PlayerRepository.getInstance().save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadTournamentsFromCsv(String filePath) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), Charset.forName("MS949")))) {
            reader.readNext(); // read header
            String[] line;
            while ((line = reader.readNext()) != null) {
                String name = line[0].trim();
                String startDateStr = line[1].trim();
                LocalDate startDate = LocalDate.parse(startDateStr);
                double weight = Double.parseDouble(line[2].trim());
                System.out.println(name + "/" + startDate + "/" + weight);

                Tournament tournament = new Tournament(name, startDate, weight);

                TournamentRepository.getInstance().insert(tournament);
            }
            TournamentRepository.getInstance().save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadGamesFromCsv(String filePath) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), Charset.forName("MS949")))) {
            reader.readNext(); // read header
            String[] line;
            while ((line = reader.readNext()) != null) {
                String tournamentName = line[0];
                String note = line[1].trim();
                String blackPlayerName = line[3].trim().toLowerCase();
                String whitePlayerName = line[4].trim().toLowerCase();
                String resultStr = line[5].trim();
                System.out.println(tournamentName + "/" + blackPlayerName + "/" + whitePlayerName + "/" + resultStr);

                long tournamentId = -1;
                Collection<Tournament> ts = TournamentRepository.getInstance().getAll();
                for (Tournament t : ts) {
                    if (t.getName().equals(tournamentName)) {
                        tournamentId = t.getId();
                    }
                }
                long blackPlayerId = -1;
                Collection<Player> ps = PlayerRepository.getInstance().getAll();
                for (Player p : ps) {
                    if (p.getName().toLowerCase().equals(blackPlayerName)) {
                        blackPlayerId = p.getId();
                    }
                }
                long whitePlayerId = -1;
                for (Player p : ps) {
                    if (p.getName().toLowerCase().equals(whitePlayerName)) {
                        whitePlayerId = p.getId();
                    }
                }
                Result result;
                if (resultStr.equals("B") || resultStr.equals("흑"))
                    result = Result.BLACK_WIN;
                else if (resultStr.equals("W") || resultStr.equals("백"))
                    result = Result.WHITE_WIN;
                else if (resultStr.equals("D") || resultStr.equals("무"))
                    result = Result.DRAW;
                else {
                    System.err.println(resultStr);
                    throw new RuntimeException();
                }

                if (tournamentId == -1 || blackPlayerId == -1 || whitePlayerId == -1)
                    throw new RuntimeException();

                Game game = new Game(tournamentId, blackPlayerId, whitePlayerId, result, note);
                GameRepository.getInstance().insert(game);
                TournamentRepository.getInstance().findById(tournamentId).getGameIds().add(game.getId());
            }
            GameRepository.getInstance().save();
            TournamentRepository.getInstance().save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveRankingDataForCsv(Object[][] rankingData, String filePath) {
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.writeNext(new String[] {"순위", "이름", "레이팅", "승", "무", "패"});
            for (Object[] row : rankingData) {
                String[] rowData = Arrays.stream(row)
                        .map(obj -> obj == null ? "" : obj.toString())
                        .toArray(String[]::new);
                writer.writeNext(rowData);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePlayerDataForCsv(Object[][] rankingData, String filePath) {
        System.out.println("csv");
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.writeNext(new String[] {"이름", "기력", "보유 승단포인트", "필요 승단포인트"});
            for (Object[] row : rankingData) {
                Object[] subRow = Arrays.copyOfRange(row, 1, row.length);
                String[] rowData = Arrays.stream(subRow)
                        .map(obj -> obj == null ? "" : obj.toString())
                        .toArray(String[]::new);
                writer.writeNext(rowData);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
