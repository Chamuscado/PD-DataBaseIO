package DataBaseIO;

import DataBaseIO.Exceptions.*;
import DataBaseIO.Elements.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseIO {
    public static boolean DEBUG = false; //private static final boolean DEBUG = false;
    private static final int DEFAULTPORT = 3306;
    private static final String ENDSTRINGCONNECTION = "?allowPublicKeyRetrieval=true&autoReconnect=true&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String PLAYERSTABLE = "players";
    private static final String[] PLAYERSELMENTS = {"idplayers", "name", "username", "password", "logado",
            "ip", "porto", "pareatual", "wins", "defeats"};
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
            Class.forName("com.mysql.cj.jdbc.Driver");
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

    public boolean addPlayer(PlayerDatabase playerDatabase) throws InvalidUserException, UseAlreadExitsException {
        if (!playerDatabase.isValidL1())
            throw new InvalidUserException();
        if (contains(playerDatabase.getUser()))
            throw new UseAlreadExitsException();
        String ip = playerDatabase.getIp();
        if (ip == null || ip.isEmpty())
            ip = "NULL";
        else
            ip = "'" + ip + "'";
        String portoStr;
        int porto = playerDatabase.getPorto();
        if (porto == PlayerDatabase.INVALIDID)
            portoStr = "NULL";
        else
            portoStr = "'" + porto + "'";

        String sql = "INSERT INTO " + PLAYERSTABLE + " (" + PLAYERSELMENTS[1] + ", " + PLAYERSELMENTS[2]
                + ", " + PLAYERSELMENTS[3] + ", " + PLAYERSELMENTS[5] + ", " + PLAYERSELMENTS[6] + ", " + PLAYERSELMENTS[8] + ", " + PLAYERSELMENTS[9]
                + ") VALUES ('" + playerDatabase.getName() + "', '" + playerDatabase.getUser() + "', '" + playerDatabase.getPass()
                + "', " + ip + ", " + portoStr + ", " + playerDatabase.getWins() + ", " + playerDatabase.getDefeats() + ");";

        return execute(sql);
    }

    public PlayerDatabase getPlayer(String user) throws UserNotFoundException {
        String sql = "SELECT * FROM " + PLAYERSTABLE + " WHERE BINARY " + PLAYERSELMENTS[2] + " = '" + user + "';";
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


    public PlayerDatabase getPlayer(int id) throws UserNotFoundException {
        if (id < 0)
            throw new UserNotFoundException("Id invalido, <" + id + ">");
        String sql = "SELECT * FROM " + PLAYERSTABLE + " WHERE " + PLAYERSELMENTS[0] + " = '" + id + "';";
        return internalgetPlayer(sql, "" + id);
    }

    private PlayerDatabase internalgetPlayer(String sql, String user) throws UserNotFoundException {
        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                String ip = result.getString(PLAYERSELMENTS[5]);
                if (result.wasNull())
                    ip = "";
                int porto = result.getInt(PLAYERSELMENTS[6]);
                if (result.wasNull())
                    porto = PlayerDatabase.INVALIDID;
                int idPar = result.getInt(PLAYERSELMENTS[7]);
                if (result.wasNull())
                    idPar = PairDatabase.INVALIDID;
                return new PlayerDatabase(result.getInt(PLAYERSELMENTS[0]),
                        result.getString(PLAYERSELMENTS[1]),
                        result.getString(PLAYERSELMENTS[2]),
                        result.getString(PLAYERSELMENTS[3]),
                        result.getInt(PLAYERSELMENTS[4]) != 0, true, ip, porto, idPar,
                        result.getInt(PLAYERSELMENTS[8]),
                        result.getInt(PLAYERSELMENTS[9]));
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

    public List<PlayerDatabase> getPlayersLogados() {
        String sql = "SELECT * FROM " + PLAYERSTABLE + " WHERE " + PLAYERSELMENTS[4] + " = '1';";
        ResultSet result = executeQuery(sql);
        return extractPlayers(result);
    }

    public List<PlayerDatabase> getAllPlayers() {
        String sql = "SELECT * FROM " + PLAYERSTABLE + ";";
        ResultSet result = executeQuery(sql);

        return extractPlayers(result);
    }

    private List<PlayerDatabase> extractPlayers(ResultSet result) {
        List<PlayerDatabase> list = new ArrayList<>();
        try {
            while (result.next()) {
                String ip = result.getString(PLAYERSELMENTS[5]);
                if (result.wasNull())
                    ip = "";
                int porto = result.getInt(PLAYERSELMENTS[6]);
                if (result.wasNull())
                    porto = PlayerDatabase.INVALIDID;
                int idPar = result.getInt(PLAYERSELMENTS[7]);
                if (result.wasNull())
                    idPar = PlayerDatabase.INVALIDID;
                list.add(new PlayerDatabase(result.getInt(PLAYERSELMENTS[0]), result.getString(PLAYERSELMENTS[1]),
                        result.getString(PLAYERSELMENTS[2]), result.getString(PLAYERSELMENTS[3]),
                        result.getInt(PLAYERSELMENTS[4]) != 0, true, ip, porto, idPar,
                        result.getInt(PLAYERSELMENTS[8]),
                        result.getInt(PLAYERSELMENTS[9])));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean login(PlayerDatabase playerDatabase) throws UserNotFoundException, WrongPassWordException {

        PlayerDatabase bdplayer = getPlayer(playerDatabase.getUser());
        if (DEBUG)
            System.out.println("Pass playerDatabase: \"" + playerDatabase.getPass() + "\" Pass bdPlayer: \""
                    + bdplayer.getPass() + "\" " + (bdplayer.getPass().compareTo(playerDatabase.getPass()) == 0));
        if (bdplayer.getPass().compareTo(playerDatabase.getPass()) == 0) {

            String sql = "UPDATE " + PLAYERSTABLE + " SET " + PLAYERSELMENTS[4] + " = '1', "
                    + PLAYERSELMENTS[5] + " = '" + playerDatabase.getIp() + "', " + PLAYERSELMENTS[6]
                    + " = '" + playerDatabase.getPorto() + "' WHERE " + PLAYERSELMENTS[2] + " = '"
                    + playerDatabase.getUser() + "';";


            boolean r = execute(sql);
            if (r)
                playerDatabase.setLogado(true);
            return r;
        } else
            throw new WrongPassWordException();

    }

    public boolean logout(PlayerDatabase playerDatabase) {
        if (playerDatabase == null)
            return false;
        String sql = "UPDATE " + PLAYERSTABLE + " SET " + PLAYERSELMENTS[4] + " = '0', "
                + PLAYERSELMENTS[5] + " = " + "NULL" + ", " + PLAYERSELMENTS[6]
                + " = " + "NULL, " + PLAYERSELMENTS[7] + " = " + "NULL" + " WHERE "
                + PLAYERSELMENTS[2] + " = '" + playerDatabase.getUser() + "';";
        boolean r = execute(sql);
        if (r)
            playerDatabase.setLogado(false);
        return r;
    }

    public boolean logout(String username) {
        if (username == null || username.isEmpty())
            return false;
        String sql = "UPDATE " + PLAYERSTABLE + " SET " + PLAYERSELMENTS[4] + " = '0', "
                + PLAYERSELMENTS[5] + " = " + "NULL" + ", " + PLAYERSELMENTS[6]
                + " = " + "NULL, " + PLAYERSELMENTS[7] + " = " + "NULL" + " WHERE "
                + PLAYERSELMENTS[2] + " = '" + username + "';";
        return execute(sql);
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
        List<PairDatabase> list = getAllPlayerPairs(user1);
        for (PairDatabase i : list) {
            if (i.getPlayerDatabases()[1].getUser().compareTo(user2) == 0)
                return true;
        }

        PlayerDatabase playerDatabase1 = getPlayer(user1);
        PlayerDatabase playerDatabase2 = getPlayer(user2);
        String sql = "INSERT INTO " + dataBaseName + "." + PAIRSTABLE + " (" + PAIRSELMENTS[1] + ", "
                + PAIRSELMENTS[2] + ")" + " VALUES ('" + playerDatabase1.getId() + "', '" + playerDatabase2.getId() + "');";
        return execute(sql);
    }

    public void setPairAtual(String user0, String user1) throws InvalidPairException, UserNotFoundException, PairNotFoundException {
        PairDatabase pair = getPair(user0, user1);
        setPairAtual(pair);
    }

    public void setPairAtual(PairDatabase pairDataBase) throws InvalidPairException, UserNotFoundException, PairNotFoundException {
        PlayerDatabase[] playerDatabases = pairDataBase.getPlayerDatabases();
        if (playerDatabases.length != 2 || playerDatabases[0] == null || playerDatabases[1] == null)
            throw new InvalidPairException();
        if (pairDataBase.getId() == PairDatabase.INVALIDID)
            pairDataBase = getPair(pairDataBase.getPlayerDatabases(0).getUser(), pairDataBase.getPlayerDatabases(1).getUser());
        for (int i = 0; i < 2; ++i) {
            if (playerDatabases[i].invalidID())
                playerDatabases[i] = getPlayer(playerDatabases[i].getUser());
            removePairAtual(playerDatabases[i].getId());
        }

        String sql = "UPDATE " + dataBaseName + "." + PLAYERSTABLE + " SET " + PLAYERSELMENTS[7] + " = "
                + pairDataBase.getId() + " WHERE " + PLAYERSELMENTS[0] + " = " + playerDatabases[0].getId() + ";";
        execute(sql);

        sql = "UPDATE " + dataBaseName + "." + PLAYERSTABLE + " SET " + PLAYERSELMENTS[7] + " = "
                + pairDataBase.getId() + " WHERE " + PLAYERSELMENTS[0] + " = " + playerDatabases[1].getId() + ";";
        execute(sql);
    }

    public PairDatabase getPairAtual(String user) throws UserNotFoundException, PairAtualNotFoundException {

        PlayerDatabase playerDatabase = getPlayer(user);
        if (playerDatabase.getIdPar() == PlayerDatabase.INVALIDID)
            throw new PairAtualNotFoundException();
        try {
            return getPair(playerDatabase.getIdPar());
        } catch (PairNotFoundException e) {
            throw new PairAtualNotFoundException();
        }

    }

    private void removePairAtual(int playerId) throws UserNotFoundException {
        PlayerDatabase player = getPlayer(playerId);
        if (player.getIdPar() == PairDatabase.INVALIDID)
            return;
        PairDatabase pair = null;
        try {
            pair = getPair(player.getIdPar());
        } catch (PairNotFoundException ignore) {
            return;
        }
        String sql = "UPDATE " + PLAYERSTABLE + " SET " + PLAYERSELMENTS[7] + " = NULL "
                + " WHERE " + PLAYERSELMENTS[0] + " = '" + pair.getPlayerDatabases(0).getId() + "';";
        execute(sql);
        sql = "UPDATE " + PLAYERSTABLE + " SET " + PLAYERSELMENTS[7] + " = NULL "
                + " WHERE " + PLAYERSELMENTS[0] + " = '" + pair.getPlayerDatabases(1).getId() + "';";
        execute(sql);
    }

    public void removePairAtual(String user) throws UserNotFoundException {
        removePairAtual(getPlayer(user).getId());

    }

    public PairDatabase getPair(String user1, String user2) throws UserNotFoundException, PairNotFoundException {
        PlayerDatabase[] playerDatabases = new PlayerDatabase[2];
        playerDatabases[0] = getPlayer(user1);
        playerDatabases[1] = getPlayer(user2);
        String sql = "select * FROM " + PAIRSTABLE + " WHERE (" + PAIRSELMENTS[1] + " = "
                + playerDatabases[0].getId() + " and " + PAIRSELMENTS[2] + " = " + playerDatabases[1].getId() + ") or ("
                + PAIRSELMENTS[1] + " = " + playerDatabases[1].getId() + " and " + PAIRSELMENTS[2] + " = "
                + playerDatabases[0].getId() + ")";
        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                return new PairDatabase(playerDatabases, result.getInt("idpairs"));
            } else {
                throw new PairNotFoundException(user1, user2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PairDatabase getPair(int id0, int id1) throws UserNotFoundException, PairNotFoundException {
        PlayerDatabase[] playerDatabases = new PlayerDatabase[2];
        playerDatabases[0] = getPlayer(id0);
        playerDatabases[1] = getPlayer(id1);
        String sql = "select * FROM " + PAIRSTABLE + " WHERE (" + PAIRSELMENTS[1] + " = " + id0 + " and "
                + PAIRSELMENTS[2] + " = " + id1 + ") or (" + PAIRSELMENTS[1] + " = " + id1 + " and "
                + PAIRSELMENTS[2] + " = " + id0 + ");";
        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                return new PairDatabase(playerDatabases, result.getInt(PAIRSELMENTS[0]));
            } else {
                throw new PairNotFoundException(playerDatabases[0].getName(), playerDatabases[1].getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PairDatabase getPair(int id) throws UserNotFoundException, PairNotFoundException {
        PlayerDatabase[] playerDatabases = new PlayerDatabase[2];
        String sql = "select * FROM " + PAIRSTABLE + " WHERE " + PAIRSELMENTS[0] + " = " + id + ";";
        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                playerDatabases[0] = getPlayer(result.getInt(PAIRSELMENTS[1]));
                playerDatabases[1] = getPlayer(result.getInt(PAIRSELMENTS[2]));
                return new PairDatabase(playerDatabases, result.getInt(PAIRSELMENTS[0]));
            } else {
                throw new PairNotFoundException(playerDatabases[0].getName(), playerDatabases[1].getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PairDatabase> getAllPlayerPairs(String user) throws UserNotFoundException {
        PlayerDatabase playerDatabase = getPlayer(user);
        return getAllPlayerPairs(playerDatabase.getId());
    }

    public List<PairDatabase> getAllPlayerPairs(int playerId) throws UserNotFoundException {
        List<PairDatabase> list = new ArrayList<>();
        String sql = "SELECT * FROM " + PAIRSTABLE + " WHERE " + PAIRSELMENTS[1] + " = '" + playerId
                + "' or " + PAIRSELMENTS[2] + " = '" + playerId + "';";
        ResultSet result = executeQuery(sql);
        try {
            if (result != null)
                while (result.next()) {
                    PlayerDatabase[] playerDatabases = new PlayerDatabase[2];
                    playerDatabases[0] = getPlayer(result.getInt(PAIRSELMENTS[1]));
                    playerDatabases[1] = getPlayer(result.getInt(PAIRSELMENTS[2]));

                    if (playerDatabases[1].getId() == playerId) {
                        PlayerDatabase p = playerDatabases[1];
                        playerDatabases[1] = playerDatabases[0];
                        playerDatabases[0] = p;
                    }

                    list.add(new PairDatabase(playerDatabases, result.getInt(PAIRSELMENTS[0])));
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PairDatabase> getAllPairs() {
        List<PairDatabase> list = new ArrayList<>();
        List<PlayerDatabase> allPlayerDatabases = getAllPlayers();
        String sql = "SELECT * FROM " + PAIRSTABLE + ";";
        ResultSet result = executeQuery(sql);
        try {
            if (result != null)
                while (result.next()) {
                    PlayerDatabase[] playerDatabases = new PlayerDatabase[2];
                    int p0 = result.getInt(PAIRSELMENTS[1]);
                    int p1 = result.getInt(PAIRSELMENTS[2]);
                    boolean c0 = false, c1 = false;
                    for (int i = 0; i < allPlayerDatabases.size(); ++i) {
                        int id = allPlayerDatabases.get(i).getId();
                        if (p0 == id) {
                            playerDatabases[0] = allPlayerDatabases.get(i);
                            c0 = true;
                        }
                        if (p1 == id) {
                            playerDatabases[1] = allPlayerDatabases.get(i);
                            c1 = true;
                        }
                        if (c0 && c1)
                            break;
                    }
                    if (playerDatabases[1].getUser().compareTo(user) == 0) {
                        PlayerDatabase p = playerDatabases[1];
                        playerDatabases[1] = playerDatabases[0];
                        playerDatabases[0] = p;
                    }

                    list.add(new PairDatabase(playerDatabases, result.getInt(PAIRSELMENTS[0])));
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean removepair(String user1, String user2) throws UserNotFoundException {
        PlayerDatabase[] playerDatabases = new PlayerDatabase[2];
        playerDatabases[0] = getPlayer(user1);
        playerDatabases[1] = getPlayer(user2);
        String sql = "delete FROM " + PAIRSTABLE + " WHERE (" + PAIRSELMENTS[1] + " = " + playerDatabases[0].getId()
                + " and " + PAIRSELMENTS[2] + " = " + playerDatabases[1].getId() + ") or (" + PAIRSELMENTS[1]
                + " = " + playerDatabases[1].getId() + " and " + PAIRSELMENTS[2] + " = " + playerDatabases[0].getId() + ")";
        return execute(sql);
    }

//-------------------------------------------------->Games<---------------------------------------------------

    public boolean createGame(PairDatabase pairDataBase) throws UserNotFoundException, PairNotFoundException,
            UnfinishedGameException, CorruptDataBaseException {
        try {
            getPairUnfinishedGame(pairDataBase);
        } catch (AnyUnfinishedGameException e) {
            int id;
            if (pairDataBase.getId() != PairDatabase.INVALIDID)
                id = pairDataBase.getId();
            else {
                id = getPair(pairDataBase.getPlayerDatabases()[0].getUser(), pairDataBase.getPlayerDatabases()[0].getUser()).getId();
            }
            String sql = "INSERT INTO " + dataBaseName + "." + GAMETABLE + " (" + GAMEELMENTS[2]
                    + ")" + " VALUES ('" + id + "');";
            return execute(sql);
        }
        throw new UnfinishedGameException(pairDataBase.toString());
    }

    public boolean setGameWinner(GameDatabase gameDataBase, PlayerDatabase playerDatabase) throws UserNotFoundException,
            GameIdInvalidException {
        int playerId = playerDatabase.getId();
        int gameId = gameDataBase.getId();
        if (playerId == PlayerDatabase.INVALIDID)
            playerId = getPlayer(playerDatabase.getUser()).getId();
        if (gameId == GameDatabase.INVALIDID)
            throw new GameIdInvalidException(gameId);
        String sql = "UPDATE " + GAMETABLE + " SET " + GAMEELMENTS[1] + " = " + playerId + " WHERE "
                + GAMEELMENTS[0] + " = '" + gameId + "';";

        if (execute(sql)) {
            sql = String.format("UPDATE %s SET %s = %s + 1 WHERE %s = '%d';", PLAYERSTABLE, PLAYERSELMENTS[8], PLAYERSELMENTS[8], PLAYERSELMENTS[0], playerDatabase.getId());
            execute(sql);
            GameDatabase game = null;
            try {
                game = getGame(gameId);
            } catch (PairNotFoundException ignore) {// Não se verifica nesta situação
            }
            int defeatPlayerId =
                    game.getPar().getPlayerDatabases(0).getUser().compareTo(playerDatabase.getUser()) == 0
                            ? game.getPar().getPlayerDatabases(1).getId()
                            : game.getPar().getPlayerDatabases(0).getId();
            sql = String.format("UPDATE %s SET %s = %s + 1 WHERE %s = '%d';", PLAYERSTABLE, PLAYERSELMENTS[9], PLAYERSELMENTS[9], PLAYERSELMENTS[0], defeatPlayerId);
            execute(sql);

            return true;
        }
        return false;
    }

    public GameDatabase getGame(int id) throws PairNotFoundException, UserNotFoundException {
        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[0] + " = " + id + ";";

        ResultSet result = executeQuery(sql);
        try {
            if (result.next()) {
                PlayerDatabase vencedor = null;
                int vencedorid = result.getInt(GAMEELMENTS[1]);
                if (!result.wasNull())
                    vencedor = getPlayer(vencedorid);
                return new GameDatabase(result.getInt(GAMEELMENTS[0]), vencedor,
                        getPair(result.getInt(GAMEELMENTS[2])));
            } else {
                throw new PairNotFoundException(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<GameDatabase> getAllPairGame(PairDatabase pairDataBase) throws PairNotFoundException, UserNotFoundException {
        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[2] + " = " + pairDataBase.getId() + ";";

        return internalGetAllPairGame(sql, pairDataBase);
    }

    public GameDatabase getPairUnfinishedGame(PairDatabase pairDataBase) throws PairNotFoundException, UserNotFoundException,
            CorruptDataBaseException, AnyUnfinishedGameException {
        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[2] + " = " + pairDataBase.getId()
                + " and " + GAMEELMENTS[1] + " IS NULL;";

        List<GameDatabase> gameDatabases = internalGetAllPairGame(sql, pairDataBase);
        if (gameDatabases.size() > 1)
            throw new CorruptDataBaseException("Exitem demasidos jogos inacabados (" + gameDatabases.size() + ") ");
        else if (gameDatabases.isEmpty())
            throw new AnyUnfinishedGameException(pairDataBase);
        else
            return gameDatabases.get(0);
    }

    public List<GameDatabase> getAllEndPairGame(PairDatabase pairDataBase) throws PairNotFoundException, UserNotFoundException {
        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[2] + " = " + pairDataBase.getId()
                + " and " + GAMEELMENTS[1] + " IS NOT NULL;";

        return internalGetAllPairGame(sql, pairDataBase);
    }

    private List<GameDatabase> internalGetAllPairGame(String sql, PairDatabase pairDataBase) throws PairNotFoundException,
            UserNotFoundException {
        List<GameDatabase> list = new ArrayList<>();
        ResultSet result = executeQuery(sql);
        try {
            if (result != null)
                while (result.next()) {
                    PlayerDatabase vencedor = null;
                    int vencedorid = result.getInt(GAMEELMENTS[1]);
                    if (!result.wasNull()) {
                        if (pairDataBase.getPlayerDatabases()[0].getId() != PlayerDatabase.INVALIDID
                                && pairDataBase.getPlayerDatabases()[0].getId() == vencedorid)
                            vencedor = pairDataBase.getPlayerDatabases()[0];
                        else if (pairDataBase.getPlayerDatabases()[1].getId() != PlayerDatabase.INVALIDID
                                && pairDataBase.getPlayerDatabases()[1].getId() == vencedorid)
                            vencedor = pairDataBase.getPlayerDatabases()[1];
                        else
                            vencedor = getPlayer(vencedorid);
                    }
                    list.add(new GameDatabase(result.getInt(GAMEELMENTS[0]), vencedor, pairDataBase));
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<GameDatabase> getAllFinishedGames() {
        List<GameDatabase> list = new ArrayList<>();

        String sql = "SELECT * FROM " + GAMETABLE + " WHERE " + GAMEELMENTS[1] + " = " + "NULL" + ";";

        ResultSet result = executeQuery(sql);
        try {
            if (result != null)
                while (result.next()) {
                    list.add(new GameDatabase(result.getInt(GAMEELMENTS[0]), null,
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

    public void refreshAllPlayersWinsAndDefeats() {
        List<PlayerDatabase> playerList = getAllPlayers();
        for (PlayerDatabase player : playerList) {
            try {
                refreshPlayerWinsAndDefeats(player.getId());
            } catch (UserNotFoundException e) {
                System.err.println("Base de Dados corrompida??? getAllPlayers retornou um player que não existe! " + e.getMessage());
                continue;
            }
        }
    }

    public void refreshPlayerWinsAndDefeats(int id) throws UserNotFoundException {
        List<PairDatabase> pairList = getAllPlayerPairs(id);
        int wins = 0;
        int defeats = 0;

        for (PairDatabase pair : pairList) {
            List<GameDatabase> gameList;
            try {
                gameList = getAllEndPairGame(pair);
            } catch (PairNotFoundException e) {
                System.err.println("Base de Dados corrompida??? getAllEndPairGame retornou um par que não existe! " + e.getMessage());
                continue;
            }
            for (GameDatabase game : gameList) {
                if (game.getVencedor().getId() == id)
                    ++wins;
                else
                    ++defeats;
            }
        }
        String sql = String.format("UPDATE %s SET %s = '%d' WHERE %s = '%d';", PLAYERSTABLE, PLAYERSELMENTS[8], wins, PLAYERSELMENTS[0], id);
        execute(sql);
        sql = String.format("UPDATE %s SET %s = '%d' WHERE %s = '%d';", PLAYERSTABLE, PLAYERSELMENTS[9], defeats, PLAYERSELMENTS[0], id);
        execute(sql);
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
