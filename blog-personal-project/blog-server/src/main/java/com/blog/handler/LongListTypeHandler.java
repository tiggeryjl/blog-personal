package com.blog.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@MappedTypes(List.class)
public class LongListTypeHandler extends BaseTypeHandler<List<Long>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter != null ? String.join(",", parameter.stream()
                .map(String::valueOf).collect(Collectors.toList())) : null);
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String str = rs.getString(columnName);
        if (str == null || str.isEmpty()) return new ArrayList<>();
        return Arrays.stream(str.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String str = rs.getString(columnIndex);
        if (str == null || str.isEmpty()) return new ArrayList<>();
        return Arrays.stream(str.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String str = cs.getString(columnIndex);
        if (str == null || str.isEmpty()) return new ArrayList<>();
        return Arrays.stream(str.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
