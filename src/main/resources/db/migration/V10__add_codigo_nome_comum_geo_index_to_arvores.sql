CREATE SEQUENCE IF NOT EXISTS seq_arvore_codigo
    START WITH 1
    INCREMENT BY 1
    NO CYCLE;

ALTER TABLE tb_arvore
    ADD COLUMN IF NOT EXISTS codigo VARCHAR(20);

UPDATE tb_arvore
SET codigo = 'ARV-' || LPAD(nextval('seq_arvore_codigo')::TEXT, 5, '0')
WHERE codigo IS NULL;

ALTER TABLE tb_arvore
    ALTER COLUMN codigo SET NOT NULL;

ALTER TABLE tb_arvore
    ADD CONSTRAINT uq_arvore_codigo UNIQUE (codigo);

ALTER TABLE tb_arvore
    ADD COLUMN IF NOT EXISTS nome_comum VARCHAR(150);

ALTER TABLE tb_arvore
    ADD COLUMN IF NOT EXISTS localizacao GEOMETRY(Point, 4326);

CREATE INDEX IF NOT EXISTS idx_arvore_localizacao_gist
    ON tb_arvore USING GIST (localizacao);

CREATE INDEX IF NOT EXISTS idx_arvore_especie
    ON tb_arvore (especie);

CREATE INDEX IF NOT EXISTS idx_arvore_nome_comum
    ON tb_arvore (nome_comum);

CREATE INDEX IF NOT EXISTS idx_arvore_ativa
    ON tb_arvore (ativa);
