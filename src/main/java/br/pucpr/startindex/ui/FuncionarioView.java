package br.pucpr.startindex.ui;

import br.pucpr.startindex.model.*;
import br.pucpr.startindex.servico.NegocioException;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Painel do funcionário: abrir chamado e acompanhar os seus (status, técnico e mensagens).
 * Só os chamados do próprio funcionário aparecem (RNF-04).
 */
class FuncionarioView {

    private final Janela janela;
    private final Funcionario funcionario;
    private final TableView<Chamado> tabela = Tabelas.chamados(false, true);
    private final TextArea detalhe = new TextArea();

    FuncionarioView(Janela janela, Funcionario funcionario) {
        this.janela = janela;
        this.funcionario = funcionario;
    }

    Parent construir() {
        TabPane abas = new TabPane(
                new Tab("Abrir chamado", formulario()),
                new Tab("Meus chamados", meusChamados()));
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        abas.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> atualizar());

        BorderPane raiz = new BorderPane(abas);
        raiz.setTop(BarraTopo.criar(janela, funcionario));
        atualizar();
        return raiz;
    }

    private Parent formulario() {
        TextField titulo = new TextField();
        titulo.setPromptText("Resumo do problema");
        TextArea descricao = new TextArea();
        descricao.setPromptText("Descreva o problema com detalhes");
        descricao.setPrefRowCount(6);
        ComboBox<Categoria> categoria = new ComboBox<>();
        categoria.getItems().addAll(Categoria.padrao());
        categoria.setPromptText("Categoria");
        ComboBox<Prioridade> prioridade = new ComboBox<>();
        prioridade.getItems().addAll(Prioridade.values());
        prioridade.setValue(Prioridade.SIMPLES);

        Button enviar = new Button("Abrir chamado");
        enviar.setOnAction(e -> {
            try {
                Chamado c = janela.sistema().abrirChamado(funcionario, titulo.getText(),
                        descricao.getText(), categoria.getValue(), prioridade.getValue());
                Alertas.sucesso("Chamado #" + c.getId() + " aberto com sucesso.");
                titulo.clear();
                descricao.clear();
                categoria.setValue(null);
                prioridade.setValue(Prioridade.SIMPLES);
                atualizar();
            } catch (NegocioException ex) {
                Alertas.erro(ex.getMessage());
            }
        });

        VBox caixa = new VBox(10,
                new Label("Título"), titulo,
                new Label("Descrição"), descricao,
                new Label("Categoria"), categoria,
                new Label("Prioridade"), prioridade,
                enviar);
        caixa.setPadding(new Insets(20));
        caixa.setMaxWidth(560);
        StackPane centro = new StackPane(caixa);
        return centro;
    }

    private Parent meusChamados() {
        Button atualizar = new Button("Atualizar");
        atualizar.setOnAction(e -> atualizar());
        Button cancelar = new Button("Cancelar chamado");
        cancelar.setOnAction(e -> {
            try {
                Chamado c = tabela.getSelectionModel().getSelectedItem();
                janela.sistema().cancelarChamado(funcionario, c);
                Alertas.sucesso("Chamado #" + c.getId() + " cancelado.");
            } catch (NegocioException ex) {
                Alertas.erro(ex.getMessage());
            }
            atualizar();
        });
        detalhe.setEditable(false);
        detalhe.setWrapText(true);
        detalhe.setPrefRowCount(7);
        detalhe.setPromptText("Selecione um chamado para ver a descrição e o acompanhamento.");
        tabela.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> mostrarDetalhe(c));
        VBox caixa = new VBox(10, new HBox(8, atualizar, cancelar), tabela, detalhe);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        caixa.setPadding(new Insets(12));
        return caixa;
    }

    private void atualizar() {
        tabela.getItems().setAll(janela.sistema().chamadosDoFuncionario(funcionario));
        mostrarDetalhe(null);
    }

    private void mostrarDetalhe(Chamado c) {
        if (c == null) {
            detalhe.clear();
            return;
        }
        detalhe.setText("Descrição:\n" + c.getDescricao() + "\n\nAcompanhamento:\n"
                + String.join("\n", c.getMensagens()));
    }
}
