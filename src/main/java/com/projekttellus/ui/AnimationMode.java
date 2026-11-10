package com.projekttellus.ui;

public enum AnimationMode {
    FULL("Completas"), REDUCED("Movimento reduzido"), OFF("Desativadas");
    private final String label;
    AnimationMode(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
