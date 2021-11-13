package com.geekbrains.model;

import lombok.Getter;

@Getter
public class ChangeNameCommand extends AbstractCommand {
    private String oldName;
    private String newName;

    public ChangeNameCommand(String oldName, String newName) {
        this.type = CommandType.CHANGE_NAME_COMMAND;
        this.oldName = oldName;
        this.newName = newName;
    }
}
