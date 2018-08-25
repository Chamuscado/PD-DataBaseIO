package DataBaseIO.Exceptions;

public class PairNotFoundException extends Throwable {
    public PairNotFoundException(String user1, String user2) {
        super("o par entre \"" + user1 + "\" ou \"" + user2 + "\" não exite ");
    }

    public PairNotFoundException(int id) {
        super("o par com o id  \"" + id + "\" não exite ");
    }
}
