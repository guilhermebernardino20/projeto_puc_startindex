package br.pucpr.startindex.estrutura;

import br.pucpr.startindex.model.Chamado;

import java.util.ArrayList;
import java.util.List;

/**
 * ESTRUTURA 2 — FILA DE PRIORIDADE (RF-07).
 *
 * Papel no sistema: organiza os chamados "Em aberto". O primeiro da fila é sempre o
 * próximo a ser atendido: maior prioridade (URGENTE > INTERMEDIARIA > SIMPLES) e, em
 * caso de empate, o mais antigo (menor data de criação). Essa regra está em
 * {@link Chamado#ORDEM_ATENDIMENTO}.
 *
 * Implementação: HEAP BINÁRIO (mínimo) guardado em um vetor, sem usar java.util.PriorityQueue.
 * No vetor, o nó da posição i tem filhos em 2i+1 e 2i+2 e pai em (i-1)/2:
 *
 * <pre>
 *            [0]            invariante: cada pai "vem antes" dos filhos
 *          /     \          segundo a ordem de atendimento
 *        [1]     [2]
 *       /  \
 *     [3]  [4]
 * </pre>
 *
 * Complexidades (n = chamados em aberto):
 *   enfileirar         O(log n)  — insere no fim e "sobe" até restaurar o heap
 *   desenfileirar      O(log n)  — troca o topo pelo último e "desce"
 *   consultarProximo   O(1)      — é a raiz
 *   remover(chamado)   O(n)      — acha o índice e depois reorganiza em O(log n)
 *   emOrdem            O(n log n)— ordena uma cópia, só para exibir na tela
 */
public class FilaDePrioridade {

    private final List<Chamado> chamados = new ArrayList<>();  // vetor que representa o heap

    /** Insere no final e sobe o elemento até a posição correta. */
    public void enfileirar(Chamado chamado) {
        chamados.add(chamado);
        subir(chamados.size() - 1);
    }

    /** Remove e retorna o próximo chamado a atender, ou {@code null} se a fila estiver vazia. */
    public Chamado desenfileirar() {
        if (chamados.isEmpty()) {
            return null;
        }
        return removerIndice(0);
    }

    /** Espia o próximo chamado sem removê-lo. */
    public Chamado consultarProximo() {
        return chamados.isEmpty() ? null : chamados.get(0);
    }

    /** Remove um chamado específico (ex.: o técnico escolheu atender um item fora do topo). */
    public boolean remover(Chamado chamado) {
        int i = chamados.indexOf(chamado);
        if (i < 0) {
            return false;
        }
        removerIndice(i);
        return true;
    }

    public int tamanho() {
        return chamados.size();
    }

    public boolean vazia() {
        return chamados.isEmpty();
    }

    /**
     * Cópia dos chamados na ordem de atendimento, sem alterar a fila.
     * O vetor do heap só garante o menor na raiz (não está totalmente ordenado),
     * por isso ordenamos uma cópia para mostrar na tela.
     */
    public List<Chamado> emOrdem() {
        List<Chamado> copia = new ArrayList<>(chamados);
        copia.sort(Chamado.ORDEM_ATENDIMENTO);
        return copia;
    }

    /**
     * Remove o elemento da posição i: coloca o último elemento no lugar dele e
     * reorganiza (ele pode precisar subir ou descer).
     */
    private Chamado removerIndice(int i) {
        Chamado removido = chamados.get(i);
        int ultimo = chamados.size() - 1;
        Chamado last = chamados.remove(ultimo);
        if (i < ultimo) {
            chamados.set(i, last);
            subir(i);
            descer(i);
        }
        return removido;
    }

    /** Enquanto o elemento for "melhor" que o pai, troca com ele. */
    private void subir(int i) {
        while (i > 0) {
            int pai = (i - 1) / 2;
            if (comparar(i, pai) >= 0) {
                break;   // já está na posição certa
            }
            trocar(i, pai);
            i = pai;
        }
    }

    /** Enquanto algum filho for "melhor", troca com o melhor dos filhos. */
    private void descer(int i) {
        int n = chamados.size();
        while (true) {
            int esq = 2 * i + 1;
            int dir = esq + 1;
            int menor = i;
            if (esq < n && comparar(esq, menor) < 0) menor = esq;
            if (dir < n && comparar(dir, menor) < 0) menor = dir;
            if (menor == i) {
                return;  // heap restaurado
            }
            trocar(i, menor);
            i = menor;
        }
    }

    private int comparar(int a, int b) {
        return Chamado.ORDEM_ATENDIMENTO.compare(chamados.get(a), chamados.get(b));
    }

    private void trocar(int a, int b) {
        Chamado tmp = chamados.get(a);
        chamados.set(a, chamados.get(b));
        chamados.set(b, tmp);
    }
}
