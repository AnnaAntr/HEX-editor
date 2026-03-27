import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.List;


public class FileManagerTest {
    private FileManager fileManager;
    private final int size = 10;

    @TempDir
    Path testDirectory;

    @BeforeEach
    void createTestFile() throws IOException {
        File testFile = testDirectory.resolve("test.dat").toFile();
        try (RandomAccessFile raf = new RandomAccessFile(testFile, "rw")) {

            for (int i = 0; i < size; i++) {
                raf.writeByte(i);
            }
        }
        fileManager = new FileManager(testFile.getPath());
    }

    @AfterEach
    void closeTestFile() {
        fileManager.closeFile();
    }

    // getFileSize ----------------------------------------------------------
    @Test
    void shouldReturnCorrectFileSize() {
        assertEquals(size, fileManager.getFileSize());
    }

    @Test
    void shouldChangeSizeAfterWriting() {
        fileManager.writeOneByte(10, (byte) 0);
        assertEquals(size + 1, fileManager.getFileSize());
    }

    @Test
    void shouldReturnZeroOnError() {
        FileManager errorFileManager = new FileManager("/test/file.txt");
        assertEquals(0, errorFileManager.getFileSize());
        errorFileManager.closeFile();
    }

    // readOneByte ------------------------------------------------------------
    @Test
    void shouldReadByteAtValidPosition() {
        assertEquals("05", fileManager.readOneByte(5));
        assertEquals("09", fileManager.readOneByte(9));
    }

    @Test
    void shouldReturnEmptyStringAtInvalidPosition() {
        assertEquals("", fileManager.readOneByte(size));
        assertEquals("", fileManager.readOneByte(-1));
    }

    @Test
    void shouldConvertByteCorrectly() {
        assertEquals("00", fileManager.readOneByte(0));
        assertEquals("09", fileManager.readOneByte(9));
    }

    // writeOneByte -----------------------------------------------------------
    @Test
    void shouldWriteByteAtPosition() {
        fileManager.writeOneByte(5, (byte) 255);
        assertEquals("FF", fileManager.readOneByte(5));
    }

    @Test
    void shouldExtendFileWhenWriting() {
        fileManager.writeOneByte(size, (byte) 127);
        assertEquals(size + 1, fileManager.getFileSize());
        assertEquals("7F", fileManager.readOneByte(10));
    }

    @Test
    void shouldOverwriteByte() {
        assertEquals("03", fileManager.readOneByte(3));
        fileManager.writeOneByte(3, (byte) 243);
        assertEquals("F3", fileManager.readOneByte(3));
    }

    // removeBytesWithShift ---------------------------------------------------
    @Test
    void shouldRemoveBytesWithShift() {
        fileManager.removeBytesWithShift(3, 6);
        assertEquals(size - 4, fileManager.getFileSize());
        assertEquals("00", fileManager.readOneByte(0));
        assertEquals("01", fileManager.readOneByte(1));
        assertEquals("02", fileManager.readOneByte(2));
        assertEquals("07", fileManager.readOneByte(3));
        assertEquals("08", fileManager.readOneByte(4));
        assertEquals("09", fileManager.readOneByte(5));
    }

    @Test
    void shouldRemoveOneByteWithShift() {
        fileManager.removeBytesWithShift(5, 5);
        assertEquals(size - 1, fileManager.getFileSize());
        assertEquals("04", fileManager.readOneByte(4));
        assertEquals("06", fileManager.readOneByte(5));
    }

    @Test
    void shouldRemoveBytesAtBeginning() {
        fileManager.removeBytesWithShift(0, 2);
        assertEquals(size - 3, fileManager.getFileSize());
        assertEquals("03", fileManager.readOneByte(0));
        assertEquals("09", fileManager.readOneByte(6));
    }

    @Test
    void shouldRemoveBytesAtEnd() {
        fileManager.removeBytesWithShift(7, 9);
        assertEquals(size - 3, fileManager.getFileSize());
        assertEquals("00", fileManager.readOneByte(0));
        assertEquals("06", fileManager.readOneByte(6));
        assertEquals("", fileManager.readOneByte(7));
    }

    // removeBytesWithZero ---------------------------------------------------
    @Test
    void shouldReplaceBytesWithZero() {
        fileManager.removeBytesWithZero(3, 6);
        assertEquals(size, fileManager.getFileSize());
        assertEquals("02", fileManager.readOneByte(2));
        assertEquals("00", fileManager.readOneByte(3));
        assertEquals("00", fileManager.readOneByte(4));
        assertEquals("00", fileManager.readOneByte(5));
        assertEquals("00", fileManager.readOneByte(6));
        assertEquals("07", fileManager.readOneByte(7));
    }

    @Test
    void shouldReplaceOneByteWithZero() {
        fileManager.removeBytesWithZero(5, 5);
        assertEquals(size, fileManager.getFileSize());
        assertEquals("00", fileManager.readOneByte(5));
    }

    // insertBytes ---------------------------------------------------------
    @Test
    void shouldInsertNullBytes() {
        fileManager.insertBytes(4, 3);
        assertEquals(size + 3, fileManager.getFileSize());
        assertEquals("00", fileManager.readOneByte(5));
        assertEquals("00", fileManager.readOneByte(6));
        assertEquals("00", fileManager.readOneByte(7));
        assertEquals("05", fileManager.readOneByte(8));
    }

    @Test
    void shouldInsertNullBytesAtBeginning() {
        fileManager.insertBytes(0, 2);
        assertEquals(size + 2, fileManager.getFileSize());
        assertEquals("00", fileManager.readOneByte(0));
        assertEquals("00", fileManager.readOneByte(1));
        assertEquals("00", fileManager.readOneByte(2));
        assertEquals("01", fileManager.readOneByte(3));
    }

    @Test
    void shouldInsertNullBytesAtEnd() {
        fileManager.insertBytes(9, 2);
        assertEquals(size + 2, fileManager.getFileSize());
        assertEquals("00", fileManager.readOneByte(10));
        assertEquals("00", fileManager.readOneByte(11));
    }

    // pasteBytesWithReplacement ----------------------------------------
    @Test
    void shouldReplaceBytesWithNew() {
        fileManager.pasteBytesWithReplacement(3, "AA BB CC");
        assertEquals(size, fileManager.getFileSize());
        assertEquals("AA", fileManager.readOneByte(3));
        assertEquals("BB", fileManager.readOneByte(4));
        assertEquals("CC", fileManager.readOneByte(5));
        assertEquals("06", fileManager.readOneByte(6));
    }

    @Test
    void shouldExtendFileWhenReplacingBeyondEnd() {
        fileManager.pasteBytesWithReplacement(12, "AA BB");
        assertEquals(14, fileManager.getFileSize());
        assertEquals("09", fileManager.readOneByte(9));
        assertEquals("00", fileManager.readOneByte(10));
        assertEquals("00", fileManager.readOneByte(11));
        assertEquals("AA", fileManager.readOneByte(12));
        assertEquals("BB", fileManager.readOneByte(13));
    }

    // pasteBytesWithShift ------------------------------------------------------------
    @Test
    void shouldPasteBytesWithShift() {
        fileManager.pasteBytesWithShift(4, "AA BB CC");
        assertEquals(size + 3, fileManager.getFileSize());
        assertEquals("AA", fileManager.readOneByte(4));
        assertEquals("BB", fileManager.readOneByte(5));
        assertEquals("CC", fileManager.readOneByte(6));
        assertEquals("04", fileManager.readOneByte(7));
        assertEquals("09", fileManager.readOneByte(12));
    }

    @Test
    void shouldPasteBytesAtBeginning() {
        fileManager.pasteBytesWithShift(0, "AA BB");
        assertEquals(size + 2, fileManager.getFileSize());
        assertEquals("AA", fileManager.readOneByte(0));
        assertEquals("BB", fileManager.readOneByte(1));
        assertEquals("00", fileManager.readOneByte(2));
    }

    @Test
    void shouldPasteBytesAtEnd() {
        fileManager.pasteBytesWithShift(9, "AA BB");
        assertEquals(size + 2, fileManager.getFileSize());
        assertEquals("AA", fileManager.readOneByte(9));
        assertEquals("BB", fileManager.readOneByte(10));
        assertEquals("09", fileManager.readOneByte(11));
    }

    // findByValue --------------------------------------------------------------------
    @Test
    void shouldFindExactMatch() {
        String[] pattern = {"05", "06"};
        List<Integer> matches = fileManager.findByValue(pattern);
        assertEquals(1, matches.size());
        assertEquals(5, (int)matches.get(0));
    }

    @Test
    void shouldHandleMaskPattern() {
        String[] pattern = {"?5", "0?"};
        List<Integer> matches = fileManager.findByValue(pattern);
        assertNotNull(matches);
    }
    @Test
    void shouldHandleDoubleMaskPattern() {
        String[] pattern = {"??"};
        List<Integer> matches = fileManager.findByValue(pattern);
        assertEquals(size, matches.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoMatches() {
        String[] pattern = {"FF", "EE"};
        List<Integer> matches = fileManager.findByValue(pattern);
        assertTrue(matches.isEmpty());
    }
}
