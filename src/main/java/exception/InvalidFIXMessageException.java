package exception;

public class InvalidFIXMessageException extends RuntimeException {
    private final StringBuilder builder = new StringBuilder(256);

    public InvalidFIXMessageException reset() {
        builder.setLength(0);
        return this;
    }

    public InvalidFIXMessageException append(int i) {
        builder.append(i);
        return this;
    }

    public InvalidFIXMessageException append(String str) {
        builder.append(str);
        return this;
    }

    public InvalidFIXMessageException append(char c) {
        builder.append(c);
        return this;
    }

    public InvalidFIXMessageException done() {
        fillInStackTrace();
        return this;
    }

    @Override
    public String getMessage(){
        return builder.toString();
    }
}
