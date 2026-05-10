import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;  // 需要先添加这个库

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);

        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/rooms", new RoomsHandler());  // 👈 新增这一行

        server.setExecutor(null);
        server.start();
        System.out.println("✅ 棋牌室预约系统已启动！");
        System.out.println("测试接口：http://localhost:8888/api/hello");
        System.out.println("包厢接口：http://localhost:8888/api/rooms");
    }

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

    // 👇 新增：包厢列表 Handler
    static class RoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 模拟一些包厢数据（之后会从数据库读取）
            List<Room> rooms = new ArrayList<>();
            rooms.add(new Room("牡丹厅", 4, 88, "空闲"));
            rooms.add(new Room("兰花厅", 6, 128, "空闲"));
            rooms.add(new Room("竹韵厅", 8, 168, "空闲"));

            // 转换成 JSON 格式
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