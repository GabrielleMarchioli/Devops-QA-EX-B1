package com.example.educacaocontinuadagamificada.aluno.application;

import com.example.educacaocontinuadagamificada.aluno.api.AlunoDTO;
import com.example.educacaocontinuadagamificada.aluno.infra.AlunoEntity;
import com.example.educacaocontinuadagamificada.aluno.infra.AlunoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public AlunoDTO criar(AlunoDTO dto) {
        AlunoEntity aluno = new AlunoEntity(
                dto.getNome(),
                dto.getCursosLiberados(),
                dto.getPlano(),
                dto.getMoedas()
        );
        return converterParaDTO(alunoRepository.save(aluno));
    }

    public List<AlunoDTO> listarTodos() {
        return alunoRepository.findAll()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public AlunoDTO buscarPorId(Long id) {
        AlunoEntity aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        return converterParaDTO(aluno);
    }

    public void excluir(Long id) {
        alunoRepository.deleteById(id);
    }

    private AlunoDTO converterParaDTO(AlunoEntity aluno) {
        return new AlunoDTO(
                aluno.getId(),
                aluno.getNome(),
                aluno.getCursosLiberados(),
                aluno.getPlano(),
                aluno.getMoedas()
        );
    }
}
