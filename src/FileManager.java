import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FileManager {

    private RandomAccessFile file;

    // "C:\\Users\\aantrushina\\Documents\\test.txt"

    FileManager(String path) {
        try {
            file = new RandomAccessFile(path, "rw");
        } catch (FileNotFoundException e) {
            System.out.println("error in constructor");
        }
    }



    public List<String> readByteArray(/*int numberOfBytes, int start*/) {
        List<String> bytes = new ArrayList<>();


        try {
            int b;
            while ((b = file.read()) != -1) {

                bytes.add(String.format("%02X", b));

                //System.out.println(b + " | hex: " + String.format("%02X", b));
            }

        } catch (IOException e) {
            System.out.println("error during reading array");
        }


        closeFile();

        return bytes;
    }


    public String readOneByte(int position) {
        int b = -1;

        try {
            file.seek(position);
            b = file.read();
            //System.out.println(b);
        } catch (IOException e) {
            System.out.println("error during reading byte");
        }

        return String.format("%02X", b);
    }


    public void writeOneByte(int position, byte b) {
        try {
            file.seek(position);
            file.writeByte(b);
//            file.writeUTF(b);
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("error during writing byte");
        }
    }


    public long getFileSize() {
        try {
            return file.length();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    public void closeFile() {
        try {
            file.close();
        } catch (IOException e) {
            //throw new RuntimeException(e);
            System.out.println("error during closing file");
        }
    }
}
