package util.window;

import java.lang.Math;

public class ComprehensiveStringWindow extends StringWindow {
    private int left;
    private int right;
    private int length;
    private int minimum;
    private int maximum;

    private String string;

    public ComprehensiveStringWindow(String string,
                                     int minimum,
                                     int maximum) {
        this.string = string;
        this.minimum = Math.max(0, minimum);
        this.maximum = Math.min(maximum, string.length());

        left = 0;
        right = minimum;
        length = string.length();
    }

    public ComprehensiveStringWindow(String string, int minimum) {
        this(string, minimum, string.length() - 1);
    }

    public ComprehensiveStringWindow(String string) {
        this(string, 1);
    }

    public boolean hasNext() {
        return maximum > 0 && left + minimum <= length;
    }

    public String next() {
        String sub = string.substring(left, right);
        right++;

        if (right > length ||
            right - left > maximum ||
            right == length && left == 0) {
            left++;
            right = left + minimum;
        }

        return sub;
    }
}
