package com.zombiemod.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PointsAnimationManager {

    private static final List<FloatingPoints> animations = new ArrayList<>();
    private static final Random random = new Random();

    public static void addAnimation(int points) {
        animations.add(new FloatingPoints(points));
    }

    public static void tick() {
        Iterator<FloatingPoints> iterator = animations.iterator();
        while (iterator.hasNext()) {
            FloatingPoints anim = iterator.next();
            anim.tick();
            if (anim.isDone()) {
                iterator.remove();
            }
        }
    }

    public static List<FloatingPoints> getAnimations() {
        return new ArrayList<>(animations);
    }

    public static void clear() {
        animations.clear();
    }

    public static class FloatingPoints {
        private final int points;
        private final float velocityX;
        private final float velocityY;
        private float x;
        private float y;
        private int age;
        private static final int MAX_AGE = 60; // 3 secondes à 20 ticks/s

        public FloatingPoints(int points) {
            this.points = points;
            // Position initiale près du compteur de points (en haut à gauche)
            this.x = 0;
            this.y = 0;

            // Vélocité aléatoire pour l'effet "dans tous les sens"
            // Plus de mouvement pour les gros points
            float spread = points >= 100 ? 3.0f : 1.5f;
            this.velocityX = (random.nextFloat() - 0.5f) * spread;
            this.velocityY = -1.0f + random.nextFloat() * -1.5f; // Toujours vers le haut
            this.age = 0;
        }

        public void tick() {
            age++;
            // Appliquer la vélocité
            x += velocityX;
            y += velocityY;
        }

        public boolean isDone() {
            return age >= MAX_AGE;
        }

        public int getPoints() {
            return points;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getAlpha() {
            // Fade out progressif
            return Math.max(0, 1.0f - ((float) age / MAX_AGE));
        }

        public float getScale() {
            // Effet de scale pour le début de l'animation
            if (age < 10) {
                return 0.5f + (age / 10.0f) * 0.5f; // 0.5 -> 1.0
            }
            return 1.0f;
        }

        public String getColor() {
            // Vert pour +10, Or pour +100
            return points >= 100 ? "§6" : "§a";
        }
    }
}
