package unilurioInscricao.unilurioInscricao.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import unilurioInscricao.unilurioInscricao.repository.CandidatoRepository;
import unilurioInscricao.unilurioInscricao.model.Candidato;
import unilurioInscricao.unilurioInscricao.model.Curso;
import unilurioInscricao.unilurioInscricao.model.Disciplina;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CandidatoRepository candidatoRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        logger.info("Acessando endpoint /api/admin/stats");
        try {
            Map<String, Long> stats = new HashMap<>();
            stats.put("totalCandidatos", candidatoRepository.count());
            stats.put("candidatasFemininas", candidatoRepository.countByGenero("Feminino"));
            stats.put("candidatosMasculinos", candidatoRepository.countByGenero("Masculino"));
            logger.info("Estatísticas retornadas: {}", stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erro ao buscar estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/stats/by-local")
    public ResponseEntity<Map<String, Long>> getCandidatesByLocal() {
        logger.info("Acessando endpoint /api/admin/stats/by-local");
        try {
            Map<String, Long> statsByLocal = new HashMap<>();
            candidatoRepository.findAll().forEach(candidato -> {
                String local = candidato.getLocalExame();
                if (local != null && !local.trim().isEmpty()) {
                    local = local.trim();
                    statsByLocal.merge(local, 1L, Long::sum);
                }
            });
            logger.info("Candidatos por local retornados: {}", statsByLocal);
            return ResponseEntity.ok(statsByLocal);
        } catch (Exception e) {
            logger.error("Erro ao buscar candidatos por local: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/candidates/by-local")
    public ResponseEntity<List<CandidateResponse>> getCandidatesByLocalExame(@RequestParam String localExame) {
        logger.info("Acessando endpoint /api/admin/candidates/by-local?localExame={}", localExame);
        try {
            List<Candidato> candidatos = candidatoRepository.findByLocalExame(localExame.trim());
            List<CandidateResponse> response = candidatos.stream().map(candidato -> {
                Set<Curso> cursos = candidato.getCursos() != null ? candidato.getCursos() : Set.of();
                List<Curso> cursosList = cursos.stream().collect(Collectors.toList());
                String disciplinas1 = cursosList.size() > 0 && cursosList.get(0).getDisciplinas() != null
                        ? cursosList.get(0).getDisciplinas().stream()
                                .map(disciplina -> disciplina != null && disciplina.getNome() != null
                                        ? disciplina.getNome()
                                        : "N/A")
                                .filter(nome -> nome != null)
                                .collect(Collectors.joining(", "))
                        : "N/A";
                String disciplinas2 = cursosList.size() > 1 && cursosList.get(1).getDisciplinas() != null
                        ? cursosList.get(1).getDisciplinas().stream()
                                .map(disciplina -> disciplina != null && disciplina.getNome() != null
                                        ? disciplina.getNome()
                                        : "N/A")
                                .filter(nome -> nome != null)
                                .collect(Collectors.joining(", "))
                        : "N/A";
                return new CandidateResponse(
                        candidato.getNumeroInscricao() != null ? candidato.getNumeroInscricao() : "N/A",
                        candidato.getNomeCompleto() != null ? candidato.getNomeCompleto() : "N/A",
                        candidato.getGenero() != null ? candidato.getGenero() : "N/A",
                        candidato.getIdade(),
                        candidato.getTelemovel() != null ? candidato.getTelemovel() : "N/A",
                        candidato.getLocalExame() != null ? candidato.getLocalExame() : "N/A",
                        cursosList.size() > 0 && cursosList.get(0).getNome() != null ? cursosList.get(0).getNome()
                                : "N/A",
                        disciplinas1,
                        cursosList.size() > 1 && cursosList.get(1).getNome() != null ? cursosList.get(1).getNome()
                                : "N/A",
                        disciplinas2,
                        candidato.getProvinciaResidencia() != null ? candidato.getProvinciaResidencia() : "N/A",
                        candidato.getDistritoNascimento() != null ? candidato.getDistritoNascimento() : "N/A");
            }).collect(Collectors.toList());
            logger.info("Candidatos retornados para local {}: {}", localExame, response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao buscar candidatos por local: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/candidates")
    public ResponseEntity<List<CandidateResponse>> getAllCandidates() {
        logger.info("Acessando endpoint /api/admin/candidates");
        try {
            List<Candidato> candidatos = candidatoRepository.findAll();
            List<CandidateResponse> response = candidatos.stream().map(candidato -> {
                Set<Curso> cursos = candidato.getCursos() != null ? candidato.getCursos() : Set.of();
                List<Curso> cursosList = cursos.stream().collect(Collectors.toList());
                String disciplinas1 = cursosList.size() > 0 && cursosList.get(0).getDisciplinas() != null
                        ? cursosList.get(0).getDisciplinas().stream()
                                .map(disciplina -> disciplina != null && disciplina.getNome() != null
                                        ? disciplina.getNome()
                                        : "N/A")
                                .filter(nome -> nome != null)
                                .collect(Collectors.joining(", "))
                        : "N/A";
                String disciplinas2 = cursosList.size() > 1 && cursosList.get(1).getDisciplinas() != null
                        ? cursosList.get(1).getDisciplinas().stream()
                                .map(disciplina -> disciplina != null && disciplina.getNome() != null
                                        ? disciplina.getNome()
                                        : "N/A")
                                .filter(nome -> nome != null)
                                .collect(Collectors.joining(", "))
                        : "N/A";
                return new CandidateResponse(
                        candidato.getNumeroInscricao() != null ? candidato.getNumeroInscricao() : "N/A",
                        candidato.getNomeCompleto() != null ? candidato.getNomeCompleto() : "N/A",
                        candidato.getGenero() != null ? candidato.getGenero() : "N/A",
                        candidato.getIdade(),
                        candidato.getTelemovel() != null ? candidato.getTelemovel() : "N/A",
                        candidato.getLocalExame() != null ? candidato.getLocalExame() : "N/A",
                        cursosList.size() > 0 && cursosList.get(0).getNome() != null ? cursosList.get(0).getNome()
                                : "N/A",
                        disciplinas1,
                        cursosList.size() > 1 && cursosList.get(1).getNome() != null ? cursosList.get(1).getNome()
                                : "N/A",
                        disciplinas2,
                        candidato.getProvinciaResidencia() != null ? candidato.getProvinciaResidencia() : "N/A",
                        candidato.getDistritoNascimento() != null ? candidato.getDistritoNascimento() : "N/A");
            }).collect(Collectors.toList());
            logger.info("Total de candidatos retornados: {}", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao buscar todos os candidatos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/juries/by-discipline-and-local")
    public ResponseEntity<Map<String, Map<String, List<JuryResponse>>>> getJuriesByDisciplineAndLocal() {
        logger.info("Acessando endpoint /api/admin/juries/by-discipline-and-local");
        try {
            List<Candidato> candidatos = candidatoRepository.findAll();
            Map<String, Map<String, Map<String, List<String>>>> candidateDisciplinesByLocal = new HashMap<>();
            Map<String, Map<String, Integer>> seatCountsByProvince = new HashMap<>();

            // Group candidates by local and collect their disciplines
            for (Candidato candidato : candidatos) {
                String localExame = candidato.getLocalExame() != null ? candidato.getLocalExame().trim() : "N/A";
                String province = candidato.getProvinciaResidencia() != null ? candidato.getProvinciaResidencia().trim()
                        : "N/A";
                String numeroInscricao = candidato.getNumeroInscricao() != null ? candidato.getNumeroInscricao()
                        : "N/A";

                Set<Curso> cursos = candidato.getCursos() != null ? candidato.getCursos() : Set.of();
                List<Curso> cursosList = cursos.stream().collect(Collectors.toList());

                // Collect all disciplines from both courses
                List<String> disciplinas = new ArrayList<>();
                if (cursosList.size() > 0 && cursosList.get(0).getDisciplinas() != null) {
                    disciplinas.addAll(cursosList.get(0).getDisciplinas().stream()
                            .map(disciplina -> disciplina != null && disciplina.getNome() != null ? disciplina.getNome()
                                    : "N/A")
                            .filter(nome -> !nome.equals("N/A"))
                            .collect(Collectors.toList()));
                }
                if (cursosList.size() > 1 && cursosList.get(1).getDisciplinas() != null) {
                    disciplinas.addAll(cursosList.get(1).getDisciplinas().stream()
                            .map(disciplina -> disciplina != null && disciplina.getNome() != null ? disciplina.getNome()
                                    : "N/A")
                            .filter(nome -> !nome.equals("N/A"))
                            .collect(Collectors.toList()));
                }

                // Store candidate with all their disciplines
                for (String disciplina : disciplinas) {
                    candidateDisciplinesByLocal.computeIfAbsent(disciplina, k -> new HashMap<>())
                            .computeIfAbsent(localExame, k -> new HashMap<>())
                            .computeIfAbsent(numeroInscricao, k -> new ArrayList<>())
                            .add(disciplina);

                    // Track seat counts for province
                    seatCountsByProvince.computeIfAbsent(province, k -> new HashMap<>())
                            .merge(disciplina + "_" + localExame, 1, Integer::sum);
                }
            }

            Map<String, Map<String, List<JuryResponse>>> juriesByDisciplineAndLocal = new HashMap<>();
            Map<Integer, List<String>> seatsToProvinces = new HashMap<>();

            for (String disciplina : candidateDisciplinesByLocal.keySet()) {
                Map<String, List<JuryResponse>> juriesByLocal = new HashMap<>();
                for (String localExame : candidateDisciplinesByLocal.get(disciplina).keySet()) {
                    Map<String, List<String>> candidatesInLocal = candidateDisciplinesByLocal.get(disciplina)
                            .get(localExame);
                    List<Candidato> uniqueCandidates = candidatoRepository.findByLocalExame(localExame).stream()
                            .filter(c -> candidatesInLocal.containsKey(c.getNumeroInscricao()))
                            .collect(Collectors.toList());

                    List<JuryResponse> juries = new ArrayList<>();
                    int jurySize = 30;
                    int totalCandidates = uniqueCandidates.size();
                    int numJuries = (int) Math.ceil((double) totalCandidates / jurySize);

                    // Track provinces with same seat counts
                    String provinceKey = uniqueCandidates.isEmpty() ? "N/A"
                            : uniqueCandidates.get(0).getProvinciaResidencia() != null
                                    ? uniqueCandidates.get(0).getProvinciaResidencia().trim()
                                    : "N/A";
                    seatsToProvinces.computeIfAbsent(numJuries, k -> new ArrayList<>())
                            .add(provinceKey + " (" + disciplina + ", " + localExame + ")");

                    for (int i = 0; i < numJuries; i++) {
                        int start = i * jurySize;
                        int end = Math.min(start + jurySize, totalCandidates);
                        List<Candidato> juryCandidates = uniqueCandidates.subList(start, end);
                        List<JuryCandidateResponse> juryCandidateResponses = juryCandidates.stream().map(candidato -> {
                            String numeroInscricao = candidato.getNumeroInscricao() != null
                                    ? candidato.getNumeroInscricao()
                                    : "N/A";
                            String disciplinasStr = candidatesInLocal.getOrDefault(numeroInscricao, List.of()).stream()
                                    .collect(Collectors.joining(", "));
                            return new JuryCandidateResponse(
                                    numeroInscricao,
                                    candidato.getNomeCompleto() != null ? candidato.getNomeCompleto() : "N/A",
                                    candidato.getGenero() != null ? candidato.getGenero() : "N/A",
                                    candidato.getIdade(),
                                    candidato.getTelemovel() != null ? candidato.getTelemovel() : "N/A",
                                    candidato.getLocalExame() != null ? candidato.getLocalExame() : "N/A",
                                    candidato.getProvinciaResidencia() != null ? candidato.getProvinciaResidencia()
                                            : "N/A",
                                    disciplinasStr);
                        }).collect(Collectors.toList());
                        juries.add(new JuryResponse("Juri " + (i + 1), juryCandidateResponses, numJuries));
                    }
                    juriesByLocal.put(localExame, juries);
                }
                juriesByDisciplineAndLocal.put(disciplina, juriesByLocal);
            }

            // Add notes about provinces with equal seats
            for (Integer seatCount : seatsToProvinces.keySet()) {
                List<String> provinces = seatsToProvinces.get(seatCount);
                if (provinces.size() > 1) {
                    String note = "Nota: As combinações " + String.join(", ", provinces)
                            + " têm o mesmo número de cadeiras (" + seatCount + ")";
                    juriesByDisciplineAndLocal.computeIfAbsent("Notas", k -> new HashMap<>())
                            .put("Nota_" + seatCount, List.of(new JuryResponse(note, List.of(), seatCount)));
                }
            }

            logger.info("Juris por disciplina e local retornados: {}", juriesByDisciplineAndLocal.keySet());
            return ResponseEntity.ok(juriesByDisciplineAndLocal);
        } catch (Exception e) {
            logger.error("Erro ao buscar juris por disciplina e local: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Classe interna para formatar a resposta completa (com cursos)
    public static class CandidateResponse {
        private String numeroCandidato;
        private String nomeCompleto;
        private String genero;
        private int idade;
        private String telemovel;
        private String localExame;
        private String curso1;
        private String disciplinas1;
        private String curso2;
        private String disciplinas2;
        private String provincia;
        private String distrito;

        public CandidateResponse(String numeroCandidato, String nomeCompleto, String genero, int idade,
                String telemovel, String localExame, String curso1, String disciplinas1, String curso2,
                String disciplinas2, String provincia, String distrito) {
            this.numeroCandidato = numeroCandidato;
            this.nomeCompleto = nomeCompleto;
            this.genero = genero;
            this.idade = idade;
            this.telemovel = telemovel;
            this.localExame = localExame;
            this.curso1 = curso1;
            this.disciplinas1 = disciplinas1;
            this.curso2 = curso2;
            this.disciplinas2 = disciplinas2;
            this.provincia = provincia;
            this.distrito = distrito;
        }

        // Getters
        public String getNumeroCandidato() {
            return numeroCandidato;
        }

        public String getNomeCompleto() {
            return nomeCompleto;
        }

        public String getGenero() {
            return genero;
        }

        public int getIdade() {
            return idade;
        }

        public String getTelemovel() {
            return telemovel;
        }

        public String getLocalExame() {
            return localExame;
        }

        public String getCurso1() {
            return curso1;
        }

        public String getDisciplinas1() {
            return disciplinas1;
        }

        public String getCurso2() {
            return curso2;
        }

        public String getDisciplinas2() {
            return disciplinas2;
        }

        public String getProvincia() {
            return provincia;
        }

        public String getDistrito() {
            return distrito;
        }
    }

    // Classe interna simplificada para juris (sem cursos e distrito, com
    // disciplinas combinadas)
    public static class JuryCandidateResponse {
        private String numeroCandidato;
        private String nomeCompleto;
        private String genero;
        private int idade;
        private String telemovel;
        private String localExame;
        private String provincia;
        private String disciplinas;

        public JuryCandidateResponse(String numeroCandidato, String nomeCompleto, String genero, int idade,
                String telemovel, String localExame, String provincia, String disciplinas) {
            this.numeroCandidato = numeroCandidato;
            this.nomeCompleto = nomeCompleto;
            this.genero = genero;
            this.idade = idade;
            this.telemovel = telemovel;
            this.localExame = localExame;
            this.provincia = provincia;
            this.disciplinas = disciplinas;
        }

        // Getters
        public String getNumeroCandidato() {
            return numeroCandidato;
        }

        public String getNomeCompleto() {
            return nomeCompleto;
        }

        public String getGenero() {
            return genero;
        }

        public int getIdade() {
            return idade;
        }

        public String getTelemovel() {
            return telemovel;
        }

        public String getLocalExame() {
            return localExame;
        }

        public String getProvincia() {
            return provincia;
        }

        public String getDisciplinas() {
            return disciplinas;
        }
    }

    public static class JuryResponse {
        private String juryName;
        private List<JuryCandidateResponse> candidates;
        private int totalSeats;

        public JuryResponse(String juryName, List<JuryCandidateResponse> candidates, int totalSeats) {
            this.juryName = juryName;
            this.candidates = candidates;
            this.totalSeats = totalSeats;
        }

        public String getJuryName() {
            return juryName;
        }

        public List<JuryCandidateResponse> getCandidates() {
            return candidates;
        }

        public int getTotalSeats() {
            return totalSeats;
        }
    }
}