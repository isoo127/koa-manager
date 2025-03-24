package koa.gui;

import koa.model.player.Player;
import koa.model.player.Rating;
import koa.model.tournament.Game;
import koa.model.tournament.Tournament;
import koa.repository.GameRepository;
import koa.repository.PlayerRepository;
import koa.repository.TournamentRepository;
import koa.util.CsvUtil;
import org.jdesktop.swingx.JXDatePicker;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;

public class RankingPanel extends JPanel {

    private final String[] columnNames = { "순위", "이름", "레이팅", "승", "무", "패" };

    private DefaultTableModel tableModel;
    private JXDatePicker datePicker;
    private final Rating rating;

    public RankingPanel() {
        rating = new Rating();
        setLayout(new BorderLayout());
        initDatePicker();
        initRankingView();
    }

    private void initDatePicker() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dateLabel = new JLabel("날짜 선택: ");

        datePicker = new JXDatePicker();
        datePicker.setFormats(new SimpleDateFormat("yyyy-MM-dd"));
        datePicker.setDate(new Date());

        JButton applyButton = new JButton("반영");
        applyButton.addActionListener(e -> {
            calculateRankingData(datePicker.getDate());
            tableModel.setDataVector(rating.getRanking(), columnNames);
        });

        JButton allButton = new JButton("전체산정");
        allButton.addActionListener(e -> {
            calculateAllTimeRankingData();
            tableModel.setDataVector(rating.getRanking(), columnNames);
        });

        JButton csvButton = new JButton("csv");
        csvButton.addActionListener(e -> CsvUtil.saveRankingDataForCsv(rating.getRanking(), "db/rank.csv"));

        topPanel.add(dateLabel);
        topPanel.add(datePicker);
        topPanel.add(applyButton);
        topPanel.add(allButton);
        topPanel.add(csvButton);

        add(topPanel, BorderLayout.NORTH);
    }

    private void initRankingView() {
        tableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable rankingTable = new JTable(tableModel);
        rankingTable.setFillsViewportHeight(true);
        rankingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rankingTable.setAutoCreateColumnsFromModel(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < columnNames.length; i++) {
            TableColumn column = new TableColumn(i);
            column.setHeaderValue(columnNames[i]);
            column.setCellRenderer(centerRenderer);
            rankingTable.addColumn(column);
        }

        JScrollPane scrollPane = new JScrollPane(rankingTable);

        add(scrollPane, BorderLayout.CENTER);
        tableModel.setDataVector(rating.getRanking(), columnNames);
    }

    private void calculateRankingData(Date date) {
        rating.resetRanking();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, -5);
        Date fiveYearsAgoDate = cal.getTime();
        LocalDate nowLocalDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate fiveYearsAgoLocalDate = fiveYearsAgoDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        List<Tournament> sortedTournaments = TournamentRepository.getInstance().getAll().stream()
                .sorted(Comparator.comparing(Tournament::getStartDate))
                .collect(Collectors.toCollection(ArrayList::new));

        for (Tournament tournament : sortedTournaments) {
            LocalDate tournamentDate = tournament.getStartDate();
            if ((tournamentDate.isAfter(fiveYearsAgoLocalDate) || tournamentDate.isEqual(fiveYearsAgoLocalDate)) &&
                    (tournamentDate.isBefore(nowLocalDate) || tournamentDate.isEqual(nowLocalDate))) {
                for (long id : tournament.getGameIds()) {
                    Game game = GameRepository.getInstance().findById(id);
                    Player blackPlayer = PlayerRepository.getInstance().findById(game.getBlackPlayerId());
                    Player whitePlayer = PlayerRepository.getInstance().findById(game.getWhitePlayerId());
                    rating.updateRating(blackPlayer, whitePlayer, game.getResult(), tournament.getWeight());
                }
            }
        }
    }

    private void calculateAllTimeRankingData() {
        rating.resetRanking();

        List<Tournament> sortedTournaments = TournamentRepository.getInstance().getAll().stream()
                .sorted(Comparator.comparing(Tournament::getStartDate))
                .collect(Collectors.toCollection(ArrayList::new));

        for (Tournament tournament : sortedTournaments) {
            for (long id : tournament.getGameIds()) {
                Game game = GameRepository.getInstance().findById(id);
                Player blackPlayer = PlayerRepository.getInstance().findById(game.getBlackPlayerId());
                Player whitePlayer = PlayerRepository.getInstance().findById(game.getWhitePlayerId());
                rating.updateRating(blackPlayer, whitePlayer, game.getResult(), tournament.getWeight());
            }
        }
    }

}