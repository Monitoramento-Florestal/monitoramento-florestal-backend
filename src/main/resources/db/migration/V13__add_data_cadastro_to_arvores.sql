ALTER TABLE tb_arvore
    ADD COLUMN IF NOT EXISTS data_cadastro TIMESTAMP;

UPDATE tb_arvore a
SET data_cadastro = COALESCE(
        (
            SELECT MIN(r.data_coleta)
            FROM tb_registro_arvore r
            WHERE r.arvore = a.id OR r.arvore_id = a.id
        ),
        CURRENT_TIMESTAMP
    )
WHERE a.data_cadastro IS NULL;

ALTER TABLE tb_arvore
    ALTER COLUMN data_cadastro SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_arvore_data_cadastro_id
    ON tb_arvore (data_cadastro, id);
