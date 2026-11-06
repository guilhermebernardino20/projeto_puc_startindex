package br.pucpr.startindex.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Classe abstrata com os dados comuns de quem usa o sistema. Funcionario e TecnicoTI
 * herdam dela (herança do diagrama de classes). A senha nunca é guardada em texto puro.
 */
public abstract class Usuario implements java.io.Serializable {

    private final int id;
    private final String nome;
    private final String email;
    private final String senhaHash;

    protected Usuario(int id, String nome, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email.trim().toLowerCase();
        this.senhaHash = hash(senha);
    }

    /** Verifica as credenciais informadas. */
    public boolean login(String email, String senha) {
        return this.email.equals(email == null ? "" : email.trim().toLowerCase())
                && senhaHash.equals(hash(senha));
    }

    /** SHA-256 da senha, em hexadecimal: só o hash fica na memória e no arquivo. */
    private static String hash(String senha) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256")
                    .digest((senha == null ? "" : senha).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public abstract String getPerfil();

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return nome;
    }
}
