import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class BasicServer {

	private static final String RESPONSE = "<RESPONSE_STRING>";

	static class MyHandler implements HttpHandler {

		public void handle(HttpExchange t) throws IOException {
			t.sendResponseHeaders(200, RESPONSE.length());
			OutputStream os = t.getResponseBody();
			os.write(RESPONSE.getBytes());
			os.close();
		}
	}

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}
}