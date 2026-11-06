package br.pucpr.startindex.ui;

import br.pucpr.startindex.model.Funcionario;
import br.pucpr.startindex.model.TecnicoTI;
import br.pucpr.startindex.model.Usuario;
import br.pucpr.startindex.servico.SistemaGestaoChamados;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Navegação entre as telas (layout e rótulos consistentes em todas elas, RNF-07). */
public class Janela {

    private final Stage stage;
    private final SistemaGestaoChamados sistema;

    public Janela(Stage stage, SistemaGestaoChamados sistema) {
        this.stage = stage;
        this.sistema = sistema;
        stage.setTitle("STARTINDEX - Gestão de Chamados");
    }

    public SistemaGestaoChamados sistema() {
        return sistema;
    }

    public void mostrarLogin() {
        trocar(new LoginView(this).construir(), 420, 380);
    }

    public void mostrarCadastro() {
        trocar(new CadastroView(this).construir(), 420, 460);
    }

    public void mostrarPainel(Usuario usuario) {
        if (usuario instanceof Funcionario f) {
            trocar(new FuncionarioView(this, f).construir(), 900, 600);
        } else if (usuario instanceof TecnicoTI t) {
            trocar(new TecnicoView(this, t).construir(), 1000, 640);
        }
    }

    private void trocar(Parent raiz, double largura, double altura) {
        Scene cena = new Scene(raiz, largura, altura);
        cena.getStylesheets().add(getClass().getResource("/br/pucpr/startindex/estilo.css").toExternalForm());
        stage.setScene(cena);
        stage.centerOnScreen();
        stage.show();
    }
}
