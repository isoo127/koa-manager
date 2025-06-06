package koa.gui;

import koa.model.player.Player;
import koa.model.player.Rating;
import koa.model.tournament.Game;
import koa.model.tournament.Result;
import koa.model.tournament.Tournament;
import koa.repository.GameRepository;
import koa.repository.PlayerRepository;
import koa.repository.TournamentRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class AddGamePanel extends JPanel {

    private final Tournament tournament;
    DefaultTableModel gameTableModel;
    private JTable gameTable;
    private Game game = null;

    public AddGamePanel(Tournament tournament) {
        this.tournament = tournament;
        setLayout(new BorderLayout());
        initInfoView();
    }

    private Object[] getGameRow(Game game) {
        Object[] row = new Object[5];
        row[0] = game.getId();
        row[1] = PlayerRepository.getInstance().findById(game.getBlackPlayerId()).getName();
        row[2] = PlayerRepository.getInstance().findById(game.getWhitePlayerId()).getName();
        row[3] = game.getResult().toString();
        row[4] = game.getNote();

        return row;
    }

    private void initInfoView() {
        // init information view
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel(
                "<" + tournament.getName() + ">    " +
                        tournament.getStartDate() +
                        "   가중치: " + tournament.getWeight());
        infoPanel.add(infoLabel);

        // init game data table
        String[] gameColumnNames = {"ID", "흑 플레이어", "백 플레이어", "결과", "비고"};
        Object[][] gameData = new Object[tournament.getGameIds().size()][5];
        int index = 0;
        for (Long id : tournament.getGameIds()) {
            Game game = GameRepository.getInstance().findById(id);
            gameData[index][0] = game.getId();
            gameData[index][1] = PlayerRepository.getInstance().findById(game.getBlackPlayerId()).getName();
            gameData[index][2] = PlayerRepository.getInstance().findById(game.getWhitePlayerId()).getName();
            gameData[index][3] = game.getResult().toString();
            gameData[index][4] = game.getNote();
            index++;
        }

        gameTableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        gameTable = new JTable(gameTableModel);
        gameTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameTable.setAutoCreateColumnsFromModel(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < gameColumnNames.length; i++) {
            TableColumn column = new TableColumn(i);
            column.setHeaderValue(gameColumnNames[i]);
            column.setCellRenderer(centerRenderer);
            gameTable.addColumn(column);
        }

        gameTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = gameTable.getSelectedRow();
                    if (selectedRow != -1) {
                        Long gameId = Long.parseLong(gameTable.getValueAt(selectedRow, 0).toString());
                        openAddGameDialog(gameId);
                    }
                }
            }
        });

        JScrollPane gameScrollPane = new JScrollPane(gameTable);
        gameTableModel.setDataVector(gameData, gameColumnNames);

        // init add button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addGameButton = new JButton("게임 추가");
        addGameButton.addActionListener(e -> openAddGameDialog(-1L));
        JButton deleteGameButton = new JButton("게임 삭제");
        deleteGameButton.addActionListener(e -> deleteGame());
        JButton showResultButton = new JButton("총 결과");
        showResultButton.addActionListener(e -> showResultDialog());
        buttonPanel.add(addGameButton);
        buttonPanel.add(deleteGameButton);
        buttonPanel.add(showResultButton);

        setLayout(new BorderLayout());
        add(infoPanel, BorderLayout.NORTH);
        add(gameScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void openAddGameDialog(Long gameId) {
        game = null;
        if (gameId != -1) {
            game = GameRepository.getInstance().findById(gameId);
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel playerSelectionPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // black player panel
        JPanel playerAPanel = new JPanel(new BorderLayout());
        playerAPanel.setBorder(BorderFactory.createTitledBorder("흑 플레이어"));

        JPanel searchAPanel = new JPanel(new BorderLayout(5, 5));
        JTextField searchAField = new JTextField();
        JButton searchAButton = new JButton("검색");
        searchAPanel.add(searchAField, BorderLayout.CENTER);
        searchAPanel.add(searchAButton, BorderLayout.EAST);
        playerAPanel.add(searchAPanel, BorderLayout.NORTH);

        DefaultListModel<Player> listModelA = new DefaultListModel<>();
        JList<Player> playerAList = new JList<>(listModelA);
        if (game != null) {
            Player blackPlayer = PlayerRepository.getInstance().findById(game.getBlackPlayerId());
            listModelA.addElement(blackPlayer);
            playerAList.setSelectedValue(blackPlayer, true);
        }
        JScrollPane scrollPaneA = new JScrollPane(playerAList);
        playerAPanel.add(scrollPaneA, BorderLayout.CENTER);

        // white player panel
        JPanel playerBPanel = new JPanel(new BorderLayout());
        playerBPanel.setBorder(BorderFactory.createTitledBorder("백 플레이어"));

        JPanel searchBPanel = new JPanel(new BorderLayout(5, 5));
        JTextField searchBField = new JTextField();
        JButton searchBButton = new JButton("검색");
        searchBPanel.add(searchBField, BorderLayout.CENTER);
        searchBPanel.add(searchBButton, BorderLayout.EAST);
        playerBPanel.add(searchBPanel, BorderLayout.NORTH);

        DefaultListModel<Player> listModelB = new DefaultListModel<>();
        JList<Player> playerBList = new JList<>(listModelB);
        if (game != null) {
            Player whitePlayer = PlayerRepository.getInstance().findById(game.getWhitePlayerId());
            listModelB.addElement(whitePlayer);
            playerBList.setSelectedValue(whitePlayer, true);
        }
        JScrollPane scrollPaneB = new JScrollPane(playerBList);
        playerBPanel.add(scrollPaneB, BorderLayout.CENTER);

        // merge player selection panel
        playerSelectionPanel.add(playerAPanel);
        playerSelectionPanel.add(playerBPanel);
        panel.add(playerSelectionPanel, BorderLayout.CENTER);

        // init result panel
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));

        JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resultPanel.add(new JLabel("게임 결과:"));
        String[] gameResults = Result.getResultListName();

        // init result radio buttons
        ButtonGroup resultGroup = new ButtonGroup();
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        List<JRadioButton> radioButtons = new ArrayList<>();
        for (int i = 0; i < gameResults.length; i++)  {
            String result = gameResults[i];
            JRadioButton radioButton = new JRadioButton(result);
            resultGroup.add(radioButton);
            radioPanel.add(radioButton);
            radioButtons.add(radioButton);
            if (game == null && i == 0) {
                radioButton.setSelected(true);
            } else if (game != null && Objects.equals(result, game.getResult().toString())) {
                radioButton.setSelected(true);
            }
        }
        resultPanel.add(radioPanel);

        // init note textbox
        resultPanel.add(new JLabel("    비고:"));
        JTextField noteField = new JTextField(20);
        resultPanel.add(noteField);
        if (game != null) {
            noteField.setText(game.getNote());
        }

        // init ok & cancel button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        bottomContainer.add(resultPanel);
        bottomContainer.add(buttonPanel);

        panel.add(bottomContainer, BorderLayout.SOUTH);

        // search button action
        searchAButton.addActionListener(e -> {
            String query = searchAField.getText().trim().toLowerCase();
            listModelA.clear();
            for (Player p : getFilteredPlayers(query)) {
                listModelA.addElement(p);
            }
        });
        searchBButton.addActionListener(e -> {
            String query = searchBField.getText().trim().toLowerCase();
            listModelB.clear();
            for (Player p : getFilteredPlayers(query)) {
                listModelB.addElement(p);
            }
        });

        // open dialog
        String dialogTitle = (game == null) ? "게임 추가" : "게임 정보 변경 (" + game.getId() + ")";
        JDialog dialog = new JDialog((Frame) null, dialogTitle, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        okButton.addActionListener(e -> {
            Player playerA = playerAList.getSelectedValue();
            Player playerB = playerBList.getSelectedValue();

            String resultStr = null;
            for (JRadioButton rb : radioButtons) {
                if (rb.isSelected()) {
                    resultStr = rb.getText();
                    break;
                }
            }

            Result gameResult = Result.of(resultStr);
            String note = noteField.getText();

            if (playerA == null || playerB == null) {
                JOptionPane.showMessageDialog(dialog, "두 플레이어를 모두 선택하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            } else if (playerA == playerB) {
                JOptionPane.showMessageDialog(dialog, "서로 다른 플레이어를 선택하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            } else {
                if (game == null) {
                    game = new Game(tournament.getId(), playerA.getId(), playerB.getId(), gameResult, note);
                    GameRepository.getInstance().insert(game);
                    tournament.getGameIds().add(game.getId());
                    gameTableModel.addRow(getGameRow(game));
                } else {
                    game.setBlackPlayerId(playerA.getId());
                    game.setWhitePlayerId(playerB.getId());
                    game.setResult(gameResult);
                    game.setNote(note);

                    int selectedRow = gameTable.getSelectedRow();
                    if (selectedRow != -1) {
                        gameTableModel.setValueAt(playerA.getName(), selectedRow, 1);
                        gameTableModel.setValueAt(playerB.getName(), selectedRow, 2);
                        gameTableModel.setValueAt(gameResult.toString(), selectedRow, 3);
                        gameTableModel.setValueAt(note, selectedRow, 4);
                    }
                }
                GameRepository.getInstance().save();
                TournamentRepository.getInstance().save();

                dialog.dispose();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void deleteGame() {
        int selectedRow = gameTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "삭제할 게임을 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "정말 삭제하시겠습니까?",
                "삭제 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String idStr = gameTableModel.getValueAt(selectedRow, 0).toString();
        long gameId = Long.parseLong(idStr);
        GameRepository.getInstance().delete(gameId);
        GameRepository.getInstance().save();
        tournament.getGameIds().remove(selectedRow);
        TournamentRepository.getInstance().save();

        gameTableModel.removeRow(selectedRow);
    }

    private ArrayList<Player> getFilteredPlayers(String query) {
        ArrayList<Player> result = new ArrayList<>();
        for (Player player : PlayerRepository.getInstance().getAll()) {
            if (player.getName().contains(query.trim())) {
                result.add(player);
            }
        }
        return result;
    }

    private void showResultDialog() {
        JDialog dialog = new JDialog((Frame) null, "총 결과", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        showResultTable(dialog);

        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void showResultTable(JDialog dialog) {
        String[] gameColumnNames = {"순위", "이름", "전적", "승", "무", "패"};

        Rating rating = new Rating();
        for (Long gameId : tournament.getGameIds()) {
            Game game = GameRepository.getInstance().findById(gameId);
            Player bp = PlayerRepository.getInstance().findById(game.getBlackPlayerId());
            Player wp = PlayerRepository.getInstance().findById(game.getWhitePlayerId());
            rating.updateRating(bp, wp, game.getResult(), 1);
        }
        Object[][] ratingList = rating.getAllRanking();
        Object[][] resultData = new Object[ratingList.length][gameColumnNames.length];
        for (int i = 0; i < ratingList.length; i++) {
            resultData[i][0] = ratingList[i][0];
            resultData[i][1] = ratingList[i][1];
            resultData[i][2] = (int)ratingList[i][3] + (int)ratingList[i][4] + (int)ratingList[i][5];
            resultData[i][3] = ratingList[i][3];
            resultData[i][4] = ratingList[i][4];
            resultData[i][5] = ratingList[i][5];
        }

        DefaultTableModel resultTableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable resultTable = new JTable(resultTableModel);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.setAutoCreateColumnsFromModel(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < gameColumnNames.length; i++) {
            TableColumn column = new TableColumn(i);
            column.setHeaderValue(gameColumnNames[i]);
            column.setCellRenderer(centerRenderer);
            resultTable.addColumn(column);
        }

        JScrollPane resultScrollPane = new JScrollPane(resultTable);
        resultTableModel.setDataVector(resultData, gameColumnNames);

        dialog.add(resultScrollPane);
    }

}
