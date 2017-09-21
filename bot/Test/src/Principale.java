
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Principale extends JFrame {

	private static final long serialVersionUID = 1L;

	JTextArea editText;
	JButton btnChangeDir;
	SocketApp socketApp;

	public Principale() {
		super("Transfert de fichiers");

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		editText = new JTextArea();
		editText.setEditable(false);
		editText.setBackground(Color.BLACK);
		editText.setForeground(Color.GREEN);
		editText.setCaretColor(Color.GREEN);

		JScrollPane scrollPane = new JScrollPane(editText);
		scrollPane.setVerticalScrollBar(new JScrollBar(JScrollBar.VERTICAL));
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		socketApp = new SocketApp(editText);

		BufferedReader buffS;
		try {
			buffS = new BufferedReader(new FileReader("currentdir"));
			String dirLu;
			try {
				dirLu = buffS.readLine();
				socketApp.SetCurrentDir(dirLu);
			} catch (IOException e1) {
			}
		} catch (FileNotFoundException e2) {
		}

		btnChangeDir = new JButton();
		btnChangeDir.setText("Changer de répertoire...");
		btnChangeDir.addActionListener(new ChangeCurrentDirClass(this));

		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(btnChangeDir, BorderLayout.NORTH);

		this.setSize(new Dimension(640, 480));
		this.setVisible(true);

	}

	class ChangeCurrentDirClass implements ActionListener {
		Principale feuille;

		public ChangeCurrentDirClass(Principale p) {
			feuille = p;
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser chooseFile = new JFileChooser();
			chooseFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooseFile.setMultiSelectionEnabled(false);
			chooseFile.setDialogTitle("Choisir un dossier...");
			chooseFile.showOpenDialog(feuille);
			String path = chooseFile.getSelectedFile().getPath();
			feuille.socketApp.SetCurrentDir(path);
			try {
				FileOutputStream optStream = new FileOutputStream("currentdir");
				try {
					optStream.write(path.getBytes());
					optStream.close();
				} catch (IOException e1) {
				}
			} catch (FileNotFoundException e1) {
			}

		}
	}

	public static void main(String[] args) {
		Principale Fenetre = new Principale();
	}

}
