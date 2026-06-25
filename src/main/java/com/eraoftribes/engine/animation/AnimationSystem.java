package com.eraoftribes.engine.animation;

import java.util.ArrayList;
import java.util.List;

public class AnimationSystem {
    private final List<Animation> activeAnimations = new ArrayList<>();

    public AnimationSystem() {
        System.out.println("[AnimationSystem] Initialized.");
    }

    public void play(Animation anim) {
        activeAnimations.add(anim);
        anim.start();
    }

    public void stop(Object target) {
        activeAnimations.removeIf(a -> a.target == target);
    }

    public void stopAll() {
        activeAnimations.clear();
    }

    public void update(double dt) {
        var it = activeAnimations.iterator();
        while (it.hasNext()) {
            var anim = it.next();
            anim.update(dt);
            if (anim.isFinished()) {
                if (anim.callback != null) anim.callback.run();
                it.remove();
            }
        }
    }

    public static class Animation {
        public enum Type { FADE, MOVE, SCALE, ROTATE, BOUNCE, GLOW, SHAKE, SLIDE, PULSE, FLIP }

        private final Type type;
        private final Object target;
        private double duration;
        private double elapsed;
        private boolean finished;
        public Runnable callback;

        public Animation(Type type, Object target, double duration) {
            this.type = type;
            this.target = target;
            this.duration = duration;
        }

        void start() { elapsed = 0; finished = false; }
        void update(double dt) {
            elapsed += dt;
            if (elapsed >= duration) finished = true;
        }
        boolean isFinished() { return finished; }

        public Type getType() { return type; }
        public double getProgress() { return Math.min(1.0, elapsed / duration); }
    }
}
