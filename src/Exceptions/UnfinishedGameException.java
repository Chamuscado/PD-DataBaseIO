package Exceptions;

public class UnfinishedGameException extends Throwable {
    public UnfinishedGameException(String s) {
        super("O par \"" + s + "\" tem um jogo inacabado ");
    }
}
