package gg.mod;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Toggle and rebind modules. Opened with Right Shift. */
public class MenuScreen extends Screen {
    private static final int ROW_H = 24;

    public MenuScreen() {
        super(Component.literal("GG"));
    }

    @Override
    protected void init() {
        int top = height / 2 - Module.values().length * ROW_H / 2;
        int left = width / 2 - 105;
        for (int i = 0; i < Module.values().length; i++) {
            Module m = Module.values()[i];
            int y = top + i * ROW_H;
            addRenderableWidget(Button.builder(toggleLabel(m), b -> {
                Hacks.toggle(minecraft, m);
                b.setMessage(toggleLabel(m));
            }).bounds(left, y, 140, 20).build());
            addRenderableWidget(Button.builder(bindLabel(m), b -> {
                Hacks.binding = m;
                rebuildWidgets();
            }).bounds(left + 145, y, 65, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(width / 2 - 50, top + Module.values().length * ROW_H + 8, 100, 20).build());
    }

    private static Component toggleLabel(Module m) {
        return Component.literal(m.title + ": " + (m.enabled ? "§aON" : "§cOFF"));
    }

    private static Component bindLabel(Module m) {
        return Component.literal(Hacks.binding == m ? "§e> ... <" : "[" + Keys.name(m.key) + "]");
    }

    /** Called by Hacks after a bind is captured so labels refresh. */
    public void refresh() {
        rebuildWidgets();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        Hacks.binding = null;
        super.removed();
    }
}
