package com.mais.seguradora.controllers;

import com.fasterxml.jackson.databind.*;
import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.enummerated.*;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** MVC e serviços reais, sem Boot, conexão com banco, SMTP ou pagamento real. */
class AdminAreaContractTest {
    AdminRepository admins; ClienteRepository clients; CorretorRepository brokers; ApoliceRepository products;
    ContratacaoRepository requests; PagamentoRepository payments; Admin admin; Cliente client; Corretor broker;
    Apolice first, second; Contratacao row; MockMvc mvc; ReceitaDashboardService revenue; AdminAreaService service;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    static final String NEW = "{\"nome\":\"Corretor Novo\",\"cpf\":\"529.982.247-25\",\"email\":\"novo@example.test\",\"telefone\":\"11999999999\",\"senha\":\"Teste@123\",\"produtoId\":1}";
    @BeforeEach void setup() {
        admins=mock(AdminRepository.class); clients=mock(ClienteRepository.class); brokers=mock(CorretorRepository.class);
        products=mock(ApoliceRepository.class); requests=mock(ContratacaoRepository.class); payments=mock(PagamentoRepository.class);
        admin=new Admin(); admin.setId(30); admin.setName("Admin Teste");
        client=Cliente.builder().id(10).nome("Cliente Teste").email("cliente@example.test").senha("PRIVATE_HASH").cpf("PRIVATE_CPF").build();
        broker=new Corretor(); broker.setId(20); broker.setNome("Corretor Teste"); broker.setEmail("corretor@example.test"); broker.setCpf("111.444.777-35"); broker.setSenha("PRIVATE_HASH");
        first=product(1,"Celular"); second=product(2,"Residencial"); first.getCorretores().add(broker); broker.getApolices().add(first);
        row=new Contratacao(); row.setId(101); row.setCliente(client); row.setApolice(first); row.setStatus(StatusContratacao.PENDENTE); row.setDataContratacao(LocalDate.now());
        when(admins.existsById(30)).thenReturn(true); when(clients.findAll()).thenReturn(List.of(client)); when(clients.count()).thenReturn(1L);
        when(brokers.findAll()).thenReturn(List.of(broker)); when(brokers.count()).thenReturn(1L);
        when(products.findAll()).thenReturn(List.of(first,second)); when(products.findAllForAdminOperation()).thenReturn(List.of(first,second));
        when(products.findById(1)).thenReturn(Optional.of(first)); when(brokers.findForAdminOperation(20)).thenReturn(Optional.of(broker));
        when(brokers.save(any())).thenAnswer(i -> { Corretor c=i.getArgument(0); if(c.getId()==null)c.setId(40); return c; });
        when(requests.findAll()).thenReturn(List.of(row)); when(requests.findById(101)).thenReturn(Optional.of(row));
        PagamentoService paymentService=new PagamentoService(); ReflectionTestUtils.setField(paymentService,"pagamentoRepository",payments);
        revenue=new ReceitaDashboardService(paymentService);
        CorretorService registration=new CorretorService(); ReflectionTestUtils.setField(registration,"apoliceRepository",products);
        ReflectionTestUtils.setField(registration,"corretorRepository",brokers); ReflectionTestUtils.setField(registration,"passwordEncoder",encoder);
        service=new AdminAreaService(admins,clients,brokers,products,requests,registration,revenue);
        var mapper=new ObjectMapper().findAndRegisterModules().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var validator=new LocalValidatorFactoryBean(); validator.afterPropertiesSet();
        mvc=MockMvcBuilders.standaloneSetup(new AdminMobileController(service)).setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    }
    Apolice product(int id,String type) { var p=new Apolice(); p.setId(id); p.setTipoApolice(type); p.setNumeroApolice("REF-"+id); p.setStatus(StatusApolice.ATIVA); p.setCorretores(new ArrayList<>()); p.setCoberturas(List.of()); return p; }
    MockHttpSession session(Object user) { var s=new MockHttpSession(); s.setAttribute(SessionSecurity.USER_ATTRIBUTE,user); SessionSecurity.renewToken(s); return s; }
    MockHttpServletRequestBuilder write(boolean links,String body,Object user) { var s=session(user); return (links?put("/api/admin/corretores/20/produtos"):post("/api/admin/corretores")).session(s).header("X-Session-Token",SessionSecurity.token(s)).contentType(MediaType.APPLICATION_JSON).content(body); }
    @Test void allRoutesRejectAnonymousAndNonAdminProfiles() throws Exception {
        for(String path:List.of("dashboard","clientes","corretores","produtos","solicitacoes","solicitacoes/101","apolices")) {
            mvc.perform(get("/api/admin/"+path)).andExpect(status().isUnauthorized()).andExpect(header().string("Cache-Control","no-store"));
            for(Object user:List.of(client,broker)) mvc.perform(get("/api/admin/"+path).session(session(user))).andExpect(status().isForbidden());
        }
        for(Object user:List.of(client,broker)) { mvc.perform(write(false,NEW,user)).andExpect(status().isForbidden()); mvc.perform(write(true,"{\"produtoIds\":[1]}",user)).andExpect(status().isForbidden()); }
        verifyNoInteractions(admins,clients,brokers,products,requests,payments);
    }
    @Test void mutationsRequireNonceFromSameSession() throws Exception {
        var s=session(admin);
        for(String token:List.of("",SessionSecurity.token(session(admin)))) {
            mvc.perform(post("/api/admin/corretores").session(s).header("X-Session-Token",token).contentType(MediaType.APPLICATION_JSON).content(NEW)).andExpect(status().isForbidden());
            mvc.perform(put("/api/admin/corretores/20/produtos").session(s).header("X-Session-Token",token).contentType(MediaType.APPLICATION_JSON).content("{\"produtoIds\":[1]}")).andExpect(status().isForbidden());
        }
        verifyNoInteractions(products,brokers,requests,payments);
    }
    @Test void removedOrMalformedAdminIdentityCannotReadOrWrite() throws Exception {
        when(admins.existsById(30)).thenReturn(false);
        mvc.perform(get("/api/admin/dashboard").session(session(admin))).andExpect(status().isUnauthorized());
        mvc.perform(write(false,NEW,admin)).andExpect(status().isUnauthorized());
        admin.setId(0); mvc.perform(get("/api/admin/clientes").session(session(admin))).andExpect(status().isUnauthorized());
        verifyNoInteractions(products,brokers,clients,requests,payments);
    }
    @Test void summaryUsesIssuedContractsInsteadOfCatalogCountAndExcludesDrafts() throws Exception {
        mvc.perform(get("/api/admin/dashboard").session(session(admin))).andExpect(status().isOk()).andExpect(jsonPath("$.totalClientes").value(1)).andExpect(jsonPath("$.pendentes").value(1)).andExpect(jsonPath("$.apolicesEmitidas").value(0));
        row.setStatus(StatusContratacao.APOLICE_EMITIDA);
        mvc.perform(get("/api/admin/apolices").session(session(admin))).andExpect(jsonPath("$.length()").value(1));
        row.setStatus(StatusContratacao.AGUARDANDO_BEM);
        mvc.perform(get("/api/admin/solicitacoes").session(session(admin))).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/admin/solicitacoes/101").session(session(admin))).andExpect(status().isNotFound());
    }
    @Test void projectionsDoNotExposePasswordsCpfOrFullEntities() throws Exception {
        for(String path:List.of("clientes","corretores","produtos","solicitacoes","solicitacoes/101","apolices")) {
            String body=mvc.perform(get("/api/admin/"+path).session(session(admin))).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andReturn().getResponse().getContentAsString();
            assertFalse(body.contains("PRIVATE_")); assertFalse(body.contains("senha")); assertFalse(body.contains("cpf")); assertFalse(body.contains("usuarioLogado"));
        }
        verify(requests,never()).save(any()); verifyNoInteractions(payments);
    }
    @Test void missingAndInvalidDetailIdsNeverExposeOtherData() throws Exception {
        mvc.perform(get("/api/admin/solicitacoes/999").session(session(admin))).andExpect(status().isNotFound());
        mvc.perform(get("/api/admin/solicitacoes/-1").session(session(admin))).andExpect(status().isNotFound());
        mvc.perform(get("/api/admin/solicitacoes/abc").session(session(admin))).andExpect(status().isBadRequest());
    }
    @Test void registrationReusesBcryptAndInitialProductIgnoresPrivilegeFields() throws Exception {
        mvc.perform(write(false,NEW.replace("\"produtoId\":1","\"produtoId\":1,\"id\":30,\"perfil\":\"ADMIN\",\"senhaHash\":\"unsafe\""),admin)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(40)).andExpect(jsonPath("$.produtos[0].id").value(1)).andExpect(jsonPath("$.senha").doesNotExist());
        verify(brokers).save(argThat(c -> c.getId()==40 && encoder.matches("Teste@123",c.getSenha()) && c.getSenha().startsWith("$2")));
        assertEquals(2,first.getCorretores().size()); verifyNoInteractions(requests,payments);
    }
    @Test void duplicateEmailsAcrossAllRolesAreRejectedBeforeSaving() throws Exception {
        when(brokers.existsByEmailIgnoreCase("novo@example.test")).thenReturn(true);
        mvc.perform(write(false,NEW,admin)).andExpect(status().isConflict());
        when(brokers.existsByEmailIgnoreCase(anyString())).thenReturn(false); when(clients.existsByEmailIgnoreCase("novo@example.test")).thenReturn(true);
        mvc.perform(write(false,NEW,admin)).andExpect(status().isConflict());
        when(clients.existsByEmailIgnoreCase(anyString())).thenReturn(false); when(admins.existsByEmailIgnoreCase("novo@example.test")).thenReturn(true);
        mvc.perform(write(false,NEW,admin)).andExpect(status().isConflict()); verify(brokers,never()).save(any());
    }
    @Test void cpfFormattingCannotBypassDuplicateCheck() throws Exception {
        broker.setCpf("52998224725"); mvc.perform(write(false,NEW,admin)).andExpect(status().isConflict()); verify(brokers,never()).save(any());
    }
    @Test void invalidRegistrationAndUtf8PasswordAreRejected() throws Exception {
        for(String body:List.of("{}",NEW.replace("Teste@123","fraca"),NEW.replace("529.982.247-25","11111111111"),NEW.replace("\"produtoId\":1","\"produtoId\":-1"),NEW.replace("Teste@123","Teste@123"+"é".repeat(33))))
            mvc.perform(write(false,body,admin)).andExpect(status().isBadRequest());
        verify(brokers,never()).save(any());
    }
    @Test void missingProductCannotCreateOrPartiallyChangeLinks() throws Exception {
        mvc.perform(write(false,NEW.replace("\"produtoId\":1","\"produtoId\":999"),admin)).andExpect(status().isNotFound());
        mvc.perform(write(true,"{\"produtoIds\":[2,999]}",admin)).andExpect(status().isNotFound());
        assertTrue(first.getCorretores().contains(broker)); assertTrue(second.getCorretores().isEmpty()); verify(products,never()).save(any()); verify(brokers,never()).save(any());
    }
    @Test void productLinksUpdateOwningSideAndAllRequestVisibilityWithoutDeletingHistory() throws Exception {
        mvc.perform(write(true,"{\"produtoIds\":[2]}",admin)).andExpect(status().isOk()).andExpect(jsonPath("$.produtos[0].id").value(2));
        assertFalse(first.getCorretores().contains(broker)); assertTrue(second.getCorretores().contains(broker));
        assertSame(first,row.getApolice()); assertEquals(StatusContratacao.PENDENTE,row.getStatus());
        verify(products).save(first); verify(products).save(second); verifyNoInteractions(requests,payments);
    }
    @Test void repeatedOrEmptyLinkSelectionsAreSafeAndCannotDeleteUser() throws Exception {
        mvc.perform(write(true,"{\"produtoIds\":[1,1]}",admin)).andExpect(status().isBadRequest());
        mvc.perform(write(true,"{\"produtoIds\":[]}",admin)).andExpect(status().isOk()).andExpect(jsonPath("$.produtos.length()").value(0));
        assertTrue(first.getCorretores().isEmpty()); verify(brokers,never()).delete(any()); verifyNoInteractions(requests,payments);
    }
    @Test void infrastructureErrorsAreSafeAndNeverLeakExceptionMessage() throws Exception {
        when(products.findAllForAdminOperation()).thenThrow(new RuntimeException("PRIVATE_DATABASE_SECRET"));
        String body=mvc.perform(write(false,NEW,admin)).andExpect(status().isServiceUnavailable()).andExpect(header().string("Cache-Control","no-store")).andReturn().getResponse().getContentAsString();
        assertFalse(body.contains("PRIVATE_")); verify(brokers,never()).save(any());
    }
    @Test void financeKeepsAllTimeTotalAndSixDatedMonthsWithExactDecimals() throws Exception {
        var month=YearMonth.now(); List<Pagamento> data=new ArrayList<>();
        data.add(payment("10.10",month.atDay(1))); data.add(payment("20.20",month.atDay(2)));
        data.add(payment("40.00",month.minusMonths(5).atDay(1))); data.add(payment("50.00",month.minusMonths(6).atDay(1)));
        data.add(payment("5.00",null)); data.add(payment(null,month.atDay(3)));
        when(payments.findByStatus(StatusPagamento.APROVADA)).thenReturn(data);
        var result=revenue.resumo(); assertEquals(new BigDecimal("125.30"),result.totalRecebido()); assertEquals(6,result.meses().size());
        assertEquals(month.minusMonths(5).toString(),result.meses().getFirst().mes()); assertEquals(new BigDecimal("30.30"),result.meses().getLast().valor());
        assertEquals(BigDecimal.ZERO,result.meses().get(1).valor());
        mvc.perform(get("/api/admin/dashboard").session(session(admin))).andExpect(jsonPath("$.receita.totalRecebido").value(125.3)).andExpect(jsonPath("$.receita.meses.length()").value(6));
        verify(payments,never()).findByStatus(StatusPagamento.PENDENTE); verify(payments,never()).findByStatus(StatusPagamento.REPROVADA);
    }
    @Test void webDashboardAndApiUseSameFinanceServiceAndPreserveWebModelKeys() {
        dashboardController controller=new dashboardController(); ReflectionTestUtils.setField(controller,"receitaDashboardService",revenue);
        ReflectionTestUtils.setField(controller,"sinistroService",mock(SinistroService.class)); ReflectionTestUtils.setField(controller,"contratacaoService",mock(ContratacaoService.class));
        ReflectionTestUtils.setField(controller,"clienteService",mock(ClienteService.class)); ReflectionTestUtils.setField(controller,"corretorService",mock(CorretorService.class)); ReflectionTestUtils.setField(controller,"apoliceService",mock(ApoliceService.class));
        when(payments.findByStatus(StatusPagamento.APROVADA)).thenReturn(List.of(payment("19.90",LocalDate.now())));
        var model=new org.springframework.ui.ExtendedModelMap(); assertEquals("cliente/dashboard/dashboardAdmin",controller.dashboardAdmin(session(admin),model));
        var api=service.dashboard(30).receita(); assertEquals(api.totalRecebido(),model.get("total")); assertEquals(api.meses().stream().map(ReceitaDashboardService.Mes::rotulo).toList(),model.get("receitaLabels"));
        assertEquals(api.meses().stream().map(ReceitaDashboardService.Mes::valor).toList(),model.get("receitaValores")); assertTrue(model.containsAttribute("segurosLabels"));
    }
    Pagamento payment(String value,LocalDate date) { var p=new Pagamento(); p.setValor(value==null?null:new BigDecimal(value)); p.setDataPagamento(date); p.setStatus(StatusPagamento.APROVADA); return p; }
}
