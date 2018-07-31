package Elements;

import java.io.Serializable;

public class Game implements Serializable{
    static final long serialVersionUID = 1;
    public static final int INVALIDID = -1;
    int id;
    Player vencedor;
    Pair par;

    public Game(int id, Player vencedor, Pair par) {
        this.id = id;
        this.vencedor = vencedor;
        this.par = par;
    }

    public Game(Player vencedor, Pair par) {
        this(INVALIDID, vencedor, par);
    }

    public int getId() {
        return id;
    }


    public Player getVencedor() {
        return vencedor;
    }

    public void setVencedor(Player vencedor) {
        this.vencedor = vencedor;
    }

    public Pair getPar() {
        return par;
    }

}
