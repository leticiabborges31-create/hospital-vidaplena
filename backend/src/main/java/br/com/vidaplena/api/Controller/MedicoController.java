package br.com.vidaplena.api.controller;

import br.com.vidaplena.api.Model.Medico;
import br.com.vidaplena.api.dto.MedicoRequest;
import br.com.vidaplena.api.dto.StatusMedicoRequest;
import br.com.vidaplena.api.service.MedicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService service;

    @GetMapping
    public List<Medico> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medico criar(@Valid @RequestBody MedicoRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public Medico atualizar(@PathVariable Long id, @Valid @RequestBody MedicoRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/status")
    public Medico alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusMedicoRequest req) {
        return service.alterarStatus(id, req.status());
    }
}