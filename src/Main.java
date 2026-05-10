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
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);

        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/rooms", new RoomsHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("✅ 棋牌室预约系统已启动！");
        System.out.println("测试接口：http://localhost:8888/api/hello");
        System.out.println("包厢接口：http://localhost:8888/api/rooms");
    }

    // 处理 /api/hello 请求
    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"message\": \"棋牌室预约系统API已就绪\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // 处理 /api/rooms 请求：从数据库读取包厢列表
    static class RoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Room> rooms = new ArrayList<>();
            String url = "jdbc:mysql://localhost:3306/chess_room?useSSL=false&serverTimezone=UTC";
            String user = "root";
            String password = "cdy20031219";  // 你的密码

            // 数据库连接与查询
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name, capacity, price, status FROM room")) {

                while (rs.next()) {
                    Room room = new Room(
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

            // 返回 JSON
            Gson gson = new Gson();
            String response = gson.toJson(rooms);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}