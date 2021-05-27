package fs.exceptions;

public class BlockSizeCantBeGreaterThanBufferException extends RuntimeException {
  public BlockSizeCantBeGreaterThanBufferException(String msg) {
    super(msg);
  }
}
