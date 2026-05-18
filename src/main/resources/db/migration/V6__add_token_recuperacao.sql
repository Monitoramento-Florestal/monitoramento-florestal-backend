DROP TABLE IF EXISTS tb_token_reset_senha;

CREATE TABLE tb_token_recuperacao (
    id UUID PRIMARY KEY,
    codigo VARCHAR(6) NOT NULL,
    usuario_id UUID NOT NULL,
    expiracao TIMESTAMP NOT NULL,
    utilizado BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_token_recuperacao_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES tb_usuario(id)
);
