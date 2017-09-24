package util.keeper;

import java.nio.CharBuffer;

public class AllAccessGateKeeper implements GateKeeper {
    public boolean admit(CharBuffer ngram) {
        return true;
    }
}
