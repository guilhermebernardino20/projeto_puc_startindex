package br.pucpr.startindex.model;

/** Usuário que abre chamados e acompanha apenas os seus (RF-03, RF-11, RNF-04). */
public class Funcionario extends Usuario {

    public Funcionario(int id, String nome, String email, String senha) {
        super(id, nome, email, senha);
    }

    @Override
    public String getPerfil() {
        return "Funcionário";
    }
}
