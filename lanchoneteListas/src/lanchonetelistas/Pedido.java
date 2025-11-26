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

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
    
}
