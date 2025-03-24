package koa.gui;

import koa.model.player.Player;
import koa.model.player.Rank;
import koa.repository.PlayerRepository;
import koa.util.CsvUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PlayerPanel extends JPanel {

    private final String[] columnNames = {"ID", "이름", "기력", "승단포인트"};

    private DefaultTableModel tableModel;
    private JTable playerTable;
    private JTextField searchField;
    private Object[][] playerData;

    public PlayerPanel() {
        loadPlayerData();
        setLayout(new BorderLayout());
        initSearchPanel();
        initPlayerTable();
    }

    private void loadPlayerData() {
        PlayerRepository repository = PlayerRepository.getInstance();
        List<Player> sortedPlayers = repository.getSortedPlayers();

        Object[][] data = new Object[sortedPlayers.size()][4];
        int index = 0;

        for (Player player : sortedPlayers) {
            data[index][0] = String.valueOf(player.getId());
            data[index][1] = player.getName();
            data[index][2] = player.getRank().toString();
            data[index][3] = player.getRank().getPoint() + "점";
            index++;
        }

        playerData = data;
    }

    private Object[] getRow(Player player) {
        Object[] row = new Object[4];
        row[0] = player.getId();
        row[1] = player.getName();
        row[2] = player.getRank().toString();
        row[3] = player.getRank().getPoint() + "점";
        return row;
    }

    private void initSearchPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("이름:"));

        searchField = new JTextField(20);
        topPanel.add(searchField);

        JButton searchButton = new JButton("검색");
        searchButton.addActionListener(e -> performSearch());
        topPanel.add(searchButton);

        JButton addButton = new JButton("추가");
        addButton.addActionListener(e -> addPlayer());
        topPanel.add(addButton);

        JButton csvButton = new JButton("csv");
        csvButton.addActionListener(e -> CsvUtil.savePlayerDataForCsv(playerData, "db/player.csv"));
        topPanel.add(csvButton);

        add(topPanel, BorderLayout.NORTH);
    }

    private void initPlayerTable() {
        tableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        playerTable = new JTable(tableModel);
        playerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerTable.setAutoCreateColumnsFromModel(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < columnNames.length; i++) {
            TableColumn column = new TableColumn(i);
            column.setHeaderValue(columnNames[i]);
            column.setCellRenderer(centerRenderer);
            playerTable.addColumn(column);
        }

        JScrollPane scrollPane = new JScrollPane(playerTable);

        playerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = playerTable.getSelectedRow();
                    if (selectedRow != -1) {
                        showPlayerDialog(selectedRow);
                    }
                }
            }
        });

        add(scrollPane, BorderLayout.CENTER);
        tableModel.setDataVector(playerData, columnNames);
    }

    private void performSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        DefaultTableModel filteredModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        PlayerRepository repository = PlayerRepository.getInstance();
        List<Player> players = repository.getSortedPlayers();
        for (Player player : players) {
            String name = player.getName().toLowerCase();
            if (name.contains(searchTerm)) {
                filteredModel.addRow(getRow(player));
            }
        }

        tableModel = filteredModel;
        playerTable.setModel(tableModel);
    }

    private void addPlayer() {
        // dialog view setting
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("이름:"), gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        panel.add(nameField, gbc);

        // 2. rank field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("랭크:"), gbc);

        gbc.gridx = 1;
        JSpinner rankSpinner = new JSpinner(new SpinnerListModel(Rank.getRankOptions()));
        rankSpinner.setValue("1급");
        panel.add(rankSpinner, gbc);

        // show dialog
        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "플레이어 추가",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        // if user confirm
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String rank = rankSpinner.getValue().toString();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "이름은 필수 항목입니다.",
                        "입력 오류",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Player player = new Player(name, Rank.of(rank));
            PlayerRepository.getInstance().insert(player);
            PlayerRepository.getInstance().save();

            tableModel.addRow(getRow(player));
            playerTable.revalidate();
        }
    }

    private void showPlayerDialog(int selectedRow) {
        String id = tableModel.getValueAt(selectedRow, 0).toString();
        Player player = PlayerRepository.getInstance().findById(Long.parseLong(id));

        // initialize custom panel
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("이름:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(player.getName(), 15);
        panel.add(nameField, gbc);

        // rank field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("기력:"), gbc);
        gbc.gridx = 1;
        JSpinner rankSpinner = new JSpinner(new SpinnerListModel(Rank.getRankOptions()));
        rankSpinner.setValue(player.getRank().toString());
        panel.add(rankSpinner, gbc);

        // rank point field
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("승단 포인트:"), gbc);
        gbc.gridx = 1;
        JTextField rankPointField = new JTextField(String.valueOf(player.getRank().getPoint()), 5);
        panel.add(rankPointField, gbc);

        // generate dialog
        int option = JOptionPane.showOptionDialog(
                null,
                panel,
                "플레이어 정보",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[] {"수정", "삭제", "취소"},
                "수정"
        );

        // select option
        if (option == 0) { // modify
            String newName = nameField.getText().trim();
            String newRank = rankSpinner.getValue().toString();
            int newPoint = Integer.parseInt(rankPointField.getText());

            // update player data
            player.setName(newName);
            player.setRank(Rank.of(newRank));
            player.getRank().setPoint(newPoint);

            // save
            PlayerRepository.getInstance().save();
            tableModel.removeRow(selectedRow);
            tableModel.insertRow(selectedRow, getRow(player));
            playerTable.revalidate();
        } else if (option == 1) { // delete
            int confirm = JOptionPane.showConfirmDialog(null, "정말 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                PlayerRepository.getInstance().delete(player.getId());
                PlayerRepository.getInstance().save();
                tableModel.removeRow(selectedRow);
                playerTable.revalidate();
            }
        }
    }

}
