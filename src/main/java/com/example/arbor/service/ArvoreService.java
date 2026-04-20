package com.example.arbor.service;

import com.example.arbor.model.Arvore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArvoreService {
    public List<Arvore> listarArvores() {
        List<Arvore> lista = new ArrayList<>();

        Arvore a1 = new Arvore();
        a1.setNome("Ipê");

        Arvore a2 = new Arvore();
        a2.setNome("Mangueira");

        lista.add(a1);
        lista.add(a2);

        return lista;
    }
}
