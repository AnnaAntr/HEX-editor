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
        catch (FileNotFoundException e) {}
    }

    public long getFileSize() {
        try {
            if (file != null)
                return file.length();
            else
                return 0;
        }
        catch (IOException e) {
            return 0;
        }
    }


    public String readOneByte(int position) {
        int b = -1;

        try {
            if (position >= file.length() || position < 0)
                return "";

            file.seek(position);
            b = file.read();
        }
        catch (IOException e) {
            return "";
        }

        return String.format("%02X", b);
    }


    public void writeOneByte(int position, byte b) {
        try {
            file.seek(position);
            file.write(b);
        }
        catch (IOException e) {}
    }


    public void removeBytesWithShift(int start, int end) {
        List<Integer> toRewrite = new ArrayList<>();

        try {
            long fileSize = file.length();
            // записываем в массив все, что после удаляемого блока
            file.seek(end + 1);
            for (long i = end + 1; i < fileSize; i++)
                toRewrite.add(file.read());

            // перезаписываем файл
            file.seek(start);
            for (Integer b : toRewrite)
                file.write(b);

            // обрезаем файл на размер блока
            file.setLength(fileSize - (end - start + 1));
        }
        catch (IOException e) {}
    }


    public void removeBytesWithZero(int start, int end) {
        try {
            // записываем в массив все, что после удаляемого блока
            file.seek(start);
            for (long i = start; i <= end; i++)
                file.write(0);
        }
        catch (IOException e) {}
    }


    public void insertBytes(int start, int number) {
        List<Integer> toRewrite = new ArrayList<>();

        try {
            long fileSize = file.length();
            // вставляем на следующую позицию после выделенного байта
            start += 1;

            // запоминаем все, что после выделенного байта
            file.seek(start);
            for (long i = start; i < fileSize ; i++)
                toRewrite.add(file.read());

            // начиная с позиции вставки записываем нули
            file.seek(start);
            for (long i = 0; i < number; i++)
                file.write(0);

            // после нулей дописываем все, что было после выделенного байта
            for (Integer b : toRewrite)
                file.write(b);
        }
        catch (IOException e) {}
    }


    public void pasteBytesWithReplacement(int start, String copiedData) {
        // парсим строку по пробелам
        String[] bytes = copiedData.split("\\s+");

        try {
            file.seek(start);

            // начиная со стартовой позиции заменяем байты на данные из буфера
            for (String b : bytes)
                file.write(Integer.parseInt(b, 16));
        }
        catch (IOException e) {}
    }


    public void pasteBytesWithShift(int start, String copiedData) {
        // парсим строку по пробелам
        String[] bytes = copiedData.split("\\s+");
        List<Integer> toRewrite = new ArrayList<>();

        try {
            long fileSize = file.length();

            // запоминаем все, что после выделенного байта
            file.seek(start);
            for (long i = start; i < fileSize ; i++)
                toRewrite.add(file.read());

            // начиная с позиции вставки записываем вставляемые байты
            file.seek(start);
            for (String b : bytes)
                file.writeByte(Integer.parseInt(b, 16));

            // после них дописываем все, что было после позиции вставки
            for (Integer b : toRewrite)
                file.write(b);
        }
        catch (IOException e) {}
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
        catch (IOException e) {}

        return matchesIndexes;
    }


    public void closeFile() {
        try {
            if (file != null)
                file.close();
        } catch (IOException e) {}
    }
}
