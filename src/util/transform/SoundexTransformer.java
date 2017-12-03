package util.transform;

import org.apache.commons.codec.language.Soundex;

public class SoundexTransformer extends ApacheCodecTransformer {
    public SoundexTransformer(NgramTransformer transformer) {
        super(transformer, new Soundex());
    }
}
