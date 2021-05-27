package fs.exceptions;

public class CanNotOpenFileException extends RuntimeException {
  public CanNotOpenFileException(String msg) {
    super(msg);
  }
}
