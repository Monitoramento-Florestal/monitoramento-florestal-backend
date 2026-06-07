-- Adiciona a coluna permitindo nulo temporariamente
ALTER TABLE tb_registro_arvore
    ADD COLUMN IF NOT EXISTS versao INTEGER;

-- Preenche registros existentes numerando por ordem de data de coleta dentro de cada árvore
UPDATE tb_registro_arvore r
SET versao = sub.row_num
    FROM (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY arvore
               ORDER BY data_coleta ASC NULLS LAST
           ) AS row_num
    FROM tb_registro_arvore
    WHERE versao IS NULL
) sub
WHERE r.id = sub.id;

-- Agora que todos têm valor, torna obrigatória e define default para novos registros
ALTER TABLE tb_registro_arvore
    ALTER COLUMN versao SET NOT NULL,
ALTER COLUMN versao SET DEFAULT 1;