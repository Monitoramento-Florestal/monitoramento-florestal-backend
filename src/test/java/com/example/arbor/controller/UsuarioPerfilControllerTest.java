package com.example.arbor.controller;

import com.example.arbor.dto.request.AlterarSenhaRequestDTO;
import com.example.arbor.dto.request.AtualizarPerfilRequestDTO;
import com.example.arbor.dto.response.UsuarioPerfilResponseDTO;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioPerfilControllerTest {

    @Mock
    private UsuarioService service;

    @InjectMocks
    private UsuarioPerfilController controller;

    @Test
    void buscarMeuPerfilDeveRetornarPerfilAutenticado() {
        Usuario usuario = usuario();
        UsuarioPerfilResponseDTO perfil = UsuarioPerfilResponseDTO.from(usuario);

        when(service.buscarMeuPerfil(usuario)).thenReturn(perfil);

        ResponseEntity<UsuarioPerfilResponseDTO> response = controller.buscarMeuPerfil(usuario);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(perfil);
    }

    @Test
    void atualizarMeuPerfilDeveRetornarPerfilAtualizado() {
        Usuario usuario = usuario();
        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Nome Atualizado",
                "atualizado@arbor.local"
        );
        UsuarioPerfilResponseDTO perfilAtualizado = new UsuarioPerfilResponseDTO(
                usuario.getId(),
                "Nome Atualizado",
                "atualizado@arbor.local",
                Perfil.PESQUISADOR,
                true
        );

        when(service.atualizarMeuPerfil(usuario, request)).thenReturn(perfilAtualizado);

        ResponseEntity<UsuarioPerfilResponseDTO> response = controller.atualizarMeuPerfil(usuario, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(perfilAtualizado);
    }

    @Test
    void alterarMinhaSenhaDeveRetornarNoContent() {
        Usuario usuario = usuario();
        AlterarSenhaRequestDTO request = new AlterarSenhaRequestDTO(
                "SenhaAtual123",
                "NovaSenha123",
                "NovaSenha123"
        );

        ResponseEntity<Void> response = controller.alterarMinhaSenha(usuario, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).alterarMinhaSenha(usuario, request);
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.fromString("af2d73c7-c1cc-4598-923e-f560a2fd3bf2"));
        usuario.setNome("Pesquisadora Arbor");
        usuario.setEmail("pesquisadora@arbor.local");
        usuario.setSenha("senha-criptografada");
        usuario.setPerfilAcesso(Perfil.PESQUISADOR);
        usuario.setAtivo(true);
        usuario.setRefreshTokenVersion(1);
        return usuario;
    }
}
