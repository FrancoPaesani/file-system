package fs.exceptions;

public class CanNotCloseFileException extends RuntimeException {
  public CanNotCloseFileException(String msg) {
    super(msg);
  }
}
