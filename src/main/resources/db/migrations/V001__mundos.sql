-- Cada estado é relacional e completo. Snapshots conservam seus próprios valores imutáveis.
CREATE TABLE IF NOT EXISTS mundos (
    id TEXT PRIMARY KEY NOT NULL,
    nome TEXT NOT NULL CHECK (length(trim(nome)) > 0),
    atualizado_em TEXT NOT NULL,
    oxigenio REAL NOT NULL CHECK (oxigenio BETWEEN 0 AND 100),
    nitrogenio REAL NOT NULL CHECK (nitrogenio BETWEEN 0 AND 100),
    co2 REAL NOT NULL CHECK (co2 BETWEEN 0 AND 100),
    helio REAL NOT NULL CHECK (helio BETWEEN 0 AND 100),
    pressao REAL NOT NULL CHECK (pressao BETWEEN 0 AND 20),
    temperatura REAL NOT NULL CHECK (temperatura BETWEEN -150 AND 200),
    aerossol REAL NOT NULL CHECK (aerossol BETWEEN 0 AND 100),
    radiacao REAL NOT NULL CHECK (radiacao BETWEEN 0 AND 100),
    vegetacao REAL NOT NULL CHECK (vegetacao BETWEEN 0 AND 100),
    agua REAL NOT NULL CHECK (agua BETWEEN 0 AND 100),
    gelo REAL NOT NULL CHECK (gelo BETWEEN 0 AND 100),
    populacao INTEGER NOT NULL CHECK (typeof(populacao) = 'integer' AND populacao BETWEEN 0 AND 100000000000),
    anos REAL NOT NULL CHECK (anos BETWEEN 0 AND 1.7976931348623157e308),
    CHECK (abs(oxigenio + nitrogenio + co2 + helio - 100) <= 0.000001),
    -- Mesma tolerância de arredondamento de Hidrosfera.
    CHECK (agua + gelo <= 100.000001)
);

CREATE TABLE IF NOT EXISTS eventos (
    id TEXT PRIMARY KEY NOT NULL,
    mundo_id TEXT NOT NULL REFERENCES mundos(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    instante TEXT NOT NULL,
    descricao TEXT NOT NULL CHECK (length(trim(descricao)) > 0),
    intensidade REAL NOT NULL CHECK (intensidade BETWEEN 0 AND 1),
    anos REAL NOT NULL CHECK (anos BETWEEN 0 AND 1.7976931348623157e308)
);

CREATE INDEX IF NOT EXISTS idx_eventos_mundo ON eventos(mundo_id, instante, id);

CREATE TABLE IF NOT EXISTS snapshots (
    id TEXT PRIMARY KEY NOT NULL,
    mundo_id TEXT NOT NULL REFERENCES mundos(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    rotulo TEXT NOT NULL CHECK (length(trim(rotulo)) > 0),
    nome_mundo TEXT NOT NULL CHECK (length(trim(nome_mundo)) > 0),
    criado_em TEXT NOT NULL,
    oxigenio REAL NOT NULL CHECK (oxigenio BETWEEN 0 AND 100),
    nitrogenio REAL NOT NULL CHECK (nitrogenio BETWEEN 0 AND 100),
    co2 REAL NOT NULL CHECK (co2 BETWEEN 0 AND 100),
    helio REAL NOT NULL CHECK (helio BETWEEN 0 AND 100),
    pressao REAL NOT NULL CHECK (pressao BETWEEN 0 AND 20),
    temperatura REAL NOT NULL CHECK (temperatura BETWEEN -150 AND 200),
    aerossol REAL NOT NULL CHECK (aerossol BETWEEN 0 AND 100),
    radiacao REAL NOT NULL CHECK (radiacao BETWEEN 0 AND 100),
    vegetacao REAL NOT NULL CHECK (vegetacao BETWEEN 0 AND 100),
    agua REAL NOT NULL CHECK (agua BETWEEN 0 AND 100),
    gelo REAL NOT NULL CHECK (gelo BETWEEN 0 AND 100),
    populacao INTEGER NOT NULL CHECK (typeof(populacao) = 'integer' AND populacao BETWEEN 0 AND 100000000000),
    anos REAL NOT NULL CHECK (anos BETWEEN 0 AND 1.7976931348623157e308),
    CHECK (abs(oxigenio + nitrogenio + co2 + helio - 100) <= 0.000001),
    CHECK (agua + gelo <= 100.000001)
);

CREATE INDEX IF NOT EXISTS idx_snapshots_mundo ON snapshots(mundo_id, criado_em, id);
