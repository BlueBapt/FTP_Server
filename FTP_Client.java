//C:\Users\Baptiste\Desktop\FTP_Server\stockage

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.ArrayList;
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

	public ArrayList<String> recevoirListeFichier(){
		boolean termine=false;
		DatagramPacket reponse;
		ArrayList<String> res=new ArrayList<>();
		try{
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
				res.add(nomFichier);

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

		}catch(Exception e){
			e.printStackTrace();
		}
		return res;
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
				if(fos.available()<1000) {
					tampon = new byte [fos.available()] ;
					termine=true;
					System.out.println("derniere fois qu'on envoit");
				}else {
					tampon = new byte [1000] ;
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

	public boolean recevoirFichier(String filename){
		try {
			filename=filename.trim();
			FileOutputStream fos = new FileOutputStream(filename);
			System.out.println("fichier a recevoir : "+filename);

			boolean termine =false;
			DatagramPacket paquet,reponse;
			
			while(!termine) {
		        byte [] tampon = new byte [1000] ;
		        paquet =new DatagramPacket ( tampon , tampon.length ) ;

				System.out.println("pret a recevoir le paquet");
		        sock.receive(paquet);
				System.out.println("paquet recu");
		        while(!verifierConnexion(paquet)) {
					byte[] rep = new String("WAI").getBytes();
					reponse=new DatagramPacket(rep,rep.length,paquet.getAddress(),paquet.getPort());
					sock.send(reponse);
					sock.receive(paquet);
				}
		        fos.write(paquet.getData());
		            
		        
		        byte[] etat = new byte[3];
		        paquet = new DatagramPacket ( etat , etat.length ) ;
		        
				System.out.println("avant etat");
		        sock.receive(paquet);
				System.out.println("apres etat");
		        while(!verifierConnexion(paquet)) {
					byte[] rep = new String("WAI").getBytes();
					reponse=new DatagramPacket(rep,rep.length,paquet.getAddress(),paquet.getPort());
					sock.send(reponse);
					sock.receive(paquet);
				}
		        
		        String res = new String(paquet.getData()).trim();
				System.out.println("envois reponse");
		        if(res.equals("STO")) { //STOP
		        	termine=true;
		        	envoyerMessageCourt("TER");
		        }else if(res.equals("CON")){
		        	envoyerMessageCourt("GO!");
		        }
				System.out.println("apres envois repose");
		        
			}
	        
			fos.close();
			System.out.println("fichier recu");
	        
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
			int i;
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
						envoyerMessageCourt("LIS");
						ArrayList<String> liste =recevoirListeFichier();
						i=1;
						System.out.println("Affichage des fichiers :");
						for(String f : liste){
							System.out.println(i+"- "+f);
							i++;
						}
						cbon=true;
						// A faire
						break;

					case "3":
						envoyerMessageCourt("REC");
						ArrayList<String> liste2 =recevoirListeFichier();
						i=1;
						System.out.println("Affichage des fichiers :");
						for(String f : liste2){
							System.out.println(i+"- "+f);
							i++;
						}
						String num="";
						boolean bonneSaisie=false;
						while(!bonneSaisie){
							System.out.println("Choisissez un fichier (le numero) :");
							num =sc.nextLine();
							try{
								if(Integer.parseInt(num)<=liste2.size() && Integer.parseInt(num)>=1 ){
									bonneSaisie=true;
								}else{
									System.out.println("Le numero n'est pas bon");
								}
							}catch(Exception e){
								System.out.println("Mauvaise saisie");
							}
						}						
						
						byte[] nomFichier = liste2.get(Integer.parseInt(num)-1).getBytes();

						DatagramPacket dp =new DatagramPacket(nomFichier,nomFichier.length,adresseServer,portServer);
						sock.send(dp);
						recevoirFichier(liste2.get(Integer.parseInt(num)-1));
						
						cbon=true;
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

