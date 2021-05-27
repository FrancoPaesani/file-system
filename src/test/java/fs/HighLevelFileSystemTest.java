package fs;

import fs.exceptions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

class HighLevelFileSystemTest {

  private LowLevelFileSystem lowLevelFileSystem;
  private HighLevelFileSystem fileSystem;

  @BeforeEach
  void initFileSystem() {
    lowLevelFileSystem = mock(LowLevelFileSystem.class);
    fileSystem = new HighLevelFileSystem(lowLevelFileSystem);
  }

  @Test
  void sePuedeAbrirUnArchivo() {
    when(lowLevelFileSystem.openFile("unArchivo.txt")).thenReturn(42);
    File file = fileSystem.openFile("unArchivo.txt");
    Assertions.assertEquals(file.getDescriptor(), 42);
  }

  @Test
  void siLaAperturaFallaUnaExcepcionEsLanzada() {
    when(lowLevelFileSystem.openFile("otroArchivo.txt")).thenReturn(-1);
    Assertions.assertThrows(CanNotOpenFileException.class, () -> fileSystem.openFile("otroArchivo.txt"));
  }

  @Test
  void sePuedeLeerSincronicamenteUnArchivoCuandoNoHayNadaParaLeer() {
    Buffer buffer = new Buffer(100);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 0, 100)).thenReturn(0);

    File file = fileSystem.openFile("ejemplo.txt");
    file.syncReadFile(buffer);

    Assertions.assertEquals(0, buffer.getStart());
    Assertions.assertEquals(-1, buffer.getEnd());
    Assertions.assertEquals(0, buffer.getCurrentSize());
  }

  @Test
  void sePuedeLeerSincronicamenteUnArchivoCuandoHayAlgoParaLeer() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 0, 9)).thenAnswer(invocation -> {
      Arrays.fill(buffer.getBytes(), 0, 4, (byte) 3);
      return 4;
    });

    File file = fileSystem.openFile("ejemplo.txt");
    file.syncReadFile(buffer);

    Assertions.assertEquals(0, buffer.getStart());
    Assertions.assertEquals(3, buffer.getEnd());
    Assertions.assertEquals(4, buffer.getCurrentSize());
    Assertions.assertArrayEquals(buffer.getBytes(), new byte[]{3, 3, 3, 3, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void leerTresCamposDeUnArchivoYEscriboEsosCamposMasUnBloqueEnOtro() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 0, 4)).thenAnswer(invocation -> {
      Arrays.fill(buffer.getBytes(), 0, 4, (byte) 0);
      return 4;
    });
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 4, 5)).thenAnswer(invocation -> {
      Arrays.fill(buffer.getBytes(), 4, 5, (byte) 1);
      return 1;
    });
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 5, 10)).thenAnswer(invocation -> {
      Arrays.fill(buffer.getBytes(), 5, 10, (byte) 2);
      return 4;
    });

    File file = fileSystem.openFile("ejemplo.txt");
    file.syncReadFileWithSize(buffer,0,4);
    file.syncReadFileWithSize(buffer,4,5);
    file.syncReadFileWithSize(buffer,5,10);
    File fileToWrite = fileSystem.openFile("aEscribir.txt");
    file.syncWriteFileBlock(buffer,0,4);

    Buffer threeBytesBuffer = new Buffer(3);
    file.syncWriteFileBlock(threeBytesBuffer,0,3);
    file.syncWriteFileBlock(buffer,4,5);
    file.syncWriteFileBlock(buffer,5,10);

    verify(lowLevelFileSystem,times(4)).syncWriteFile(anyInt(),any(),anyInt(),anyInt());
  }

  @Test
  void siLaLecturaSincronicaFallaUnaExcepciónEsLanzada() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("archivoMalito.txt")).thenReturn(13);
    when(lowLevelFileSystem.syncReadFile(anyInt(), any(), anyInt(), anyInt())).thenReturn(-1);

    File file = fileSystem.openFile("archivoMalito.txt");

    Assertions.assertThrows(CanNotReadFileException.class, () -> file.syncReadFile(buffer));
  }

  @Test
  void sePuedeEscribirSincronicamenteUnArchivoCuandoNoHayNadaParaEscribir() { //el buffer se vacía? queda igual?
    Buffer buffer = new Buffer(0);

    when(lowLevelFileSystem.openFile("escribir.txt")).thenReturn(68);

    File file = fileSystem.openFile("escribir.txt");
    file.syncWriteFile(buffer);
    verify(lowLevelFileSystem, atMostOnce()).syncWriteFile(anyInt(), any(), anyInt(), anyInt());
  }

  @Test
  void sePuedeEscribirSincronicamenteUnArchivoCuandoHayAlgoParaEscribir() {
    Buffer buffer = new Buffer(25);

    when(lowLevelFileSystem.openFile("escribirAlgo.txt")).thenReturn(2);

    File file = fileSystem.openFile("escribirAlgo.txt");
    file.syncWriteFile(buffer);

    verify(lowLevelFileSystem, atMostOnce()).syncWriteFile(anyInt(), any(), anyInt(), anyInt());
  }

  @Test
  void sePuedeLeerUnArchivoYEscribirloEnBloquesEnOtro() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 0, 9)).thenAnswer(invocation -> {
      Arrays.fill(buffer.getBytes(), 0, 4, (byte) 3);
      Arrays.fill(buffer.getBytes(),4,6,(byte) 5);
      Arrays.fill(buffer.getBytes(),6,10,(byte) 8);
      return 10;
    });

    File file = fileSystem.openFile("ejemplo.txt");
    file.syncReadFile(buffer);

    File fileToWrite = fileSystem.openFile("migracion.txt");
    file.syncWriteFileBufferInBlocks(buffer,5);
    verify(lowLevelFileSystem,times(2)).syncWriteFile(anyInt(),any(), anyInt(),anyInt());
  }

  @Test
  void sePuedeLeerAsincronicamenteUnArchivo() {
    Buffer buffer = new Buffer(25);

    when(lowLevelFileSystem.openFile("leerAlgoAsinc.txt")).thenReturn(10);

    File file = fileSystem.openFile("leerAlgoAsinc.txt");
    file.asyncReadFile(buffer, Mockito.mock(Consumer.class));

    verify(lowLevelFileSystem, times(1)).asyncReadFile(
        anyInt(), any(), anyInt(), anyInt(), any(Consumer.class));
  }

  @Test
  void sePuedeEscribirAsincronicamenteUnArchivo() {
    Buffer buffer = new Buffer(25);

    when(lowLevelFileSystem.openFile("escribirAlgoAsinc.txt")).thenReturn(90);

    File file = fileSystem.openFile("escribirAlgoAsinc.txt");
    file.asyncWriteFile(buffer, Mockito.mock(Runnable.class));

    verify(lowLevelFileSystem, times(1)).asyncWriteFile(
        anyInt(), any(), anyInt(), anyInt(), any(Runnable.class));
  }

  @Test
  void sePuedeCerrarUnArchivo() {
    when(lowLevelFileSystem.openFile("unArchivo.txt")).thenReturn(42);
    File file = fileSystem.openFile("unArchivo.txt");
    file.closeFile();
    verify(lowLevelFileSystem, Mockito.atMostOnce()).closeFile(anyInt());
  }

  @Test
  void noSePuedeCerrarUnArchivoConFDInvalidoODosVeces() {
    when(lowLevelFileSystem.openFile("archivoACerrar.txt")).thenReturn(40);
    File closedFile = fileSystem.openFile("archivoACerrar.txt");
    closedFile.closeFile();
    Assertions.assertThrows(CanNotCloseFileException.class, closedFile::closeFile);
  }

  @Test
  void noSePuedeEscribirEnBloquesMasGrandeQueElBuffer() {
    Buffer buffer = new Buffer(10);
    File file = fileSystem.openFile("pass.txt");
    try {
      file.syncWriteFileBufferInBlocks(buffer, 15);
    }
    catch (BlockSizeCantBeGreaterThanBufferException exception) {
      Assertions.assertEquals(exception.getClass(),BlockSizeCantBeGreaterThanBufferException.class);
    }
  }

  @Test
  void sePuedeSaberSiUnPathEsUnArchivoRegular() {
    when(lowLevelFileSystem.exists("/etc/server.c")).thenReturn(true);
    when(lowLevelFileSystem.isDirectory("/etc/server.c")).thenReturn(false);
    when(lowLevelFileSystem.isRegularFile("/etc/server.c")).thenReturn(true);
    Path path = fileSystem.getPath("/etc/server.c");
    Assertions.assertEquals(path.getType(), PathType.REGULAR_FILE);
  }

  @Test
  void sePuedeSaberSiUnPathEsUnDirectorio() {
    when(lowLevelFileSystem.exists("/etc/")).thenReturn(true);
    when(lowLevelFileSystem.isDirectory("/etc/")).thenReturn(true);
    when(lowLevelFileSystem.isRegularFile("/etc/")).thenReturn(false);
    Path path = fileSystem.getPath("/etc/");
    Assertions.assertEquals(path.getType(), PathType.DIRECTORY);
  }

  @Test
  void sePuedeSaberSiUnPathExiste() {
    when(lowLevelFileSystem.exists("/bin/nk2.0/make/")).thenReturn(true);
    Assertions.assertTrue(fileSystem.exists("/bin/nk2.0/make/"));
  }

  @Test
  void sePuedeObtenerElPathSolicitado() {
    when(lowLevelFileSystem.exists("/bin/nk2.0/make/")).thenReturn(true);
    Path path = fileSystem.getPath("/bin/nk2.0/make/");
    Assertions.assertEquals(path.getPath(), "/bin/nk2.0/make/");
  }
}
