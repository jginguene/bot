
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Vector;

import javax.swing.JTextArea;

public class SocketApp {

	public String CurrentDir; // Dossier racine a afficher
	public Vector<Socket> vectSocket; // Les clients
	public ServerSocket servSocket; // Mon serveur
	protected JTextArea texteOut; // Sortie

	public SocketApp(JTextArea texte) {
		vectSocket = new Vector<Socket>();
		texteOut = texte;

		try {
			Print("Ouverture sur le port 8082...");
			servSocket = new ServerSocket(8082);
			Print("Serveur en écoute...");
		} catch (IOException e) {
			Print("Echec de l'ouverture...");
			return;
		}
		ListenServerSocket listen = new ListenServerSocket();
		listen.start();

	}

	public void SetCurrentDir(String dir) {
		CurrentDir = dir;
		if (CurrentDir.isEmpty() || CurrentDir.charAt(CurrentDir.length() - 1) != '/')
			CurrentDir += "/";
		Print("Répertoire changé : " + dir);
	}

	private String TailleToString(long taille) {
		String retour = new String();
		if (taille < 1024)
			retour = new String(taille + " o");
		else if (taille < 1024 * 1024)
			retour = new String(String.valueOf((double) taille / 1024.0) + " Ko");
		else if (taille < 1024 * 1024 * 1024)
			retour = new String(String.valueOf((double) taille / (1024.0 * 1024.0)) + " Mo");
		else if (taille < 1024 * 1024 * 1024 * 1024)
			retour = new String(String.valueOf((double) taille / (1024.0 * 1024.0 * 1024.0)) + " Go");
		if (retour.length() > 8)
			retour = retour.substring(0, 5) + retour.substring(retour.length() - 3);
		return retour;
	}

	private synchronized void Print(String monTexte) {
		texteOut.append(monTexte + "\n");
		texteOut.setCaretPosition(texteOut.getText().length());
	}

	class ListenServerSocket extends Thread {

		public void run() {
			while (true) {
				try {
					Print("En attente de clients...");
					Socket sock = servSocket.accept();
					Print("Client détécté. Ip : " + sock.getInetAddress().getHostAddress()
							+ " - Ajout à la liste.");
					vectSocket.add(sock);
					sock = null;
					ActionSocket action = new ActionSocket(vectSocket.size() - 1);
					action.start();
				} catch (IOException e) {
					Print("L'application n'a pas pu attendre, un problème est survenu...");
				}
			}
		}
	}

	class ActionSocket extends Thread {
		InputStream inputS;
		OutputStream outputS;
		int n;
		Socket s;

		public ActionSocket(int numSocket) throws IOException {
			n = numSocket;
			s = vectSocket.get(n);
			inputS = s.getInputStream();
			outputS = s.getOutputStream();
		}

		public void run() {
			String adressS = s.getInetAddress().getHostAddress();

			try {
				Print("Client " + adressS + " : en attente d'une requête...");
				while (inputS.available() == 0) // On attends des données
				{
				}

				// Extraction du corps de la requete
				byte[] tblBytes = new byte[inputS.available() + 1000];
				int lus = inputS.read(tblBytes);
				String requete = new String(tblBytes, 0, lus);

				// Extraction du lien demandé
				int posDeb = 4;
				int posFin = requete.indexOf("HTTP") - 1;
				String lien = requete.substring(posDeb, posFin);

				Print("Client " + adressS + " : demande : " + lien);

				// Transformation Url->String (on enleve les %20 et autres)
				java.net.URI url = null;
				try {
					url = new java.net.URI(lien);
				} catch (URISyntaxException e) {
				}

				// On enleve le '/' du début
				lien = url.getPath().substring(1);

				// Chemin d'acces demandé
				File file = new File(CurrentDir + lien);
				String pathfile = CurrentDir + lien;

				if (file.isDirectory()) {
					// Si c'est un répertoire on affiche le contenu sous format html

					if (pathfile.isEmpty() || pathfile.charAt(pathfile.length() - 1) != '/') {
						pathfile += "/";
						lien += "/";
						file = new File(pathfile);
					}

					Print("Client " + adressS + " : envoie une vue sur : " + file.getPath());

					String toSendSocket = new String(
							"<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><title>Vue repertoire</title>");
					toSendSocket += "<BODY BGCOLOR=\"#000000\" text=#00FF00></BODY><b>Repertoire : " + lien + "</b>"
							+ "<br><br>";

					toSendSocket += "Dossier : &nbsp;" + "<i>" + "<a href=\"" + "/" + lien + ".." + "\">" + ".."
							+ "</a>" + "</i>" + "<br>";

					String[] liste = file.list(new DossierFiltre(pathfile)); // Liste les dossiers
					for (String elem : liste) {
						File f = new File(pathfile + elem);
						System.out.println(lien);
						toSendSocket += "Dossier : &nbsp;" + "<i>" + "<a href=\"" + "/" + lien + f.getName() + "\">"
								+ f.getName() + "</a>" + "</i>" + "<br>";
					}
					toSendSocket += "<br>";

					liste = file.list(new FichierFiltre(pathfile)); // Liste les dossiers
					for (String elem : liste) {
						File f = new File(pathfile + elem);
						toSendSocket += "Fichier : &nbsp;" + "<i>" + "<a href=\"" + "/" + lien + f.getName() + "\">"
								+ f.getName() + "</a>" + "</i> - " + TailleToString(f.length()) + "<br>";
					}

					toSendSocket += "<br> Programme par Zives</head></html>";
					outputS.write(toSendSocket.getBytes()); // On envoie
				} else if (file.isFile()) {
					// C'est un fichier on transmet l'entete HTTP de la réponse + les données du
					// fichier
					Print("Client " + adressS + " : envoie fichier : " + file.getPath());

					String toSendSocket = new String("HTTP/1.1 200 OK" + "\n" + "Accept-Ranges: " + "bytes" + "\n"
							+ "Content-Length: " + file.length() + "\n" + "Connection: Close" + "\n" + "Content-Type: "
							+ "application/octet-stream" + "\n\n");
					outputS.write(toSendSocket.getBytes());
					FileInputStream fileStream = new FileInputStream(file);

					byte[] data = new byte[10240];
					int longueur = 10240;

					while (longueur == 10240) {
						longueur = fileStream.read(data);
						outputS.write(data, 0, longueur);
					}
					fileStream.close();
					Print("Client " + adressS + " : envoie fichier terminé : " + file.getPath());
				} else {
					// On sait pas ce que c'est
					Print("Client " + adressS + " : requete non valable");
					String toSendSocket = new String(
							"<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><title>Vue répertoire</title>");
					toSendSocket += "<BODY BGCOLOR=\"#000000\" text=#00FF00></BODY>La source demandée n'existe pas <br>";
					toSendSocket += "</head></html>";
					outputS.write(toSendSocket.getBytes());
				}

				// Le client devient mort
				outputS.close();
				inputS.close();
				s.close();

			} catch (IOException e) {
				Print("Client " + adressS + " : l'envoi des données n'a pu être terminé correctement.");
			}

			vectSocket.remove(n);
			Print("Client " + adressS + " : client supprimé.");

		}
	}

	class DossierFiltre implements FilenameFilter {
		String acces;

		public DossierFiltre(String chem) {
			acces = chem;
		}

		public boolean accept(File dir, String name) {
			if (!new File(acces + name).isFile())
				return true;
			return false;
		}

	}

	class FichierFiltre implements FilenameFilter {
		String acces;

		public FichierFiltre(String chem) {
			acces = chem;
		}

		public boolean accept(File dir, String name) {
			if (new File(acces + name).isFile())
				return true;
			return false;
		}

	}

}
