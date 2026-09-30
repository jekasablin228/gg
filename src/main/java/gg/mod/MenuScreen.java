package gg.mod;

import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Compact dashboard opened and closed with Insert. */
public class MenuScreen extends Screen {
    private boolean fun;
    private UUID target;
    private int left, top, panelWidth, panelHeight, rowHeight;

    public MenuScreen() {
        super(Component.literal("GG Dashboard"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(520, width - 16);
        panelHeight = Math.min(330, height - 16);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        rowHeight = Math.min(36, (panelHeight - 108) / Module.values().length);
        int tabWidth = (panelWidth - 36) / 2;
        addRenderableWidget(Button.builder(Component.literal(fun ? "Modules" : "§bModules"), b -> {
            fun = false;
            Hacks.binding = null;
            rebuildWidgets();
        }).bounds(left + 14, top + 44, tabWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal(fun ? "§dFun / LAN" : "Fun / LAN"), b -> {
            fun = true;
            Hacks.binding = null;
            rebuildWidgets();
        }).bounds(left + 22 + tabWidth, top + 44, tabWidth, 20).build());

        if (fun) {
            List<Player> players = targets();
            if (players.stream().noneMatch(p -> p.getUUID().equals(target)))
                target = players.isEmpty() ? null : players.get(0).getUUID();
            Button selector = Button.builder(Component.literal("Player: " + targetName()), b -> {
                List<Player> online = targets();
                if (online.isEmpty()) target = null;
                else {
                    int current = -1;
                    for (int i = 0; i < online.size(); i++)
                        if (online.get(i).getUUID().equals(target)) current = i;
                    target = online.get((current + 1) % online.size()).getUUID();
                }
                rebuildWidgets();
            }).bounds(left + 14, top + 76, panelWidth - 28, 20).build();
            addRenderableWidget(selector);
            String[] labels = {"§bGlow · 5 seconds", "§dFloat · 2 seconds", "§aSuper jump · 5 seconds"};
            for (int i = 0; i < labels.length; i++) {
                final int action = i;
                Button button = Button.builder(Component.literal(labels[i]),
                        b -> Pranks.play(minecraft, target, action))
                        .bounds(left + 14, top + 102 + i * 24, panelWidth - 28, 20).build();
                button.active = minecraft.getSingleplayerServer() != null && target != null;
                addRenderableWidget(button);
            }
        } else {
            int keyWidth = Math.max(62, panelWidth / 5);
            int toggleWidth = panelWidth - keyWidth - 38;
            for (int i = 0; i < Module.values().length; i++) {
                Module m = Module.values()[i];
                int y = top + 76 + i * rowHeight;
                addRenderableWidget(Button.builder(toggleLabel(m), b -> {
                    Hacks.toggle(minecraft, m);
                    b.setMessage(toggleLabel(m));
                }).bounds(left + 14, y, toggleWidth, 20).build());
                addRenderableWidget(Button.builder(bindLabel(m), b -> {
                    Hacks.binding = m;
                    rebuildWidgets();
                }).bounds(left + 24 + toggleWidth, y, keyWidth, 20).build());
            }
        }
    }

    private List<Player> targets() {
        if (minecraft.level == null || minecraft.player == null) return List.of();
        return minecraft.level.players().stream()
                .filter(p -> !p.getUUID().equals(minecraft.player.getUUID()))
                .sorted(java.util.Comparator.comparing(p -> p.getName().getString()))
                .map(p -> (Player) p).toList();
    }

    private String targetName() {
        return targets().stream().filter(p -> p.getUUID().equals(target))
                .map(p -> p.getName().getString()).findFirst().orElse("No other players");
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xA0080B14);
        g.fill(left + 3, top + 3, left + panelWidth + 3, top + panelHeight + 3, 0x70000000);
        g.fill(left, top, left + panelWidth, top + panelHeight, 0xF0141928);
        g.fill(left, top, left + panelWidth, top + 2, 0xFF58D8EE);
        g.fill(left, top + 2, left + panelWidth, top + 38, 0xFF1C2438);
        g.drawString(font, "GG  /  DASHBOARD", left + 14, top + 13, 0xFFEDF4FF, false);
        String hint = "INSERT · close";
        g.drawString(font, hint, left + panelWidth - font.width(hint) - 14, top + 13, 0xFF8D9EBB, false);
        g.fill(left + 14, top + panelHeight - 27, left + panelWidth - 14, top + panelHeight - 26, 0xFF303B50);
        String footer = fun ? (minecraft.getSingleplayerServer() == null
                ? "Host your world / LAN to use these effects"
                : "Click player to switch · effects expire automatically")
                : (Hacks.binding == null ? "Click a key to rebind · ESC closes"
                : "Press a key · DELETE clears · ESC cancels");
        g.drawString(font, footer, left + 14, top + panelHeight - 17, 0xFF9BACCA, false);
        if (fun && panelHeight >= 260)
            g.drawString(font, "One prank per 5 seconds. Existing effects are kept.",
                    left + 14, top + 182, 0xFF9BACCA, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private static Component toggleLabel(Module m) {
        return Component.literal((m.enabled ? "§b● " : "§7○ ") + m.title
                + (m.enabled ? "  §aON" : "  §8OFF"));
    }

    private static Component bindLabel(Module m) {
        return Component.literal(Hacks.binding == m ? "§ePress key" : "§7[" + Keys.name(m.key) + "]");
    }

    public void refresh() { rebuildWidgets(); }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void removed() {
        Hacks.binding = null;
        super.removed();
    }
}
