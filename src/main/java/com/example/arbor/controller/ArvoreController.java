package com.example.arbor.controller;

import com.example.arbor.model.Arvore;
import com.example.arbor.service.ArvoreService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping ("/api/arvores")
public class ArvoreController {
    private final ArvoreService service;

    public ArvoreController(ArvoreService service) {
        this.service = service;
    }

    @GetMapping
    public List<Arvore> listar() {
        return service.listarArvores();
    }
}
