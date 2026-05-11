import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8889), 0);

        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/rooms", new RoomsHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/reserve", new ReserveHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("✅ 棋牌室预约系统已启动！");
        System.out.println("测试接口：http://localhost:8889/api/hello");
        System.out.println("包厢接口：http://localhost:8889/api/rooms");
    }

    // 统一设置 CORS 头
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    // 处理 OPTIONS 预检请求
    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            setCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    // ==================== Hello Handler ====================
    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if (handleOptions(exchange)) return;

            String response = "{\"message\": \"棋牌室预约系统API已就绪\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // ==================== Rooms Handler ====================
    static class RoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if (handleOptions(exchange)) return;

            List<Room> rooms = new ArrayList<>();
            String url = "jdbc:mysql://localhost:3306/chess_room?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            String user = "root";
            String password = "cdy20031219";

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name, capacity, price, status FROM room")) {

                while (rs.next()) {
                    Room room = new Room(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("capacity"),
                            rs.getInt("price"),
                            rs.getString("status")
                    );
                    rooms.add(room);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                String errorResponse = "{\"error\": \"Database connection failed\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, errorResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(errorResponse.getBytes());
                os.close();
                return;
            }

            Gson gson = new Gson();
            String response = gson.toJson(rooms);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // ==================== Register Handler ====================
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if (handleOptions(exchange)) return;

            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            String phone = "";
            String password = "";
            for (String pair : body.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2 && kv[0].equals("phone")) phone = kv[1];
                if (kv.length == 2 && kv[0].equals("password")) password = kv[1];
            }

            String url = "jdbc:mysql://localhost:3306/chess_room?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            String user = "root";
            String dbPassword = "cdy20031219";

            try (Connection conn = DriverManager.getConnection(url, user, dbPassword);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO user (phone, password) VALUES (?, ?)")) {
                stmt.setString(1, phone);
                stmt.setString(2, password);
                stmt.executeUpdate();
                String response = "{\"success\": true, \"message\": \"注册成功\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            } catch (SQLException e) {
                String response = "{\"success\": false, \"message\": \"手机号已存在\"}";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }
    }

    // ==================== Login Handler ====================
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if (handleOptions(exchange)) return;

            String query = exchange.getRequestURI().getQuery();
            String phone = "", password = "";
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2 && kv[0].equals("phone")) phone = kv[1];
                    if (kv.length == 2 && kv[0].equals("password")) password = kv[1];
                }
            }

            String url = "jdbc:mysql://localhost:3306/chess_room?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            String user = "root";
            String dbPassword = "cdy20031219";

            try (Connection conn = DriverManager.getConnection(url, user, dbPassword);
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT id, name, level FROM user WHERE phone = ? AND password = ?")) {
                stmt.setString(1, phone);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String response = String.format(
                            "{\"success\": true, \"userId\": %d, \"name\": \"%s\", \"level\": \"%s\"}",
                            rs.getInt("id"), rs.getString("name"), rs.getString("level"));
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                } else {
                    String response = "{\"success\": false, \"message\": \"手机号或密码错误\"}";
                    exchange.sendResponseHeaders(401, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                String response = "{\"success\": false, \"message\": \"服务器错误\"}";
                exchange.sendResponseHeaders(500, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }
    }

    // ==================== Reserve Handler ====================
    static class ReserveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if (handleOptions(exchange)) return;

            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            int userId = 0, roomId = 0;
            String startTime = "", endTime = "";
            for (String pair : body.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2 && kv[0].equals("userId")) userId = Integer.parseInt(kv[1]);
                if (kv.length == 2 && kv[0].equals("roomId")) roomId = Integer.parseInt(kv[1]);
                if (kv.length == 2 && kv[0].equals("startTime")) startTime = kv[1];
                if (kv.length == 2 && kv[0].equals("endTime")) endTime = kv[1];
            }

            System.out.println("收到预约请求: userId=" + userId + ", roomId=" + roomId + ", start=" + startTime + ", end=" + endTime);

            String url = "jdbc:mysql://localhost:3306/chess_room?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            String user = "root";
            String dbPassword = "cdy20031219";

            try (Connection conn = DriverManager.getConnection(url, user, dbPassword)) {
                // 检查用户是否存在
                try (PreparedStatement checkUser = conn.prepareStatement("SELECT id FROM user WHERE id = ?")) {
                    checkUser.setInt(1, userId);
                    if (!checkUser.executeQuery().next()) {
                        String response = "{\"success\": false, \"message\": \"用户不存在\"}";
                        exchange.sendResponseHeaders(400, response.getBytes().length);
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.getResponseBody().close();
                        return;
                    }
                }

                // 检查时间冲突
                try (PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT id FROM reservation WHERE room_id = ? AND " +
                                "((start_time <= ? AND end_time > ?) OR " +
                                "(start_time < ? AND end_time >= ?))")) {
                    checkStmt.setInt(1, roomId);
                    checkStmt.setString(2, endTime);
                    checkStmt.setString(3, startTime);
                    checkStmt.setString(4, endTime);
                    checkStmt.setString(5, startTime);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        String response = "{\"success\": false, \"message\": \"该时段已被预约\"}";
                        exchange.sendResponseHeaders(409, response.getBytes().length);
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.getResponseBody().close();
                        return;
                    }
                }

                // 插入预约
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO reservation (user_id, room_id, start_time, end_time) VALUES (?, ?, ?, ?)")) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, roomId);
                    insertStmt.setString(3, startTime);
                    insertStmt.setString(4, endTime);
                    insertStmt.executeUpdate();
                    String response = "{\"success\": true, \"message\": \"预约成功\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                String response = "{\"success\": false, \"message\": \"数据库错误: " + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }
    }
}