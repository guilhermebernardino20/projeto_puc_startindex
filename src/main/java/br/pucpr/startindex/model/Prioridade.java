package br.pucpr.startindex.model;

/** Níveis de prioridade. A ordem de declaração define a precedência (URGENTE vem primeiro). */
public enum Prioridade {
    URGENTE("Urgente"),
    INTERMEDIARIA("Intermediária"),
    SIMPLES("Simples");

    private final String rotulo;

    Prioridade(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
