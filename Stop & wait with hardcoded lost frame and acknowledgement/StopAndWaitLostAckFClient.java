
package stopandwaitlostackfclient;

import java.net.*;
import java.io.*;
import java.util.*;
 /*
Client Message format 1st message/REQUEST :  REQUESTfilenameCRLF
        Remaining messages/ACK            :  ACKcountCRLF
*/

public class StopAndWaitLostAckFClient {
public static byte[] REQUEST = new byte[] { 0x52, 0x69,0x51, 0x55, 0x45, 0x53, 0x54};//prepended in 1st message sent by client
        public static byte[] ACK = new byte[] { 0x41, 0x43, 0x4B };//prepended in all the messages except 1st message sent by client
        
        public static byte[] CRLF = new byte[] { 0x0a, 0x0d };//postpended in all the messages sent by client
        public static byte[] MESSAGE_END = { 0x45, 0x4e, 0x44, 0xa, 0xd }; //" END CRLF" used in message(received from server side) extraction
    
        public static int MESSAGE_FRONT_OFFSET = 4; //"RDTconsignment" consignment takes 1 byte;used in message(received from server side) extraction
        //public static int MESSAGE_BACK_OFFSET = 2; //"CRLF" used in message(received from server side) extraction
        public static int MESSAGE_LAST_BACK_OFFSET = 5; //"ENDCRLF" used in message(received from server side) extraction
        public static byte[] MESSAGE_START = { 0x52, 0x44, 0x54 }; // "RDT" used in message(received from server side) extraction
        public static int turn=0;
	public static void main(String[] args) {
	 
	    DatagramSocket cs = null;
		FileOutputStream fos = null;
                
		try {

	    	cs = new DatagramSocket();
	 
			byte[] rd, sd;
			
			DatagramPacket sp,rp;
                        
			int count=0;
			boolean end = false;
			
                        String consignmentHex;
                        int consignment;
			// write received data into demoText1.html
			fos = new FileOutputStream("demoPDF1.pdf");

			while(!end)
			{
			    
                            if(count==0)//REQUESTfilenameCRLF
                            {
                                
                                String ack="demoPDF.pdf";   //write filename accordingly!!!
                                sd=ack.getBytes();	
                                sd=concatenateByteArrays(REQUEST,sd,CRLF);
                            }
                            else{//ACKcountCRLF
                                 String hex=Integer.toHexString(count);
                                if(hex.length()==1)
                                    hex="0"+hex;
                                sd=hexStringToByteArray(hex);
                                
                                
                                String ack=Integer.toString(count);
                                //sd=ack.getBytes();
                                sd=concatenateByteArrays(ACK,sd,CRLF);
                            }
                            	
			    sp=new DatagramPacket(sd,sd.length,InetAddress.getByName(args[0]),Integer.parseInt(args[1]));
									  
  									  	  
                            if(!(count==5 && turn==0)){  //intentinally drop ack 5
                                cs.send(sp);
                                if(count==0)
                                    System.out.println("Sent Request");
                                
                                else
                                    System.out.println("Sent Ack #"+count);
                                }
                                else
                                    System.out.println("Forgot ack "+count);	
                            

			
                            rd=new byte[521];//bcz in server side some bytes are added along with data packet of 512+6(RDTcount CRLF)+3(END)=521 bytes.
                            rp=new DatagramPacket(rd,rd.length); 
			    cs.receive(rp);	

                            String reply=new String(rp.getData());	 
                                //System.out.println(reply);
                            byte[] replyInByte = rp.getData();
                            
                          
                            consignmentHex=byteToHex(replyInByte[MESSAGE_START.length]);
                            consignment=Integer.parseInt(consignmentHex,16);  
                            if(consignment<count)//duplicate packet checking
                            {
                                System.out.println("Duplicate packet!!!");
                                turn=1;
                                count--;
                            }
                            else
                            {
                                System.out.println("Received CONSIGNMENT #" + consignment);

                                if(matchByteSequence(replyInByte, replyInByte.length-MESSAGE_END.length , MESSAGE_END.length, MESSAGE_END))//if last consignment
                                {
                                    end = true;


                                    //fos.write(replyInByte, MESSAGE_FRONT_OFFSET, replyInByte.length-MESSAGE_FRONT_OFFSET-MESSAGE_LAST_BACK_OFFSET);

                                }
                                
                                fos.write(replyInByte, MESSAGE_FRONT_OFFSET, replyInByte.length-MESSAGE_FRONT_OFFSET-MESSAGE_LAST_BACK_OFFSET);
                            }
                            
                           
                            
                            count++;
                            }

		} catch (IOException ex) {
			System.out.println(ex.getMessage());

		} finally {

			try {
				if (fos != null)
					fos.close();
				if (cs != null)
					cs.close();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
        public static byte[] concatenateByteArrays(byte[] a, byte[] b, byte[] c) 
        {
            byte[] result = new byte[a.length + b.length + c.length]; 
            System.arraycopy(a, 0, result, 0, a.length); 
            System.arraycopy(b, 0, result, a.length, b.length);
            System.arraycopy(c, 0, result, a.length+b.length, c.length);
            //System.arraycopy(d, 0, result, a.length+b.length+c.length, d.length);
            return result;
        }
        static public boolean matchByteSequence(byte[] input, int offset, int length, byte[] ref) {
        
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

    
