package com.mais.seguradora.controllers;

import com.fasterxml.jackson.databind.*;
import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.SinistroMobileDtos.Abertura;
import com.mais.seguradora.enummerated.*;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Sem contexto Boot, JPA, email ou banco configurado. */
class ComplementoMobileContractTest {
    SinistroRepository claims;ContratacaoRepository contracts;PagamentoRepository payments;ClienteRepository clients;CorretorRepository brokers;AdminRepository admins;
    Cliente owner,other;Corretor broker;Admin admin;Apolice product;Contratacao contract;Pagamento payment;Sinistro claim;
    MockMvc mvc;SinistroMobileService service;
    static final String BODY="{\"apoliceId\":1,\"descricao\":\"Tela quebrada após queda\",\"valorPrejuizo\":150.50}";
    @BeforeEach void setup() {
        claims=mock(SinistroRepository.class);contracts=mock(ContratacaoRepository.class);payments=mock(PagamentoRepository.class);clients=mock(ClienteRepository.class);brokers=mock(CorretorRepository.class);admins=mock(AdminRepository.class);
        owner=Cliente.builder().id(10).nome("Cliente Teste").email("cliente@example.test").telefone("11999999999").senha("PRIVATE_HASH").cpf("PRIVATE_CPF").build();other=Cliente.builder().id(11).nome("Outro Cliente").build();
        broker=new Corretor();broker.setId(20);broker.setNome("Corretor Teste");broker.setEmail("corretor@example.test");broker.setTelefone("11888888888");broker.setSenha("PRIVATE_HASH");
        admin=new Admin();admin.setId(30);admin.setName("Admin Teste");admin.setEmail("admin@example.test");admin.setSenha("PRIVATE_PASSWORD");
        product=new Apolice();product.setId(1);product.setTipoApolice("Celular");product.setNumeroApolice("REF-1");
        contract=new Contratacao();contract.setId(101);contract.setCliente(owner);contract.setApolice(product);contract.setStatus(StatusContratacao.APOLICE_EMITIDA);
        payment=new Pagamento();payment.setId(501);payment.setContratacao(contract);payment.setStatus(StatusPagamento.APROVADA);
        claim=new Sinistro();claim.setId(701);claim.setCliente(owner);claim.setApolice(product);claim.setDescricao("Registro anterior");claim.setDataSinsitro(LocalDate.now().minusDays(1));claim.setStatus(StatusSinistro.NEGADO);claim.setValorPrejuizo(new BigDecimal("20.00"));
        when(clients.existsById(10)).thenReturn(true);when(clients.existsById(11)).thenReturn(true);when(admins.existsById(30)).thenReturn(true);
        when(clients.findForSolicitacao(10)).thenReturn(Optional.of(owner));when(clients.findById(10)).thenReturn(Optional.of(owner));when(brokers.findForAdminOperation(20)).thenReturn(Optional.of(broker));when(brokers.findById(20)).thenReturn(Optional.of(broker));when(admins.findForPerfil(30)).thenReturn(Optional.of(admin));when(admins.findById(30)).thenReturn(Optional.of(admin));
        when(contracts.findByClienteId(10)).thenReturn(List.of(contract));when(contracts.findForSinistro(10,1)).thenReturn(List.of(contract));
        when(payments.findByContratacao_IdOrderByIdAsc(101)).thenReturn(List.of(payment));when(payments.findForMobileOperation(101)).thenReturn(List.of(payment));
        when(claims.findByCliente_IdOrderByIdDesc(anyInt())).thenReturn(List.of(claim));when(claims.findAll()).thenReturn(List.of(claim));when(claims.findByIdAndCliente_Id(701,10)).thenReturn(Optional.of(claim));when(claims.findById(701)).thenReturn(Optional.of(claim));
        when(claims.save(any())).thenAnswer(i -> { Sinistro s=i.getArgument(0);s.setId(702);return s; });
        service=new SinistroMobileService(claims,contracts,payments,clients,admins);
        var validator=new LocalValidatorFactoryBean();validator.afterPropertiesSet();
        mvc=MockMvcBuilders.standaloneSetup(new SinistroMobileController(service),new PerfilMobileController(new PerfilMobileService(clients,brokers,admins)),new SessaoController()).setValidator(validator)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))).build();
    }
    MockHttpSession session(Object user) { var s=new MockHttpSession();s.setAttribute(SessionSecurity.USER_ATTRIBUTE,user);SessionSecurity.renewToken(s);return s; }
    MockHttpServletRequestBuilder open(String body,Object u) { var s=session(u);return post("/api/cliente/sinistros").session(s).header("X-Session-Token",SessionSecurity.token(s)).contentType(MediaType.APPLICATION_JSON).content(body); }
    MockHttpServletRequestBuilder profile(String body,Object u) { var s=session(u);return put("/api/perfil").session(s).header("X-Session-Token",SessionSecurity.token(s)).contentType(MediaType.APPLICATION_JSON).content(body); }
    @Test void allReadsAndWritesRequireSession() throws Exception {
        for(String p:List.of("/api/cliente/sinistros","/api/cliente/sinistros/opcoes","/api/cliente/sinistros/701","/api/admin/sinistros","/api/admin/sinistros/701","/api/perfil"))mvc.perform(get(p)).andExpect(status().isUnauthorized()).andExpect(header().string("Cache-Control","no-store"));
        mvc.perform(post("/api/cliente/sinistros").contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/perfil").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Nome Teste\"}")).andExpect(status().isUnauthorized());verifyNoInteractions(claims,contracts,clients,brokers,admins,payments);
    }
    @Test void wrongRolesCannotReadOrOpenClaims() throws Exception {
        for(Object u:List.of(broker,admin)){for(String p:List.of("","/opcoes","/701"))mvc.perform(get("/api/cliente/sinistros"+p).session(session(u))).andExpect(status().isForbidden());mvc.perform(open(BODY,u)).andExpect(status().isForbidden());}
        for(Object u:List.of(owner,broker))for(String p:List.of("","/701"))mvc.perform(get("/api/admin/sinistros"+p).session(session(u))).andExpect(status().isForbidden());verifyNoInteractions(claims,contracts,payments);
    }
    @Test void nonceRequiredForBothMutationsAndBoundToSession() throws Exception {
        for(String token:List.of("",SessionSecurity.token(session(other)))){var s=session(owner);mvc.perform(post("/api/cliente/sinistros").session(s).header("X-Session-Token",token).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());mvc.perform(put("/api/perfil").session(s).header("X-Session-Token",token).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Teste Novo\"}")).andExpect(status().isForbidden());}verifyNoInteractions(claims,contracts,clients);
    }
    @Test void removedIdentityCannotReadOrWrite() throws Exception {
        when(clients.existsById(10)).thenReturn(false);when(clients.findById(10)).thenReturn(Optional.empty());when(clients.findForSolicitacao(10)).thenReturn(Optional.empty());
        mvc.perform(get("/api/cliente/sinistros").session(session(owner))).andExpect(status().isUnauthorized());mvc.perform(open(BODY,owner)).andExpect(status().isUnauthorized());mvc.perform(get("/api/perfil").session(session(owner))).andExpect(status().isUnauthorized());mvc.perform(profile("{\"nome\":\"Nome Novo\",\"telefone\":\"11999999999\"}",owner)).andExpect(status().isUnauthorized());
        when(admins.existsById(30)).thenReturn(false);mvc.perform(get("/api/admin/sinistros").session(session(admin))).andExpect(status().isUnauthorized());verify(claims,never()).save(any());
    }
    @Test void foreignMissingAndNonPositiveClaimIdsHaveSame404() throws Exception { for(int id:List.of(701,999,0,-1))mvc.perform(get("/api/cliente/sinistros/"+id).session(session(other))).andExpect(status().isNotFound());mvc.perform(get("/api/cliente/sinistros/abc").session(session(owner))).andExpect(status().isBadRequest()); }
    @Test void clientHistoryFiltersOwnerAndNeverInventsContractAssociation() throws Exception {
        mvc.perform(get("/api/cliente/sinistros").session(session(other))).andExpect(jsonPath("$.length()").value(0));
        String body=mvc.perform(get("/api/cliente/sinistros/701").session(session(owner))).andExpect(jsonPath("$.clienteNome").isEmpty()).andExpect(jsonPath("$.status").value("NEGADO")).andExpect(jsonPath("$.contratacaoId").doesNotExist()).andReturn().getResponse().getContentAsString();assertFalse(body.contains("PRIVATE_"));assertFalse(body.contains("senha"));assertFalse(body.contains("cpf"));assertFalse(body.contains("email"));
    }
    @Test void adminHasReadOnlyProjectionAndNoDecisionEndpoint() throws Exception { mvc.perform(get("/api/admin/sinistros").session(session(admin))).andExpect(jsonPath("$[0].clienteNome").value("Cliente Teste"));mvc.perform(get("/api/admin/sinistros/701").session(session(admin))).andExpect(status().isOk());mvc.perform(post("/api/admin/sinistros/701/aprovar").session(session(admin))).andExpect(status().isNotFound());verify(claims,never()).save(any()); }
    @Test void eligibleOptionAndOpeningUseServerDateAndInitialState() throws Exception {
        mvc.perform(get("/api/cliente/sinistros/opcoes").session(session(owner))).andExpect(jsonPath("$[0].elegivel").value(true));
        mvc.perform(open(BODY,owner)).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andExpect(jsonPath("$.id").value(702)).andExpect(jsonPath("$.status").value("ANALISANDO")).andExpect(jsonPath("$.dataRegistro").value(LocalDate.now().toString())).andExpect(jsonPath("$.valorPrejuizo").value(150.50));verify(claims).save(argThat(s -> s.getCliente()==owner&&s.getApolice()==product));verify(contracts,never()).save(any());verify(payments,never()).save(any());
    }
    @Test void openingLocksOwnerThenContractsThenPaymentsBeforeDuplicateCheck() {service.abrir(10,new Abertura(1,"Tela quebrada após queda",new BigDecimal("0.01")));var order=inOrder(clients,contracts,payments,claims);order.verify(clients).findForSolicitacao(10);order.verify(contracts).findForSinistro(10,1);order.verify(payments).findForMobileOperation(101);order.verify(claims).existsByCliente_IdAndApolice_IdAndStatus(10,1,StatusSinistro.ANALISANDO);order.verify(claims).save(any());}
    @Test void unpaidAndUnissuedContractsCannotOpen() throws Exception {
        for(var state:StatusContratacao.values())if(state!=StatusContratacao.APOLICE_EMITIDA){contract.setStatus(state);mvc.perform(open(BODY,owner)).andExpect(status().isConflict());}
        contract.setStatus(StatusContratacao.APOLICE_EMITIDA);for(var state:List.of(StatusPagamento.PENDENTE,StatusPagamento.REPROVADA)){payment.setStatus(state);mvc.perform(open(BODY,owner)).andExpect(status().isConflict());}verify(claims,never()).save(any());
    }
    @Test void foreignProductCannotBeSelected() throws Exception {mvc.perform(open(BODY.replace("\"apoliceId\":1","\"apoliceId\":999"),owner)).andExpect(status().isNotFound());contract.setCliente(other);mvc.perform(open(BODY,owner)).andExpect(status().isNotFound());verify(claims,never()).save(any());}
    @Test void multipleIssuedContractsBlockRatherThanGuessing() throws Exception {var duplicate=new Contratacao();duplicate.setId(102);duplicate.setCliente(owner);duplicate.setApolice(product);duplicate.setStatus(StatusContratacao.APOLICE_EMITIDA);when(contracts.findForSinistro(10,1)).thenReturn(List.of(contract,duplicate));when(contracts.findByClienteId(10)).thenReturn(List.of(contract,duplicate));mvc.perform(open(BODY,owner)).andExpect(status().isConflict());mvc.perform(get("/api/cliente/sinistros/opcoes").session(session(owner))).andExpect(jsonPath("$[0].elegivel").value(false));verify(claims,never()).save(any());}
    @Test void openClaimBlocksRepeatButClosedHistoryDoesNot() throws Exception {when(claims.existsByCliente_IdAndApolice_IdAndStatus(10,1,StatusSinistro.ANALISANDO)).thenReturn(true);mvc.perform(open(BODY,owner)).andExpect(status().isConflict());mvc.perform(get("/api/cliente/sinistros/opcoes").session(session(owner))).andExpect(jsonPath("$[0].elegivel").value(false));verify(claims,never()).save(any());}
    @Test void malformedAmountsDescriptionsAndInjectedStateAreRejected() throws Exception {
        for(String amount:List.of("0","-1","0.001","100000000","null","\"abc\""))mvc.perform(open(BODY.replace("150.50",amount),owner)).andExpect(status().isBadRequest());
        for(String body:List.of("{}",BODY.replace("Tela quebrada após queda","curta"),BODY.replace("Tela quebrada após queda"," ".repeat(20)),BODY.replace("Tela quebrada após queda","x".repeat(2001)),BODY.replace("150.50}","150.50,\"status\":\"APROVADO\"}")))mvc.perform(open(body,owner)).andExpect(status().isBadRequest());verify(claims,never()).save(any());
    }
    @Test void profilesUseFreshDatabaseFieldsWithSafeProjectionForAllRoles() throws Exception {for(Object u:List.of(owner,broker,admin)){String body=mvc.perform(get("/api/perfil").session(session(u))).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andReturn().getResponse().getContentAsString();assertFalse(body.contains("PRIVATE_"));assertFalse(body.contains("senha"));assertFalse(body.contains("cpf"));}mvc.perform(get("/api/perfil").session(session(admin))).andExpect(jsonPath("$.editaTelefone").value(false)).andExpect(jsonPath("$.telefone").isEmpty());}
    @Test void savingOwnProfilePreservesCredentialsAuthorityNonceAndRefreshesSessionName() throws Exception {
        var s=session(owner);String token=SessionSecurity.token(s);mvc.perform(put("/api/perfil").session(s).header("X-Session-Token",token).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"  Cliente Atualizado  \",\"telefone\":\"(11) 98888-7777\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Cliente Atualizado")).andExpect(jsonPath("$.telefone").value("11988887777"));
        mvc.perform(get("/auten/sessao").session(s)).andExpect(jsonPath("$.usuario.nome").value("Cliente Atualizado")).andExpect(jsonPath("$.csrfToken").value(token));assertEquals(10,owner.getId());assertEquals("PRIVATE_HASH",owner.getSenha());assertEquals("PRIVATE_CPF",owner.getCpf());assertEquals("cliente@example.test",owner.getEmail());verify(clients).save(same(owner));
    }
    @Test void brokerAndAdminHaveOnlyApprovedFields() throws Exception {mvc.perform(profile("{\"nome\":\"Corretor Atualizado\",\"telefone\":\"11977776666\"}",broker)).andExpect(status().isOk());mvc.perform(profile("{\"nome\":\"Admin Atualizado\"}",admin)).andExpect(status().isOk());mvc.perform(profile("{\"nome\":\"Admin Atualizado\",\"telefone\":\"11977776666\"}",admin)).andExpect(status().isBadRequest());assertEquals("PRIVATE_PASSWORD",admin.getSenha());verify(admins,times(1)).save(any());}
    @Test void forbiddenProfileFieldsAndInvalidValuesNeverSave() throws Exception {
        for(String field:List.of("id","email","senha","cpf","perfil","corretores"))mvc.perform(profile("{\"nome\":\"Teste Novo\",\"telefone\":\"11999999999\",\""+field+"\":\"injetado\"}",owner)).andExpect(status().isBadRequest());
        for(String body:List.of("{}","[]","{\"nome\":123}","{\"nome\":\"Ab\",\"telefone\":\"11999999999\"}","{\"nome\":\"Teste Novo\",\"telefone\":\"abc\"}","{\"nome\":\"Teste Novo\"}","{\"nome\":\""+"A".repeat(81)+"\",\"telefone\":\"11999999999\"}"))mvc.perform(profile(body,owner)).andExpect(status().isBadRequest());verify(clients,never()).save(any());
    }
    @Test void clientNameMustRespectExistingEntityPatternBeforePersistence() throws Exception {
        mvc.perform(profile("{\"nome\":\"测试姓名\",\"telefone\":\"11999999999\"}",owner)).andExpect(status().isBadRequest());
        verify(clients,never()).save(any());
    }
    @Test void concurrentLoginCannotReceiveAnotherProfilesPresentationUpdate() throws Exception {
        for(Object replacement:List.of(other,broker)) {
            var s=session(owner);String originalToken=SessionSecurity.token(s);
            doAnswer(call -> {s.setAttribute(SessionSecurity.USER_ATTRIBUTE,replacement);SessionSecurity.renewToken(s);return call.getArgument(0);}).when(clients).save(any());
            mvc.perform(put("/api/perfil").session(s).header("X-Session-Token",originalToken).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nome\":\"Cliente Atualizado\",\"telefone\":\"11988887777\"}")).andExpect(status().isUnauthorized());
            assertSame(replacement,s.getAttribute(SessionSecurity.USER_ATTRIBUTE));
            assertEquals("Outro Cliente",other.getNome());assertEquals("Corretor Teste",broker.getNome());
        }
    }
    @Test void infrastructureErrorsReturnControlled503WithoutLeakingOrSuccess() throws Exception {doThrow(new RuntimeException("PRIVATE_DATABASE_SECRET")).when(claims).save(any());String body=mvc.perform(open(BODY,owner)).andExpect(status().isServiceUnavailable()).andReturn().getResponse().getContentAsString();assertFalse(body.contains("PRIVATE_"));when(clients.findById(10)).thenThrow(new RuntimeException("PRIVATE_DATABASE_SECRET"));mvc.perform(get("/api/perfil").session(session(owner))).andExpect(status().isServiceUnavailable());}
}
