package br.pucpr.startindex.ui;

import br.pucpr.startindex.servico.NegocioException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/** Tela de cadastro de funcionário ou técnico de TI (RF-01 e RF-02). */
class CadastroView {

    private final Janela janela;

    CadastroView(Janela janela) {
        this.janela = janela;
    }

    Parent construir() {
        Label titulo = new Label("Criar conta");
        titulo.getStyleClass().add("titulo");

        TextField nome = new TextField();
        nome.setPromptText("Nome completo");
        TextField email = new TextField();
        email.setPromptText("E-mail");
        PasswordField senha = new PasswordField();
        senha.setPromptText("Senha (mín. 4 caracteres)");
        ComboBox<String> perfil = new ComboBox<>();
        perfil.getItems().addAll("Funcionário", "Técnico de TI");
        perfil.setValue("Funcionário");
        perfil.setMaxWidth(Double.MAX_VALUE);

        Button criar = new Button("Cadastrar");
        criar.setDefaultButton(true);
        criar.setMaxWidth(Double.MAX_VALUE);
        criar.setOnAction(e -> {
            try {
                if (perfil.getValue().equals("Funcionário")) {
                    janela.sistema().cadastrarFuncionario(nome.getText(), email.getText(), senha.getText());
                } else {
                    janela.sistema().cadastrarTecnico(nome.getText(), email.getText(), senha.getText());
                }
                Alertas.sucesso("Cadastro realizado! Faça login para continuar.");
                janela.mostrarLogin();
            } catch (NegocioException ex) {
                Alertas.erro(ex.getMessage());
            }
        });

        Hyperlink voltar = new Hyperlink("Voltar ao login");
        voltar.setOnAction(e -> janela.mostrarLogin());

        VBox caixa = new VBox(12, titulo, nome, email, senha, perfil, criar, voltar);
        caixa.setAlignment(Pos.CENTER);
        caixa.setPadding(new Insets(32));
        return caixa;
    }
}
