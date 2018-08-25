package DataBaseIO.Elements;

import java.io.Serializable;

public class GameDatabase implements Serializable{
    static final long serialVersionUID = 1;
    public static final int INVALIDID = -1;
    int id;
    PlayerDatabase vencedor;
    PairDatabase par;

    public GameDatabase(int id, PlayerDatabase vencedor, PairDatabase par) {
        this.id = id;
        this.vencedor = vencedor;
        this.par = par;
    }

    public GameDatabase(PlayerDatabase vencedor, PairDatabase par) {
        this(INVALIDID, vencedor, par);
    }

    public int getId() {
        return id;
    }


    public PlayerDatabase getVencedor() {
        return vencedor;
    }

    public void setVencedor(PlayerDatabase vencedor) {
        this.vencedor = vencedor;
    }

    public PairDatabase getPar() {
        return par;
    }

}
