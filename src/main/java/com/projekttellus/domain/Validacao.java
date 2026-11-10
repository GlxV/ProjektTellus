package com.projekttellus.domain;

public final class Validacao {
    private Validacao() { }
    public static double intervalo(String nome, double valor, double min, double max) {
        if (!Double.isFinite(valor) || valor < min || valor > max)
            throw new IllegalArgumentException(nome + " deve estar entre " + min + " e " + max + ".");
        return valor;
    }
    public static String texto(String nome, String valor) {
        if (valor == null || valor.isBlank() || valor.strip().length() > 100)
            throw new IllegalArgumentException(nome + " deve conter de 1 a 100 caracteres.");
        return valor.strip();
    }
    public static double limitar(double valor, double min, double max) {
        return Math.clamp(valor, min, max);
    }
}
