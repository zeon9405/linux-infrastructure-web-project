import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.net.URLDecoder;
import java.sql.PreparedStatement;

public class AppServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(
            new InetSocketAddress("127.0.0.1", 8081), 0
        );

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String response;
            int status = 200;

            if (path.equals("/hello")) {
                response = "Hello from Java Application!\n";
            } else if (path.equals("/")) {
                response = "Java Application Server is working.\n";
            } else if (path.equals("/db")) {
                try {
                    String password = System.getenv("DB_PASSWORD");

                    if (password == null || password.isEmpty()) {
                        throw new IllegalStateException(
                            "DB_PASSWORD is not configured"
                        );
                    }

                    String url =
                        "jdbc:mariadb://localhost:3306/infra_db";

                    try (
                        Connection conn = DriverManager.getConnection(
                            url, "infra_app", password
                        );
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(
                            "SELECT id, message FROM test_table ORDER BY id"
                        )
                    ) {
                        StringBuilder result = new StringBuilder();

                        while (rs.next()) {
                            result.append(rs.getInt("id"))
                                  .append(": ")
                                  .append(rs.getString("message"))
                                  .append("\n");
                        }

                        response = result.toString();
                    }
                } catch (Exception e) {
                    System.err.println(
                        "Database request failed: " + e.getMessage()
                    );
                    response = "Database request failed\n";
                    status = 500;
                }
            } else if (path.equals("/messages")) {
                if (!exchange.getRequestMethod().equals("POST")) {
                    response = "Method Not Allowed\n";
                    status = 405;
                } else {
                    try {
                        String contentType =
                            exchange.getRequestHeaders()
                                    .getFirst("Content-Type");

                        if (contentType == null ||
                            !contentType.startsWith(
                                "application/x-www-form-urlencoded"
                            )) {
                            response = "Unsupported Content-Type\n";
                            status = 415;
                        } else {
                            String requestBody = new String(
                                exchange.getRequestBody().readAllBytes(),
                                StandardCharsets.UTF_8
                            );

                            String message = null;

                            for (String pair : requestBody.split("&")) {
                                String[] parts = pair.split("=", 2);

                                String key = URLDecoder.decode(
                                    parts[0], StandardCharsets.UTF_8
                                );

                                if (key.equals("message")) {
                                    message = parts.length > 1
                                        ? URLDecoder.decode(
                                            parts[1],
                                            StandardCharsets.UTF_8
                                        )
                                        : "";
                                    break;
                                }
                            }

                            if (message == null || message.isBlank()) {
                                response = "message is required\n";
                                status = 400;
                            } else if (message.length() > 100) {
                                response = "message is too long\n";
                                status = 400;
                            } else {
                                String password =
                                    System.getenv("DB_PASSWORD");

                                if (password == null ||
                                    password.isEmpty()) {
                                    throw new IllegalStateException(
                                        "DB_PASSWORD is not configured"
                                    );
                                }

                                String url =
                                    "jdbc:mariadb://localhost:3306/infra_db";

                                try (
                                    Connection conn =
                                        DriverManager.getConnection(
                                            url, "infra_app", password
                                        );
                                    PreparedStatement stmt =
                                        conn.prepareStatement(
                                            "INSERT INTO test_table (message) VALUES (?)"
                                        )
                                ) {
                                    stmt.setString(1, message);
                                    stmt.executeUpdate();
                                }

                                response = "Message created\n";
                                status = 201;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println(
                            "Message creation failed: " + e.getMessage()
                        );
                        response = "Message creation failed\n";
                        status = 500;
                    }
                }
            } else {
                response = "Not Found\n";
                status = 404;
            }

            byte[] body = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                "Content-Type", "text/plain; charset=UTF-8"
            );
            exchange.sendResponseHeaders(status, body.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.start();

        System.out.println(
            "Java HTTP Server listening on 127.0.0.1:8081"
        );
    }
}
