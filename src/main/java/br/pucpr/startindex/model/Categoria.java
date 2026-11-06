package br.pucpr.startindex.model;

import java.util.List;

/** Categoria pré-definida de chamado (RF-05). */
public class Categoria implements java.io.Serializable {

    private static final List<Categoria> PADRAO = List.of(
            new Categoria(1, "Hardware"),
            new Categoria(2, "Software"),
            new Categoria(3, "Rede / Internet"),
            new Categoria(4, "Acesso e senhas"),
            new Categoria(5, "E-mail"),
            new Categoria(6, "Impressora"),
            new Categoria(7, "Outros"));

    private final int id;
    private final String nome;

    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public static List<Categoria> padrao() {
        return PADRAO;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Categoria c && c.id == id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return nome;
    }
}
