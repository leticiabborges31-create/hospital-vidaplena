package br.com.vidaplena.api.controller;

import br.com.vidaplena.api.dto.PacienteRequest;
import br.com.vidaplena.api.dto.PacienteResponse;
import br.com.vidaplena.api.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Rotas públicas do paciente (sem login). Cada uma confere código + CPF. */
@RestController
@RequestMapping("/api/paciente")
@RequiredArgsConstructor
public class PacienteController {

    private final AgendamentoService service;

    @PostMapping("/checkin")
    public PacienteResponse checkin(@Valid @RequestBody PacienteRequest req) {
        return PacienteResponse.de(service.checkinPaciente(req.codigo(), req.cpf()));
    }

    @PostMapping("/cancelar")
    public PacienteResponse cancelar(@Valid @RequestBody PacienteRequest req) {
        return PacienteResponse.de(service.cancelarPaciente(req.codigo(), req.cpf()));
    }
}
