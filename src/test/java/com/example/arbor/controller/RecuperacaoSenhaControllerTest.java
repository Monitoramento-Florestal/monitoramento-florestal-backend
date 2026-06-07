package com.example.arbor.controller;

import com.example.arbor.dto.request.RedefinirSenhaDTO;
import com.example.arbor.dto.request.SolicitarRecuperacaoDTO;
import com.example.arbor.dto.request.VerificarCodigoDTO;
import com.example.arbor.dto.response.MensagemResponseDTO;
import com.example.arbor.service.RecuperacaoSenhaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaControllerTest {

    @Mock
    private RecuperacaoSenhaService service;

    @InjectMocks
    private RecuperacaoSenhaController controller;

    @Test
    void solicitarRecuperacaoDeveRetornarMensagemNeutra() {
        SolicitarRecuperacaoDTO request = new SolicitarRecuperacaoDTO("usuario@arbor.local");

        ResponseEntity<MensagemResponseDTO> response = controller.solicitarRecuperacao(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("Se o e-mail estiver cadastrado, um codigo de recuperacao sera enviado.");
        verify(service).solicitarRecuperacao(request);
    }

    @Test
    void verificarCodigoDeveRetornarMensagemDeCodigoValido() {
        VerificarCodigoDTO request = new VerificarCodigoDTO("123456", "usuario@arbor.local");

        ResponseEntity<MensagemResponseDTO> response = controller.verificarCodigo(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Codigo valido. Prossiga para redefinir sua senha.");
        verify(service).verificarCodigo(request);
    }

    @Test
    void redefinirSenhaDeveRetornarMensagemDeSucesso() {
        RedefinirSenhaDTO request = new RedefinirSenhaDTO(
                "123456",
                "usuario@arbor.local",
                "NovaSenha123",
                "NovaSenha123"
        );

        ResponseEntity<MensagemResponseDTO> response = controller.redefinirSenha(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Senha redefinida com sucesso.");
        verify(service).redefinirSenha(request);
    }
}
