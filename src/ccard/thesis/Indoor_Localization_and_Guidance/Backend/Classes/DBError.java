package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import java.io.PrintStream;

/**
 * Created by Ch on 3/4/14.
 */
public class DBError extends Exception {

    public DBError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DBError(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public void setStackTrace(StackTraceElement[] trace) {
        super.setStackTrace(trace);
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream err) {
        super.printStackTrace(err);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public Throwable getCause() {
        return super.getCause();
    }
}
