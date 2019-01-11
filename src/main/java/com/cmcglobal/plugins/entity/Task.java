package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.StringLength;
import org.springframework.core.annotation.Order;

import static net.java.ao.schema.StringLength.UNLIMITED;

@Preload
public interface Task extends Entity {

    @Order(value = 1)
    @StringLength(value = UNLIMITED)
    String getTypeOfWork();

    @StringLength(value = UNLIMITED)
    void setTypeOfWork(String typeOfWork);

}
