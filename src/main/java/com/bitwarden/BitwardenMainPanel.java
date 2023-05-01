package com.bitwarden;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
public class BitwardenMainPanel extends PluginPanel {
    @Inject
    BitwardenMainPanel(
            Bitwarden bitwardenAPI
    ) {
        add(new BitwardenLoginPanel(this, bitwardenAPI));
    }

    public void replacePanel(Component panel) {
        removeAll();
        add(panel);
        revalidate();
        repaint();
    }
}
