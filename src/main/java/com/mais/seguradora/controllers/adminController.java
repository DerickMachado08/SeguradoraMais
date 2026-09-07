package com.mais.seguradora.controllers;

import com.mais.seguradora.services.SinistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class adminController {

    @Autowired
    private SinistroService sinistroService;

    @PostMapping("/aprovar/{id}")
    public String aprovar(@PathVariable("id") Integer id) {

        sinistroService.aprovarSinistro(id);

        return "redirect:/dashboardAdmin";
    }

    @PostMapping("/negar/{id}")
    public String negado(@PathVariable("id") Integer id) {

        sinistroService.negarSinistro(id);

        return "redirect:/dashboardAdmin";
    }


}
