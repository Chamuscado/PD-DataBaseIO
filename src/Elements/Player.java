package Elements;

import java.io.Serializable;

public class Player implements Serializable {
    public static final int INVALIDID = -1;
    static final long serialVersionUID = 1;
    private int id;
    private int idPar;
    private String name;
    private String user;
    private String pass;
    private boolean logado;
    private String ip;
    private int porto;

    public Player(String user, String pass,String ip, int porto) {
        this(INVALIDID,null, user, pass,false,false,ip,porto);
    }

    public Player(String name, String user, String pass) {
        this(INVALIDID, name, user, pass, false);
    }

    public Player(int id, String name, String user, String pass, boolean logado) {
        this(id, name, user, pass, false, false);
    }

    public Player(int id, String name, String user, String pass, boolean logado, boolean bd,
                  String ip, int porto) {
        this(id, name, user, pass, logado, bd, ip, porto, INVALIDID);
    }

    public Player(int id, String name, String user, String pass, boolean logado, boolean bd,
                  String ip, int porto, int idPar) {
        this.id = id;
        this.name = name;
        this.user = user;
        if (bd)
            this.pass = pass;
        else
            this.pass = Integer.toHexString(pass.hashCode());
        this.logado = logado;
        this.ip = ip;
        this.porto = porto;
        this.idPar = idPar;
    }

    public Player(int id, String name, String user, String pass, boolean logado, boolean bd) {
        this(id, name, user, pass, logado, bd, "", -1);
    }

    public boolean invalidID() {
        return id == INVALIDID;
    }

    public int getIdPar() {
        return idPar;
    }

    public void setIdPar(int idPar) {
        this.idPar = idPar;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPorto() {
        return porto;
    }

    public void setPorto(int porto) {
        this.porto = porto;
    }

    public boolean isLogado() {
        return logado;
    }

    public void setLogado(boolean logado) {
        this.logado = logado;
    }

    public boolean isValidL1() {
        return !(user.isEmpty() || pass.isEmpty() || name.isEmpty());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "Id: " + id + " Nome: " + name + " User: " + user + " Pass: " + pass + " Logado: "
                + logado + " Address: " + ip + ":" + porto;
    }
}
