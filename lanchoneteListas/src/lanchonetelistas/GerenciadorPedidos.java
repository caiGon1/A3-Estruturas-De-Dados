package lanchonetelistas;
import java.util.*;

public class GerenciadorPedidos {

    public enum TipoEstrutura {
        LISTA, PILHA, FILA, LISTA_ENCADEADA
    }

    private TipoEstrutura atual = TipoEstrutura.LISTA;
    private List<Pedido> lista = new ArrayList<>();
    private Stack<Pedido> pilha = new Stack<>();
    private Queue<Pedido> fila = new LinkedList<>();
    private LinkedList<Pedido> listaEncadeada = new LinkedList<>();

    public void setEstrutura(TipoEstrutura tipo) {
        this.atual = tipo;
    }

    public void adicionar(Pedido p) {
        switch(atual) {
            case LISTA -> lista.add(p);
            case PILHA -> pilha.push(p);
            case FILA -> fila.add(p);
            case LISTA_ENCADEADA -> listaEncadeada.add(p);
        }
    }

    public void remover() {
        switch(atual) {
            case LISTA, LISTA_ENCADEADA -> {
                if (!lista.isEmpty()) lista.remove(lista.size() - 1);
            }
            case PILHA -> {
                if (!pilha.isEmpty()) pilha.pop();
            }
            case FILA -> {
                if (!fila.isEmpty()) fila.remove();
            }
        }
    }

    public List<Pedido> getPedidos() {
        switch(atual) {
            case LISTA: return new ArrayList<>(lista);
            case PILHA: return new ArrayList<>(pilha);
            case FILA: return new ArrayList<>(fila);
            case LISTA_ENCADEADA: return new ArrayList<>(listaEncadeada);
        }
        return null;
    }

    public String getNomeEstrutura() {
        switch(atual) {
            case LISTA -> {
                return "Lista";
            }
            case PILHA -> {
                return "Pilha";
            }
            case FILA -> {
                return "Fila";
            }
            case LISTA_ENCADEADA -> {
                return "Lista Encadeada";
            }
        }
        return "";
    }
}

