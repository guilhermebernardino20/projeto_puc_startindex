package br.pucpr.startindex.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/** Mensagens de confirmação e erro (RNF-05). */
final class Alertas {
    private Alertas() {}

    static void sucesso(String mensagem) {
        mostrar(AlertType.INFORMATION, "Sucesso", mensagem);
    }

    static void erro(String mensagem) {
        mostrar(AlertType.ERROR, "Erro", mensagem);
    }

    private static void mostrar(AlertType tipo, String titulo, String mensagem) {
        Alert a = new Alert(tipo, mensagem);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
