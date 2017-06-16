package util;

import java.util.Iterator;

public class ComprehensiveStringWindow extends StringWindow {
    private int left;
    private int right;
    private boolean report_self;
    
    private String string;    

    public ComprehensiveStringWindow(String string, boolean report_self) {
        this.string = string;
        this.report_self = report_self;
        
        left = 0;
        right = 1;
    }

    public ComprehensiveStringWindow(String string) {
        this(string, false);
    }

    public Iterator<String> iterator() {
        return this;
    }

    public boolean hasNext() {
        return left < string.length();
    }

    public String next() {
        String s = string.substring(left, right);

        right++;
        if (right == string.length() && left == 0 && !report_self ||
            right > string.length()) {
            left++;
            right = left + 1;
        }

        return s;
    }
}
