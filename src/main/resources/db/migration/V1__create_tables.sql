CREATE TABLE tb_usuario (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil_acesso VARCHAR(50) NOT NULL,
    matricula_ufrpe VARCHAR(255),
    refresh_token_version INTEGER DEFAULT 0
);


CREATE TABLE tb_arvore (
    id UUID PRIMARY KEY,
    especie VARCHAR(100) NOT NULL,
    altura_atual DOUBLE PRECISION NOT NULL,
    condicao VARCHAR(50) NOT NULL,
    localizacao GEOMETRY(Point, 4326) NOT NULL
);


CREATE TABLE tb_registro_arvore (
    id UUID PRIMARY KEY,
    pesquisador_id UUID NOT NULL,
    data_coleta TIMESTAMP,
    arvore_id UUID,
    especie_nova VARCHAR(255),
    localizacao_nova GEOMETRY(Point, 4326),
    status VARCHAR(50) NOT NULL,
    administrador_id UUID,
    data_analise TIMESTAMP,
    motivo_recusa VARCHAR(255),
    altura_coletada DOUBLE PRECISION,
    condicao_coletada VARCHAR(50),
    CONSTRAINT fk_registro_pesquisador FOREIGN KEY (pesquisador_id) REFERENCES tb_usuario(id),
    CONSTRAINT fk_registro_arvore FOREIGN KEY (arvore_id) REFERENCES tb_arvore(id),
    CONSTRAINT fk_registro_admin FOREIGN KEY (administrador_id) REFERENCES tb_usuario(id)
);


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
