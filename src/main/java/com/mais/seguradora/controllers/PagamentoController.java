package com.mais.seguradora.controllers;

import com.mais.seguradora.Repositories.PagamentoRepository;
import com.mais.seguradora.enummerated.StatusPagamento;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import com.mais.seguradora.services.ContratacaoService;
import com.mais.seguradora.services.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.tax.Registration;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("pagamento")
public class PagamentoController {

    @Autowired
    ContratacaoService contratacaoService;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @GetMapping("/cliente")
    public String pagamentoCliente(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        if (cliente == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("pagamentos", pagamentoRepository.findByContratacao_ClienteOrderByDataVencimentoDesc(cliente));
        return "pagamentosCliente";
    }

    @PostMapping("/checkout/{idContratacao}")
    public ResponseEntity<?> criarCheclkout(@PathVariable("idContratacao") Integer idContratacao) {
        try {
            String url = stripeService.criarCheckout(idContratacao);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (StripeException e) {
            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "erro",
                                    e.getMessage()));
        }
    }

    @GetMapping("/sucesso/{id}")
    public String sucesso(@PathVariable("id") Integer id, Model model) {

        Contratacao contratacao = contratacaoService.buscarPorId(id);

        pagamentoRepository
                .findByContratacao_IdAndStatus(
                        id,
                        StatusPagamento.PENDENTE
                )
                .ifPresent(pagamento -> {
                    pagamento.setStatus(StatusPagamento.APROVADA);
                    pagamentoRepository.save(pagamento);

                    pagamento.setDataPagamento(LocalDate.now());

                    pagamento.setIdApolice(
                            pagamento.getContratacao().getApolice().getId()
                    );

                    pagamento.setFormaPagamento("CARTÃO");

                    pagamentoRepository.save(pagamento);
                });

        Cliente cliente = contratacao.getCliente();

        model.addAttribute("cliente", cliente);

        return "pagamento-sucesso";
    }
}
