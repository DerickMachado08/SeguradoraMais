package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ClienteRepository;
import com.mais.seguradora.exception.RecursoDuplicadoException;
import com.mais.seguradora.model.Cliente;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class ClienteCadastroServiceTest {
    private ClienteService service;
    private ClienteRepository repository;
    @BeforeEach void setup() {
        service = new ClienteService(); repository = mock(ClienteRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("Teste@123")).thenReturn("hash-isolado");
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "passwordEncoder", encoder);
    }
    private Cliente cliente() { return Cliente.builder().cpf("52998224725").email("pessoa@example.test").dataNascimento(LocalDate.of(1990,1,1)).senha("Teste@123").build(); }
    @Test void mascaraCpfNovoSemAlterarRegistrosExistentes() {
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        Cliente saved = service.cadastrar(cliente());
        assertEquals("529.982.247-25", saved.getCpf()); assertEquals("hash-isolado", saved.getSenha());
        verify(repository).existsByCpf("529.982.247-25"); verify(repository).existsByCpf("52998224725");
        verify(repository).save(saved);
    }
    @Test void cpfLegadoSemMascaraImpedeCadastroDuplicado() {
        when(repository.existsByCpf("52998224725")).thenReturn(true);
        assertThrows(RecursoDuplicadoException.class, () -> service.cadastrar(cliente())); verify(repository, never()).save(any());
    }
    @Test void cpfLegadoComMascaraImpedeCadastroDuplicado() {
        when(repository.existsByCpf("529.982.247-25")).thenReturn(true);
        assertThrows(RecursoDuplicadoException.class, () -> service.cadastrar(cliente())); verify(repository, never()).save(any());
    }
}
