package koa.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("대한오목협회 인원 관리 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        initComponents();
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel rankingPanel = new RankingPanel();
        JPanel playerPanel = new PlayerPanel();
        JPanel tournamentPanel = new TournamentPanel();

        tabbedPane.addTab("랭킹", rankingPanel);
        tabbedPane.addTab("플레이어", playerPanel);
        tabbedPane.addTab("대회", tournamentPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

}
