package server;
//C:\Users\Baptiste\Desktop\FTP_Server\stockage

import java.io.FileInputStream;
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
	

	
	public void initialiser(){
        try{
            sock=new DatagramSocket(5054);
            creerLienServer(InetAddress.getByName("localhost"),5069);
        }catch (Exception e){
        	e.printStackTrace();
            System.out.println("bruh");
        }
    }
	
	public void creerLienServer(InetAddress ia,int port) {
		adresseServer=ia;
		portServer=port;
	}
	
	public boolean verifierConnexion(DatagramPacket dp) {
		return (dp.getAddress().equals(adresseServer) && dp.getPort()==this.portServer);
	}
	
	
	public boolean envoyerFichier() {
		try {
			String chemin="C:/Users/El�ve/Pictures/actually.png"; // C:/Users/Baptiste/Pictures/abdellou.png
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
				System.out.println("on envois");
				if(fos.available()<70) {
					tampon = new byte [fos.available()] ;
					termine=true;
					System.out.println("derniere fois qu'on envoit");
				}else {
					tampon = new byte [70] ;
				}
				fos.read(tampon);
				
				if(termine) {
		        	etat = new String("STO").getBytes();
		        	reponse = new DatagramPacket ( etat , etat.length ,adresseServer,portServer) ;
		        	sock.send(reponse);
				}else {
					etat = new String("CON").getBytes();
		        	reponse = new DatagramPacket ( etat , etat.length ,adresseServer,portServer) ;
		        	sock.send(reponse);
				}
				
				etat = new byte[3];
				reponse =new DatagramPacket ( etat , etat.length ) ;
		        sock.receive(reponse);
				
			}
			
			
		
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	
	public boolean demarrer() {
		try {
			while (true) {
					byte[] co = new String("SYN").getBytes();
					byte[] rep;
					DatagramPacket paquet;
					
					boolean connexionReussie=false;
					while(!connexionReussie) {
						paquet =new DatagramPacket(co, 3,adresseServer,portServer);
						sock.send(paquet);
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
					
					envoyerFichier();
					System.out.println("Appuyez sur entr�e pour continuer");
					Scanner sc = new Scanner(System.in);
					sc.nextLine();
					
			}
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args) {
		FTP_Client serv = new FTP_Client();
		serv.demarrer();
	}

}

