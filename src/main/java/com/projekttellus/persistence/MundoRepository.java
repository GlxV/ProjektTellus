package com.projekttellus.persistence;

import com.projekttellus.domain.Terra;
import com.projekttellus.patterns.memento.TerraMemento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Armazenamento de mundos independentes, sem dependências da interface gráfica. */
public interface MundoRepository extends AutoCloseable {
    /**
     * Persiste o estado atual e, quando presentes, acrescenta evento e snapshot numa só transação.
     * Os dois registros devem pertencer à Terra; seus identificadores são únicos e imutáveis.
     * Uma falha não deixa nenhuma das três gravações aplicada.
     */
    void salvar(Terra terra, RegistroEvento evento, TerraMemento snapshot);

    /** Mundos pela atualização mais recente; lista imutável. */
    List<MundoResumo> listar();

    Optional<Terra> carregar(UUID id);

    /** Eventos em ordem cronológica, com precisão de nanossegundos; lista imutável. */
    List<RegistroEvento> historico(UUID id);

    /** Retratos em ordem de captura; lista imutável. */
    List<TerraMemento> snapshots(UUID id);

    /** Exclui também histórico e snapshots. Um identificador ausente não é erro. */
    void excluir(UUID id);

    /** Idempotente; operações posteriores falham com PersistenciaException. */
    @Override void close();
}
