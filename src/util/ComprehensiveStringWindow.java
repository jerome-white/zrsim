package util;

import java.util.Iterator;

public class ComprehensiveStringWindow extends StringWindow {
    private int left;
    private int right;
    private int length;
    private int minimum;
    private int maximum;
    private boolean report_self;

    private String string;

    public ComprehensiveStringWindow(String string,
				     int minimum,
				     int maximum,
				     boolean report_self) {
        this.string = string;
	this.minimum = minimum;
	this.maximum = maximum;
        this.report_self = report_self;

        left = 0;
        right = minimum;
	length = string.length();
    }

    public ComprehensiveStringWindow(String string, int minimum, int maximum) {
	this(string, minimum, maximum, false);
    }

    public ComprehensiveStringWindow(String string, int minimum) {
	this(string, minimum, string.length());
    }

    public ComprehensiveStringWindow(String string) {
        this(string, 1);
    }

    public Iterator<String> iterator() {
        return this;
    }

    public boolean hasNext() {
        return left + minimum <= length;
    }

    public String next() {
        String sub = string.substring(left, right);

        right++;
        if (right > length ||
	    right - left > maximum ||
	    right == length && left == 0 && !report_self) {
            left++;
            right = left + minimum;
        }

        return sub;
    }
}
