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
			filename=filename.trim();
			FileOutputStream fos = new FileOutputStream(filename);
			System.out.println("nom du fichier recu : "+filename);
			
			while(!termine) {
		        byte [] tampon = new byte [200] ;
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
		        	etat = new String("TER").getBytes();
		        	reponse = new DatagramPacket ( etat , etat.length ,adresseClient,portClient) ;
		        	sock.send(reponse);
		        }else if(res.equals("CON")){
		        	etat = new String("GO!").getBytes();
		        	reponse = new DatagramPacket ( etat , etat.length ,adresseClient,portClient) ;
		        	sock.send(reponse);
		        }
				System.out.println("apres envois repose");
		        
			}
	        
			fos.close();
	        
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
    }
	
	
	public boolean demarrer() {
		try {
			while (true) {
				if(adresseClient==null) {
					byte[] co = new byte[3];
					DatagramPacket paquet;
					
					boolean connexionReussie=false;
					while(!connexionReussie) {
						paquet =new DatagramPacket(co, 3);
						System.out.println("en attente d'une connexion");
						sock.receive(paquet);
						String res = new String(paquet.getData()).trim();
						System.out.println(res);
						if(res.equals("SYN")) {
							creerLienClient(paquet);
							co = new String("ACK").getBytes();
							paquet =new DatagramPacket(co, 3,paquet.getAddress(),paquet.getPort());
							sock.send(paquet);
							connexionReussie=true;
							System.out.println("connexion reussie");
						}else {
							co = new String("NON").getBytes();
							paquet =new DatagramPacket(co, 3,paquet.getAddress(),paquet.getPort());
							sock.send(paquet);
							co = new byte[3];
						}
					}
					
					System.out.println("Reception du fichier");
					recevoirFichier();
					System.out.println("fichier recu");
					adresseClient=null;
					
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args) {
		FTP_Server serv = getFTP_Server();
		serv.demarrer();
	}

}
