ALTER TABLE tb_arvore
    ADD COLUMN IF NOT EXISTS dap_atual DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS copa_atual DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS estado_geral VARCHAR(255),
    ADD COLUMN IF NOT EXISTS vigor VARCHAR(255),
    ADD COLUMN IF NOT EXISTS estrutura_tronco VARCHAR(255),
    ADD COLUMN IF NOT EXISTS estrutura_base VARCHAR(255),
    ADD COLUMN IF NOT EXISTS estrutura_copa VARCHAR(255),
    ADD COLUMN IF NOT EXISTS inclinacao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ancoragem VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fluxo_pedestre VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fluxo_automovel VARCHAR(255),
    ADD COLUMN IF NOT EXISTS tipo_via VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fiacao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS calcada VARCHAR(255),
    ADD COLUMN IF NOT EXISTS iluminacao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS edificacao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS prioridade VARCHAR(255),
    ADD COLUMN IF NOT EXISTS observacoes VARCHAR(255);

ALTER TABLE tb_arvore
    ALTER COLUMN condicao DROP NOT NULL,
    ALTER COLUMN localizacao DROP NOT NULL;

ALTER TABLE tb_registro_arvore
    ADD COLUMN IF NOT EXISTS especie VARCHAR(255),
    ADD COLUMN IF NOT EXISTS bairro VARCHAR(255),
    ADD COLUMN IF NOT EXISTS rua VARCHAR(255),
    ADD COLUMN IF NOT EXISTS referencia VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pesquisador UUID,
    ADD COLUMN IF NOT EXISTS arvore UUID,
    ADD COLUMN IF NOT EXISTS administrador UUID,
    ADD COLUMN IF NOT EXISTS dap_coletada DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS copa_coletada DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS estado_geral VARCHAR(255),
    ADD COLUMN IF NOT EXISTS vigor VARCHAR(255),
    ADD COLUMN IF NOT EXISTS estrutura_tronco VARCHAR(255),
    ADD COLUMN IF NOT EXISTS estrutura_base VARCHAR(255),
    ADD COLUMN IF NOT EXISTS estrutura_copa VARCHAR(255),
    ADD COLUMN IF NOT EXISTS inclinacao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ancoragem VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fluxo_pedestre VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fluxo_automovel VARCHAR(255),
    ADD COLUMN IF NOT EXISTS tipo_via VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fiacao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS calcada VARCHAR(255),
    ADD COLUMN IF NOT EXISTS iluminacao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS edificacao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS prioridade VARCHAR(255),
    ADD COLUMN IF NOT EXISTS observacoes VARCHAR(255);

UPDATE tb_registro_arvore
SET pesquisador = pesquisador_id
WHERE pesquisador IS NULL AND pesquisador_id IS NOT NULL;

UPDATE tb_registro_arvore
SET arvore = arvore_id
WHERE arvore IS NULL AND arvore_id IS NOT NULL;

UPDATE tb_registro_arvore
SET administrador = administrador_id
WHERE administrador IS NULL AND administrador_id IS NOT NULL;

UPDATE tb_registro_arvore
SET especie = especie_nova
WHERE especie IS NULL AND especie_nova IS NOT NULL;

ALTER TABLE tb_registro_arvore
    ALTER COLUMN pesquisador_id DROP NOT NULL;

CREATE TABLE IF NOT EXISTS tb_registro_problemas_copa (
    registro_id UUID,
    arvore_id UUID,
    problema VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tb_registro_problemas_tronco (
    registro_id UUID,
    arvore_id UUID,
    problema VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tb_registro_problemas_raiz (
    registro_id UUID,
    arvore_id UUID,
    problema VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tb_alvo_potencial (
    registro_id UUID,
    arvore_id UUID,
    alvo_potencial VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tb_alvo_sensivel (
    registro_id UUID,
    arvore_id UUID,
    alvo_sensivel VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS arvore_acoes (
    arvore_id UUID,
    acoes VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS registro_arvore_acoes (
    registro_arvore_id UUID,
    acoes VARCHAR(255)
);
