
package stopandwaitlostackfserver;






import java.net.*;
import java.io.*;
import java.util.*;
 
public class StopAndWaitLostAckFServer {
        
 /*
Server Message format :  RDTconsignmentdataCRLF
    Last message      :  RDTconsignmentdataENDCRLF  or  RDTconsignmentENDCRLF(in case file size is multiple of 512)
*/       
        
        
  
       
        
        public static byte[] ACK_START = { 0x41, 0x43, 0x4B };//"ACK" used in message(received from client side) extraction
        
        public static byte[] REQUEST = new byte[] { 0x52, 0x69,0x51, 0x55, 0x45, 0x53, 0x54};//"REQUEST" used in message(received from client side) extraction
        public static byte[] RDT = new byte[] { 0x52, 0x44, 0x54 };//"RDT" prepended in all messages sent from server
        //public static byte[] SEQ_H = new byte[] { 0x23 };//"#" prepended in all messages(after "RDT") sent from server
        public static byte[] END = new byte[] { 0x45, 0x4e, 0x44 };//"END" postpended in the last messages(before "CRLF") sent by server
        public static byte[] CRLF = new byte[] { 0x0a, 0x0d };//"CRLF" postpended in all the messages sent by server
        public static int turn=0;
        public static int result=0;
        public static int resendFlag=0;
        public static int prevResult=0;
        public static byte[] demo=new byte[512];
	public static void main(String[] args) {
                DatagramSocket ss = null;
		FileInputStream fis = null;
                DatagramPacket rp, sp;
		byte[] rd, sd;
                String consignmentString;
		InetAddress ip;
		int port;
                //int result=0;
                byte[] consignmentB=new byte[1];
                int consignment=0;
		try
                {
                    ss = new DatagramSocket(Integer.parseInt(args[0]));
                    System.out.println("Server is up....");


			// read file into buffer
                    
                    rd=new byte[100];
                    rp = new DatagramPacket(rd,rd.length);
				 
                    ss.receive(rp);
				 
                    // get client's consignment request from DatagramPacket
                    ip = rp.getAddress(); 
                    port =rp.getPort();
                    
                    //Request received
                    
                    
                    System.out.println("Client IP Address = " + ip);
                    System.out.println("Client port = " + port);
                    System.out.println("Received Client's request.");
                    
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
                    consignmentString="00";
                    consignment = Integer.parseInt(consignmentString);
                    //while loop for file sending and receving ack
                    while(true && result!=-1)
                    {
                        try
                        {
                            ss.setSoTimeout(3000);
                        
                            //ss.setSoTimeout(3000);
                            rd=new byte[100];
                            sd=new byte[512];
                            if(resendFlag==0){
                            result = fis.read(sd);
                            demo=sd.clone();
                            prevResult=result;
                            }
                            else
                            {
                                sd=demo.clone();
                                result=prevResult;
                                resendFlag=0;
                            }
                            if(consignment==2 && turn==0)
                            {
                                System.out.println("Packet "+consignment+" Lost");
                                turn=1;
                                
                                
                            }
                            else
                            {
                                
                                
                                //result = fis.read(sd);

                                //consignmentB=consignmentString.getBytes(); 
                                consignmentB=hexStringToByteArray(consignmentString);
                                if (result == -1) 
                                {
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
                                    //ss.setSoTimeout(3000);
                                }


                                sp=new DatagramPacket(sd,sd.length,ip,port);
                                //intentionally drop packet 2
                                    ss.send(sp);
                                    System.out.println("Sent Consignment #" + consignment);
                            }
                            rp = new DatagramPacket(rd,rd.length);

                            ss.receive(rp);

                            // get client's consignment request from DatagramPacket
                            ip = rp.getAddress(); 
                            port =rp.getPort();
                            System.out.println("Client IP Address = " + ip);
                            System.out.println("Client port = " + port);


                            //Ack of client exctraction
                            byte[] strConsignmentByte = rp.getData();
                            //consignmentString = new String(strConsignmentByte, ACK_START.length, 1);
                            consignmentString=byteToHex(strConsignmentByte[ACK_START.length]);
                            if(consignmentString.length()==1)
                                consignmentString="0"+consignmentString;
                            //consignment = Integer.parseInt(consignmentString);
                            consignment=Integer.parseInt(consignmentString,16);  
                            System.out.println("Client ACK = " + consignment);
                            rp=null;
                            sp = null;
                        
                    }

                    catch(SocketTimeoutException e)
                    {
                        if(result!=-1){
                            System.out.println("Resending packet!!!");
                            resendFlag=1;
                        }
                        else
                            System.out.println("Process ended!!!");
                        
                    }
                }       
                        
                    

                }
                catch (IOException ex) {
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
