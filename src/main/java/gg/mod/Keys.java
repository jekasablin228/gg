package gg.mod;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;

/** Polls the keyboard directly so binds work without registering vanilla key mappings. */
public final class Keys {
    // Only valid GLFW key codes: querying gaps makes GLFW report an error.
    private static final int[] ALL = build();
    private static final boolean[] wasDown = new boolean[GLFW.GLFW_KEY_LAST + 1];

    private Keys() {}

    private static int[] build() {
        int[][] ranges = {{32, 32}, {39, 39}, {44, 57}, {59, 59}, {61, 61}, {65, 93}, {96, 96},
                {256, 269}, {280, 284}, {290, 314}, {320, 336}, {340, 348}};
        List<Integer> keys = new ArrayList<>();
        for (int[] r : ranges) for (int k = r[0]; k <= r[1]; k++) keys.add(k);
        return keys.stream().mapToInt(Integer::intValue).toArray();
    }

    /** Returns the keys that went down since the previous call. */
    public static List<Integer> poll(long window) {
        List<Integer> pressed = new ArrayList<>();
        for (int k : ALL) {
            boolean down = GLFW.glfwGetKey(window, k) == GLFW.GLFW_PRESS;
            if (down && !wasDown[k]) pressed.add(k);
            wasDown[k] = down;
        }
        return pressed;
    }

    public static String name(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "-";
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        switch (key) {
            case GLFW.GLFW_KEY_SPACE: return "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RALT";
            case GLFW.GLFW_KEY_TAB: return "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "CAPS";
            case GLFW.GLFW_KEY_INSERT: return "INSERT";
            case GLFW.GLFW_KEY_DELETE: return "DELETE";
            case GLFW.GLFW_KEY_HOME: return "HOME";
            case GLFW.GLFW_KEY_END: return "END";
            case GLFW.GLFW_KEY_PAGE_UP: return "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PGDN";
            case GLFW.GLFW_KEY_UP: return "UP";
            case GLFW.GLFW_KEY_DOWN: return "DOWN";
            case GLFW.GLFW_KEY_LEFT: return "LEFT";
            case GLFW.GLFW_KEY_RIGHT: return "RIGHT";
            default: break;
        }
        if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) return "NUM" + (key - GLFW.GLFW_KEY_KP_0);
        String n = GLFW.glfwGetKeyName(key, 0);
        return n != null ? n.toUpperCase() : "KEY" + key;
    }
}
