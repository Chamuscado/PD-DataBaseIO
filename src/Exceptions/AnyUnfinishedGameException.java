package Exceptions;


import Elements.Pair;

public class AnyUnfinishedGameException extends Throwable {
    public AnyUnfinishedGameException(Pair pair) {
        super("Não exitem jogos inacabados para o par \"" + pair + "\" ");
    }
}
