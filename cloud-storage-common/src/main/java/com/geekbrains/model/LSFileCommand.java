package com.geekbrains.model;


import lombok.Getter;

import java.util.List;

@Getter
public class LSFileCommand extends AbstractCommand {
    private List<String> fileList;

    public LSFileCommand(List<String> fileList) {
        this.fileList = fileList;
        this.type = CommandType.LS_FILES;
    }
}
