package unilurioInscricao.unilurioInscricao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import unilurioInscricao.unilurioInscricao.model.Candidato;

public interface CandidatoRepository extends JpaRepository<Candidato, Long> {

    Optional<Candidato> findByNumeroInscricao(String numeroInscricao);

    List<Candidato> findByLocalExame(String localExame);

    Long countByGenero(String genero);
}
