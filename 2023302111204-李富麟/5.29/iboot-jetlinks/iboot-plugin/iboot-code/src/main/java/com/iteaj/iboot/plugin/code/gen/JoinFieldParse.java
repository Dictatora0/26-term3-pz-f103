package com.iteaj.iboot.plugin.code.gen;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Data
public class JoinFieldParse {

    private List<LcdField> fields; // 引用字段
    private List<LcdField> foreign; // 外键字段
    private DesignTableInfo tableInfo;

    public JoinFieldParse(DesignTableInfo tableInfo, List<LcdField> foreign, List<LcdField> fields) {
        this.fields = fields;
        this.foreign = foreign;
        this.tableInfo = tableInfo;
    }

    public String resolve() {
        char startAlias = 'a';
        Map<String, Character> tableAlias = new HashMap<>();
        tableAlias.put(this.tableInfo.getName(), startAlias);

        StringBuilder sb = new StringBuilder("select a.* from ")
                .append(this.tableInfo.getName()).append(" ").append(startAlias).append("\r\t");
        if(!CollectionUtils.isEmpty(this.fields) && !CollectionUtils.isEmpty(foreign)) {

            IntStream.range(0, foreign.size()).forEach(value -> {
                LcdField lcdField = foreign.get(value); // 外键字段

                char alias = (char) (startAlias + value + 1);
                tableAlias.put(lcdField.getTable(), alias);

                sb.append(" left join ").append(lcdField.getTable())
                        .append(" ").append(alias).append("\r\t");
            });

            Map<String, List<LcdField>> lcdFieldMap = new HashMap();
            this.fields.forEach(field -> {
                List<LcdField> lcdFields = lcdFieldMap.get(field.getTable());
                if(lcdFields == null) {
                    lcdFields = new ArrayList<>();
                }

                lcdFields.add(field);
            });

        }
        return sb.toString();
    }
}
