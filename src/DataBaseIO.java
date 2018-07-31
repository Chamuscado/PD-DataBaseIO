import Elements.Game;
import Elements.Pair;
import Elements.Player;
import Exceptions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseIO {
    public static boolean DEBUG = false; //private static final boolean DEBUG = false;
    private static final int DEFAULTPORT = 3306;
    private static final String ENDSTRINGCONNECTION = "?allowPublicKeyRetrieval=true&autoReconnect=true&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String PLAYERSTABLE = "players";
    private static final String[] PLAYERSELMENTS = {"idplayers", "name", "username", "password", "logado",
            "ip", "porto", "pareatual"};
    private static final String PAIRSTABLE = "pairs";
    private static final String[] PAIRSELMENTS = {"idpairs", "players_idplayers", "players_idplayers1"};
    private static final String GAMETABLE = "games";
    private static final String[] GAMEELMENTS = {"idgames", "vencedor", "pairs_idpairs"};

    private String user;
    private String pass;
    private String dataBaseName;
    private String ip;
    private int port;
    private Connection connection;

    public DataBaseIO(String dataBaseName, String user, String pass, String ip, int port) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Não existe a biblioteca necessária para comunicar com a base de dados" + e);
        }
        this.user = user;
        this.pass = pass;
        this.dataBaseName = dataBaseName;
        this.ip = ip;
        this.port = port;
    }

    public DataBaseIO(String dataBaseName, String user, String pass, String ip) {
        this(dataBaseName, user, pass, ip, DEFAULTPORT);
    }

    public boolean connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/"
                    + dataBaseName + ENDSTRINGCONNECTION, user, pass);
        } catch (SQLException e) {
            System.out.println("Não foi possivel comunicar com a base de dandos: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        connection = null;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

//------------------------------------------------->Players<--------------------------------------------------

    public boolean addPlayer(Player player) throws InvalidUserException, UseAlreadExitsException {
        if (!player.isValidL1())
            throw new InvalidUserException();
        if (contains(player.getUser()))
            throw new UseAlreadExitsException();
        String ip = player.getIp();
        if (ip == null || ip.isEmpty())
            ip = "NULL";
        else
            ip = "'" + ip + "'";
        String portoStr;
        int porto = player.getPorto();
        if (porto == Player.INVALIDID)
            portoStr = "NULL";
        else
            portoStr = "'" + porto + "'";

        String sql = "INSERT INTO " + PLAYERSTABLE + " (" + PLAYERSELMENTS[1] + ", " + PLAYERSELMENTS[2]
                + ", " + PLAYERSELMENTS[3] + ", " + PLAYERSELMENTS[5] + ", " + PLAYERSELMENTS[6]
                + ") VALUES ('" + player.getName() + "', '" + player.getUser() + "', '" + player.getPass()
                + "', " + ip + ", " + portoStr + ");";

        return execute(sql);
    }

    public Player getPlayer(String user) throws UserNotFoundException {
        String sql = "SELECT * FROM " + PLAYERSTABLE + " WHERE " + PLAYERSELMENTS[2] + " = '" + user + "';";
        return internalgetPlayer(sql, user);
    }

    public boolean contains(String user) {
        try {
            getPlayer(user);
        } catch (UserNotFoundException e) {
            return false;
        }
        return true;
    }


    public Player getPlayer(int id) throws UserNotFoundException {
        if (id < 0)
            throw new UserNotFoundException("Id invalido, <" + id + ">");
        String sql = "SELECT * FROM " + PLAYERSTABLE + " WHERE " + PLAYERSELMENTS[0] + " = '" + id + "';";
        return internalgetPlayer(sql, "" + id);
    }

    private Player internalgetPlayer(String sql, String user) throws UserNotFoundException {
        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                String ip = result.getString(PLAYERSELMENTS[5]);
                if (!result.wasNull())
                    ip = "";
                int porto = result.getInt(PLAYERSELMENTS[6]);
                if (!result.wasNull())
                    porto = Player.INVALIDID;
                int idPar = result.getInt(PLAYERSELMENTS[7]);
                if (!result.wasNull())
                    idPar = Player.INVALIDID;
                return new Player(result.getInt(PLAYERSELMENTS[0]),
                        result.getString(PLAYERSELMENTS[1]),
                        result.getString(PLAYERSELMENTS[2]),
                        result.getString(PLAYERSELMENTS[3]),
                        result.getInt(PLAYERSELMENTS[4]) != 0, true, ip, porto, idPar);
            } else {
                throw new UserNotFoundException(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean removePlayer(String user) throws UserNotFoundException {
        getPlayer(user);                                 //remover caso não queiram saber se existia ou não
        String sql = "delete FROM " + PLAYERSTABLE + " WHERE " + PLAYERSELMENTS[2] + " = '" + user + "';";
        return execute(sql);
    }

    public List<Player> getPlayersLogados() {
        String sql = "SELECT * FROM " + PLAYERSTABLE + " WHERE " + PLAYERSELMENTS[4] + " = '1';";
        ResultSet result = executeQuery(sql);
        return extractPlayers(result);
    }

    public List<Player> getAllPlayers() {
        String sql = "SELECT * FROM " + PLAYERSTABLE + ";";
        ResultSet result = executeQuery(sql);

        return extractPlayers(result);
    }

    private List<Player> extractPlayers(ResultSet result) {
        List<Player> list = new ArrayList<>();
        try {
            while (result.next()) {
                String ip = result.getString(PLAYERSELMENTS[5]);
                if (!result.wasNull())
                    ip = "";
                int porto = result.getInt(PLAYERSELMENTS[6]);
                if (!result.wasNull())
                    porto = Player.INVALIDID;
                int idPar = result.getInt(PLAYERSELMENTS[7]);
                if (!result.wasNull())
                    idPar = Player.INVALIDID;
                list.add(new Player(result.getInt(PLAYERSELMENTS[0]), result.getString(PLAYERSELMENTS[1]),
                        result.getString(PLAYERSELMENTS[2]), result.getString(PLAYERSELMENTS[3]),
                        result.getInt(PLAYERSELMENTS[4]) != 0, true, ip, porto, idPar));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean login(Player player) throws UserNotFoundException, WrongPassWordException {

        Player bdplayer = getPlayer(player.getUser());
        if (DEBUG)
            System.out.println("Pass player: \"" + player.getPass() + "\" Pass bdPlayer: \""
                    + bdplayer.getPass() + "\" " + (bdplayer.getPass().compareTo(player.getPass()) == 0));
        if (bdplayer.getPass().compareTo(player.getPass()) == 0) {

            String sql = "UPDATE " + PLAYERSTABLE + " SET " + PLAYERSELMENTS[4] + " = '1', "
                    + PLAYERSELMENTS[5] + " = '" + player.getIp() + "', " + PLAYERSELMENTS[6]
                    + " = '" + player.getPorto() + "' WHERE " + PLAYERSELMENTS[2] + " = '"
                    + player.getUser() + "';";


            boolean r = execute(sql);
            if (r)
                player.setLogado(true);
            return r;
        } else
            throw new WrongPassWordException();

    }

    public boolean logout(Player player) {
        if (player == null)
            return false;
        String sql = "UPDATE " + PLAYERSTABLE + " SET " + PLAYERSELMENTS[4] + " = '0', "
                + PLAYERSELMENTS[5] + " = " + "NULL" + ", " + PLAYERSELMENTS[6]
                + " = " + "NULL, " + PLAYERSELMENTS[7] + " = " + "NULL" + " WHERE "
                + PLAYERSELMENTS[2] + " = '" + player.getUser() + "';";
        boolean r = execute(sql);
        if (r)
            player.setLogado(false);
        return r;
    }

    public void logoutall() {
        String sql = "UPDATE " + PLAYERSTABLE + " SET " + PLAYERSELMENTS[4] + " = '0', " +
                PLAYERSELMENTS[5] + " = " + "NULL" + ", " + PLAYERSELMENTS[6]
                + " = " + "NULL, " + PLAYERSELMENTS[7]
                + " = " + "NULL " + " WHERE " + PLAYERSELMENTS[4] + " != '0';";

        execute(sql);
    }

//-------------------------------------------------->Pairs<---------------------------------------------------

    public boolean createpair(String user1, String user2) throws UserNotFoundException {
        List<Pair> list = getPairPlayer(user1);
        for (Pair i : list) {
            if (i.getPlayers()[1].getUser().compareTo(user2) == 0)
                return true;
        }

        Player player1 = getPlayer(user1);
        Player player2 = getPlayer(user2);
        String sql = "INSERT INTO " + dataBaseName + "." + PAIRSTABLE + " (" + PAIRSELMENTS[1] + ", "
                + PAIRSELMENTS[2] + ")" + " VALUES ('" + player1.getId() + "', '" + player2.getId() + "');";
        return execute(sql);
    }

    public void setPairAtual(Pair pair) throws InvalidPairException, UserNotFoundException {
        Player[] players = pair.getPlayers();
        if (players.length != 2 || players[0] == null || players[1] == null)
            throw new InvalidPairException();
        for (int i = 0; i < 2; ++i) {
            if (players[i].invalidID())
                players[i] = getPlayer(players[i].getUser());
            removePairAtual(players[i].getId());
        }

        String sql = "UPDATE " + dataBaseName + "." + PLAYERSTABLE + " SET " + PLAYERSELMENTS[7] + " = "
                + players[1].getId() + " WHERE " + PLAYERSELMENTS[0] + " = " + players[0].getId() + ";";
        execute(sql);

        sql = "UPDATE " + dataBaseName + "." + PLAYERSTABLE + " SET " + PLAYERSELMENTS[7] + " = "
                + players[0].getId() + " WHERE " + PLAYERSELMENTS[0] + " = " + players[1].getId() + ";";
        execute(sql);
    }

    public Pair getPairAtual(String user) throws UserNotFoundException, PairAtualNotFoundException {

        Player player = getPlayer(user);
        if (player.getIdPar() == Player.INVALIDID)
            throw new PairAtualNotFoundException();
        try {
            return getPair(player.getId(), player.getIdPar());
        } catch (PairNotFoundException e) {
            throw new PairAtualNotFoundException();
        }

    }

    private void removePairAtual(int id) {
        String sql = "UPDATE " + PLAYERSTABLE + " SET " + PLAYERSELMENTS[7] + " = NULL "
                + " WHERE " + PLAYERSELMENTS[0] + " = '" + id + "';";
        execute(sql);
    }

    public void removePairAtual(String user) throws UserNotFoundException {
        removePairAtual(getPlayer(user).getIp());

    }

    public Pair getPair(String user1, String user2) throws UserNotFoundException, PairNotFoundException {
        Player[] players = new Player[2];
        players[0] = getPlayer(user1);
        players[1] = getPlayer(user2);
        String sql = "select * FROM " + PAIRSTABLE + " WHERE (" + PAIRSELMENTS[1] + " = "
                + players[0].getId() + " and " + PAIRSELMENTS[2] + " = " + players[1].getId() + ") or ("
                + PAIRSELMENTS[1] + " = " + players[1].getId() + " and " + PAIRSELMENTS[2] + " = "
                + players[0].getId() + ")";
        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                return new Pair(players, result.getInt("idpairs"));
            } else {
                throw new PairNotFoundException(user1, user2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Pair getPair(int id0, int id1) throws UserNotFoundException, PairNotFoundException {
        Player[] players = new Player[2];
        players[0] = getPlayer(id0);
        players[1] = getPlayer(id1);
        String sql = "select * FROM " + PAIRSTABLE + " WHERE (" + PAIRSELMENTS[1] + " = " + id0 + " and "
                + PAIRSELMENTS[2] + " = " + id1 + ") or (" + PAIRSELMENTS[1] + " = " + id1 + " and "
                + PAIRSELMENTS[2] + " = " + id0 + ");";
        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                return new Pair(players, result.getInt(PAIRSELMENTS[0]));
            } else {
                throw new PairNotFoundException(players[0].getName(), players[1].getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Pair getPair(int id) throws UserNotFoundException, PairNotFoundException {
        Player[] players = new Player[2];
        String sql = "select * FROM " + PAIRSTABLE + " WHERE (" + PAIRSELMENTS[0] + " = " + id + ";";
        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                players[0] = getPlayer(result.getInt(PAIRSELMENTS[1]));
                players[1] = getPlayer(result.getInt(PAIRSELMENTS[2]));
                return new Pair(players, result.getInt(PAIRSELMENTS[0]));
            } else {
                throw new PairNotFoundException(players[0].getName(), players[1].getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Pair> getPairPlayer(String user) throws UserNotFoundException {
        List<Pair> list = new ArrayList<>();
        Player player = getPlayer(user);
        String sql = "SELECT * FROM " + PAIRSTABLE + " WHERE " + PAIRSELMENTS[1] + " = '" + player.getId()
                + "' or " + PAIRSELMENTS[2] + " = '" + player.getId() + "';";
        ResultSet result = executeQuery(sql);
        try {
            if (result != null)
                while (result.next()) {
                    Player[] players = new Player[2];
                    players[0] = getPlayer(result.getInt(PAIRSELMENTS[1]));
                    players[1] = getPlayer(result.getInt(PAIRSELMENTS[2]));

                    if (players[1].getUser().compareTo(user) == 0) {
                        Player p = players[1];
                        players[1] = players[0];
                        players[0] = p;
                    }

                    list.add(new Pair(players, result.getInt(PAIRSELMENTS[0])));
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Pair> getAllPairs() {
        List<Pair> list = new ArrayList<>();
        List<Player> allPlayers = getAllPlayers();
        String sql = "SELECT * FROM " + PAIRSTABLE + ";";
        ResultSet result = executeQuery(sql);
        try {
            if (result != null)
                while (result.next()) {
                    Player[] players = new Player[2];
                    int p0 = result.getInt(PAIRSELMENTS[1]);
                    int p1 = result.getInt(PAIRSELMENTS[2]);
                    boolean c0 = false, c1 = false;
                    for (int i = 0; i < allPlayers.size(); ++i) {
                        int id = allPlayers.get(i).getId();
                        if (p0 == id) {
                            players[0] = allPlayers.get(i);
                            c0 = true;
                        }
                        if (p1 == id) {
                            players[1] = allPlayers.get(i);
                            c1 = true;
                        }
                        if (c0 && c1)
                            break;
                    }
                    if (players[1].getUser().compareTo(user) == 0) {
                        Player p = players[1];
                        players[1] = players[0];
                        players[0] = p;
                    }

                    list.add(new Pair(players, result.getInt(PAIRSELMENTS[0])));
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean removepair(String user1, String user2) throws UserNotFoundException {
        Player[] players = new Player[2];
        players[0] = getPlayer(user1);
        players[1] = getPlayer(user2);
        String sql = "delete FROM " + PAIRSTABLE + " WHERE (" + PAIRSELMENTS[1] + " = " + players[0].getId()
                + " and " + PAIRSELMENTS[2] + " = " + players[1].getId() + ") or (" + PAIRSELMENTS[1]
                + " = " + players[1].getId() + " and " + PAIRSELMENTS[2] + " = " + players[0].getId() + ")";
        return execute(sql);
    }

//-------------------------------------------------->Games<---------------------------------------------------

    public boolean createGame(Pair pair) throws UserNotFoundException, PairNotFoundException,
            UnfinishedGameException, CorruptDataBaseException {
        try {
            getPairUnfinishedGame(pair);
        } catch (AnyUnfinishedGameException e) {
            int id;
            if (pair.getId() != Pair.INVALIDID)
                id = pair.getId();
            else {
                id = getPair(pair.getPlayers()[0].getUser(), pair.getPlayers()[0].getUser()).getId();
            }
            String sql = "INSERT INTO " + dataBaseName + "." + GAMETABLE + " (" + GAMEELMENTS[2]
                    + ")" + " VALUES ('" + id + "');";
            return execute(sql);
        }
        throw new UnfinishedGameException(pair.toString());
    }

    public boolean setGameVencedor(Game game, Player player) throws UserNotFoundException,
            GameIdInvalidException {
        int playerId = player.getId();
        int gameId = game.getId();
        if (playerId == Player.INVALIDID)
            playerId = getPlayer(player.getUser()).getId();
        if (gameId == Game.INVALIDID)
            throw new GameIdInvalidException(gameId);
        String sql = "UPDATE " + GAMETABLE + " SET " + GAMEELMENTS[1] + " = " + playerId + " WHERE "
                + GAMEELMENTS[0] + " = '" + gameId + "';";

        return execute(sql);
    }

    public Game getGame(int id) throws PairNotFoundException, UserNotFoundException {
        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[0] + " = " + id + ";";

        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                Player vencedor = null;
                int vencedorid = result.getInt(GAMEELMENTS[1]);
                if (!result.wasNull())
                    vencedor = getPlayer(vencedorid);
                return new Game(result.getInt(GAMEELMENTS[0]), vencedor,
                        getPair(result.getInt(GAMEELMENTS[2])));
            } else {
                throw new PairNotFoundException(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Game> getAllPairGame(Pair pair) throws PairNotFoundException, UserNotFoundException {
        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[2] + " = " + pair.getId() + ";";

        return internalGetAllPairGame(sql, pair);
    }

    public Game getPairUnfinishedGame(Pair pair) throws PairNotFoundException, UserNotFoundException,
            CorruptDataBaseException, AnyUnfinishedGameException {
        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[2] + " = " + pair.getId()
                + " and " + GAMEELMENTS[1] + " IS NULL;";

        List<Game> games = internalGetAllPairGame(sql, pair);
        if (games.size() > 1)
            throw new CorruptDataBaseException("Exitem demasidos jogos inacabados (" + games.size() + ") ");
        else if (games.isEmpty())
            throw new AnyUnfinishedGameException(pair);
        else
            return games.get(0);
    }

    public List<Game> getAllEndPairGame(Pair pair) throws PairNotFoundException, UserNotFoundException {
        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[2] + " = " + pair.getId()
                + " and " + GAMEELMENTS[1] + " IS NOT NULL;";

        return internalGetAllPairGame(sql, pair);
    }

    private List<Game> internalGetAllPairGame(String sql, Pair pair) throws PairNotFoundException,
            UserNotFoundException {
        List<Game> list = new ArrayList<>();
        ResultSet result = executeQuery(sql);
        try {
            if (result != null)
                while (result.next()) {
                    Player vencedor = null;
                    int vencedorid = result.getInt(GAMEELMENTS[1]);
                    if (!result.wasNull()) {
                        if (pair.getPlayers()[0].getId() != Player.INVALIDID
                                && pair.getPlayers()[0].getId() == vencedorid)
                            vencedor = pair.getPlayers()[0];
                        else if (pair.getPlayers()[1].getId() != Player.INVALIDID
                                && pair.getPlayers()[1].getId() == vencedorid)
                            vencedor = pair.getPlayers()[1];
                        else
                            vencedor = getPlayer(vencedorid);
                    }
                    list.add(new Game(result.getInt(GAMEELMENTS[0]), vencedor, pair));
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Game> getAllFinishedGames() {
        List<Game> list = new ArrayList<>();

        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[1] + " = " + "NULL" + ";";

        ResultSet result = executeQuery(sql);
        try {
            if (result != null)
                while (result.next()) {
                    list.add(new Game(result.getInt(GAMEELMENTS[0]), null,
                            getPair(result.getInt(GAMEELMENTS[2]))));
                }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UserNotFoundException e) {
            System.err.println("Base de Dados corrompida???");
        } catch (PairNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean removeGame(int id) throws UserNotFoundException {
        String sql = "delete FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[0] + " = " + id + ";";
        return execute(sql);
    }

//-------------------------------------------------->Comuns<--------------------------------------------------

    private boolean execute(String sql) {
        Statement statement = null;
        if (connection == null)
            throw new NotConnectedException("Ainda não foi estabelecida a comunicação com a base de dados");
        try {
            statement = connection.createStatement();

            if (DEBUG) System.out.print(sql);

            statement.execute(sql);

        } catch (SQLException e) {
            //e.printStackTrace();
            if (DEBUG) System.out.println(" - \033[31;1m failed\033[0m " + e.getMessage());
            return false;
        }
        if (DEBUG) System.out.println(" -  \033[33m Successful\033[0m");
        return true;

    }

    private ResultSet executeQuery(String sql) {
        Statement statement = null;
        ResultSet result = null;
        if (connection == null)
            throw new NotConnectedException("Ainda não foi estabelecida a comunicação com a base de dados");
        try {
            statement = connection.createStatement();
            if (DEBUG) System.out.print(sql);
            result = statement.executeQuery(sql);
        } catch (SQLException e) {
            //e.printStackTrace();
            if (DEBUG) System.out.println(" - \033[31;1m failed\033[0m");
            return null;
        }

        if (DEBUG) System.out.println(" -  \033[33m Successful\033[0m");
        return result;
    }

}
