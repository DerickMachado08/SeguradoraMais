package com.mais.seguradora.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mais.seguradora.Repositories.ClienteRepository;
import com.mais.seguradora.handler.GlobalExceptionHandler;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.*;
import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Servidor de teste explícito: controllers reais, repositório em memória, sem Boot/JPA/banco. */
public final class AuthUiServer {
    private final Map<String, Cliente> clients = new ConcurrentHashMap<>();
    private final Map<String, MockHttpSession> sessions = new ConcurrentHashMap<>();
    private final MockMvc mvc;
    private AuthUiServer() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Cliente cliente = Cliente.builder().id(10).nome("Cliente de Teste").cpf("529.982.247-25")
                .email("cliente@example.test").dataNascimento(LocalDate.of(1990,1,1)).senha(encoder.encode("Teste@123")).build();
        clients.put(cliente.getEmail(), cliente);
        Cliente empty = Cliente.builder().id(11).nome("Cliente Sem Pedidos").cpf("123.456.789-09")
                .email("vazio@example.test").dataNascimento(LocalDate.of(1990,1,1)).senha(encoder.encode("Teste@123")).build();
        clients.put(empty.getEmail(), empty);
        Cliente portfolio = Cliente.builder().id(12).nome("Cliente Carteira Teste").cpf("111.444.777-35")
                .email("carteira@example.test").dataNascimento(LocalDate.of(1990,1,1)).senha(encoder.encode("Teste@123")).build();
        clients.put(portfolio.getEmail(), portfolio);
        ClienteRepository repository = mock(ClienteRepository.class);
        when(repository.findByEmail(anyString())).thenAnswer(call -> Optional.ofNullable(clients.get(call.getArgument(0))));
        when(repository.existsByEmail(anyString())).thenAnswer(call -> clients.containsKey(call.getArgument(0)));
        when(repository.existsByCpf(anyString())).thenAnswer(call -> clients.values().stream().anyMatch(value -> value.getCpf().equals(call.<String>getArgument(0))));
        AtomicInteger sequence = new AtomicInteger(40);
        when(repository.save(any(Cliente.class))).thenAnswer(call -> {
            Cliente value = call.getArgument(0); value.setId(sequence.incrementAndGet()); clients.put(value.getEmail(), value); return value;
        });
        ClienteService clientes = new ClienteService();
        ReflectionTestUtils.setField(clientes, "repository", repository);
        ReflectionTestUtils.setField(clientes, "passwordEncoder", encoder);
        Corretor corretor = new Corretor(); corretor.setId(20); corretor.setNome("Corretor de Teste"); corretor.setEmail("corretor@example.test");
        Admin admin = new Admin(); admin.setId(30); admin.setName("Admin de Teste"); admin.setEmail("admin@example.test");
        CorretorService corretores = mock(CorretorService.class); AdminService admins = mock(AdminService.class);
        when(corretores.autenticar("corretor@example.test", "Teste@123")).thenReturn(corretor);
        Corretor semCarteira = new Corretor(); semCarteira.setId(21); semCarteira.setNome("Corretor Sem Carteira"); semCarteira.setEmail("semcarteira@example.test");
        when(corretores.autenticar("semcarteira@example.test", "Teste@123")).thenReturn(semCarteira);
        when(admins.autenticar("admin@example.test", "Teste@123")).thenReturn(admin);
        LoginController login = new LoginController();
        ReflectionTestUtils.setField(login, "clienteservice", clientes);
        ReflectionTestUtils.setField(login, "corretorService", corretores);
        ReflectionTestUtils.setField(login, "adminService", admins);
        ClienteController cadastro = new ClienteController(); ReflectionTestUtils.setField(cadastro, "service", clientes);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean(); validator.afterPropertiesSet();
        Object[] areas = ClienteUiFixtures.controllers(repository, clients, corretor);
        mvc = MockMvcBuilders.standaloneSetup(login, cadastro, new SessaoController(), areas[0], areas[1])
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator)
                .setMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8), new MappingJackson2HttpMessageConverter(mapper)).build();
    }
    private void handle(HttpExchange exchange) throws java.io.IOException {
        String path = exchange.getRequestURI().getPath(), method = exchange.getRequestMethod();
        if (!(method.equals("GET") && path.equals("/auten/sessao"))
                && !(method.equals("POST") && Set.of("/auten/login", "/auten/logout", "/cliente").contains(path))
                && !(Set.of("GET", "POST").contains(method) && path.startsWith("/api/cliente/"))
                && !(Set.of("GET", "POST", "PUT").contains(method) && path.startsWith("/api/corretor/"))) {
            exchange.sendResponseHeaders(404, -1); exchange.close(); return;
        }
        try {
            Path expiryControl = Path.of("tmp", "auth-ui", "expire-sessions.flag").toAbsolutePath().normalize();
            if (Files.isRegularFile(expiryControl) && Files.readString(expiryControl).trim().equals("expire-synthetic-sessions")) {
                for (MockHttpSession value : sessions.values()) if (!value.isInvalid()) value.invalidate();
                sessions.clear(); Files.delete(expiryControl);
                System.out.println("Sessões sintéticas invalidadas; servidor e conexões mantidos.");
            }
            byte[] body = exchange.getRequestBody().readNBytes(16385);
            if (body.length > 16384) { exchange.sendResponseHeaders(413, -1); return; }
            MockHttpServletRequestBuilder request = switch (method) { case "GET" -> get(path); case "PUT" -> put(path); default -> post(path); };
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType != null) request.contentType(contentType);
            if (path.equals("/auten/login")) {
                for (String pair : new String(body, StandardCharsets.UTF_8).split("&")) {
                    String[] field = pair.split("=", 2);
                    if (field.length == 2) request.param(URLDecoder.decode(field[0], StandardCharsets.UTF_8), URLDecoder.decode(field[1], StandardCharsets.UTF_8));
                }
            } else request.content(body);
            String token = exchange.getRequestHeaders().getFirst("X-Session-Token");
            if (token != null) request.header("X-Session-Token", token);
            String previousId = null;
            String cookies = exchange.getRequestHeaders().getFirst("Cookie");
            if (cookies != null) for (String part : cookies.split(";")) if (part.trim().startsWith("JSESSIONID=")) previousId = part.trim().substring(11);
            MockHttpSession previous = previousId == null ? null : sessions.get(previousId);
            if (previous != null && !previous.isInvalid()) request.session(previous);
            var result = mvc.perform(request).andReturn();
            var response = result.getResponse();
            if (previousId != null) sessions.remove(previousId);
            MockHttpSession current = (MockHttpSession) result.getRequest().getSession(false);
            if (current != null && !current.isInvalid()) {
                sessions.put(current.getId(), current);
                if (path.equals("/auten/login") && response.getStatus() == 200)
                    exchange.getResponseHeaders().add("Set-Cookie", "JSESSIONID=" + current.getId() + "; Path=/; HttpOnly; SameSite=Lax");
            }
            for (String name : response.getHeaderNames()) for (String value : response.getHeaders(name)) exchange.getResponseHeaders().add(name, value);
            // Cada resposta fecha a conexão local: evita reuso de sockets ociosos nos roteiros longos.
            exchange.getResponseHeaders().set("Connection", "close");
            byte[] output = response.getContentAsByteArray();
            exchange.sendResponseHeaders(response.getStatus(), output.length == 0 ? -1 : output.length);
            if (output.length > 0) exchange.getResponseBody().write(output);
            System.out.println(method + " " + path + " -> " + response.getStatus());
        } catch (Exception failure) {
            exchange.sendResponseHeaders(503, -1);
            System.out.println("Falha no servidor isolado: " + failure.getClass().getSimpleName());
        } finally { exchange.close(); }
    }
    public static void main(String[] args) throws Exception {
        AuthUiServer test = new AuthUiServer();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 18081), 0);
        server.createContext("/", test::handle); server.setExecutor(Executors.newFixedThreadPool(4));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        server.start(); System.out.println("AUTH_UI_READY 127.0.0.1:18081 — SOMENTE FIXTURES LOCAIS");
    }
}
