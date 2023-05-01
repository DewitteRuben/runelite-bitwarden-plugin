package com.bitwarden;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@Slf4j

public class BitwardenVaultPanel extends PluginPanel {

    private JTextField textField;


    BitwardenVaultPanel(BitwardenMainPanel mainPanel, Bitwarden bitwardenAPI) {
        super(false);

        setBorder(new EmptyBorder(5, 5, 5, 5));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        JButton btnNewButton = new JButton("Refresh");
        btnNewButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                bitwardenAPI.sync();
                mainPanel.replacePanel(new BitwardenVaultPanel(mainPanel, bitwardenAPI));
            }
        });

        GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
        gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
        gbc_btnNewButton.gridx = 0;
        gbc_btnNewButton.gridy = 0;
        add(btnNewButton, gbc_btnNewButton);

        JButton btnNewButton_1 = new JButton("Logout");
        btnNewButton_1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                bitwardenAPI.logout();
                mainPanel.replacePanel(new BitwardenLoginPanel(mainPanel, bitwardenAPI));
            }
        });

        GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
        gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
        gbc_btnNewButton_1.gridx = 1;
        gbc_btnNewButton_1.gridy = 0;
        add(btnNewButton_1, gbc_btnNewButton_1);

        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.EAST;
        gbc_panel.insets = new Insets(0, 0, 5, 0);
        gbc_panel.fill = GridBagConstraints.VERTICAL;
        gbc_panel.gridx = 2;
        gbc_panel.gridy = 0;
        add(panel, gbc_panel);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        List<Bitwarden.PasswordEntry> passwordEntries = bitwardenAPI.getRunescapePasswords();

        int startY = 2;
        for (Bitwarden.PasswordEntry passwordEntry: passwordEntries) {
            JLabel lblNewLabel = new JLabel(passwordEntry.login.username);
            GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
            gbc_lblNewLabel.gridwidth = 2;
            gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
            gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
            gbc_lblNewLabel.gridx = 0;
            gbc_lblNewLabel.gridy = startY;
            add(lblNewLabel, gbc_lblNewLabel);

            startY += 1;

            JButton btnCopy = new JButton("Copy");
            btnCopy.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(passwordEntry.login.password), null);
                }
            });

            GridBagConstraints gbc_btnCopy = new GridBagConstraints();
            gbc_btnCopy.insets = new Insets(0, 0, 5, 5);
            gbc_btnCopy.gridx = 0;
            gbc_btnCopy.gridy = startY;
            add(btnCopy, gbc_btnCopy);

            startY += 1;
        }
    }

}
