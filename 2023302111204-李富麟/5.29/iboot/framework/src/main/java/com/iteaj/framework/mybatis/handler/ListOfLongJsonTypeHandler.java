package com.iteaj.framework.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@MappedTypes(value = {List.class})
public class ListOfLongJsonTypeHandler extends BaseTypeHandler<List> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List list, JdbcType jdbcType) throws SQLException {
        Object collect = list.stream()
                .filter(item -> item != null)
                .map(item -> item.toString())
                .collect(Collectors.joining(","));
        preparedStatement.setString(i, collect.toString());
    }

    @Override
    public List getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String string = resultSet.getString(s);
        if(StringUtils.hasText(string)) {
            Type rawType = getRawType();
        }

        return null;
    }

    @Override
    public List getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String string = resultSet.getString(i);
        if(StringUtils.hasText(string)) {
            Type rawType = getRawType();
        }
        return null;
    }

    @Override
    public List getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return null;
    }
}
