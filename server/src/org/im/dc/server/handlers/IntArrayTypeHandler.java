package org.im.dc.server.handlers;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class IntArrayTypeHandler implements TypeHandler<int[]> {

    @Override
    public int[] getResult(ResultSet rs, String columnName) throws SQLException {
        Array arr = rs.getArray(columnName);
        return arr != null ? (int[]) arr.getArray() : null;
    }

    @Override
    public int[] getResult(ResultSet rs, int columnIndex) throws SQLException {
        Array arr = rs.getArray(columnIndex);
        return arr != null ? (int[]) arr.getArray() : null;
    }

    @Override
    public int[] getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, int[] parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, Types.ARRAY);
        } else {
            Integer[] p = new Integer[parameter.length];
            for (int n = 0; n < p.length; n++) {
                p[n] = parameter[n];
            }
            Array arr = ps.getConnection().createArrayOf("int", p);
            ps.setArray(i, arr);
        }
    }
}
