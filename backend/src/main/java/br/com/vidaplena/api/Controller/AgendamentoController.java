package br.com.vidaplena.api.controller;

import br.com.vidaplena.api.dto.AgendamentoRequest;
import br.com.vidaplena.api.Model.Agendamento;
import br.com.vidaplena.api.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {


    private final AgendamentoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Agendamento criar(@Valid @RequestBody AgendamentoRequest req) {
        return service.criar(req);
    }

    @GetMapping
    public List<Agendamento> listar() {
        return service.listar();
    }

    @GetMapping("/ocupados")
    public List<String> ocupados(@RequestParam String medico, @RequestParam LocalDate data) {
        return service.horariosOcupados(medico, data);
    }

    @GetMapping("/{codigo}")
    public Agendamento buscar(@PathVariable String codigo) {
        return service.buscar(codigo);
    }

    @PatchMapping("/{codigo}/cancelar")
    public Agendamento cancelar(@PathVariable String codigo) {
        return service.cancelar(codigo);
    }

    @PostMapping("/{codigo}/checkin")
    public Agendamento checkin(@PathVariable String codigo) {
        return service.checkin(codigo);
    }
}