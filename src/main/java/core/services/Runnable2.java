package core.services;

import core.Chuu;

public interface Runnable2 extends Runnable {

    @Override
    default void run() {
        try {
            execute();
        } catch (Exception e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
    }

    void execute();
}
