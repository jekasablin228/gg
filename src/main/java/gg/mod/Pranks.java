package gg.mod;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Short, reversible effects implemented on the locally hosted server. */
public final class Pranks {
    private static long lastUse;
    private static IntegratedServer lastServer;

    private Pranks() {}

    public static void play(Minecraft mc, UUID target, int action) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || target == null || action < 0 || action > 2) return;
        long now = System.nanoTime();
        if (lastServer == server && now - lastUse < 5_000_000_000L) {
            message(mc, "Wait 5 seconds between pranks");
            return;
        }
        lastServer = server;
        lastUse = now;
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(target);
            if (player == null || !player.isAlive()) {
                message(mc, "Player is no longer available");
                return;
            }
            if (action == 0) {
                if (player.hasEffect(MobEffects.GLOWING)) {
                    message(mc, "Player already has this effect");
                    return;
                }
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            } else if (action == 1) {
                if (player.hasEffect(MobEffects.LEVITATION) || player.hasEffect(MobEffects.SLOW_FALLING)) {
                    message(mc, "Player already has a flight effect");
                    return;
                }
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
            } else {
                if (player.hasEffect(MobEffects.JUMP_BOOST)) {
                    message(mc, "Player already has this effect");
                    return;
                }
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 100, 1));
            }
            message(mc, "Prank applied to " + player.getName().getString());
        });
    }

    private static void message(Minecraft mc, String text) {
        mc.execute(() -> mc.gui.setOverlayMessage(Component.literal(text), false));
    }
}
