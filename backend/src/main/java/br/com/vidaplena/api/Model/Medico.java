package br.com.vidaplena.api.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "MEDICO")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String crm;

    @Column(nullable = false)
    private String especialidade;

    private String telefone;

    private String email;

    // Presencial, Teleconsulta ou Ambos
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMedico status = StatusMedico.ATIVO;
}
