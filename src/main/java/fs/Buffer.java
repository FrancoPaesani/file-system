package fs;

public class Buffer {
  private byte[] bytes;
  private int start;
  private int end;

  public Buffer(int size) {
    if (size < 0) {
      throw new RuntimeException("The size to read must be greater or equal to zero.");
    }
    this.start = 0;
    this.end = size - 1;
    this.bytes = new byte[size];
  }

  public void verifyReadSize(int readSize) {
    if (readSize > end - start + 1) {
      throw new RuntimeException("Can't read more than the buffer size.");
    }
  }

  public void verifyBufferSize() {
    verifyReadSize(getCurrentSize());
  }

  public void limit(int read) {
    this.end = read - 1;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public int getCurrentSize() {
    return end + 1;
  }

  public int getMaxSize() {
    return bytes.length;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }
}
