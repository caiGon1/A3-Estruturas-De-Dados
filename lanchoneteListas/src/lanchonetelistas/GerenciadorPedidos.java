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
            case LISTA: lista.add(p); break;
            case PILHA: pilha.push(p); break;
            case FILA: fila.add(p); break;
            case LISTA_ENCADEADA: listaEncadeada.add(p); break;
        }
    }

    public void remover() {
        switch(atual) {
            case LISTA:
            case LISTA_ENCADEADA:
                if (!lista.isEmpty()) lista.remove(lista.size() - 1);
                break;
            case PILHA:
                if (!pilha.isEmpty()) pilha.pop();
                break;
            case FILA:
                if (!fila.isEmpty()) fila.remove();
                break;
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
            case LISTA: return "Lista";
            case PILHA: return "Pilha";
            case FILA: return "Fila";
            case LISTA_ENCADEADA: return "Lista Encadeada";
        }
        return "";
    }
}

