package com.bitwarden;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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
                mainPanel.removeAll();
                mainPanel.add(new BitwardenVaultPanel(mainPanel, bitwardenAPI));
                mainPanel.revalidate();
                mainPanel.repaint();
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

                mainPanel.removeAll();
                mainPanel.add(new BitwardenLoginPanel(mainPanel, bitwardenAPI));
                mainPanel.revalidate();
                mainPanel.repaint();
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

            textField = new JTextField();
            textField.setEditable(false);
            textField.setText(passwordEntry.login.password);
            GridBagConstraints gbc_textField = new GridBagConstraints();
            gbc_textField.gridwidth = 2;
            gbc_textField.insets = new Insets(0, 0, 0, 5);
            gbc_textField.fill = GridBagConstraints.HORIZONTAL;
            gbc_textField.gridx = 0;
            gbc_textField.gridy = startY + 1;
            add(textField, gbc_textField);
            textField.setColumns(10);

            startY += 10;
        }
    }

}
