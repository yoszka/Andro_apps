package tomasz.jokiel.sqlitebackup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Environment;

class FileUtils {
    static void writeToFileOnExternalStorage(String fileName, String fileContent) throws IOException{
        BufferedWriter out = null;
        try {
            File root = Environment.getExternalStorageDirectory();
            File backupFile = new File(root, fileName);
            FileWriter filewriter = new FileWriter(backupFile);
            
            out = new BufferedWriter(filewriter);
            out.write(fileContent);
        }finally{
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException("Can't close file", e);
                }
            }
        }
    }

    static String readFromFileOnExternalStorage(String fileName) throws IOException {
        BufferedReader fileReader = null;

        try {
            File backupFile = new File(Environment.getExternalStorageDirectory().getPath()+"/"+fileName);
            FileInputStream inFile = new FileInputStream(backupFile);
            fileReader = new BufferedReader(new InputStreamReader(inFile));
            String dataRow = "";
            String buffer = "";
            while ((dataRow = fileReader.readLine()) != null) {
                buffer += dataRow + "\n";
            }
            return buffer;
        }finally{
            if(fileReader != null){
                try {
                    fileReader.close();
                } catch (IOException e) {
                    throw new RuntimeException("Can't close file", e);
                }
            }
        }
    }
}
