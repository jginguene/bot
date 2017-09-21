import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class ClientProcessor implements Runnable {

	private Socket sock;

	private PrintWriter writer = null;

	private BufferedInputStream reader = null;

	public ClientProcessor(Socket pSock) {
		sock = pSock;

	}

	// Le traitement lanc� dans un thread s�par�
	public void run() {
		System.err.println("Lancement du traitement de la connexion cliente");
		boolean closeConnexion = false;
		// tant que la connexion est active, on traite les demandes

		while (!sock.isClosed()) {
			try {

				writer = new PrintWriter(sock.getOutputStream());
				reader = new BufferedInputStream(sock.getInputStream());

				// On attend la demande du client

				String response = read();

				InetSocketAddress remote = (InetSocketAddress) sock.getRemoteSocketAddress();

				// InetSocketAddress src = (InetSocketAddress) sock.getLocalSocketAddress();
				InetAddress src = sock.getInetAddress();

				// On affiche quelques infos, pour le d�buggage
				String debug = "";
				debug = "Thread : " + Thread.currentThread().getName() + ". ";
				debug += "\nDemande depuis l'adresse : " + src.getHostName() + ".";
				debug += "\nDemande de l'adresse : " + remote.getAddress().getHostAddress() + ".";
				debug += " Sur le port : " + remote.getPort() + ".\n";
				debug += "\t -> Commande re�ue : " + response + "\n";
				System.err.println("\n" + debug);

				// On traite la demande du client en fonction de la commande envoy�e

				String toSend = "Test";

				writer.write(toSend);
				writer.flush();

				if (closeConnexion) {
					System.err.println("COMMANDE CLOSE DETECTEE ! ");
					writer = null;
					reader = null;
					sock.close();
					break;
				}
			} catch (SocketException e) {

				System.err.println("LA CONNEXION A ETE INTERROMPUE ! ");

				break;

			} catch (IOException e) {

				e.printStackTrace();

			}

		}

	}

	// La m�thode que nous utilisons pour lire les r�ponses
	private String read() throws IOException {
		String response = "";
		int stream;
		byte[] b = new byte[4096];
		stream = reader.read(b);
		response = new String(b, 0, stream);
		return response;
	}

}