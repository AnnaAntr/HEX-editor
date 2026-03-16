import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private RandomAccessFile file;

    FileManager(String path) {
        try {
            file = new RandomAccessFile(path, "rw");
        }
        catch (FileNotFoundException e) {
            System.out.println("error in constructor");
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


    public void removeBytesWithShift(int start, int end) {
        List<Byte> toRewrite = new ArrayList<>();

        try {
            long fileSize = file.length();
            // записываем в массив все, что после удаляемого блока
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


    public void removeBytesWithZero(int start, int end) {
        try {
            // записываем в массив все, что после удаляемого блока
            file.seek(start);
            for (long i = start; i <= end; i++)
                file.writeByte(0);
        }
        catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("error during removing array");
            e.printStackTrace();
        }
    }


    public void insertBytes(int start, int number) {
        List<Byte> toRewrite = new ArrayList<>();

        try {
            long fileSize = file.length();
            // вставляем на следующую позицию после выделенного байта
            start += 1;

            // запоминаем все, что после выделенного байта
            file.seek(start);
            for (long i = start; i < fileSize ; i++)
                toRewrite.add(file.readByte());

            // увеличиваем размер файла на количество вставляемых байт
            file.setLength(fileSize + number);

            // начиная с позиции вставки записываем нули
            file.seek(start);
            for (long i = 0; i < number; i++)
                file.writeByte(0);

            // после нулей дописываем все, что было после выделенного байта
            for (Byte b : toRewrite)
                file.writeByte(b);
        }
        catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("error during inserting byte");
        }
    }


    public void pasteBytesWithReplacement(int start, String copiedData) {
        // парсим строку по пробелам
        String[] bytes = copiedData.split("\\s+");

        try {
            file.seek(start);

            // начиная со стартовой позиции заменяем байты на данные из буфера
            for (String b : bytes)
                file.writeByte(Integer.parseInt(b, 16));
        }
        catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("error during replacing");
        }
    }


    public void pasteBytesWithShift(int start, String copiedData) {
        // парсим строку по пробелам
        String[] bytes = copiedData.split("\\s+");
        List<Byte> toRewrite = new ArrayList<>();

        try {
            long fileSize = file.length();
            // вставляем на следующую позицию после выделенного байта
            start += 1;

            // запоминаем все, что после выделенного байта
            file.seek(start);
            for (long i = start; i < fileSize ; i++)
                toRewrite.add(file.readByte());

            // увеличиваем размер файла на количество вставляемых байт
            file.setLength(fileSize + bytes.length);

            // начиная с позиции вставки записываем вставляемые байты
            file.seek(start);
            for (String b : bytes)
                file.writeByte(Integer.parseInt(b, 16));

            // после них дописываем все, что было после позиции вставки
            for (Byte b : toRewrite)
                file.writeByte(b);
        }
        catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("error during pasting with shift");
        }
    }


    public List<Integer> findByValue(String[] pattern) {
        List<Integer> matchesIndexes = new ArrayList<>();

        try {
            for (int i = 0; i <= file.length() - pattern.length; i++) {
                boolean found = true;

                for (int j = 0; j < pattern.length; j++) {
                    file.seek(i + j);

                    String b = String.format("%02X", file.readByte());
                    String p = pattern[j];

                    // если в строке поиска есть ?
                    // ? == 1 любой символ
                    if (p.indexOf('?') != -1) {
                        // сравниваем по символам
                        if (p.charAt(0) == '?' && p.charAt(1) == '?') {
                            break;
                        }
                        else if (p.charAt(0) == '?' && (p.charAt(1) != b.charAt(1))) {
                            found = false;
                            break;
                        }
                        else if (p.charAt(1) == '?' && (p.charAt(0) != b.charAt(0))) {
                            found = false;
                            break;
                        }
                    }

                    else if (!b.equals(p)) {
                        found = false;
                        break;
                    }
                }

                if (found)
                    matchesIndexes.add(i);
            }
        }
        catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("error during search");
        }

        return matchesIndexes;
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
