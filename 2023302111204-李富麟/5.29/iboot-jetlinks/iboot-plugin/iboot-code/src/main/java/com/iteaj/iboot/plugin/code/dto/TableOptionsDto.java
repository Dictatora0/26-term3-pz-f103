package com.iteaj.iboot.plugin.code.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class TableOptionsDto {

    private String label;

    private String value;

    /**
     * 字段类型
     */
    private String type;

    private boolean disabled;

    private boolean isLeaf;

    private JsonNode target;

    private List<TableOptionsDto> children;

    public TableOptionsDto(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public TableOptionsDto(String value, String type, JsonNode target) {
        this.value = value;
        this.label = value;
        this.type = type;
        this.target = target;
        this.isLeaf = true;
    }

    public TableOptionsDto addChildren(String label, String value, String type) {
        if(this.children == null) {
            this.children = new ArrayList<>();
        }

        this.children.add(new TableOptionsDto(label, value).setType(type).setLeaf(true));

        return this;
    }

    public boolean getIsLeaf() {
        return isLeaf;
    }
}
