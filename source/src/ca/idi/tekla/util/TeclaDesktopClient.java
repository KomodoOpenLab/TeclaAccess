package ca.idi.tekla.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;



import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;
import ca.idi.tekla.TeclaVoiceInput;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TeclaDesktopClient implements Runnable {
	
	WifiManager wifiman;
	Socket client;
	MulticastSocket multisock;
	
	InetAddress serveraddress;
	
	Object lock;	
	
	DatagramPacket pack;
	
	public static int SPEECH_REQUEST_INTENT=2819;
	
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
		//disconnect_event=65;
		//dictation_event=55;
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
			else
				return;
			buf=echomessage.getBytes();
			
			//multisock.leaveGroup(InetAddress.getByName("225.0.0.0"));
			
			flag=false;
			
			
			
				client=new Socket();
							
				client.connect(new InetSocketAddress(serveraddress,PORTNUMBER+2),30000);
				
				out=new ObjectOutputStream(client.getOutputStream());
				out.flush();
								
				send(TeclaApp.password);
				
				in=new ObjectInputStream(client.getInputStream());
				
				Log.v("connection",""+out);
				
				String result=receive();
				
				Log.v("connection",""+result);
				if(result!=null && result.equals("Success")){
					
					connectionstatus=true;
					send("disevent:"+TeclaApp.disconnect_event);
					send("dictevent:"+TeclaApp.dictation_event);
				}
				
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			disconnect();
		}
    }

	 public synchronized void send(String data){
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
	    		Log.v("connection","receiving"+data);
	    		return data;
	    	  		
	    	}catch (IOException e){
	    		e.printStackTrace();
	    		return null;
	    	}
	    	
	    }
	    
	    public boolean connectionstatus(){
	    	if(client != null){
	    		return client.isConnected()&&connectionstatus;
	    	}
	    	return false;
	    }
	    
	    public void disconnect(){
	    	connectionstatus=false;
	    		try {
	    			if(in != null)
	    				in.close();				
	    			if(out != null)
	    				out.close();
	    			if(client!=null)
	    				client.close();
	    			// TODO set send to pc key to not connected state
	    			Log.v("dictation","Disconnected");
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
				if(multisock!=null &&multisock.isConnected())
					multisock.close();
			try {
				
				
				multisock=new MulticastSocket(PORTNUMBER);
				Log.v("connection","step 1 new multisock");
				multisock.setSoTimeout(40000);			
				Log.v("connection","step 2 setting timeout");
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
				
				multisock.setSoTimeout(10000);
				
				locker=true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				locker=true;
				e1.printStackTrace();
				multisock.close();
				return;
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
					break;
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
		/*
		public void startDictation(){
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
	        startActivityForResult(intent, SPEECH_REQUEST_INTENT);
		}
		public String createDialog(final ArrayList<String> list,Context context){
			String dictated=null;
			
			final Dialog dictatedialog=new Dialog(context);
			Log.v("voice","creating Dialog");
			dictatedialog.setContentView(ca.idi.tekla.R.layout.dictationdialog);
			
			ListView lv=(ListView) dictatedialog.findViewById(R.id.resultlist);
			Button nextwindow=(Button) dictatedialog.findViewById(R.id.button_next_window);
			Button nextfield=(Button) dictatedialog.findViewById(R.id.button_next_field);
			
			
			ArrayAdapter<String> listsadapter=new ArrayAdapter<String>(dictatedialog.getContext(),
					android.R.layout.simple_list_item_1,list);
			lv.setAdapter(listsadapter);
			listsadapter.notifyDataSetChanged();
			
			lv.setOnItemClickListener(new OnItemClickListener(){

				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					String Dictation=list.get((int)arg3);
					send_dictation_data(Dictation);
					dictatedialog.dismiss();
				}
				
			});
			
			nextwindow.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					send("command:"+NEXT_WINDOW);
				}
			});
			
			nextfield.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					send("command:"+NEXT_FIELD);
				}
			});
			dictatedialog.setOnDismissListener(new OnDismissListener(){

				public void onDismiss(DialogInterface arg0) {
					// TODO Auto-generated method stub
					Log.v("voice","Dismissed");
					/*synchronized(TeclaApp.dictation_lock){
					TeclaApp.dictation_lock.notify();
					}
					TeclaApp.dict_lock=false;
					((Activity)dictatedialog.getContext()).finish();
					
					
				}
				
			});
			Log.v("voice","created dialog");
			dictatedialog.show();
			return dictated;
		}/*
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data){
			if(requestCode==SPEECH_REQUEST_INTENT){
				
				if(resultCode ==RESULT_OK){
					
					ArrayList<String>list=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					
					if(list.size()>0){
						
						createDialog(list);
						
					}
					else{
						
						
					}
					
				}
				else{
					
				}
					
				
			}
			
			
		}*/
}


