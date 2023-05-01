package com.bitwarden;

import com.google.inject.Provider;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.skillcalculator.SkillCalculatorPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
		name = "Bitwarden"
)
public class BitwardenPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private Provider<BitwardenMainPanel> mainPanel;

	@Inject Provider<BitwardenVaultPanel> vaultPanel;

	@Inject
	private BitwardenConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Bitwarden bitwardenAPI;

	private NavigationButton uiNavigationButton;

	@Override
	protected void startUp() throws Exception {
		final BufferedImage icon = ImageUtil.loadImageResource(SkillCalculatorPlugin.class, "calc.png");

		uiNavigationButton = NavigationButton.builder()
				.tooltip("Bitwarden plugin")
				.icon(icon)
				.panel(mainPanel.get())
				.build();

		clientToolbar.addNavigation(uiNavigationButton);
	}

	@Override
	protected void shutDown() throws Exception {
		log.info("Bitwarden stopped!");
		clientToolbar.removeNavigation(uiNavigationButton);
		bitwardenAPI.logout();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Bitwarden says " + config.greeting(), null);
		}
	}

	@Provides
	BitwardenConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(BitwardenConfig.class);
	}
}