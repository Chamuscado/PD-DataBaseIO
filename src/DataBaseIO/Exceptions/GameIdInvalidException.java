package DataBaseIO.Exceptions;

public class GameIdInvalidException extends Throwable {


    public GameIdInvalidException(int gameId) {
        super("Jogo com id : \"" + gameId + "\" não exite ");
    }
}
