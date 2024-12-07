package com.NullByte.AutoAdvertiser.addon.modules;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import com.NullByte.AutoAdvertiser.addon.AddonTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WhisperSpam extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Random random = new Random();
    private ScheduledExecutorService scheduler;

    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("Messages")
        .description("Messages to randomly send to players.")
        .defaultValue(List.of("Check out my shop at /warp shop!", "Best prices in town!"))
        .build()
    );

    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between whispers in seconds.")
        .defaultValue(3)
        .min(0.1)
        .sliderRange(0.1, 30)
        .build()
    );

    private final Setting<Boolean> excludeSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("Exclude Self")
        .description("Don't whisper to yourself.")
        .defaultValue(true)
        .build()
    );

    public WhisperSpam() {
        super(AddonTemplate.CATEGORY, "Whisper Spam", "Automatically whispers random messages to players.");
    }

    @Override
    public void onActivate() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        long delayMs = (long)(delay.get() * 1000);
        scheduler.scheduleAtFixedRate(() -> {
            if (mc.player != null && mc.world != null) {
                whisperRandomPlayer();
            }
        }, 0, delayMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDeactivate() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void whisperRandomPlayer() {
        if (mc.getNetworkHandler() == null) return;

        // Get all players from tab list
        List<String> players = new ArrayList<>();
        mc.getNetworkHandler().getPlayerList().forEach(entry -> {
            String name = entry.getProfile().getName();
            if (!excludeSelf.get() || !name.equals(mc.player.getName().getString())) {
                players.add(name);
            }
        });

        if (players.isEmpty() || messages.get().isEmpty()) return;

        // Select random player and message
        String player = players.get(random.nextInt(players.size()));
        String message = messages.get().get(random.nextInt(messages.get().size()));

        // Send whisper
        mc.player.networkHandler.sendChatCommand("w " + player + " " + message);
        info("Whispered to " + player + ": " + message);
    }
}
