package br.pucpr.startindex.ui;

import br.pucpr.startindex.model.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

final class BarraTopo {
    private BarraTopo() {}

    static Node criar(Janela janela, Usuario usuario) {
        Label marca = new Label("STARTINDEX");
        marca.getStyleClass().add("marca");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        Label quem = new Label(usuario.getNome() + " · " + usuario.getPerfil());
        Button sair = new Button("Sair");
        sair.setOnAction(e -> janela.mostrarLogin());
        HBox barra = new HBox(12, marca, espaco, quem, sair);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(10, 16, 10, 16));
        barra.getStyleClass().add("barra");
        return barra;
    }
}
