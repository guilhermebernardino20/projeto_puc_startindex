package br.pucpr.startindex.estrutura;

import br.pucpr.startindex.model.Chamado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ESTRUTURA 3 — PILHA (LIFO) (RF-09 e RF-10).
 *
 * Papel no sistema: é o "Histórico" dos chamados finalizados. O último chamado
 * finalizado fica no topo e é o primeiro a poder ser reaberto — útil quando o
 * problema volta a acontecer logo depois de "resolvido".
 *
 * <pre>
 *   push(C)  -->  | C |  topo   pop() devolve C primeiro (Last In, First Out)
 *                 | B |
 *                 | A |  base
 * </pre>
 *
 * Complexidades: push, pop e consultarTopo são todos O(1) (operam no fim do vetor).
 */
public class Historico {

    // O fim do vetor é o topo da pilha.
    private final List<Chamado> chamadosFinalizados = new ArrayList<>();

    /** Empilha um chamado recém-finalizado. */
    public void push(Chamado chamado) {
        chamadosFinalizados.add(chamado);
    }

    /** Desempilha e retorna o topo, ou {@code null} se a pilha estiver vazia. */
    public Chamado pop() {
        return chamadosFinalizados.isEmpty()
                ? null
                : chamadosFinalizados.remove(chamadosFinalizados.size() - 1);
    }

    /** Espia o topo sem remover. */
    public Chamado consultarTopo() {
        return chamadosFinalizados.isEmpty()
                ? null
                : chamadosFinalizados.get(chamadosFinalizados.size() - 1);
    }

    public int tamanho() {
        return chamadosFinalizados.size();
    }

    public boolean vazio() {
        return chamadosFinalizados.isEmpty();
    }

    /** Cópia da base para o topo (ordem de finalização). Usada para salvar em arquivo. */
    public List<Chamado> baseParaTopo() {
        return new ArrayList<>(chamadosFinalizados);
    }

    /** Cópia com o topo (último finalizado) na primeira posição. Usada para exibir na tela. */
    public List<Chamado> topoPrimeiro() {
        List<Chamado> copia = new ArrayList<>(chamadosFinalizados);
        Collections.reverse(copia);
        return copia;
    }
}
