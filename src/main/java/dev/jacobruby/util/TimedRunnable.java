package dev.jacobruby.util;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class TimedRunnable extends BukkitRunnable {
    private int tick;

    @Override
    public final void run() {
        this.run(this.tick++);
    }

    /**
     * The first time this is called, {@code tick} will be {@code 0}.
     * @param tick the number of times {@link #run(int)} has been called before
     */
    protected abstract void run(int tick);
}
