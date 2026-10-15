package br.com.vidaplena.api.service;

import br.com.vidaplena.api.dto.AgendamentoRequest;
import br.com.vidaplena.api.Model.Agendamento;
import br.com.vidaplena.api.Model.StatusAgendamento;
import br.com.vidaplena.api.Repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository repo;

    public Agendamento criar(AgendamentoRequest r) {
        if (!r.ehEspera()) {
            if (r.dataHora() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe data e hora");
            }
            if (r.dataHora().isBefore(java.time.LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolha um horário futuro");
            }
            if (repo.existsByMedicoAndDataHoraAndStatus(r.medico(), r.dataHora(), StatusAgendamento.AGENDADO)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário já ocupado");
            }
        }
        Agendamento a = new Agendamento();
        a.setCodigo(gerarCodigo());
        a.setNomePaciente(r.nomePaciente());
        a.setCpf(r.cpf());
        a.setTelefone(r.telefone());
        a.setConvenio(r.convenio());
        a.setEspecialidade(r.especialidade());
        a.setMedico(r.medico());
        a.setTipo(r.tipo());
        a.setDataHora(r.dataHora());
        a.setStatus(r.ehEspera() ? StatusAgendamento.ESPERA : StatusAgendamento.AGENDADO);
        return repo.save(a);
    }
    public List<Agendamento> listar() {
        return repo.findAllByOrderByDataHoraAsc();
    }

    public Agendamento buscar(String codigo) {
        return repo.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
    }

    public List<String> horariosOcupados(String medico, LocalDate data) {
        return repo.findByMedicoAndDataHoraBetweenAndStatus(
                        medico, data.atStartOfDay(), data.plusDays(1).atStartOfDay(), StatusAgendamento.AGENDADO)
                .stream()
                .map(a -> a.getDataHora().toLocalTime().toString())
                .toList();
    }

    public Agendamento cancelar(String codigo) {
        Agendamento a = buscar(codigo);
        if (a.getStatus() == StatusAgendamento.CHECKIN_FEITO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível cancelar após o check-in");
        }
        a.setStatus(StatusAgendamento.CANCELADO);
        return repo.save(a);
    }

    public Agendamento checkin(String codigo) {
        Agendamento a = buscar(codigo);
        if (a.getStatus() != StatusAgendamento.AGENDADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este agendamento não permite check-in");
        }
        if (!a.getDataHora().toLocalDate().equals(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O check-in só pode ser feito no dia da consulta");
        }
        a.setStatus(StatusAgendamento.CHECKIN_FEITO);
        return repo.save(a);
    }

    // ---- Ações do paciente: exigem o código E o CPF do agendamento ----

    public Agendamento checkinPaciente(String codigo, String cpf) {
        return checkin(conferir(codigo, cpf).getCodigo());
    }

    public Agendamento cancelarPaciente(String codigo, String cpf) {
        return cancelar(conferir(codigo, cpf).getCodigo());
    }

    /** Mesma resposta (404) para código inexistente e CPF errado, para não revelar quais códigos existem. */
    private Agendamento conferir(String codigo, String cpf) {
        Agendamento a = buscar(codigo);
        String informado = cpf == null ? "" : cpf.replaceAll("\\D", "");
        String gravado = a.getCpf() == null ? "" : a.getCpf().replaceAll("\\D", "");
        if (informado.isEmpty() || !informado.equals(gravado)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado");
        }
        return a;
    }

    private String gerarCodigo() {
        String c;
        do {
            c = "VP-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        } while (repo.findByCodigo(c).isPresent());
        return c;
    }
}