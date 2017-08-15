package exec;

import java.io.PrintStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.lang.Iterable;
import java.lang.Runnable;
import java.lang.ThreadLocal;
import java.util.Stack;
import java.util.Iterator;

import java.util.concurrent.ThreadFactory;


public class StreamStorageThreadFactory implements ThreadFactory,
                                                   Iterable<Path> {
    public static final ThreadLocal<PrintStream> printStreamResource =
        new ThreadLocal<PrintStream>();

    private Path tmpdir;
    private Stack<Path> tmpfiles;

    public StreamStorageThreadFactory(Path tmpdir) {
        this.tmpdir = tmpdir;
        tmpfiles = new Stack<Path>();
    }

    public Thread newThread(Runnable r) {
        try {
            tmpfiles.push(Files.createTempFile(tmpdir, null, null));
            OutputStream output = Files.newOutputStream(tmpfiles.peek());

            return new Thread(r) {
                public void run() {
                    printStreamResource.set(new PrintStream(output, true));
                    super.run();
                }
            };
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Iterator<Path> iterator() {
        return tmpfiles.iterator();
    }
}
