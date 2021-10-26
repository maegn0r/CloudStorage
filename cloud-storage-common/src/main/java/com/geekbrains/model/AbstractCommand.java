package com.geekbrains.model;

import lombok.Getter;

import java.io.Serializable;

@Getter
public abstract class AbstractCommand implements Serializable {
    protected MessageType type;
}
