package lanchonetelistas;

public class Pedido {
    public String item;
    public int quantidade;
    public double valor;

    public Pedido(String item, int quantidade, double valor) {
        this.item = item;
        this.quantidade = quantidade;
        this.valor = valor;
    }
}
