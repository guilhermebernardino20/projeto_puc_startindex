package br.pucpr.startindex.estrutura;

import br.pucpr.startindex.model.Chamado;

/**
 * Nó da lista encadeada: guarda um chamado e a referência para o próximo nó.
 *
 * <pre>
 *   [ valor | proximo ] --> [ valor | proximo ] --> null
 * </pre>
 *
 * É visível apenas dentro do pacote, pois é um detalhe de implementação da {@link ListaEncadeada}.
 */
class NoChamado {
    final Chamado valor;   // o dado armazenado
    NoChamado proximo;     // próximo nó da lista (null no último)

    NoChamado(Chamado valor) {
        this.valor = valor;
    }
}
