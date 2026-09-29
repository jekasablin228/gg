package gg.mod;

import org.lwjgl.glfw.GLFW;

public enum Module {
    ESP("ESP", GLFW.GLFW_KEY_H),
    AIM("Aim", GLFW.GLFW_KEY_J),
    XRAY("X-Ray", GLFW.GLFW_KEY_K),
    VANISH("Vanish", GLFW.GLFW_KEY_N),
    CREATIVE("Creative", GLFW.GLFW_KEY_M);

    public final String title;
    public final int defaultKey;
    /** GLFW key code, or {@link GLFW#GLFW_KEY_UNKNOWN} when unbound. */
    public int key;
    public volatile boolean enabled;

    Module(String title, int defaultKey) {
        this.title = title;
        this.defaultKey = defaultKey;
        this.key = defaultKey;
    }
}
