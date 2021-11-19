package server;
import java.net.* ; 
import java.io.* ;
import java.util.*;
public class FTP_Server{
	
	private static FTP_Server serv;
	
	private DatagramSocket sock;
	private InetAddress adresseClient;
	private int portClient;
	
	private FTP_Server() {
		initialiser();
	}
	
	public static FTP_Server getFTP_Server() {
		if(serv==null) {
			serv=new FTP_Server();
		}
		return serv;
	}
	
	public void initialiser(){
        try{
            sock=new DatagramSocket(5069);
        }catch (Exception e){
            System.out.println("bruh");
        }
    }
	
	public void creerLienClient(DatagramPacket dp) {
		adresseClient=dp.getAddress();
		portClient=dp.getPort();
		try {
			byte[] rep = new String("ACK").getBytes();
			DatagramPacket reponse = new DatagramPacket(rep,rep.length,adresseClient,portClient);
			sock.send(reponse);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean verifierConnexion(DatagramPacket dp) {
		return (dp.getAddress().equals(adresseClient) && dp.getPort()==this.portClient);
	}
	
	
	public boolean recevoirFichier(){
		
		try {
			boolean termine=false;
			
			byte[] nomb = new byte[70];
			
			DatagramPacket paquet =new DatagramPacket (nomb , nomb.length );
			DatagramPacket reponse;
	        
			sock.receive(paquet);
			while(!verifierConnexion(paquet)) {
				byte[] rep = new String("WAI").getBytes();
				reponse=new DatagramPacket(rep,rep.length,paquet.getAddress(),paquet.getPort());
				sock.send(reponse);
				sock.receive(paquet);
			}
			
			String filename = new String(paquet.getData());

			FileOutputStream fos = new FileOutputStream(filename);
			
			while(!termine) {
		        byte [] tampon = new byte [70] ;
		        paquet =new DatagramPacket ( tampon , tampon.length ) ;

		        sock.receive(paquet);
		        while(!verifierConnexion(paquet)) {
					byte[] rep = new String("WAI").getBytes();
					reponse=new DatagramPacket(rep,rep.length,paquet.getAddress(),paquet.getPort());
					sock.send(reponse);
					sock.receive(paquet);
				}
		        fos.write(paquet.getData());
		            
		        
		        byte[] etat = new byte[3];
		        paquet = new DatagramPacket ( etat , etat.length ) ;
		        
		        sock.receive(paquet);
		        while(!verifierConnexion(paquet)) {
					byte[] rep = new String("WAI").getBytes();
					reponse=new DatagramPacket(rep,rep.length,paquet.getAddress(),paquet.getPort());
					sock.send(reponse);
					sock.receive(paquet);
				}
		        
		        String res = new String(paquet.getData());
		        if(res.equals("STO")) { //STOP
		        	termine=true;
		        	etat = new String("TER").getBytes();
		        	reponse = new DatagramPacket ( etat , etat.length ,adresseClient,portClient) ;
		        	sock.send(reponse);
		        }else {
		        	etat = new String("GO!").getBytes();
		        	reponse = new DatagramPacket ( etat , etat.length ,adresseClient,portClient) ;
		        	sock.send(reponse);
		        }
		        
			}
	        
			fos.close();
	        
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
    }

}

