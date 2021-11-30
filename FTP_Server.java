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

	public void envoyerMessageCourt(String s){
		try{
			byte[] tmp = s.getBytes();
			DatagramPacket dp = new DatagramPacket(tmp,tmp.length,adresseClient,portClient);
			sock.send(dp);
		}catch(Exception e){
			e.printStackTrace();
		}
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
            System.out.println("le port est deja pris");
			System.exit(1);
        }
    }
	
	public void creerLienClient(DatagramPacket dp) {
		adresseClient=dp.getAddress();
		portClient=dp.getPort();
		try {
			envoyerMessageCourt("ACK");
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
			
			byte[] nomb = new byte[254];
			
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
			FileOutputStream fos = new FileOutputStream("stockage/"+filename);
			System.out.println("nom du fichier recu : "+filename);
			
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


	public boolean envoyerFichier() {
		try {
			
			byte[] nomb = new byte[254];
		    DatagramPacket paquet = new DatagramPacket ( nomb , nomb.length ) ;
			DatagramPacket reponse;
		        
			System.out.println("reception nom fichier");
		    sock.receive(paquet);
			System.out.println("apres reception");
		    while(!verifierConnexion(paquet)) {
				byte[] rep = new String("WAI").getBytes();
				reponse=new DatagramPacket(rep,rep.length,paquet.getAddress(),paquet.getPort());
				sock.send(reponse);
				sock.receive(paquet);
			}

			String chemin = new String(paquet.getData()).trim();
			FileInputStream fos = new FileInputStream("stockage/"+chemin);
			
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
				DatagramPacket dp = new DatagramPacket ( tampon , tampon.length ,adresseClient,portClient) ;
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
	
	
	public boolean demarrer() {
		while(true){
			connexion();
			String instruction=recevoirChoix();
			switch(instruction){
				case "0":
					recevoirFichier();
					break;
				case "1":
					envoyerListeFichier();
					break;
				case "2":
					envoyerListeFichier();
					envoyerFichier();
					break;
				case "95":
					System.out.println("eteinte du serveur a distance...");
					System.exit(1);
				default:
					System.out.println("commande inconnue");
					break;
			}
			adresseClient=null;
		}
	}

	public void envoyerListeFichier(){
		String[] fichiers = listerStockage();
		boolean termine=false;
		int i=0;
		DatagramPacket reponse;
		try{
			while(!termine){
				byte[] tmp = fichiers[i].getBytes();
				System.out.println("envois de "+fichiers[i]);
				DatagramPacket dp = new DatagramPacket(tmp,tmp.length,adresseClient,portClient);
				sock.send(dp);

				if(i==fichiers.length-1){
					envoyerMessageCourt("STO");
					termine=true;
				}else{
					envoyerMessageCourt("CON");
				}
				i++;

				byte[] etat = new byte[3];
				dp =new DatagramPacket ( etat , etat.length ) ;
		        sock.receive(dp);
				while(!verifierConnexion(dp)) {
					byte[] rep = new String("WAI").getBytes();
					reponse=new DatagramPacket(rep,rep.length,dp.getAddress(),dp.getPort());
					sock.send(reponse);
					sock.receive(dp);
				}
			}
			System.out.println("Listage envoye");

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static String[] listerStockage(){
        try{
            File repertoire = new File("stockage");
            String liste[] = repertoire.list();
            return liste;
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }

	public String recevoirChoix(){
		try{
			byte[] tmp = new byte[3];
			DatagramPacket dp = new DatagramPacket(tmp,tmp.length);
			sock.receive(dp);
			String mess = new String(dp.getData()).trim();
			switch(mess){
				case "ENV":
					return "0";
				
				case "LIS":
					return "1";

				case "REC":
					return "2";
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public void connexion(){
		try{
			if(adresseClient==null) {
				byte[] co = new byte[3];
				DatagramPacket paquet;
				
				boolean connexionReussie=false;
				while(!connexionReussie) {
					paquet =new DatagramPacket(co, 3);
					System.out.println("En attente d'une connexion...");
					sock.receive(paquet);
					String res = new String(paquet.getData()).trim();
					if(res.equals("SYN")) {
						creerLienClient(paquet);
						connexionReussie=true;
						System.out.println("connexion reussie");
					}else {
						co = new String("NON").getBytes();
						paquet =new DatagramPacket(co, 3,paquet.getAddress(),paquet.getPort());
						sock.send(paquet);
						co = new byte[3];
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FTP_Server serv = getFTP_Server();
		serv.demarrer();

	}

}

