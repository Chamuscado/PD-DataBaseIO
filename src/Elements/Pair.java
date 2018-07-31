package Elements;

import java.io.Serializable;

public class Pair implements Serializable{
    static final long serialVersionUID = 1;
    public static final int INVALIDID = -1;
    Player[] players;
    int id;

    public Pair(Player[] players, int id) {
        this.players = players;
        this.id = id;
    }

    public Pair(Player[] players) {
        this(players, INVALIDID);
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ID: " + id + " Jogador 1: " + players[0].getUser() + " Jogador 2: " + players[1].getUser();
    }
}
