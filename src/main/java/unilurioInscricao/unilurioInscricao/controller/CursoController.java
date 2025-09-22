package unilurioInscricao.unilurioInscricao.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unilurioInscricao.unilurioInscricao.model.Curso;
import unilurioInscricao.unilurioInscricao.repository.CursoRepository;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CursoController {

    private final CursoRepository cursoRepository;

    // Listar todas as formações necessárias únicas
    @GetMapping("/formacoes-necessarias")
    public ResponseEntity<List<String>> listarFormacoesNecessarias() {
        List<String> formacoes = cursoRepository.findDistinctFormacaoNecessaria();
        return ResponseEntity.ok(formacoes);
    }

    // Listar cursos por formação necessária
    @GetMapping("/cursos/por-formacao")
    public ResponseEntity<List<Curso>> listarCursosPorFormacao(@RequestParam String formacaoNecessaria) {
        List<Curso> cursos = cursoRepository.findByFormacaoNecessaria(formacaoNecessaria);
        return ResponseEntity.ok(cursos);
    }

    // Listar todos os cursos (mantido para compatibilidade)
    @GetMapping("/cursos")
    public ResponseEntity<List<Curso>> listarCursos() {
        return ResponseEntity.ok(cursoRepository.findAll());
    }

    // Obter curso por ID
    @GetMapping("/cursos/{id}")
    public ResponseEntity<Curso> getCursoById(@PathVariable Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado com ID: " + id));
        return ResponseEntity.ok(curso);
    }

    // Criar novo curso
    @PostMapping("/cursos")
    public ResponseEntity<Curso> createCurso(@RequestBody Curso curso) {
        if (curso.getNome() == null || curso.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do curso é obrigatório.");
        }
        return ResponseEntity.ok(cursoRepository.save(curso));
    }

    // Atualizar curso existente
    @PutMapping("/cursos/{id}")
    public ResponseEntity<Curso> updateCurso(@PathVariable Long id, @RequestBody Curso cursoDetails) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado com ID: " + id));
        if (cursoDetails.getNome() == null || cursoDetails.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do curso é obrigatório.");
        }
        curso.setNome(cursoDetails.getNome());
        if (cursoDetails.getFormacaoNecessaria() != null) {
            curso.setFormacaoNecessaria(cursoDetails.getFormacaoNecessaria());
        }
        return ResponseEntity.ok(cursoRepository.save(curso));
    }

    // Excluir curso
    @DeleteMapping("/cursos/{id}")
    public ResponseEntity<Void> deleteCurso(@PathVariable Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado com ID: " + id));
        if (!curso.getCandidatos().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir o curso, pois está associado a candidatos.");
        }
        cursoRepository.delete(curso);
        return ResponseEntity.noContent().build();
    }
}