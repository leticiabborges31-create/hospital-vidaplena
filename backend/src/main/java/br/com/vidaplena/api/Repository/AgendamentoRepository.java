package br.com.vidaplena.api.Repository;

import br.com.vidaplena.api.Model.Agendamento;
import br.com.vidaplena.api.Model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    Optional<Agendamento> findByCodigo(String codigo);
    boolean existsByMedicoAndDataHoraAndStatus(String medico, LocalDateTime dataHora, StatusAgendamento status);
    List<Agendamento> findByMedicoAndDataHoraBetweenAndStatus(
            String medico, LocalDateTime inicio, LocalDateTime fim, StatusAgendamento status);

    List<Agendamento> findAllByOrderByDataHoraAsc();
}
