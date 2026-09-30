package gg.mod;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class Hacks {
    public static final int MENU_KEY = GLFW.GLFW_KEY_INSERT;
    private static final double AIM_RANGE = 8.0;

    /** Module waiting for a new key in the menu, or null. */
    public static Module binding;

    private static boolean configLoaded;
    private static GameType modeBeforeCreative = GameType.SURVIVAL;
    /** True while X-Ray owns the night vision on the local player (so a real potion is never removed). */
    private static boolean xrayNightVision;

    private static final Set<Block> XRAY_BLOCKS = Set.of(
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS,
            Blocks.SPAWNER, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.LAVA);

    private Hacks() {}

    public static boolean esp() {
        return Module.ESP.enabled;
    }

    public static boolean xray() {
        return Module.XRAY.enabled;
    }

    public static boolean xrayVisible(BlockState state) {
        return XRAY_BLOCKS.contains(state.getBlock());
    }

    public static void onClientTick(Minecraft mc) {
        if (!configLoaded) {
            configLoaded = true;
            Config.load();
        }
        List<Integer> pressed = Keys.poll(mc.getWindow().handle());

        // Insert closes the menu even while a module is waiting for a bind.
        if (mc.screen instanceof MenuScreen && pressed.contains(MENU_KEY)) {
            mc.setScreen(null);
            return;
        }

        if (binding != null) {
            if (!(mc.screen instanceof MenuScreen menu)) {
                binding = null;
            } else if (!pressed.isEmpty()) {
                int key = pressed.get(0);
                // Backspace/Delete unbinds; Escape is left to close the menu.
                if (key != GLFW.GLFW_KEY_ESCAPE && key != MENU_KEY) {
                    binding.key = key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE
                            ? GLFW.GLFW_KEY_UNKNOWN : key;
                    binding = null;
                    Config.save();
                    menu.refresh();
                }
            }
            return;
        }

        if (mc.player == null || mc.level == null) return;
        updateXrayBrightness(mc.player);

        if (mc.screen == null) {
            for (int key : pressed) {
                if (key == MENU_KEY) {
                    mc.setScreen(new MenuScreen());
                    return;
                }
                for (Module m : Module.values()) {
                    if (m.key == key) toggle(mc, m);
                }
            }
            if (Module.AIM.enabled && mc.options.keyAttack.isDown()) aimAtNearest(mc.player, mc);
        }
    }

    public static void toggle(Minecraft mc, Module m) {
        boolean on = !m.enabled;
        if (m == Module.VANISH && !setVanish(mc, on)) return;
        if (m == Module.CREATIVE && !setCreative(mc, on)) return;
        m.enabled = on;
        if (m == Module.XRAY) mc.levelRenderer.allChanged();
        // Action bar only: nothing goes to chat or to other players.
        mc.gui.setOverlayMessage(Component.literal(m.title + (m.enabled ? ": ON" : ": OFF")), false);
    }

    // Underground ores sit at light level 0 and render almost black; client-side night vision lights them up.
    // Re-applied every tick because respawn and dimension changes clear client effects.
    private static void updateXrayBrightness(LocalPlayer player) {
        if (Module.XRAY.enabled) {
            if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,
                        MobEffectInstance.INFINITE_DURATION, 0, false, false, false));
                xrayNightVision = true;
            }
        } else if (xrayNightVision) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            xrayNightVision = false;
        }
    }

    private static void aimAtNearest(LocalPlayer self, Minecraft mc) {
        LivingEntity best = null;
        double bestDist = AIM_RANGE * AIM_RANGE;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == self || !(e instanceof LivingEntity living) || !living.isAlive() || e instanceof ArmorStand) continue;
            double d = self.distanceToSqr(e);
            if (d < bestDist && self.hasLineOfSight(e)) {
                bestDist = d;
                best = living;
            }
        }
        if (best == null) return;

        Vec3 eye = self.getEyePosition();
        Vec3 target = best.getBoundingBox().getCenter();
        double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        self.setYRot(yaw);
        self.setXRot(pitch);
        self.setYHeadRot(yaw);
    }

    // Creative and vanish change server state, so they only work when this client hosts the world.
    private static IntegratedServer hostServer(Minecraft mc) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            mc.gui.setOverlayMessage(Component.literal("Works only when you host the world"), false);
            return null;
        }
        return server;
    }

    private static boolean setCreative(Minecraft mc, boolean on) {
        IntegratedServer server = hostServer(mc);
        if (server == null) return false;
        UUID id = mc.player.getUUID();
        // Calling setGameMode directly skips /gamemode, so no chat feedback is sent to anyone.
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(id);
            if (sp == null) return;
            if (on) {
                modeBeforeCreative = sp.gameMode.getGameModeForPlayer();
                sp.setGameMode(GameType.CREATIVE);
            } else {
                sp.setGameMode(modeBeforeCreative == GameType.CREATIVE ? GameType.SURVIVAL : modeBeforeCreative);
            }
        });
        return true;
    }

    private static boolean setVanish(Minecraft mc, boolean on) {
        IntegratedServer server = hostServer(mc);
        if (server == null) return false;
        UUID id = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(id);
            if (sp == null) return;
            VanishState.vanished = on ? id : null;
            sp.setSilent(on);
            sp.setInvisible(on);
        });
        return true;
    }
}
