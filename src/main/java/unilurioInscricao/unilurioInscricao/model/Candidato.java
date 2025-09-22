package unilurioInscricao.unilurioInscricao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Candidato {

    @Id
    @Column(name = "numeroInscricao", nullable = false, unique = true, length = 10)
    private String numeroInscricao; // Chave primária personalizada (ex.: 202500109)

    private String apelido;
    private String nome; // Campo único para nome
    private String nomePai;
    private String nomeMae;
    private String genero;
    private String estadoCivil;
    private LocalDate dataNascimento;
    private String paisNascimento;
    private String provinciaNascimento;
    private String distritoNascimento;
    private String provinciaResidencia;
    private String localExame;
    private String tipoDocumento;
    private String numeroDocumento;
    private LocalDate dataValidacao;
    private String telemovel;
    private String formacaoNecessaria;
    private Integer anoConclusao;
    private String provinciaEscola;
    private String escolaPreUniversitaria;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] nuit;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] outrosDocumentos;

    @ManyToMany
    @JoinTable(name = "candidato_curso", joinColumns = @JoinColumn(name = "numeroInscricao"), inverseJoinColumns = @JoinColumn(name = "curso_id"))
    private Set<Curso> cursos = new HashSet<>();

    private String password; // Campo para senha (primeira letra de nome + apelido)

    public void addCurso(Curso curso) {
        this.cursos.add(curso);
        curso.getCandidatos().add(this);
    }

    public String getNomeCompleto() {
        return (nome != null ? nome : "") + (apelido != null ? " " + apelido : "");
    }

    public int getIdade() {
        if (dataNascimento == null)
            return 0;
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }
}