
CREATE TABLE tb_usuario (
                            id UUID PRIMARY KEY,
                            nome VARCHAR(150) NOT NULL,
                            email VARCHAR(150) NOT NULL UNIQUE,
                            senha VARCHAR(255) NOT NULL,
                            perfil_acesso VARCHAR(50) NOT NULL
);


CREATE TABLE tb_arvore (
                           id UUID PRIMARY KEY,
                           especie VARCHAR(100) NOT NULL,
                           altura DOUBLE PRECISION NOT NULL,
                           condicao VARCHAR(50) NOT NULL,
                           data_registro DATE,
                           localizacao GEOMETRY(Point, 4326) NOT NULL
);