import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FileManager {

    private RandomAccessFile file;
    //private long fileSize;

    FileManager(String path) {
        try {
            file = new RandomAccessFile(path, "rw");
            //fileSize = getFileSize();
//        } catch (FileNotFoundException e) {
//            System.out.println("error in constructor");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long getFileSize() {
        try {
            return file.length();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
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
            b = file.readByte();
        }
        catch (IOException e) {
            System.out.println("error during reading byte");
        }

        return String.format("%02X", b);
    }


    public void writeOneByte(int position, byte b) {
        try {
            file.seek(position);
            file.writeByte(b);
        }
        catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("error during writing byte");
        }
    }


    public void removeOneByte(int position) {
        List<Byte> toRewrite = new ArrayList<>();

        try {
            long fileSize = file.length();
            // записываем в массив все, что после удаляемого байта
            file.seek(position + 1);
            for (long i = position + 1; i < fileSize; i++)
                toRewrite.add(file.readByte());

            // перезаписываем файл
            file.seek(position);
            for (Byte b : toRewrite)
                file.writeByte(b);

            // обрезаем файл на 1 байт
            file.setLength(fileSize - 1);
        }
        catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("error during deleting byte");
            e.printStackTrace();
        }
    }

    public  void removeByteArray(int start, int end) {
        List<Byte> toRewrite = new ArrayList<>();

        try {
            long fileSize = file.length();
            // записываем в массив все, что между началом и концом удаляемого блока
            file.seek(end + 1);
            for (long i = end + 1; i < fileSize; i++)
                toRewrite.add(file.readByte());

            // перезаписываем файл
            file.seek(start);
            for (Byte b : toRewrite)
                file.writeByte(b);

            // обрезаем файл на размер блока
            file.setLength(fileSize - (end - start + 1));
        }
        catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("error during removing array");
            e.printStackTrace();
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
