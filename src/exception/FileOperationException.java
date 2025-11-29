package exception;

import java.io.IOException;

/**
 * Exception thrown when a file operation fails
 */
public class FileOperationException extends IOException {
  private String filePath;
  private String operation;

  public FileOperationException(String filePath, String operation) {
    super("File operation '" + operation + "' failed for file: " + filePath);
    this.filePath = filePath;
    this.operation = operation;
  }

  public FileOperationException(String filePath, String operation, Throwable cause) {
    super("File operation '" + operation + "' failed for file: " + filePath, cause);
    this.filePath = filePath;
    this.operation = operation;
  }

  public FileOperationException(String filePath, String operation, String message) {
    super(message);
    this.filePath = filePath;
    this.operation = operation;
  }

  public String getFilePath() {
    return filePath;
  }

  public String getOperation() {
    return operation;
  }
}