package com.projekttellus.persistence;

/** Falha de armazenamento; preserva a causa para diagnóstico fora da apresentação principal. */
public final class PersistenciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem) { super(mensagem); }
    public PersistenciaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
