package com.geekbrains.model;

import lombok.Getter;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FileInfo implements Serializable {
    private String fileName;
    private long fileSize;

    public FileInfo(Path path) {
      try {
          this.fileName = path.getFileName().toString();
          if (Files.isDirectory(path)) {
              this.fileSize = -1L;
          } else this.fileSize = Files.size(path);
      } catch (IOException ioex){
          throw new RuntimeException("Что-то пошло не так с файлом: " + path.getFileName().toAbsolutePath());
      }
    }
}
