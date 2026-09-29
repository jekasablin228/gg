package gg.mod;

import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
    public static volatile boolean esp, aim, xray;
    private static boolean vanish, creative;
    private static GameType modeBeforeCreative = GameType.SURVIVAL;

    private static final double AIM_RANGE = 8.0;
    private static final boolean[] wasDown = new boolean[GLFW.GLFW_KEY_LAST + 1];

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

    public static boolean xrayVisible(BlockState state) {
        return XRAY_BLOCKS.contains(state.getBlock());
    }

    public static void onClientTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        boolean ingame = mc.screen == null;

        if (pressed(mc, GLFW.GLFW_KEY_H) && ingame) {
            esp = !esp;
            status(mc, "ESP", esp);
        }
        if (pressed(mc, GLFW.GLFW_KEY_J) && ingame) {
            aim = !aim;
            status(mc, "Aim", aim);
        }
        if (pressed(mc, GLFW.GLFW_KEY_K) && ingame) {
            xray = !xray;
            mc.levelRenderer.allChanged();
            status(mc, "X-Ray", xray);
        }
        if (pressed(mc, GLFW.GLFW_KEY_N) && ingame) toggleVanish(mc);
        if (pressed(mc, GLFW.GLFW_KEY_M) && ingame) toggleCreative(mc);

        if (aim && ingame && mc.options.keyAttack.isDown()) aimAtNearest(mc.player, mc);
    }

    private static boolean pressed(Minecraft mc, int key) {
        boolean down = GLFW.glfwGetKey(mc.getWindow().handle(), key) == GLFW.GLFW_PRESS;
        boolean was = wasDown[key];
        wasDown[key] = down;
        return down && !was;
    }

    // Action bar only: nothing goes to chat or to other players.
    private static void status(Minecraft mc, String name, boolean on) {
        mc.gui.setOverlayMessage(Component.literal(name + (on ? ": ON" : ": OFF")), false);
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
        if (server == null) {
            mc.gui.setOverlayMessage(Component.literal("Works only when you host the world"), false);
        }
        return server;
    }

    private static void toggleCreative(Minecraft mc) {
        IntegratedServer server = hostServer(mc);
        if (server == null) return;
        creative = !creative;
        boolean on = creative;
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
        status(mc, "Creative", on);
    }

    private static void toggleVanish(Minecraft mc) {
        IntegratedServer server = hostServer(mc);
        if (server == null) return;
        vanish = !vanish;
        boolean on = vanish;
        UUID id = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(id);
            if (sp == null) return;
            VanishState.vanished = on ? id : null;
            sp.setSilent(on);
            sp.setInvisible(on);
        });
        status(mc, "Vanish", on);
    }
}
