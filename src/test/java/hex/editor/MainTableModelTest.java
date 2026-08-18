package hex.editor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;

import java.lang.reflect.Field;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
public class MainTableModelTest {
    private MainTableModel mainTableModel;

    @Mock
    private FileManager mockFileManager;

    @TempDir
    Path testDirectory;
    private String testPath;

    @BeforeEach
    void setUp() {
        mainTableModel = new MainTableModel();
        testPath = testDirectory.resolve("test.dat").toString();
    }

    @AfterEach
    void closeFileManager() {
        if (mainTableModel.getFileManager() != null) {
            mainTableModel.getFileManager().closeFile();
        }
    }

    @Test
    void shouldReturnVisibleRowCountWhenNoFileManager() {
        assertEquals(40, mainTableModel.calculateTotalRowCount());
    }

    @Test
    void shouldCalculateRowCountBasedOnFileSize() {
        when(mockFileManager.getFileSize()).thenReturn(100L);
        setPrivateFileManager(mainTableModel, mockFileManager);

        assertEquals(11, mainTableModel.calculateTotalRowCount());
        verify(mockFileManager).getFileSize();
    }

    @Test
    void shouldRecalculateRowCountAfterColumnCountChanged() {
        when(mockFileManager.getFileSize()).thenReturn(100L);
        setPrivateFileManager(mainTableModel, mockFileManager);

        mainTableModel.setColumnCount(15);

        assertEquals(7, mainTableModel.calculateTotalRowCount());
    }

    @Test
    void shouldSetColumnCountWithOneMoreColumn() {
        mainTableModel.setColumnCount(5);

        assertEquals(6, mainTableModel.getColumnCount());
    }

    @Test
    void shouldCreateFileManagerWithValidPath() {
        mainTableModel.setFileManager(testPath);

        FileManager fileManager = mainTableModel.getFileManager();
        assertNotNull(fileManager);
    }

    @Test
    void shouldFireTableStructureChanged() {
        MainTableModel spyModel = spy(mainTableModel);
        spyModel.setColumnCount(5);

        verify(spyModel).fireTableStructureChanged();
    }

    @Test
    void shouldSetFileManagerToNull() {
        mainTableModel.setFileManager(null);

        assertNull(mainTableModel.getFileManager());
    }

    @Test
    void shouldRecalculateRowCountAfterSettingFileManager() {
        MainTableModel spyModel = spy(mainTableModel);
        spyModel.setFileManager(testPath);

        verify(spyModel).setTotalRowCount();

        spyModel.getFileManager().closeFile();
    }

    @Test
    void shouldFireTableStructureChangedAfterSettingFileManager() {
        MainTableModel spyModel = spy(mainTableModel);
        spyModel.setFileManager(testPath);

        verify(spyModel).fireTableStructureChanged();

        spyModel.getFileManager().closeFile();
    }

    @Test
    void shouldReturnRowNumberForColumnZero() {
        Object value = mainTableModel.getValueAt(5, 0);

        assertEquals(50, value);
    }

    @Test
    void shouldCalculateRowNumberCorrectlyWithDifferentColumnCount() {
        mainTableModel.setColumnCount(11);
        mainTableModel.setFileManager(testPath);

        Object value = mainTableModel.getValueAt(3, 0);
        assertEquals(33, value);
    }

    @Test
    void shouldReturnNullWhenNoFileManager() {
        Object value = mainTableModel.getValueAt(5, 1);

        assertNull(value);
    }

    @Test
    void shouldReadFromFileWhenInVisibleRange() {
        when(mockFileManager.getFileSize()).thenReturn(100L);
        when(mockFileManager.readOneByte(15)).thenReturn("1A");

        setPrivateFileManager(mainTableModel, mockFileManager);
        mainTableModel.setFirstVisibleRow(0);
        mainTableModel.setLastVisibleRow(39);

        Object value = mainTableModel.getValueAt(1, 6);
        assertEquals("1A", value);
        verify(mockFileManager).readOneByte(15);
    }

    @Test
    void shouldReturnEmptyStringForColumnZero() {
        String name = mainTableModel.getColumnName(0);

        assertEquals("", name);
    }

    @Test
    void shouldReturnColumnNumberMinusOne() {
        assertEquals("0", mainTableModel.getColumnName(1));
        assertEquals("5", mainTableModel.getColumnName(6));
        assertEquals("9", mainTableModel.getColumnName(10));
    }

    @Test
    void shouldUpdateColumnNamesWhenColumnCountChanges() {
        mainTableModel.setColumnCount(5);

        assertEquals("", mainTableModel.getColumnName(0));
        assertEquals("0", mainTableModel.getColumnName(1));
        assertEquals("4", mainTableModel.getColumnName(5));
        assertEquals("5", mainTableModel.getColumnName(6));
    }

    @Test
    void shouldNotEditColumnZero() {
        assertFalse(mainTableModel.isCellEditable(0, 0));
        assertFalse(mainTableModel.isCellEditable(5, 0));
        assertFalse(mainTableModel.isCellEditable(10, 0));
    }

    @Test
    void shouldEditOtherColumns() {
        assertTrue(mainTableModel.isCellEditable(0, 1));
        assertTrue(mainTableModel.isCellEditable(5, 5));
        assertTrue(mainTableModel.isCellEditable(10, 10));
    }

    @Test
    void shouldNotWriteWhenNoFileManager() {
        assertDoesNotThrow(() -> mainTableModel.setValueAt("AA", 0, 1));
    }

    @Test
    void shouldParseHexValuesCorrectly() {
        setPrivateFileManager(mainTableModel, mockFileManager);

        mainTableModel.setValueAt("0A", 0, 1);
        verify(mockFileManager).writeOneByte(anyInt(), eq((byte) 0x0A));

        mainTableModel.setValueAt("FF", 0, 1);
        verify(mockFileManager).writeOneByte(anyInt(), eq((byte) 0xFF));

        mainTableModel.setValueAt("10", 0, 1);
        verify(mockFileManager).writeOneByte(anyInt(), eq((byte) 0x10));
    }

    @Test
    void shouldFireTableCellUpdated() {
        MainTableModel spyModel = spy(mainTableModel);
        setPrivateFileManager(spyModel, mockFileManager);

        spyModel.setValueAt("AA", 1, 2);

        verify(spyModel).fireTableCellUpdated(1, 2);
    }

    private void setPrivateFileManager(Object target, Object value) {
        try {
            Field field = target.getClass().getDeclaredField("fileManager");
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail("Не удалось установить приватный fileManager: " + e.getMessage());
        }
    }

}
