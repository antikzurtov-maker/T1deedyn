package dev.t1deedyn;

/** Minimal shared base for client-side visual/gameplay modules. */
public abstract class VisualModule {
    private final String name;

    protected VisualModule(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public abstract boolean isEnabled();
    public abstract void setEnabled(boolean enabled);
}
