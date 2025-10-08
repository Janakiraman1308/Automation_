package org.utils;

import java.sql.*;
import java.util.*;

/**
 * Lightweight JDBC helper utilities.
 * Methods are static for easy use from tests/pages.
 */
public class databaseMethods {

    // --- Connection helpers ---

    public static Connection getConnection(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getConnectionFromEnv() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Environment variable DB_URL is required");
        }
        return getConnection(url, user, pass);
    }

    // --- Query / update helpers ---

    public static List<Map<String, Object>> executeQuery(String url, String user, String password, String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection(url, user, password)) {
            return executeQuery(conn, sql, params);
        }
    }

    public static List<Map<String, Object>> executeQuery(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return resultSetToList(rs);
            }
        }
    }

    public static int executeUpdate(String url, String user, String password, String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection(url, user, password)) {
            return executeUpdate(conn, sql, params);
        }
    }

    public static int executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        }
    }

    public static int[] executeBatch(Connection conn, String sql, List<Object[]> batchParams) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] params : batchParams) {
                setParameters(ps, params);
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }

    public static Optional<String> queryForString(Connection conn, String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = executeQuery(conn, sql, params);
        if (rows.isEmpty()) return Optional.empty();
        Object firstVal = rows.get(0).values().stream().findFirst().orElse(null);
        return Optional.ofNullable(firstVal != null ? firstVal.toString() : null);
    }

    // --- Transaction helper ---

    public static <T> T runInTransaction(Connection conn, SQLTransaction<T> action) throws Exception {
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            T result = action.apply(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
        }
    }

    // --- Utilities ---

    private static void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    public static void closeQuietly(AutoCloseable ac) {
        if (ac == null) return;
        try { ac.close(); } catch (Exception ignored) {}
    }

    // Functional interface for transaction blocks
    @FunctionalInterface
    public interface SQLTransaction<T> {
        T apply(Connection conn) throws Exception;
    }
}