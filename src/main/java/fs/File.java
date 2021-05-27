package fs;

import fs.exceptions.CanNotCloseFileException;
import fs.exceptions.CanNotReadFileException;
import java.util.function.Consumer;

public class File {
  private int fd;
  private LowLevelFileSystem fs;

  public File(int fd, LowLevelFileSystem fs) {
    this.fd = fd;
    this.fs = fs;
  }

  public void closeFile() {
    if (this.fd != -1) {
      this.fs.closeFile(this.fd);
      this.fd = -1; //TODO: usar setter para el test y borrar esto.
    } else {
      throw new CanNotCloseFileException("The file is closed or doesn't exist.");
    }
  }

  public int syncReadFile(Buffer buffer) {
    int readSize = this.fs.syncReadFile(
        this.fd, buffer.getBytes(), buffer.getStart(), buffer.getEnd());
    if (readSize == -1) {
      throw new CanNotReadFileException("Couldn't read the file with fd=" + fd);
    }
    buffer.limit(readSize);
    buffer.verifyReadSize(readSize);
    return readSize;
  }

  public void syncWriteFile(Buffer buffer) {
    this.fs.syncWriteFile(this.fd, buffer.getBytes(), buffer.getStart(), buffer.getEnd());
  }

  public void asyncReadFile(Buffer buffer, Consumer<Buffer> callback) {
    this.fs.asyncReadFile(this.fd,
        buffer.getBytes(), buffer.getStart(), buffer.getEnd(),
        readSize -> {
          buffer.verifyReadSize(readSize);
          callback.accept(buffer);
        });
  }

  public void asyncWriteFile(Buffer buffer, Runnable callback) {
    this.fs.asyncWriteFile(this.fd,
        buffer.getBytes(), buffer.getStart(), buffer.getEnd(),
        callback);
  }

  public int getDescriptor() {
    return fd;
  }
}
