package unilurioInscricao.unilurioInscricao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import unilurioInscricao.unilurioInscricao.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    @Query("SELECT DISTINCT c.formacaoNecessaria FROM Curso c WHERE c.formacaoNecessaria IS NOT NULL AND c.formacaoNecessaria <> ''")
    List<String> findDistinctFormacaoNecessaria();

    // Buscar cursos por formação necessária
    List<Curso> findByFormacaoNecessaria(String formacaoNecessaria);

    // Buscar cursos por formação necessária contendo texto (para busca parcial)
    List<Curso> findByFormacaoNecessariaContainingIgnoreCase(String formacaoNecessaria);

}