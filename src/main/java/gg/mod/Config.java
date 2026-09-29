package gg.mod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraft.client.Minecraft;

/** Key binds persisted in config/ggmod.properties. */
public final class Config {
    private Config() {}

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("ggmod.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(f)) {
            p.load(r);
        } catch (IOException e) {
            return;
        }
        for (Module m : Module.values()) {
            try {
                m.key = Integer.parseInt(p.getProperty(m.name(), String.valueOf(m.defaultKey)));
            } catch (NumberFormatException ignored) {
                m.key = m.defaultKey;
            }
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : Module.values()) p.setProperty(m.name(), String.valueOf(m.key));
        Path f = file();
        try {
            Files.createDirectories(f.getParent());
            try (Writer w = Files.newBufferedWriter(f)) {
                p.store(w, "ggmod key binds (GLFW key codes, -1 = unbound)");
            }
        } catch (IOException ignored) {
        }
    }
}
