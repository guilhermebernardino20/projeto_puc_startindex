package br.pucpr.startindex.ui;

import br.pucpr.startindex.model.Chamado;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/** Fábrica de tabelas de chamados reutilizadas pelas telas. */
final class Tabelas {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Tabelas() {}

    static TableView<Chamado> chamados(boolean mostrarSolicitante, boolean mostrarTecnico) {
        TableView<Chamado> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        t.setPlaceholder(new javafx.scene.control.Label("Nenhum chamado."));
        t.getColumns().add(coluna("Nº", c -> String.valueOf(c.getId()), 50));
        t.getColumns().add(coluna("Título", Chamado::getTitulo, 220));
        t.getColumns().add(coluna("Categoria", c -> c.getCategoria().getNome(), 120));
        t.getColumns().add(coluna("Prioridade", c -> c.getPrioridade().toString(), 110));
        t.getColumns().add(coluna("Criado em", c -> FORMATO.format(c.getDataCriacao()), 130));
        t.getColumns().add(coluna("Status", c -> c.getStatus().toString(), 120));
        if (mostrarSolicitante) {
            t.getColumns().add(coluna("Solicitante", c -> c.getSolicitante().getNome(), 140));
        }
        if (mostrarTecnico) {
            t.getColumns().add(coluna("Técnico", c -> c.getTecnicoResponsavel() == null
                    ? "—" : c.getTecnicoResponsavel().getNome(), 140));
        }
        return t;
    }

    private static TableColumn<Chamado, String> coluna(String nome, Function<Chamado, String> f, double largura) {
        TableColumn<Chamado, String> col = new TableColumn<>(nome);
        col.setCellValueFactory(c -> new SimpleStringProperty(f.apply(c.getValue())));
        col.setPrefWidth(largura);
        col.setSortable(false);
        return col;
    }
}
