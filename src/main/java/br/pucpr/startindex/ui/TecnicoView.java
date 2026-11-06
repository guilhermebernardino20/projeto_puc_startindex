package br.pucpr.startindex.ui;

import br.pucpr.startindex.model.Chamado;
import br.pucpr.startindex.model.TecnicoTI;
import br.pucpr.startindex.servico.NegocioException;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Painel do técnico de TI: abas Em aberto (fila de prioridade), Meus atendimentos,
 * Histórico (pilha) e Relatórios. A tela só chama o SistemaGestaoChamados; não tem regra de negócio.
 */
class TecnicoView {

    private final Janela janela;
    private final TecnicoTI tecnico;
    private final TableView<Chamado> emAberto = Tabelas.chamados(true, false);
    private final TableView<Chamado> emAtendimento = Tabelas.chamados(true, false);
    private final TableView<Chamado> historico = Tabelas.chamados(true, true);
    private final TextArea relatorio = new TextArea();
    private final Label resumoAberto = new Label();
    private final Label resumoHistorico = new Label();

    TecnicoView(Janela janela, TecnicoTI tecnico) {
        this.janela = janela;
        this.tecnico = tecnico;
    }

    Parent construir() {
        TabPane abas = new TabPane(
                new Tab("Em aberto", abaEmAberto()),
                new Tab("Meus atendimentos", abaAtendimentos()),
                new Tab("Histórico", abaHistorico()),
                new Tab("Relatórios", abaRelatorios()));
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        abas.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> atualizar());

        BorderPane raiz = new BorderPane(abas);
        raiz.setTop(BarraTopo.criar(janela, tecnico));
        atualizar();
        return raiz;
    }

    private Parent abaEmAberto() {
        Button aceitar = new Button("Aceitar chamado");
        aceitar.setOnAction(e -> executar(() -> {
            Chamado c = emAberto.getSelectionModel().getSelectedItem();
            janela.sistema().aceitarChamado(tecnico, c);
            return "Chamado #" + c.getId() + " aceito. Ele está em \"Meus atendimentos\".";
        }));
        Button rejeitar = new Button("Rejeitar chamado");
        rejeitar.setOnAction(e -> executar(() -> {
            Chamado c = emAberto.getSelectionModel().getSelectedItem();
            janela.sistema().rejeitarChamado(tecnico, c);
            return "Chamado #" + c.getId() + " rejeitado.";
        }));
        return aba(new HBox(8, aceitar, rejeitar, resumoAberto), emAberto);
    }

    private Parent abaAtendimentos() {
        Button finalizar = new Button("Finalizar chamado");
        finalizar.setOnAction(e -> executar(() -> {
            Chamado c = emAtendimento.getSelectionModel().getSelectedItem();
            janela.sistema().finalizarChamado(tecnico, c);
            return "Chamado #" + c.getId() + " finalizado e movido para o histórico.";
        }));
        return aba(new HBox(8, finalizar), emAtendimento);
    }

    private Parent abaHistorico() {
        Button reabrir = new Button("Reabrir último finalizado");
        reabrir.setOnAction(e -> executar(() -> {
            Chamado c = janela.sistema().reabrirChamado();
            return "Chamado #" + c.getId() + " reaberto e devolvido à fila \"Em aberto\".";
        }));
        return aba(new HBox(8, reabrir, resumoHistorico), historico);
    }

    private Parent abaRelatorios() {
        relatorio.setEditable(false);
        relatorio.setStyle("-fx-font-family: 'Menlo', monospace;");
        Button atualizar = new Button("Atualizar");
        atualizar.setOnAction(e -> atualizar());
        VBox caixa = new VBox(10, atualizar, relatorio);
        VBox.setVgrow(relatorio, Priority.ALWAYS);
        caixa.setPadding(new Insets(12));
        return caixa;
    }

    private String textoRelatorio() {
        var r = janela.sistema().gerarRelatorio();
        StringBuilder sb = new StringBuilder("Total de chamados: " + r.total() + "\n\nPor status\n");
        r.porStatus().forEach((k, v) -> sb.append(String.format("  %-16s %d%n", k, v)));
        sb.append("\nPor prioridade\n");
        r.porPrioridade().forEach((k, v) -> sb.append(String.format("  %-16s %d%n", k, v)));
        sb.append("\nPor categoria\n");
        r.porCategoria().forEach((k, v) -> sb.append(String.format("  %-16s %d%n", k, v)));
        sb.append("\nTempo médio de resolução: ");
        if (r.tempoMedioResolucao() == null) {
            sb.append("sem chamados finalizados");
        } else {
            var d = r.tempoMedioResolucao();
            sb.append(String.format("%dh %02dmin", d.toHours(), d.toMinutesPart()));
        }
        return sb.toString();
    }

    private Parent aba(HBox acoes, TableView<Chamado> tabela) {
        acoes.setPadding(new Insets(0, 0, 4, 0));
        VBox caixa = new VBox(10, acoes, tabela);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        caixa.setPadding(new Insets(12));
        return caixa;
    }

    /** Executa a ação, mostra confirmação ou erro (RNF-05) e atualiza as tabelas. */
    private void executar(java.util.function.Supplier<String> acao) {
        try {
            Alertas.sucesso(acao.get());
        } catch (NegocioException ex) {
            Alertas.erro(ex.getMessage());
        }
        atualizar();
    }

    private void atualizar() {
        var s = janela.sistema();
        emAberto.getItems().setAll(s.chamadosEmAberto());
        emAtendimento.getItems().setAll(s.chamadosEmAtendimento(tecnico));
        historico.getItems().setAll(s.historico());
        relatorio.setText(textoRelatorio());
        resumoAberto.setText(emAberto.getItems().size() + " chamado(s) na fila — o primeiro tem a maior prioridade.");
        resumoHistorico.setText("O topo da pilha (primeira linha) é o primeiro a ser reaberto.");
    }
}
