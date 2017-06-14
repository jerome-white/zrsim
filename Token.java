import java.lang.StringBuffer;

public class Token {
    private String ngram;
    private Location location;

    public Token(String ngram, Location location) {
        this.ngram = ngram;
        this.location = location;
    }

    public String toString() {
        String separator = ",";
        StringBuffer buffer = new StringBuffer();

        buffer.append(location.document).append(separator);
        buffer.append(ngram).append(separator);        
        buffer.append(location.offset);

        return buffer.toString();
    }
}
