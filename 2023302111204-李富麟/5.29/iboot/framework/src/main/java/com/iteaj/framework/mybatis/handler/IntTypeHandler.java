package com.iteaj.framework.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * create time: 2019/8/17
 *
 * @author iteaj
 * @since 1.0
 */
public class IntTypeHandler extends BaseTypeHandler<int[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, int[] parameter, JdbcType jdbcType) throws SQLException {
        String collect = Stream.of(parameter).map(String::valueOf).collect(Collectors.joining(","));
        ps.setString(i, collect);
    }

    @Override
    public int[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        if(rs.wasNull()) return new int[0];
        return Arrays.stream(string.split(",")).mapToInt(Integer::valueOf).toArray();
    }

    @Override
    public int[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        if(rs.wasNull()) return new int[0];
        return Arrays.stream(string.split(",")).mapToInt(Integer::valueOf).toArray();
    }

    @Override
    public int[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String string = cs.getString(columnIndex);
        if(cs.wasNull()) return new int[0];
        return Arrays.stream(string.split(",")).mapToInt(Integer::valueOf).toArray();
    }
}