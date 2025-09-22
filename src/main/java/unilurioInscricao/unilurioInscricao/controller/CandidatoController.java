
package unilurioInscricao.unilurioInscricao.controller;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unilurioInscricao.unilurioInscricao.model.Candidato;
import unilurioInscricao.unilurioInscricao.model.Curso;
import unilurioInscricao.unilurioInscricao.repository.CandidatoRepository;
import unilurioInscricao.unilurioInscricao.repository.CursoRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for handling candidate-related operations such as registration,
 * login, updates, and PDF generation.
 */
@RestController
@RequestMapping("/api/candidatos")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CandidatoController {

    private static final Logger log = LoggerFactory.getLogger(CandidatoController.class);
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024; // 3MB
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final CandidatoRepository candidatoRepository;
    private final CursoRepository cursoRepository;

    /**
     * DTO for login request.
     */
    private static class LoginRequest {
        private String numeroInscricao;
        private String password;

        public String getNumeroInscricao() {
            return numeroInscricao;
        }

        public void setNumeroInscricao(String numeroInscricao) {
            this.numeroInscricao = numeroInscricao;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * DTO for login response.
     */
    private static class LoginResponse {
        private String numeroInscricao;
        private boolean isAdmin;

        public String getNumeroInscricao() {
            return numeroInscricao;
        }

        public void setNumeroInscricao(String numeroInscricao) {
            this.numeroInscricao = numeroInscricao;
        }

        public boolean getIsAdmin() {
            return isAdmin;
        }

        public void setIsAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
        }
    }

    /**
     * DTO for error response.
     */
    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * DTO for candidate creation/update response.
     */
    private static class CandidatoResponse {
        private String numeroInscricao;

        public String getNumeroInscricao() {
            return numeroInscricao;
        }

        public void setNumeroInscricao(String numeroInscricao) {
            this.numeroInscricao = numeroInscricao;
        }
    }

    /**
     * Encodes a password using BCrypt.
     *
     * @param rawPassword The plain-text password.
     * @return The hashed password.
     */
    private String encodePassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * Validates required fields for candidate creation or update.
     *
     * @param params Map of field names to their values.
     * @return Error message if validation fails, null otherwise.
     */
    private String validateRequiredFields(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                return "O campo " + entry.getKey() + " é obrigatório.";
            }
        }
        return null;
    }

    /**
     * Validates and processes course IDs.
     *
     * @param cursosIds List of course ID strings.
     * @return Set of Curso objects if valid, null if validation fails.
     */
    private Set<Curso> validateAndGetCursos(List<String> cursosIds) {
        if (cursosIds == null || cursosIds.isEmpty()) {
            return null;
        }
        try {
            Set<Long> cursosIdsSet = cursosIds.stream()
                    .filter(id -> !id.isBlank())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            if (cursosIdsSet.size() > 2) {
                return null;
            }
            Set<Curso> cursos = new HashSet<>(cursoRepository.findAllById(cursosIdsSet));
            return cursos.size() == cursosIdsSet.size() ? cursos : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Processes uploaded files and combines them into a single byte array.
     *
     * @param files List of MultipartFile objects.
     * @return Combined byte array of all files.
     * @throws IOException If file reading fails.
     */
    private byte[] processFiles(List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return new byte[0];
        }
        return files.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> {
                    try {
                        return file.getBytes();
                    } catch (IOException e) {
                        log.error("Erro ao ler arquivo: {}", file.getOriginalFilename(), e);
                        return new byte[0];
                    }
                })
                .reduce(new byte[0], (a, b) -> {
                    byte[] combined = new byte[a.length + b.length];
                    System.arraycopy(a, 0, combined, 0, a.length);
                    System.arraycopy(b, 0, combined, a.length, b.length);
                    return combined;
                });
    }

    /**
     * Handles candidate login, including admin access.
     *
     * @param loginRequest The login request containing numeroInscricao and
     *                     password.
     * @return ResponseEntity with login response or error.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Admin login
            if ("1234".equals(loginRequest.getNumeroInscricao()) && "1234".equals(loginRequest.getPassword())) {
                log.info("Admin login successful with numeroInscricao: {}", loginRequest.getNumeroInscricao());
                LoginResponse response = new LoginResponse();
                response.setNumeroInscricao(loginRequest.getNumeroInscricao());
                response.setIsAdmin(true);
                return ResponseEntity.ok(response);
            }

            // Regular candidate login
            Optional<Candidato> candidatoOpt = candidatoRepository
                    .findByNumeroInscricao(loginRequest.getNumeroInscricao());
            if (candidatoOpt.isEmpty()) {
                log.error("Candidato não encontrado com número de inscrição: {}", loginRequest.getNumeroInscricao());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Número de inscrição inválido."));
            }

            Candidato candidato = candidatoOpt.get();
            if (!BCrypt.checkpw(loginRequest.getPassword(), candidato.getPassword())) {
                log.error("Senha incorreta para o número de inscrição: {}", loginRequest.getNumeroInscricao());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Senha incorreta."));
            }

            LoginResponse response = new LoginResponse();
            response.setNumeroInscricao(candidato.getNumeroInscricao());
            response.setIsAdmin(false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao processar login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao processar login: " + e.getMessage()));
        }
    }

    /**
     * Creates a new candidate with the provided details.
     *
     * @param apelido                Candidate's surname.
     * @param nome                   Candidate's first name.
     * @param nomePai                Father's name.
     * @param nomeMae                Mother's name.
     * @param genero                 Gender.
     * @param estadoCivil            Marital status.
     * @param dataNascimento         Date of birth.
     * @param paisNascimento         Country of birth.
     * @param provinciaNascimento    Province of birth.
     * @param distritoNascimento     District of birth.
     * @param provinciaResidencia    Province of residence.
     * @param localExame             Exam location.
     * @param tipoDocumento          Document type.
     * @param numeroDocumento        Document number.
     * @param dataValidacao          Document validation date.
     * @param telemovel              Phone number.
     * @param formacaoNecessaria     Required education.
     * @param anoConclusao           Year of completion.
     * @param provinciaEscola        School province.
     * @param escolaPreUniversitaria Pre-university school.
     * @param nuitFile               NUIT document file.
     * @param outrosDocumentosFiles  Other document files.
     * @param cursosIds              List of course IDs.
     * @param senha                  Password.
     * @return ResponseEntity with candidate response or error.
     */
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<?> criarCandidato(
            @RequestParam String apelido,
            @RequestParam String nome,
            @RequestParam String nomePai,
            @RequestParam String nomeMae,
            @RequestParam String genero,
            @RequestParam String estadoCivil,
            @RequestParam String dataNascimento,
            @RequestParam String paisNascimento,
            @RequestParam String provinciaNascimento,
            @RequestParam String distritoNascimento,
            @RequestParam String provinciaResidencia,
            @RequestParam String localExame,
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento,
            @RequestParam String dataValidacao,
            @RequestParam String telemovel,
            @RequestParam String formacaoNecessaria,
            @RequestParam Integer anoConclusao,
            @RequestParam String provinciaEscola,
            @RequestParam String escolaPreUniversitaria,
            @RequestParam("nuit") MultipartFile nuitFile,
            @RequestParam(value = "outrosDocumentos", required = false) List<MultipartFile> outrosDocumentosFiles,
            @RequestParam(value = "cursosIds", required = false) List<String> cursosIds,
            @RequestParam String senha) {
        try {
            log.info("Recebendo requisição para criar candidato: {}", apelido);

            // Validate required fields
            Map<String, String> fields = new HashMap<>();
            fields.put("apelido", apelido);
            fields.put("nome", nome);
            fields.put("genero", genero);
            fields.put("estadoCivil", estadoCivil);
            fields.put("dataNascimento", dataNascimento);
            fields.put("paisNascimento", paisNascimento);
            fields.put("provinciaNascimento", provinciaNascimento);
            fields.put("distritoNascimento", distritoNascimento);
            fields.put("provinciaResidencia", provinciaResidencia);
            fields.put("localExame", localExame);
            fields.put("tipoDocumento", tipoDocumento);
            fields.put("numeroDocumento", numeroDocumento);
            fields.put("dataValidacao", dataValidacao);
            fields.put("telemovel", telemovel);
            fields.put("formacaoNecessaria", formacaoNecessaria);
            fields.put("provinciaEscola", provinciaEscola);
            fields.put("escolaPreUniversitaria", escolaPreUniversitaria);
            fields.put("senha", senha);
            String validationError = validateRequiredFields(fields);
            if (validationError != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(validationError));
            }

            // Validate password length
            if (senha.length() < MIN_PASSWORD_LENGTH) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("A senha deve ter pelo menos " + MIN_PASSWORD_LENGTH + " caracteres."));
            }

            // Validate courses
            Set<Curso> cursos = validateAndGetCursos(cursosIds);
            if (cursos == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                cursosIds == null || cursosIds.isEmpty() ? "Pelo menos um curso deve ser selecionado."
                                        : "Um ou mais IDs de cursos são inválidos."));
            }

            // Validate NUIT file
            if (nuitFile == null || nuitFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("O arquivo NUIT é obrigatório."));
            }
            if (nuitFile.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("O arquivo NUIT excede o limite de 3MB."));
            }

            // Create candidate
            Candidato candidato = new Candidato();
            candidato.setApelido(apelido);
            candidato.setNome(nome);
            candidato.setNomePai(nomePai);
            candidato.setNomeMae(nomeMae);
            candidato.setGenero(genero);
            candidato.setEstadoCivil(estadoCivil);
            try {
                candidato.setDataNascimento(LocalDate.parse(dataNascimento));
                candidato.setDataValidacao(LocalDate.parse(dataValidacao));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Formato de data inválido."));
            }
            candidato.setPaisNascimento(paisNascimento);
            candidato.setProvinciaNascimento(provinciaNascimento);
            candidato.setDistritoNascimento(distritoNascimento);
            candidato.setProvinciaResidencia(provinciaResidencia);
            candidato.setLocalExame(localExame);
            candidato.setTipoDocumento(tipoDocumento);
            candidato.setNumeroDocumento(numeroDocumento);
            candidato.setTelemovel(telemovel);
            candidato.setFormacaoNecessaria(formacaoNecessaria);
            candidato.setAnoConclusao(anoConclusao);
            candidato.setProvinciaEscola(provinciaEscola);
            candidato.setEscolaPreUniversitaria(escolaPreUniversitaria);
            candidato.setPassword(encodePassword(senha));
            candidato.setNuit(nuitFile.getBytes());
            candidato.setOutrosDocumentos(processFiles(outrosDocumentosFiles));

            // Generate numeroInscricao
            int anoAtual = LocalDate.now().getYear();
            String sequencia = String.format("%03d", candidatoRepository.count() + 1);
            String mesNascimento = String.format("%02d", candidato.getDataNascimento().getMonthValue());
            String numeroInscricao = anoAtual + sequencia + mesNascimento;
            candidato.setNumeroInscricao(numeroInscricao);

            // Associate courses
            cursos.forEach(candidato::addCurso);

            // Save candidate
            Candidato salvo = candidatoRepository.save(candidato);
            log.info("Candidato salvo com numeroInscricao: {}", salvo.getNumeroInscricao());

            // Prepare response
            CandidatoResponse response = new CandidatoResponse();
            response.setNumeroInscricao(salvo.getNumeroInscricao());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Erro ao processar arquivos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao processar arquivos: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao salvar candidato: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao salvar candidato: " + e.getMessage()));
        }
    }

    /**
     * Updates an existing candidate's details.
     *
     * @param numeroInscricao        Candidate's registration number.
     * @param apelido                Candidate's surname.
     * @param nome                   Candidate's first name.
     * @param nomePai                Father's name.
     * @param nomeMae                Mother's name.
     * @param genero                 Gender.
     * @param estadoCivil            Marital status.
     * @param dataNascimento         Date of birth.
     * @param paisNascimento         Country of birth.
     * @param provinciaNascimento    Province of birth.
     * @param distritoNascimento     District of birth.
     * @param provinciaResidencia    Province of residence.
     * @param localExame             Exam location.
     * @param tipoDocumento          Document type.
     * @param numeroDocumento        Document number.
     * @param dataValidacao          Document validation date.
     * @param telemovel              Phone number.
     * @param formacaoNecessaria     Required education.
     * @param anoConclusao           Year of completion.
     * @param provinciaEscola        School province.
     * @param escolaPreUniversitaria Pre-university school.
     * @param nuitFile               NUIT document file.
     * @param outrosDocumentosFiles  Other document files.
     * @param cursosIds              List of course IDs.
     * @param senhaAtual             Current password.
     * @param novaSenha              New password.
     * @return ResponseEntity with candidate response or error.
     */
    @PutMapping(value = "/{numeroInscricao}", consumes = { "multipart/form-data" })
    public ResponseEntity<?> atualizarCandidato(
            @PathVariable String numeroInscricao,
            @RequestParam String apelido,
            @RequestParam String nome,
            @RequestParam String nomePai,
            @RequestParam String nomeMae,
            @RequestParam String genero,
            @RequestParam String estadoCivil,
            @RequestParam String dataNascimento,
            @RequestParam String paisNascimento,
            @RequestParam String provinciaNascimento,
            @RequestParam String distritoNascimento,
            @RequestParam String provinciaResidencia,
            @RequestParam String localExame,
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento,
            @RequestParam String dataValidacao,
            @RequestParam String telemovel,
            @RequestParam String formacaoNecessaria,
            @RequestParam Integer anoConclusao,
            @RequestParam String provinciaEscola,
            @RequestParam String escolaPreUniversitaria,
            @RequestParam(value = "nuit", required = false) MultipartFile nuitFile,
            @RequestParam(value = "outrosDocumentos", required = false) List<MultipartFile> outrosDocumentosFiles,
            @RequestParam(value = "cursosIds", required = false) List<String> cursosIds,
            @RequestParam(value = "senhaAtual", required = false) String senhaAtual,
            @RequestParam(value = "novaSenha", required = false) String novaSenha) {
        try {
            log.info("Recebendo requisição para atualizar candidato: {}", numeroInscricao);

            // Verify candidate exists
            Optional<Candidato> candidatoOpt = candidatoRepository.findByNumeroInscricao(numeroInscricao);
            if (candidatoOpt.isEmpty()) {
                log.error("Candidato não encontrado com número de inscrição: {}", numeroInscricao);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Candidato não encontrado com número de inscrição: " + numeroInscricao));
            }
            Candidato candidato = candidatoOpt.get();

            // Validate required fields
            Map<String, String> fields = new HashMap<>();
            fields.put("apelido", apelido);
            fields.put("nome", nome);
            fields.put("genero", genero);
            fields.put("estadoCivil", estadoCivil);
            fields.put("dataNascimento", dataNascimento);
            fields.put("paisNascimento", paisNascimento);
            fields.put("provinciaNascimento", provinciaNascimento);
            fields.put("distritoNascimento", distritoNascimento);
            fields.put("provinciaResidencia", provinciaResidencia);
            fields.put("localExame", localExame);
            fields.put("tipoDocumento", tipoDocumento);
            fields.put("numeroDocumento", numeroDocumento);
            fields.put("dataValidacao", dataValidacao);
            fields.put("telemovel", telemovel);
            fields.put("formacaoNecessaria", formacaoNecessaria);
            fields.put("provinciaEscola", provinciaEscola);
            fields.put("escolaPreUniversitaria", escolaPreUniversitaria);
            String validationError = validateRequiredFields(fields);
            if (validationError != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(validationError));
            }

            // Validate password change
            if (novaSenha != null && !novaSenha.isBlank()) {
                if (senhaAtual == null || senhaAtual.isBlank()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse("A senha atual é obrigatória para alterar a senha."));
                }
                if (!BCrypt.checkpw(senhaAtual, candidato.getPassword())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ErrorResponse("Senha atual incorreta."));
                }
                if (novaSenha.length() < MIN_PASSWORD_LENGTH) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse(
                                    "A nova senha deve ter pelo menos " + MIN_PASSWORD_LENGTH + " caracteres."));
                }
                candidato.setPassword(encodePassword(novaSenha));
                log.info("Senha atualizada para o candidato: {}", numeroInscricao);
            }

            // Validate courses
            Set<Curso> cursos = validateAndGetCursos(cursosIds);
            if (cursos == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                cursosIds == null || cursosIds.isEmpty() ? "Pelo menos um curso deve ser selecionado."
                                        : "Um ou mais IDs de cursos são inválidos."));
            }

            // Validate NUIT file
            if (nuitFile != null && !nuitFile.isEmpty() && nuitFile.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("O arquivo NUIT excede o limite de 3MB."));
            }

            // Update candidate
            candidato.setApelido(apelido);
            candidato.setNome(nome);
            candidato.setNomePai(nomePai);
            candidato.setNomeMae(nomeMae);
            candidato.setGenero(genero);
            candidato.setEstadoCivil(estadoCivil);
            try {
                candidato.setDataNascimento(LocalDate.parse(dataNascimento));
                candidato.setDataValidacao(LocalDate.parse(dataValidacao));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Formato de data inválido."));
            }
            candidato.setPaisNascimento(paisNascimento);
            candidato.setProvinciaNascimento(provinciaNascimento);
            candidato.setDistritoNascimento(distritoNascimento);
            candidato.setProvinciaResidencia(provinciaResidencia);
            candidato.setLocalExame(localExame);
            candidato.setTipoDocumento(tipoDocumento);
            candidato.setNumeroDocumento(numeroDocumento);
            candidato.setTelemovel(telemovel);
            candidato.setFormacaoNecessaria(formacaoNecessaria);
            candidato.setAnoConclusao(anoConclusao);
            candidato.setProvinciaEscola(provinciaEscola);
            candidato.setEscolaPreUniversitaria(escolaPreUniversitaria);
            if (nuitFile != null && !nuitFile.isEmpty()) {
                candidato.setNuit(nuitFile.getBytes());
            }
            candidato.setOutrosDocumentos(processFiles(outrosDocumentosFiles));
            candidato.getCursos().clear();
            cursos.forEach(candidato::addCurso);

            // Save candidate
            Candidato salvo = candidatoRepository.save(candidato);
            log.info("Candidato atualizado com numeroInscricao: {}", salvo.getNumeroInscricao());

            // Prepare response
            CandidatoResponse response = new CandidatoResponse();
            response.setNumeroInscricao(salvo.getNumeroInscricao());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Erro ao processar arquivos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao processar arquivos: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao atualizar candidato: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao atualizar candidato: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a candidate by their registration number.
     *
     * @param numeroInscricao The candidate's registration number.
     * @return ResponseEntity with candidate details or error.
     */
    @GetMapping("/{numeroInscricao}")
    public ResponseEntity<?> getCandidato(@PathVariable String numeroInscricao) {
        try {
            Optional<Candidato> candidatoOpt = candidatoRepository.findByNumeroInscricao(numeroInscricao);
            if (candidatoOpt.isEmpty()) {
                log.error("Candidato não encontrado com número de inscrição: {}", numeroInscricao);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Candidato não encontrado com número de inscrição: " + numeroInscricao));
            }

            Candidato candidato = candidatoOpt.get();
            candidato.setNuit(null);
            candidato.setOutrosDocumentos(null);
            return ResponseEntity.ok(candidato);
        } catch (Exception e) {
            log.error("Erro ao buscar candidato: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro ao buscar candidato: " + e.getMessage()));
        }
    }

    /**
     * Generates a PDF document for a candidate's registration details.
     *
     * @param numeroInscricao The candidate's registration number.
     * @return ResponseEntity with the PDF file or error.
     * @throws DocumentException If PDF generation fails.
     * @throws IOException       If file operations fail.
     */
    @GetMapping("/{numeroInscricao}/print")
    public ResponseEntity<InputStreamResource> generatePdf(@PathVariable String numeroInscricao)
            throws DocumentException, IOException {
        Optional<Candidato> candidatoOpt = candidatoRepository.findByNumeroInscricao(numeroInscricao);
        if (candidatoOpt.isEmpty()) {
            log.error("Candidato não encontrado com número de inscrição: {}", numeroInscricao);
            throw new RuntimeException("Candidato não encontrado com número de inscrição: " + numeroInscricao);
        }
        Candidato candidato = candidatoOpt.get();

        // Configure PDF document
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Add logo
        try {
            Image logo = Image.getInstance("src/main/resources/static/unilurio-logo.png");
            logo.scaleToFit(100, 100);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);
        } catch (IOException e) {
            log.warn("Logotipo não encontrado, continuando sem ele: {}", e.getMessage());
        }

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new java.awt.Color(0, 102, 204));
        Paragraph title = new Paragraph("Comprovativo de Pré-Inscrição", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Candidate information
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12, java.awt.Color.BLACK);
        Paragraph info = new Paragraph(
                "Nome do Candidato: " + (candidato.getNome() != null ? candidato.getNome() : "") + " " +
                        (candidato.getApelido() != null ? candidato.getApelido() : "") + "\n" +
                        "Número de Inscrição: "
                        + (candidato.getNumeroInscricao() != null ? candidato.getNumeroInscricao() : "") + "\n" +
                        "Local de Realização do Exame: "
                        + (candidato.getLocalExame() != null ? candidato.getLocalExame() : ""),
                infoFont);
        info.setAlignment(Element.ALIGN_LEFT);
        info.setSpacingAfter(20);
        document.add(info);

        // Courses and disciplines table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        PdfPCell cursoCell = new PdfPCell(new Phrase("Curso", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        PdfPCell disciplinasCell = new PdfPCell(
                new Phrase("Disciplinas", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cursoCell.setBackgroundColor(new java.awt.Color(211, 211, 211));
        disciplinasCell.setBackgroundColor(new java.awt.Color(211, 211, 211));
        table.addCell(cursoCell);
        table.addCell(disciplinasCell);

        List<Curso> cursos = cursoRepository.findAllById(candidato.getCursos().stream().map(Curso::getId).toList());
        if (cursos.isEmpty()) {
            log.warn("Nenhum curso encontrado para o candidato: {}", numeroInscricao);
            table.addCell("Nenhum curso selecionado");
            table.addCell("");
        } else {
            for (Curso curso : cursos) {
                table.addCell(curso.getNome() != null ? curso.getNome() : "Sem nome");
                String disciplinasStr = curso.getDisciplinas() != null ? curso.getDisciplinas().stream()
                        .map(d -> d.getNome() != null ? d.getNome() : "Sem nome")
                        .collect(Collectors.joining(", ")) : "Nenhuma disciplina";
                table.addCell(disciplinasStr);
            }
        }
        document.add(table);

        // Footer
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);
        Paragraph footer = new Paragraph(
                "Universidade de Lúrio - Data: " + new java.util.Date() + "\n" +
                        "Para mais informações, contacte: +258 21 123 456 | info@unilurio.ac.mz",
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        // Prepare response
        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=comprovativo_" + numeroInscricao + ".pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
