package util.keeper;

import java.nio.CharBuffer;

public class SentenceGateKeeper implements GateKeeper {
    private char[] stop;

    public SentenceGateKeeper() {
        stop = new char[] {
            '.',
            '?',
            '!'
        };
    }

    public boolean admit(CharBuffer ngram) {
        for (int i = 0; i < ngram.length(); i++) {
            char at = ngram.charAt(i);
            for (char c : stop) {
                if (c == at) {
                    return false;
                }
            }
        }

        return true;
    }
}
