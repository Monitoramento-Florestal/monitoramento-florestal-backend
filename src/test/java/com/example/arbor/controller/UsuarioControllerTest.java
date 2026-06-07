package com.example.arbor.controller;

import com.example.arbor.dto.request.AtualizarUsuarioAdminRequestDTO;
import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.PageResponseDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService service;

    @InjectMocks
    private UsuarioController controller;

    @Test
    void listarTodosDeveRetornarPaginaDeUsuarios() {
        Usuario executor = usuario(Perfil.GESTOR);
        PageResponseDTO<UsuarioResponseDTO> pagina = new PageResponseDTO<>(
                List.of(new UsuarioResponseDTO(usuario(Perfil.PESQUISADOR))),
                1,
                10,
                25
        );

        when(service.listarTodos(Perfil.PESQUISADOR, true, "ana", 1, 10, executor)).thenReturn(pagina);

        ResponseEntity<PageResponseDTO<UsuarioResponseDTO>> response = controller.listarTodos(
                Perfil.PESQUISADOR,
                true,
                "ana",
                1,
                10,
                executor
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pagina);
    }

    @Test
    void cadastrarDeveRetornarCreated() {
        Usuario executor = usuario(Perfil.ADMINISTRADOR);
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "Pesquisadora",
                "pesquisadora@arbor.local",
                "Senha123",
                Perfil.PESQUISADOR
        );
        UsuarioResponseDTO usuarioCriado = new UsuarioResponseDTO(usuario(Perfil.PESQUISADOR));

        when(service.salvar(request, executor)).thenReturn(usuarioCriado);

        ResponseEntity<UsuarioResponseDTO> response = controller.cadastrar(request, executor);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(usuarioCriado);
    }

    @Test
    void atualizarDeveDelegarParaServiceERetornarUsuarioAtualizado() {
        UUID id = UUID.fromString("c7241dc0-8b57-4dd0-bcb5-2f28499e20e3");
        Usuario executor = usuario(Perfil.GESTOR);
        AtualizarUsuarioAdminRequestDTO request = new AtualizarUsuarioAdminRequestDTO(
                "Novo Nome",
                "novo@arbor.local",
                Perfil.GESTOR,
                true
        );
        UsuarioResponseDTO usuarioAtualizado = new UsuarioResponseDTO(usuario(Perfil.GESTOR));

        when(service.atualizarUsuario(id, request, executor)).thenReturn(usuarioAtualizado);

        ResponseEntity<UsuarioResponseDTO> response = controller.atualizar(id, request, executor);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(usuarioAtualizado);
    }

    @Test
    void deletarDeveRetornarNoContent() {
        UUID id = UUID.fromString("c7241dc0-8b57-4dd0-bcb5-2f28499e20e3");
        Usuario executor = usuario(Perfil.ADMINISTRADOR);

        ResponseEntity<Void> response = controller.deletar(id, executor);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).deletar(id, executor);
    }

    private Usuario usuario(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.fromString("7de556ed-3d22-4640-9c67-396d06cbefb9"));
        usuario.setNome("Usuario Arbor");
        usuario.setEmail("usuario@arbor.local");
        usuario.setSenha("senha-criptografada");
        usuario.setPerfilAcesso(perfil);
        usuario.setAtivo(true);
        usuario.setRefreshTokenVersion(2);
        return usuario;
    }
}
