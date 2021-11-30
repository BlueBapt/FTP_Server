//C:\Users\Baptiste\Desktop\FTP_Server\stockage

import java.io.FileInputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
public class FTP_Client{
	
	private DatagramSocket sock;
	private InetAddress adresseServer;
	private int portServer;
	
	public FTP_Client() {
		initialiser();
	}

	public void envoyerMessageCourt(String s){
		try{
			byte[] tmp = s.getBytes();
			DatagramPacket dp = new DatagramPacket(tmp,tmp.length,adresseServer,portServer);
			sock.send(dp);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

	
	public void initialiser(){
        try{
            sock=new DatagramSocket(5054);
            creerLienServer(InetAddress.getByName("localhost"),5069);
        }catch (Exception e){
        	e.printStackTrace();
            System.out.println("Le port est deja pris");
        }
    }
	
	public void creerLienServer(InetAddress ia,int port) {
		adresseServer=ia;
		portServer=port;
	}
	
	public boolean verifierConnexion(DatagramPacket dp) {
		return (dp.getAddress().equals(adresseServer) && dp.getPort()==this.portServer);
	}

	public void recevoirListeFichier(){
		boolean termine=false;
		DatagramPacket reponse;
		try{
			int i=1;
			while(!termine){
				byte[] tmp = new byte[257];
				DatagramPacket dp = new DatagramPacket(tmp,tmp.length);
				sock.receive(dp);
				while(!verifierConnexion(dp)) {
					tmp = new byte[257];
					dp = new DatagramPacket(tmp,tmp.length);
					sock.receive(dp);
				}

				String nomFichier = new String(dp.getData()).trim();
				System.out.println(i+"- "+nomFichier);
				i++;

				byte[] etat=new byte[3];
				dp = new DatagramPacket(etat,etat.length);
				sock.receive(dp);
				while(!verifierConnexion(dp)) {
					etat = new byte[3];
					dp = new DatagramPacket(etat,etat.length);
					sock.receive(dp);
				}

				String action = new String(dp.getData()).trim();
				if(action.equals("STO")){
					termine=true;
					envoyerMessageCourt("TER");
				}else{
					envoyerMessageCourt("GO!");
				}

			}
			System.out.println("Listage recu");

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public boolean envoyerFichier(String chemin) {
		try {
			String[] tab=chemin.split("/");
			String nom = tab[tab.length-1];
			FileInputStream fos = new FileInputStream(chemin);
			
			byte[] nomFichier = nom.getBytes();
			DatagramPacket reponse=new DatagramPacket(nomFichier,nomFichier.length,adresseServer,portServer);
			sock.send(reponse);
			
			boolean termine = false;
			byte[] etat;
			while(!termine) {
				byte [] tampon;
				if(fos.available()<200) {
					tampon = new byte [fos.available()] ;
					termine=true;
					System.out.println("derniere fois qu'on envoit");
				}else {
					tampon = new byte [200] ;
				}
				fos.read(tampon);
				DatagramPacket dp = new DatagramPacket ( tampon , tampon.length ,adresseServer,portServer) ;
				System.out.println("on envoit");
				sock.send(dp);
				System.out.println("envoye");


				if(termine) {
		        	envoyerMessageCourt("STO");
				}else {
					envoyerMessageCourt("CON");
				}
				
				System.out.println("avant reponse");
				etat = new byte[3];
				reponse =new DatagramPacket ( etat , etat.length ) ;
		        sock.receive(reponse);
				System.out.println("apresReponse");

				
			}
			
			
		
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	
	public boolean connexion() {
		try {
			byte[] rep;
			DatagramPacket paquet;
			
			boolean connexionReussie=false;
			while(!connexionReussie) {
				envoyerMessageCourt("SYN");
				rep = new byte[3];
				paquet = new DatagramPacket(rep,3);
				sock.receive(paquet);
				String res = new String(paquet.getData());
				if(res.equals("ACK")) {
					connexionReussie=true;
				}else {
					Thread.sleep(1000);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void demarrer(){
		try{
			this.connexion();
			boolean cbon = false;
			while(!cbon){
				System.out.println("Choisissez votre action :");
				System.out.println("1 - envoyer un fichier");
				System.out.println("2 - voir les fichiers sur le serveur");
				System.out.println("3 - recevoir un fichier");
				Scanner sc = new Scanner(System.in);
				String choix =sc.nextLine();
				switch(choix){
					case "1":
						System.out.println("Entrez le chemin du fichier");
						String chemin =sc.nextLine();
						System.out.println();
						try{
							File f = new File(chemin);
							if(!f.exists()){
								throw new Exception("le fichier n'existe pas");
							}
						}catch(Exception e){
							System.out.println("Erreur : le fichier n'existe pas.");
							break;
						}
						envoyerMessageCourt("ENV");

						envoyerFichier(chemin);
						cbon=true;
						break;

					case "2":
						System.out.println("Affichage des fichiers :");
						envoyerMessageCourt("LIS");
						recevoirListeFichier();
						cbon=true;
						// A faire
						break;

					case "3":
						System.out.println("Affichage des fichiers :");
						envoyerMessageCourt("REC");
						recevoirListeFichier();
						System.out.println("Choisissez un fichier :");
						//aled(sc.nextLine());
						

						cbon=true;
						// A faire
						break;

					case "95":
						//eteindre le serveur
						cbon=true;
						break;

					default:
						System.out.println("Vous n'avez pas bien saisi votre choix");
						cbon=true;
						break;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	
	public static void main(String[] args) {
		FTP_Client serv = new FTP_Client();
		serv.demarrer();
	}

}

