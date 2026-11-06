package br.pucpr.startindex.estrutura;

import br.pucpr.startindex.model.Chamado;

import java.util.ArrayList;
import java.util.List;

/**
 * ESTRUTURA 1 — LISTA ENCADEADA SIMPLES (RF-06).
 *
 * Papel no sistema: é o "cadastro central" que guarda TODOS os chamados registrados,
 * em qualquer estado (aberto, em atendimento, finalizado ou rejeitado).
 *
 * Por que lista encadeada? O número de chamados não é conhecido de antemão e cresce
 * durante a execução; a lista aloca um nó por chamado e não precisa de um tamanho fixo
 * nem de reorganizar um vetor ao inserir ou remover.
 *
 * Complexidades (n = número de chamados):
 *   inserir  O(1)  — guardamos também o ponteiro para o último nó
 *   buscar   O(n)  — percorre a lista a partir do primeiro nó
 *   remover  O(n)  — precisa achar o nó e o seu anterior
 *
 * Implementada "à mão" (sem java.util.LinkedList), pois é o objetivo da disciplina.
 */
public class ListaEncadeada {

    private NoChamado primeiro;  // cabeça da lista
    private NoChamado ultimo;    // cauda: permite inserir ao final em O(1)
    private int tamanho;

    /** Insere ao final da lista. */
    public void inserir(Chamado chamado) {
        NoChamado no = new NoChamado(chamado);
        if (primeiro == null) {
            primeiro = no;          // lista vazia: o novo nó é também o primeiro
        } else {
            ultimo.proximo = no;    // encadeia depois do último atual
        }
        ultimo = no;
        tamanho++;
    }

    /**
     * Remove o chamado com o id informado.
     * Percorre mantendo o nó "anterior" para poder religar a lista após a remoção.
     *
     * @return true se encontrou e removeu
     */
    public boolean remover(int id) {
        NoChamado anterior = null;
        for (NoChamado atual = primeiro; atual != null; anterior = atual, atual = atual.proximo) {
            if (atual.valor.getId() == id) {
                if (anterior == null) {
                    primeiro = atual.proximo;          // removeu a cabeça
                } else {
                    anterior.proximo = atual.proximo;  // "pula" o nó removido
                }
                if (atual == ultimo) {
                    ultimo = anterior;                 // removeu a cauda: atualiza o ponteiro
                }
                tamanho--;
                return true;
            }
        }
        return false;
    }

    /** @return o chamado com o id informado, ou {@code null} se não existir. */
    public Chamado buscar(int id) {
        for (NoChamado atual = primeiro; atual != null; atual = atual.proximo) {
            if (atual.valor.getId() == id) {
                return atual.valor;
            }
        }
        return null;
    }

    public int tamanho() {
        return tamanho;
    }

    /** Cópia dos elementos na ordem de inserção (usada pela interface e pelos relatórios). */
    public List<Chamado> paraLista() {
        List<Chamado> resultado = new ArrayList<>(tamanho);
        for (NoChamado atual = primeiro; atual != null; atual = atual.proximo) {
            resultado.add(atual.valor);
        }
        return resultado;
    }
}
