package fs;

public class Path {
  private String path;
  private LowLevelFileSystem fs;
  private PathType type;

  public Path(String path, LowLevelFileSystem lowLevelFileSystem) {
    this.path = path;
    this.fs = lowLevelFileSystem;
    pathType(path, lowLevelFileSystem);
  }

  private void pathType(String path, LowLevelFileSystem lowLevelFileSystem) {
    if (this.fs.isDirectory(path)) {
      this.type = PathType.DIRECTORY;
    }
    if (this.fs.isRegularFile(path)) {
      this.type = PathType.REGULAR_FILE;
    }
  }

  public PathType getType() {
    return type;
  }

  public String getPath() {
    return this.path;
  }
}
