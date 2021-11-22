package server;
//C:\Users\Baptiste\Desktop\FTP_Server\stockage

import java.net.* ; 
import java.io.* ;
import java.util.*;
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
			String chemin="C:/Users/Baptiste/Pictures/abdellou.png";
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
				if(fos.available()<70) {
					tampon = new byte [fos.available()] ;
					termine=true;
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

}

