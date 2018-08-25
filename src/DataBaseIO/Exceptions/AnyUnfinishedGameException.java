package DataBaseIO.Exceptions;


import DataBaseIO.Elements.PairDatabase;

public class AnyUnfinishedGameException extends Throwable {
    public AnyUnfinishedGameException(PairDatabase pairDataBase) {
        super("Não exitem jogos inacabados para o par \"" + pairDataBase + "\" ");
    }
}
