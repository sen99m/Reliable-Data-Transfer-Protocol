package stopandwaitnormalfserver;


import java.net.*;
import java.io.*;
import java.util.*;
 
public class StopAndWaitNormalFServer {
        
 /*
Server Message format :  RDTconsignmentdataCRLF
    Last message      :  RDTconsignmentdataENDCRLF  or  RDTconsignmentENDCRLF(in case file size is multiple of 512)
*/       
        
        
  
       
        
        public static byte[] ACK_START = { 0x41, 0x43, 0x4B };//"ACK" used in message(received from client side) extraction
        
        public static byte[] REQUEST = new byte[] { 0x52, 0x69,0x51, 0x55, 0x45, 0x53, 0x54};//"REQUEST" used in message(received from client side) extraction
        public static byte[] RDT = new byte[] { 0x52, 0x44, 0x54 };//"RDT" prepended in all messages sent from server
        
        public static byte[] END = new byte[] { 0x45, 0x4e, 0x44 };//"END" postpended in the last messages(before "CRLF") sent by server
        public static byte[] CRLF = new byte[] { 0x0a, 0x0d };//"CRLF" postpended in all the messages sent by server
        
	public static void main(String[] args) {
 
		DatagramSocket ss = null;
		FileInputStream fis = null;
		DatagramPacket rp, sp;
		byte[] rd, sd;

		InetAddress ip;
		int port;
		
		try {
			ss = new DatagramSocket(Integer.parseInt(args[0]));
			System.out.println("Server is up....");


			// read file into buffer
			//fis = new FileInputStream("demoImg.png");

			int consignment=0;
                        
                        byte[] consignmentB=new byte[1];
			
                        String consignmentString;
			int result = 0; // number of bytes read
	 
			while(true && result!=-1){
	 
				rd=new byte[100];
				sd=new byte[512];
				
				rp = new DatagramPacket(rd,rd.length);
				 
				ss.receive(rp);
				 
				// get client's consignment request from DatagramPacket
				ip = rp.getAddress(); 
				port =rp.getPort();
				System.out.println("Client IP Address = " + ip);
				System.out.println("Client port = " + port);

				
                                //Ack of client exctraction
                                byte[] strConsignmentByte = rp.getData();
                                if (!matchByteSequence(strConsignmentByte, 0 , REQUEST.length, REQUEST))//ACK
                                {
                                    
                                    
                                    //consignmentString = new String(strConsignmentByte, ACK_START.length, 1);
                                    consignmentString=byteToHex(strConsignmentByte[ACK_START.length]);
                                    if(consignmentString.length()==1)
                                    consignmentString="0"+consignmentString;
                                    
                                }
                                else//REQUEST
                                    
                                {
                                    consignmentString="00";
                                    byte[] filenameByte1 = rp.getData();
                                    byte[] filenameByte=new byte[filenameByte1.length-9];
                                    int j=0;
                                    for(int i=7;filenameByte1[i]!=0x0a;i++){
                                        filenameByte[j]=filenameByte1[i];
                                        j++;
                                    }

                                    String filename=new String(filenameByte);
                                    filename=filename.trim();

                                    fis = new FileInputStream(filename);
                                }
                                //consignment = Integer.parseInt(consignmentString);
				consignment=Integer.parseInt(consignmentString,16);  
				System.out.println("Client ACK = " + consignment);
                                
                                //consignmentB=consignmentString.getBytes();
                                
                                
                                consignmentB=hexStringToByteArray(consignmentString);
                                
                                
				result = fis.read(sd);
                                
                                if (result == -1) {
                                        //consignment = -1;
                                        
					sd = concatenateByteArrays(RDT, consignmentB, END, CRLF);//RDTconsignmentENDCRLF
					
				}
                                else if(result<512)
                                {
                                    //consignment = -1;
                                    
                                    
                                    sd = concatenateByteArrays(RDT, consignmentB, sd, END, CRLF);//RDTconsignmentdataENDCRLF
                                    
                                    result =-1;
                                            
                                }
                                else
                                {
                                    
                                    sd = concatenateByteArrays(RDT, consignmentB, sd, CRLF);//RDTconsignmentdataCRLF
                                }
                                    
				
				sp=new DatagramPacket(sd,sd.length,ip,port);
				 
				ss.send(sp);
				 
				rp=null;
				sp = null;
				 
				System.out.println("Sent Consignment #" + consignment);
                                
	 
			}
			
		} catch (IOException ex) {
			System.out.println(ex.getMessage());

		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}
		
	}
        static public boolean matchByteSequence(byte[] input, int offset, int length, byte[] ref)
        {
        
            boolean result = true;

            if (length == ref.length) {
                for (int i=0; i<ref.length; i++) {
                    if (input[offset+i] != ref[i]) {
                        result = false;
                        break;
                    }
                }
            }
            return result;
        }
        public static byte[] concatenateByteArrays(byte[] a, byte[] b, byte[] c, byte[] d) {
            byte[] result = new byte[a.length + b.length + c.length + d.length]; 
            System.arraycopy(a, 0, result, 0, a.length); 
            System.arraycopy(b, 0, result, a.length, b.length);
            System.arraycopy(c, 0, result, a.length+b.length, c.length);
            System.arraycopy(d, 0, result, a.length+b.length+c.length, d.length);
            return result;
        }

        public static byte[] concatenateByteArrays(byte[] a, byte[] b, byte[] c, byte[] d, byte[] e) {
            byte[] result = new byte[a.length + b.length + c.length + d.length + e.length]; 
            System.arraycopy(a, 0, result, 0, a.length); 
            System.arraycopy(b, 0, result, a.length, b.length);
            System.arraycopy(c, 0, result, a.length+b.length, c.length);
            System.arraycopy(d, 0, result, a.length+b.length+c.length, d.length);
            System.arraycopy(e, 0, result, a.length+b.length+c.length+d.length, e.length);
            return result;
        }
        public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));

        }


        return data;
    }
    public static String byteToHex(byte b) {
        int i = b & 0xFF;
        return Integer.toHexString(i);
    }
        
    
}


