package br.com.vidaplena.api.Repository;

import br.com.vidaplena.api.Model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    boolean existsByCrm(String crm);
    boolean existsByCrmAndIdNot(String crm, Long id);
    List<Medico> findAllByOrderByNomeAsc();
}