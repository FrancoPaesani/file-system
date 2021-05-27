package fs.exceptions;

public class CanNotReadFileException extends RuntimeException {
  public CanNotReadFileException(String msg) {
    super(msg);
  }
}
