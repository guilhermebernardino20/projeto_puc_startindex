package br.pucpr.startindex.model;

/** Usuário da equipe de suporte: aceita, rejeita, finaliza e reabre chamados (RF-04, RF-08, RF-10). */
public class TecnicoTI extends Usuario {

    public TecnicoTI(int id, String nome, String email, String senha) {
        super(id, nome, email, senha);
    }

    @Override
    public String getPerfil() {
        return "Técnico de TI";
    }
}
