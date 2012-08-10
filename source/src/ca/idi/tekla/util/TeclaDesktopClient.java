package ca.idi.tekla.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;

import ca.idi.tekla.TeclaApp;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class TeclaDesktopClient implements Runnable {
	
	WifiManager wifiman;
	Socket client;
	MulticastSocket multisock;
	
	InetAddress serveraddress;
	
	Object lock;
	
	DatagramPacket pack;
	
	
	
	ObjectInputStream in;
	ObjectOutputStream out;
	
	byte[] buffer;
	
	boolean locker,connectionstatus,flag;
	
	public static final int NEXT_FIELD=0x82;
	public static final int NEXT_WINDOW=0x81;
	
	public static final int PORTNUMBER=28195;
	
	
	public static final String echomessage="TeclaShield";
	
	public TeclaDesktopClient(Context context){
		
		wifiman=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		locker=connectionstatus=false;	
		
	}
	
	public void connect(){
		/*
		 * should be called for starting connection.
		 */
		if(!wifiman.isWifiEnabled()){
			return;
		}	
    	DatagramPacket packet;
    	byte[] buf=new byte[256];
    	Log.v("connection","Started");
    	locker=false;
    	Thread p=new Thread(this);
    	p.start();
    	try{
    		while(!locker);
    		
    		Log.v("connection","pack in connect="+pack);
    		
			if(pack!=null){
				Log.v("connection",new String(pack.getData()));
				serveraddress=pack.getAddress();
				Log.v("connection",""+serveraddress);
			}
			buf=echomessage.getBytes();
			
			//multisock.leaveGroup(InetAddress.getByName("225.0.0.0"));
			
			MulticastSocket multisocksend=new MulticastSocket(PORTNUMBER+1);
			
			packet=new DatagramPacket(buf,buf.length,InetAddress.getByName("226.0.0.0"),PORTNUMBER+1); 
			
			multisocksend.setSoTimeout(30000);
			
			for(int i=0;i<8;i++)
			{
			multisocksend.send(packet);
			}
			
			flag=false;
			
			
			
				client=new Socket();
							
				client.connect(new InetSocketAddress(serveraddress,PORTNUMBER+2),60000);
				
				out=new ObjectOutputStream(client.getOutputStream());
				out.flush();
				
				
				send(TeclaApp.password);
				
				
				in=new ObjectInputStream(client.getInputStream());
				
				Log.v("connection",""+out);
				
				String result=receive();
				
				Log.v("connection",""+result);
				if(result!=null && result.equals("Success")){
					
					connectionstatus=true;
				}
				
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			disconnect();
		}
    }

	 public void send(String data){
		 /*
		  * Sends data over the output stream.
		  */
	    	try{
	    		
	    		if(out != null)
	    		{
	    			Log.v("connection","password="+data);
	    			out.writeUTF(data);
	    			out.flush();
	    			
	    		}
	    	}catch (IOException e){
	    		disconnect();
	    		e.printStackTrace();
	    	}
	    }
	    
	    public String receive(){
	    	
	    	try{
	    		String data=in.readUTF();
	    		Log.v("conenction","receiving"+data);
	    		return data;
	    	  		
	    	}catch (IOException e){
	    		disconnect();
	    		e.printStackTrace();
	    		return null;
	    	}
	    	
	    }
	    
	    public boolean connectionstatus(){
	    	if(client != null){
	    		return client.isConnected();
	    	}
	    	return false;
	    }
	    
	    public void disconnect(){
	    		try {
	    			if(in != null)
	    				in.close();				
	    			if(out != null)
	    				out.close();
	    			if(client!=null)
	    				client.close();
	    			connectionstatus=false;
	    			
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	
	    	
	    }
	    
	    
	    public void search_server(){
	    	
	    	/*
	    	 *	Searches for server i.e starts the multisock to receive packet 
	    	 */
	    	
	    	new Thread(this).start();
	    }
	    
		public void run() {
		
				DatagramPacket packet;
				buffer=new byte[256];
				int count=0;
				pack =new DatagramPacket(buffer,buffer.length);
				flag=true;
				
			try {
				
				
				multisock=new MulticastSocket(PORTNUMBER);
				
				multisock.setSoTimeout(0);			
				
				multisock.joinGroup(InetAddress.getByName("225.0.0.0"));
				
				buffer=new byte[256];
				
				packet=new DatagramPacket(buffer,buffer.length);
				
				Log.v("connection","pack="+pack);
				
				/*
				 * Receive server broadcast to obtain server's IP address.
				 */
				
				multisock.receive(packet);
				
				pack=new DatagramPacket(packet.getData(),packet.getData().length);
				
				pack.setAddress(packet.getAddress());
				
				
				Log.v("connection","pack="+new String(pack.getData()));
				
				multisock.setSoTimeout(30000);
				
				locker=true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			while(flag){ 
				
				try {
					buffer=new byte[256];
					
					packet=new DatagramPacket(buffer,buffer.length);
					
					multisock.receive(packet);
					
					pack=new DatagramPacket(packet.getData(),packet.getData().length);
					
					pack.setAddress(packet.getAddress());
					count++;
					
					Log.v("connection","count="+count);
					
				} catch (IOException e) { 
					// TODO Auto-generated catch block
					e.printStackTrace();
					multisock.close();
				}
				
				
			}
			multisock.close();
		}

		public boolean isConnected(){
			return connectionstatus;
		}
		public void send_dictation_data(String text){
			send("dictate:"+text);
		}
		public void send_switch_event(byte b){
			send("command:"+b);
		}
		public void send_keypress_event(int keycode){
			send("keypress:"+keycode);
		}
		
		
}


