package com.roc.utils.r.sshclient.examples;

import java.io.File;
import java.io.FileOutputStream;
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

public class SCPFromM {

	public static void main(String[] arg) {
		scpFromRemote("localhost", "22", "ralabern", "ralabern", "/folder/test.txt", "/folder/test.txt");
	}

	public static void scpFromRemote (String host, String port, String user, String pass, String rfile, String lfile) {
		FileOutputStream fos = null;
		try {
			String prefix = null;
			if(new File(lfile).isDirectory()){
				prefix = lfile+File.separator;
			}

			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, Integer.parseInt(port));
			session.setUserInfo(new UserPass(pass));
			session.connect();

			// exec 'scp -f rfile' remotely
			String command = "scp -f "+rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();
			channel.connect();

			byte[] buf = new byte[1024];
			// send '\0'
			buf[0]=0; out.write(buf, 0, 1); out.flush();

			while(true) {
				int c = checkAck(in);
				if(c != 'C') {
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize = 0L;
				while(true) {
					if(in.read(buf, 0, 1) < 0) {
						// error
						break; 
					}
					if(buf[0] == ' ')break;
					filesize=filesize*10L+(long)(buf[0]-'0');
				}

				String file = null;
				for(int i=0;;i++) {
					in.read(buf, i, 1);
					if(buf[i]==(byte)0x0a) {
						file=new String(buf, 0, i);
						break;
					}
				}

				//System.out.println("filesize="+filesize+", file="+file);

				// send '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();

				// read a content of lfile
				fos = new FileOutputStream(prefix==null ? lfile : prefix+file);
				int foo;
				while(true) {
					if(buf.length<filesize) foo=buf.length;
					else foo=(int)filesize;
					foo = in.read(buf, 0, foo);
					if(foo<0) {
						// error 
						break;
					}
					fos.write(buf, 0, foo);
					filesize-=foo;
					if(filesize==0L) break;
				}
				fos.close();
				fos=null;

				if(checkAck(in)!=0) {
					return;
				}

				// send '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();
			}
			session.disconnect();
		} catch(Exception e) {
			System.out.println(e);
			try{if(fos!=null)fos.close();}catch(Exception ee){}
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
