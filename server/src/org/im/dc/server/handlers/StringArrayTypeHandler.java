package org.im.dc.server.handlers;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class StringArrayTypeHandler implements TypeHandler<String[]> {

    @Override
    public String[] getResult(ResultSet rs, String columnName) throws SQLException {
        Array arr = rs.getArray(columnName);
        return arr != null ? (String[]) arr.getArray() : null;
    }

    @Override
    public String[] getResult(ResultSet rs, int columnIndex) throws SQLException {
        Array arr = rs.getArray(columnIndex);
        return arr != null ? (String[]) arr.getArray() : null;
    }

    @Override
    public String[] getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, Types.ARRAY);
        } else {
            Array arr = ps.getConnection().createArrayOf("varchar", parameter);
            ps.setArray(i, arr);
        }
    }
}
