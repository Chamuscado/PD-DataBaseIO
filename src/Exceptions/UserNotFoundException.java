package Exceptions;

public class UserNotFoundException extends Exception {

    public UserNotFoundException(String username) {
        super("o user \"" + username + "\" não exite ");
    }
}
