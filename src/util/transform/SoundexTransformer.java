package util.transform;

import org.apache.commons.codec.language.Soundex;

public class SoundexTransformer extends TransformDecorator {
    Soundex soundex;

    public SoundexTransformer(NgramTransformer transformer) {
        super(transformer);

        soundex = new Soundex();
    }

    public String transform(String ngram) {
        return super.transform(soundex.encode(ngram));
    }
}
