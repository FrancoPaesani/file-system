package fs;

import fs.exceptions.CanNotOpenFileException;
import fs.exceptions.NotValidPathException;

public class HighLevelFileSystem {
  private LowLevelFileSystem fs;

  public HighLevelFileSystem(LowLevelFileSystem fs) {
    this.fs = fs;
  }

  public File openFile(String path) {
    int fd = fs.openFile(path);
    if (fd == -1) {
      throw new CanNotOpenFileException("Invalid file descriptor (fd).");
    }
    return new File(fd, this.fs);
  }

  //Lo puse acá porque asumo que no se quiere utilizar
  // la lowLevel de forma directa. Se podría hacer el new Path
  // y que el mismo Path tenga el metodo exists también.
  public boolean exists(String path) {
    return fs.exists(path);
  }

  public Path getPath(String path) {
    if (fs.exists(path)) {
      return new Path(path, this.fs);
    } else {
      throw new NotValidPathException("The path doesn't exist.");
    }
  }
}
