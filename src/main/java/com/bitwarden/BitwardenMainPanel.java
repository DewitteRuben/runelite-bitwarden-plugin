package com.bitwarden;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;

@Slf4j
public class BitwardenMainPanel extends PluginPanel {
    @Inject
    private Bitwarden bitwardenAPI;

    BitwardenMainPanel() {
        add(new BitwardenLoginPanel(this, bitwardenAPI));
    }
}
