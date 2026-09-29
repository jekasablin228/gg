package gg.mod;

import java.util.UUID;

// Kept free of client classes: read on the integrated server thread.
public final class VanishState {
    public static volatile UUID vanished;

    private VanishState() {}
}
