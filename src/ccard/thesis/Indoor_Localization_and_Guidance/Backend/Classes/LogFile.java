package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.content.Context;
import android.os.AsyncTask;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Ch on 4/17/14.
 */
public class LogFile {
    private static LogFile ourInstance = null;
    private Context context;
    private StringBuilder log;
    private String file;

    public static final String file_name  = "ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes.LOG";

    private AsyncTask<String,Integer,Integer> writer;

    public static LogFile getInstance(){
        if (null == ourInstance) return null;
        return ourInstance;
    }

    public static void createLog(Context context){
        if (ourInstance == null)
            ourInstance = new LogFile(context);
    }

    private LogFile(Context context) {
        this.context = context;
        log = new StringBuilder();

        File dir = context.getDir("Logs",Context.MODE_PRIVATE);
        File test = new File(dir.getAbsolutePath()+File.separator+file_name);
        file = test.getAbsolutePath();
        if (test.exists()){
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
                out.println("--------------------------\n Time: "+
                        DateFormat.getInstance().format(new Date())+
                        "\n-----------------------\n\n");
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                test.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void e(String s){
        log.append("===================\nERROR: ")
                .append(DateFormat.getInstance().format(new Date()))
                .append("\n")
                .append(s)
                .append("\nEND ERROR\n====================\n");
    }

    public void l(String s){
        log.append("Log ").append(DateFormat.getInstance().format(new Date())).append(": ")
                .append(s).append("\n");
    }

    public String getLog(){ return file; }

    public void release(){ log.setLength(0); }

    public void flushLog(){
        if (writer == null){
            writer = new writeFile().execute(file,log.toString());
            release();
        } else if (writer.getStatus() == AsyncTask.Status.FINISHED){
            writer = null;
            writer = new writeFile().execute(file,log.toString());
            release();
        }
    }

    private class writeFile extends AsyncTask<String,Integer,Integer>{


        @Override
        protected Integer doInBackground(String... params) {

            String file_name = params[0];
            String text = params[1];

            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file_name,true)));
                out.println(text);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return 1;
        }
    }
}
