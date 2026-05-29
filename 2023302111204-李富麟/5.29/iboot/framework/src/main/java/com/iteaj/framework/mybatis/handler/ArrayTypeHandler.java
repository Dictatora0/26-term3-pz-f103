package com.iteaj.framework.mybatis.handler;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

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
@MappedJdbcTypes(value = {JdbcType.VARCHAR, JdbcType.ARRAY})
@MappedTypes(value = {String[].class})
public class ArrayTypeHandler extends BaseTypeHandler<String[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        if(ArrayUtil.isNotEmpty(parameter)) {
            ps.setString(i, String.join(",", parameter));
        } else {
            ps.setString(i, null);
        }
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        if(StrUtil.isNotBlank(string)) {
            return string.split(",");
        }

        return new String[0];
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        if(StrUtil.isNotBlank(string)) {
            return string.split(",");
        }

        return new String[0];
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return new String[0];
    }
}
