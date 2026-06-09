CREATE TABLE tb_solicitacao_aprovacao (
    id UUID PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    data_submissao TIMESTAMP NOT NULL,
    pesquisador_id UUID NOT NULL,
    data_revisao TIMESTAMP,
    revisor_id UUID,
    motivo_recusa TEXT,
    arvore_id UUID,
    registro_alvo_id UUID,
    proposta_arvore JSONB,
    proposta_registro JSONB
);

ALTER TABLE tb_solicitacao_aprovacao
    ADD CONSTRAINT fk_solicitacao_pesquisador
        FOREIGN KEY (pesquisador_id)
            REFERENCES tb_usuario(id);

ALTER TABLE tb_solicitacao_aprovacao
    ADD CONSTRAINT fk_solicitacao_revisor
        FOREIGN KEY (revisor_id)
            REFERENCES tb_usuario(id);

ALTER TABLE tb_solicitacao_aprovacao
    ADD CONSTRAINT fk_solicitacao_arvore
        FOREIGN KEY (arvore_id)
            REFERENCES tb_arvore(id);

ALTER TABLE tb_solicitacao_aprovacao
    ADD CONSTRAINT fk_solicitacao_registro
        FOREIGN KEY (registro_alvo_id)
            REFERENCES tb_registro_arvore(id);

ALTER TABLE tb_registro_arvore
    ADD COLUMN IF NOT EXISTS registro_origem_id UUID;

CREATE INDEX idx_solicitacao_status
    ON tb_solicitacao_aprovacao(status);

CREATE INDEX idx_solicitacao_pesquisador
    ON tb_solicitacao_aprovacao(pesquisador_id);

CREATE INDEX idx_solicitacao_pesquisador_status
    ON tb_solicitacao_aprovacao(pesquisador_id, status);

CREATE INDEX idx_solicitacao_tipo
    ON tb_solicitacao_aprovacao(tipo);

CREATE INDEX idx_solicitacao_data_submissao
    ON tb_solicitacao_aprovacao(data_submissao);

CREATE INDEX idx_solicitacao_status_submissao
    ON tb_solicitacao_aprovacao(status, data_submissao);
