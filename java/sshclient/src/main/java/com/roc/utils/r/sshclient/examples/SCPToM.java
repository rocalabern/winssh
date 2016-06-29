package com.roc.utils.r.sshclient.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class SCPToM {
	
	public static void main(String[] arg) {
		scpToRemote("localhost", "22", "ralabern", "ralabern", "/folder/test.txt", "/folder/test.txt");
	}
	
	public static void scpToRemote (String host, String port, String user, String pass, String lfile, String rfile) {
		FileInputStream fis = null;
		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, Integer.parseInt(port));
			session.setUserInfo(new UserPass(pass));
			session.connect();

			boolean ptimestamp = true;

			// exec 'scp -t rfile' remotely
			String command = "scp " + (ptimestamp ? "-p" :"") +" -t "+rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();
			channel.connect();

			if (checkAck(in) != 0) {
				return;
			}

			File _lfile = new File(lfile);

			if (ptimestamp) {
				command = "T "+(_lfile.lastModified()/1000)+" 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command += (" "+(_lfile.lastModified()/1000)+" 0\n"); 
				out.write(command.getBytes()); out.flush();
				if (checkAck(in) != 0) {
					return;
				}
			}

			// send "C0644 filesize filename", where filename should not include '/'
			long filesize = _lfile.length();
			command = "C0644 "+filesize+" ";
			if (lfile.lastIndexOf('/') > 0) {
				command += lfile.substring(lfile.lastIndexOf('/')+1);
			} else {
				command += lfile;
			}
			command += "\n";
			out.write(command.getBytes()); out.flush();
			if (checkAck(in) != 0) {
				return;
			}

			// send a content of lfile
			fis = new FileInputStream(lfile);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if(len <= 0) break;
				out.write(buf, 0, len); //out.flush();
			}
			fis.close();
			fis = null;
			// send '\0'
			buf[0] = 0; out.write(buf, 0, 1); out.flush();
			if (checkAck(in) != 0) {
				return;
			}
			out.close();

			channel.disconnect();
			session.disconnect();
		} catch(Exception e) {
			e.printStackTrace();
			try{if(fis!=null)fis.close();}catch(Exception ee){}
		}
	}

	static int checkAck(InputStream in) throws IOException{
		int b=in.read();
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if(b==0) return b;
		if(b==-1) return b;

		if(b==1 || b==2){
			StringBuffer sb=new StringBuffer();
			int c;
			do {
				c=in.read();
				sb.append((char)c);
			}
			while(c!='\n');
			if(b==1){ // error
				System.out.print(sb.toString());
			}
			if(b==2){ // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	public static class UserPass implements UserInfo, UIKeyboardInteractive {
		String pass;
		
		public UserPass(String pass) {
			this.pass = pass;
		}
		
		public String getPassword(){ return pass; }
		
		public boolean promptYesNo(String str) { System.err.println(str); return true; }
		
		public String getPassphrase(){ return null; }
		
		public boolean promptPassphrase(String message) { return true; }
		
		public boolean promptPassword(String message) { System.err.println(message); return true; }
		
		public void showMessage(String message) { System.err.println(message); }

		public String[] promptKeyboardInteractive(String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo) {
			System.err.println("Requesting... ("+name+", "+instruction+", "+Arrays.toString(prompt)+")"); 
			String[] response=new String[prompt.length];
			response[0] = pass;
			return response;
		}
	}
}
