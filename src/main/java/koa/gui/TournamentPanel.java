package koa.gui;

import koa.model.tournament.Tournament;
import koa.repository.TournamentRepository;
import org.jdesktop.swingx.JXDatePicker;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TournamentPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable tournamentTable;
    private final String[] columnNames = {"ID", "이름", "시작일", "가중치"};

    public TournamentPanel() {
        setLayout(new BorderLayout());
        initView();
    }

    private Object[][] loadTournamentData() {
        TournamentRepository repository = TournamentRepository.getInstance();
        Collection<Tournament> tournaments = repository.getAll();

        List<Tournament> tournamentList = new ArrayList<>(tournaments);
        Collections.reverse(tournamentList);

        Object[][] data = new Object[tournamentList.size()][4];
        int index = 0;

        for (Tournament tournament : tournamentList) {
            data[index][0] = String.valueOf(tournament.getId());
            data[index][1] = tournament.getName();
            data[index][2] = tournament.getStartDate().toString();
            data[index][3] = tournament.getWeight();
            index++;
        }

        return data;
    }

    private void initView() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("대회 추가");
        addButton.addActionListener((ActionEvent e) -> addTournament());
        topPanel.add(addButton);
        JButton deleteButton = new JButton("대회 삭제");
        deleteButton.addActionListener((ActionEvent e) -> deleteTournament());
        topPanel.add(deleteButton);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tournamentTable = new JTable(tableModel);
        tournamentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tournamentTable.setAutoCreateColumnsFromModel(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn column = new TableColumn(i);
            column.setHeaderValue(columnNames[i]);
            column.setCellRenderer(centerRenderer);
            tournamentTable.addColumn(column);
        }

        tournamentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = tournamentTable.getSelectedRow();
                    if (selectedRow != -1) {
                        showTournamentView(selectedRow);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tournamentTable);
        add(scrollPane, BorderLayout.CENTER);
        tableModel.setDataVector(loadTournamentData(), columnNames);
    }

    private void addTournament() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("이름:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        panel.add(nameField, gbc);

        // start date field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("시작일:"), gbc);
        gbc.gridx = 1;
        JXDatePicker startDatePicker = new JXDatePicker();
        startDatePicker.setFormats(new SimpleDateFormat("yyyy-MM-dd"));
        panel.add(startDatePicker, gbc);

        // weight field
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("가중치:"), gbc);
        gbc.gridx = 1;
        JTextField weightField = new JTextField(5);
        panel.add(weightField, gbc);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "토너먼트 추가",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            Date startDate = startDatePicker.getDate();
            String weightStr = weightField.getText().trim();

            if (name.isEmpty() || startDate == null || weightStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "모든 필드를 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "가중치는 숫자로 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            Tournament tournament = new Tournament(name, startLocalDate, weight);
            TournamentRepository.getInstance().insert(tournament);
            TournamentRepository.getInstance().save();

            tableModel.addRow(getRow(tournament));
        }
    }

    private void deleteTournament() {
        int selectedRow = tournamentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "삭제할 대회를 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
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

        String idStr = tableModel.getValueAt(selectedRow, 0).toString();
        long tournamentId = Long.parseLong(idStr);
        TournamentRepository.getInstance().delete(tournamentId);
        TournamentRepository.getInstance().save();

        tableModel.removeRow(selectedRow);
    }

    private Object[] getRow(Tournament tournament) {
        String startStr = tournament.getStartDate() != null ? tournament.getStartDate().toString() : "";
        return new Object[] {
                tournament.getId(),
                tournament.getName(),
                startStr,
                tournament.getWeight()
        };
    }

    private void showTournamentView(int selectedRow) {
        String idStr = tableModel.getValueAt(selectedRow, 0).toString();
        long tournamentId = Long.parseLong(idStr);
        Tournament tournament = TournamentRepository.getInstance().findById(tournamentId);

        JFrame tournamentFrame = new JFrame("대회 상세 정보");
        tournamentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tournamentFrame.setSize(600, 400);
        tournamentFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new AddGamePanel(tournament);
        tournamentFrame.add(mainPanel);
        tournamentFrame.setVisible(true);
    }

}
