package br.pucpr.startindex.servico;

/** Violação de regra de negócio, exibida ao usuário (RNF-05). */
public class NegocioException extends RuntimeException {
    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
