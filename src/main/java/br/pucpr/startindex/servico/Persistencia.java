package br.pucpr.startindex.servico;

import java.io.*;
import java.nio.file.*;

/** Grava e lê o {@link Estado} em arquivo, para os dados sobreviverem ao fechamento do app. */
public class Persistencia {

    private final Path arquivo;

    public Persistencia(Path arquivo) {
        this.arquivo = arquivo;
    }

    public static Persistencia padrao() {
        return new Persistencia(Path.of(System.getProperty("user.home"), ".startindex", "dados.bin"));
    }

    /** Grava em arquivo temporário e depois o move, para nunca deixar o arquivo pela metade. */
    public void salvar(Estado estado) throws IOException {
        Files.createDirectories(arquivo.getParent());
        Path tmp = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tmp))) {
            out.writeObject(estado);
        }
        Files.move(tmp, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /** @return o estado salvo, ou {@code null} se não houver arquivo legível. */
    public Estado carregar() {
        if (!Files.exists(arquivo)) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(arquivo))) {
            return (Estado) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            return null;
        }
    }
}
