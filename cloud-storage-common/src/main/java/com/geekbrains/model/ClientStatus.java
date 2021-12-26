package com.geekbrains.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientStatus {
    private ActionType currentAction;
    private Path currentFileName;
    private long fileSize;
    private int currentPart;
    private int partsCount;
    private boolean isLogIn;

}
