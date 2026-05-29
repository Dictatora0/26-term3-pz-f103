package com.iteaj.framework.mybatis.handler;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * create time: 2020/5/2
 *
 * @author iteaj
 * @since 1.0
 */
@MappedTypes(value = {ArrayNode.class, ObjectNode.class, ContainerNode.class})
public class JacksonTypeHandler extends BaseTypeHandler<ContainerNode> {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Logger logger = LoggerFactory.getLogger(JacksonTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ContainerNode parameter, JdbcType jdbcType) throws SQLException {
        if(parameter != null) {
            try {
                String json = mapper.writeValueAsString(parameter);
                ps.setString(i, json);
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public ContainerNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        if(StrUtil.isNotBlank(string)) {
            try {
                return mapper.readValue(string, ContainerNode.class);
            } catch (IOException e) {
                logger.error("错误的json格式: {}", string, e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public ContainerNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        if(null != string) {
            try {
                return mapper.readValue(string, ContainerNode.class);
            } catch (IOException e) {
                logger.error("错误的json格式: {}", string, e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public ContainerNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
