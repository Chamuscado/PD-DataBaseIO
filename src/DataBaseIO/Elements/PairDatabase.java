package DataBaseIO.Elements;

import DataBaseIO.Exceptions.UserNotFoundException;

import java.io.Serializable;

public class PairDatabase implements Serializable {
    static final long serialVersionUID = 1;
    public static final int INVALIDID = -1;
    PlayerDatabase[] playerDatabases;
    int id;

    public PairDatabase(PlayerDatabase[] playerDatabases, int id) {
        this.playerDatabases = playerDatabases;
        this.id = id;
    }

    public PairDatabase(PlayerDatabase[] playerDatabases) {
        this(playerDatabases, INVALIDID);
    }

    public PlayerDatabase[] getPlayerDatabases() {
        return playerDatabases;
    }

    public PlayerDatabase getPlayerDatabases(int index) throws UserNotFoundException {
        if(index >= 0 && index <playerDatabases.length)
        return playerDatabases[index];
        else
            throw new UserNotFoundException("Invalid Index");
    }

    public void setPlayerDatabases(PlayerDatabase[] playerDatabases) {
        this.playerDatabases = playerDatabases;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ID: " + id + " Jogador 1: " + playerDatabases[0].getUser() + " Jogador 2: " + playerDatabases[1].getUser();
    }
}
