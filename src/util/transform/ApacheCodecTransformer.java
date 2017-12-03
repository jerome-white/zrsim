package util.transform;

import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.EncoderException;

public class ApacheCodecTransformer extends TransformDecorator {
    StringEncoder apacheEncoder;

    public ApacheCodecTransformer(NgramTransformer transformer,
                                  StringEncoder apacheEncoder) {
        super(transformer);

        this.apacheEncoder = apacheEncoder;
    }

    public String transform(String ngram) {
        try {
            return super.transform(apacheEncoder.encode(ngram));
        }
        catch (EncoderException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
