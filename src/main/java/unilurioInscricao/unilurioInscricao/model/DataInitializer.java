package unilurioInscricao.unilurioInscricao.model;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import unilurioInscricao.unilurioInscricao.repository.CursoRepository;

@Component
public class DataInitializer {

    @Autowired
    private CursoRepository cursoRepository;

    @PostConstruct
    public void init() {
        if (cursoRepository.count() == 0) {
            Curso[] cursos = {
                    // Licenciaturas em Ciências da Educação
                    createCurso("Licenciatura em Medicina",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Medicina Dentária",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Farmácia",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Nutrição",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio ou Ramo Equivalente"),

                    createCurso("Licenciatura em Enfermagem",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Optometria",
                            new String[] { "Física II (50%)", "Biologia III (50%)" },
                            "12ª Classe do SNE com Física e Biologia no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Arquitectura e Planeamento Físico",
                            new String[] { "Desenho (70%)", "Matemática III (30%)" },
                            "12ª Classe do SNE com Desenho e Matemática no Certificado do Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Urbanismo e Ordenamento do Território",
                            new String[] { "Geografia II (60%)", "Matemática II (40%)" },
                            "12ª Classe do SNE com Matemática e Geografia no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    // Licenciaturas em Engenharia
                    createCurso("Licenciatura em Engenharia Informática",
                            new String[] { "Matemática I (50%)", "Física I (50%)" },
                            "12ª Classe do SNE com Matemática e Física no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Engenharia Civil",
                            new String[] { "Matemática I (50%)", "Física I (50%)" },
                            "12ª Classe do SNE com Matemática, Física e Desenho no Certificado ou Curso de Ensino Técnico Médio Equivalente"),

                    createCurso("Licenciatura em Engenharia Mecânica",
                            new String[] { "Matemática I (50%)", "Física I (50%)" },
                            "12ª Classe do SNE com Matemática, Física e Desenho no Certificado ou Curso de Ensino Técnico Médio Equivalente"),

                    createCurso("Licenciatura em Engenharia Geológica",
                            new String[] { "Química II (50%)", "Matemática III (50%)" },
                            "12ª Classe do SNE com Matemática e Química no Certificado OU Curso de Ensino Técnico Médio Equivalente"),

                    createCurso("Licenciatura em Ciências Biológicas",
                            new String[] { "Biologia I (60%)", "Química I (40%)" },
                            "12ª Classe do SNE com  Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Gestão de Recursos Naturais",
                            new String[] { "Biologia I (60%)", "Química I (40%)" },
                            "12ª Classe do SNE com  Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Engenharia Florestal",
                            new String[] { "Biologia I (60%)", "Química I (40%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Engenharia Desenvolvimentista Rural",
                            new String[] { "Biologia I (50%)", "Química I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Engenharia Zootécnica",
                            new String[] { "Biologia I (50%)", "Química I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Turismo e Hotelaria",
                            new String[] { "Geografia I (50%)", "Português III (50%)" },
                            "12ª Classe do SNE (ou Curso Técnico Médio do Ramo Equivalente)"),

                    createCurso("Licenciatura em Desenvolvimento Local e Relações Internacionais",
                            new String[] { "História I (50%)", "Português I (50%)" },
                            "12ª Classe do SNE (ou Curso Técnico Médio do Ramo Equivalente)"),
                    // Licenciaturas em Ciências da Educação
                    createCurso("Licenciatura em Medicina",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Medicina Dentária",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Farmácia",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Nutrição",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio ou Ramo Equivalente"),

                    createCurso("Licenciatura em Enfermagem",
                            new String[] { "Química I (50%)", "Biologia I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Optometria",
                            new String[] { "Física II (50%)", "Biologia III (50%)" },
                            "12ª Classe do SNE com Física e Biologia no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Arquitectura e Planeamento Físico",
                            new String[] { "Desenho (70%)", "Matemática III (30%)" },
                            "12ª Classe do SNE com Desenho e Matemática no Certificado do Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Urbanismo e Ordenamento do Território",
                            new String[] { "Geografia II (60%)", "Matemática II (40%)" },
                            "12ª Classe do SNE com Matemática e Geografia no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    // Licenciaturas em Engenharia
                    createCurso("Licenciatura em Engenharia Informática",
                            new String[] { "Matemática I (50%)", "Física I (50%)" },
                            "12ª Classe do SNE com Matemática e Física no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Engenharia Civil",
                            new String[] { "Matemática I (50%)", "Física I (50%)" },
                            "12ª Classe do SNE com Matemática, Física e Desenho no Certificado ou Curso de Ensino Técnico Médio Equivalente"),

                    createCurso("Licenciatura em Engenharia Mecânica",
                            new String[] { "Matemática I (50%)", "Física I (50%)" },
                            "12ª Classe do SNE com Matemática, Física e Desenho no Certificado ou Curso de Ensino Técnico Médio Equivalente"),

                    createCurso("Licenciatura em Engenharia Geológica",
                            new String[] { "Química II (50%)", "Matemática III (50%)" },
                            "12ª Classe do SNE com Matemática e Química no Certificado OU Curso de Ensino Técnico Médio Equivalente"),

                    createCurso("Licenciatura em Ciências Biológicas",
                            new String[] { "Biologia I (60%)", "Química I (40%)" },
                            "12ª Classe do SNE com  Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Gestão de Recursos Naturais",
                            new String[] { "Biologia I (60%)", "Química I (40%)" },
                            "12ª Classe do SNE com  Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Engenharia Florestal",
                            new String[] { "Biologia I (60%)", "Química I (40%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Engenharia Desenvolvimentista Rural",
                            new String[] { "Biologia I (50%)", "Química I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Engenharia Zootécnica",
                            new String[] { "Biologia I (50%)", "Química I (50%)" },
                            "12ª Classe do SNE com Biologia e Química no Certificado ou Curso de Ensino Técnico Médio do Ramo Equivalente"),

                    createCurso("Licenciatura em Turismo e Hotelaria",
                            new String[] { "Geografia I (50%)", "Português III (50%)" },
                            "12ª Classe do SNE (ou Curso Técnico Médio do Ramo Equivalente)"),

                    createCurso("Licenciatura em Desenvolvimento Local e Relações Internacionais",
                            new String[] { "História I (50%)", "Português I (50%)" },
                            "12ª Classe do SNE (ou Curso Técnico Médio do Ramo Equivalente)")
            };

            for (Curso curso : cursos) {
                cursoRepository.save(curso);
            }
        }
    }

    // Método para criar curso com formação necessária
    private Curso createCurso(String nome, String[] disciplinasNomes, String formacaoNecessaria) {
        Curso curso = new Curso();
        curso.setNome(nome);
        curso.setFormacaoNecessaria(formacaoNecessaria);
        List<Disciplina> disciplinas = new ArrayList<>();
        for (String disciplinaNome : disciplinasNomes) {
            Disciplina disciplina = new Disciplina();
            disciplina.setNome(disciplinaNome);
            disciplina.setCurso(curso);
            disciplinas.add(disciplina);
        }
        curso.setDisciplinas(disciplinas);
        return curso;
    }
}