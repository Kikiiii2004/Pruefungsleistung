package edu.swarmintelligence.mayfly;

@FunctionalInterface
public interface MayflyEventListener {
    void onEvent(MayflyEvent e);
}
