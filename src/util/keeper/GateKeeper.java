package util.keeper;

import java.nio.CharBuffer;

public interface GateKeeper {
    public boolean admit(CharBuffer ngram);
}
