package br.com.vidaplena.api.service;

import br.com.vidaplena.api.Model.Medico;
import br.com.vidaplena.api.Model.StatusMedico;
import br.com.vidaplena.api.Repository.MedicoRepository;
import br.com.vidaplena.api.dto.MedicoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final MedicoRepository repo;

    public List<Medico> listar() {
        return repo.findAllByOrderByNomeAsc();
    }

    public Medico criar(MedicoRequest r) {
        String crm = r.crm().trim();
        if (repo.existsByCrm(crm)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CRM já cadastrado");
        }
        Medico m = new Medico();
        preencher(m, r, crm);
        m.setStatus(StatusMedico.ATIVO);
        return repo.save(m);
    }

    public Medico atualizar(Long id, MedicoRequest r) {
        Medico m = buscar(id);
        String crm = r.crm().trim();
        if (repo.existsByCrmAndIdNot(crm, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CRM já cadastrado");
        }
        preencher(m, r, crm);
        return repo.save(m);
    }

    public Medico alterarStatus(Long id, StatusMedico status) {
        Medico m = buscar(id);
        m.setStatus(status);
        return repo.save(m);
    }

    private Medico buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado"));
    }

    private void preencher(Medico m, MedicoRequest r, String crm) {
        m.setNome(r.nome().trim());
        m.setCrm(crm);
        m.setEspecialidade(r.especialidade().trim());
        m.setTelefone(r.telefone());
        m.setEmail(r.email());
        m.setTipo(r.tipo());
    }
}