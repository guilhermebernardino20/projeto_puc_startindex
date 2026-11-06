package br.pucpr.startindex.ui;

import br.pucpr.startindex.model.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/** Tela de login (RNF-02): nenhuma outra funcionalidade é acessível sem autenticar. */
class LoginView {

    private final Janela janela;

    LoginView(Janela janela) {
        this.janela = janela;
    }

    Parent construir() {
        Label titulo = new Label("STARTINDEX");
        titulo.getStyleClass().add("titulo");
        Label sub = new Label("Gestão de chamados de TI");

        TextField email = new TextField();
        email.setPromptText("E-mail");
        PasswordField senha = new PasswordField();
        senha.setPromptText("Senha");

        Button entrar = new Button("Entrar");
        entrar.setDefaultButton(true);
        entrar.setMaxWidth(Double.MAX_VALUE);
        entrar.setOnAction(e -> {
            Usuario u = janela.sistema().autenticar(email.getText(), senha.getText());
            if (u == null) {
                Alertas.erro("E-mail ou senha inválidos.");
            } else {
                janela.mostrarPainel(u);
            }
        });

        Hyperlink cadastro = new Hyperlink("Criar conta");
        cadastro.setOnAction(e -> janela.mostrarCadastro());

        VBox caixa = new VBox(12, titulo, sub, email, senha, entrar, cadastro);
        caixa.setAlignment(Pos.CENTER);
        caixa.setPadding(new Insets(32));
        return caixa;
    }
}
