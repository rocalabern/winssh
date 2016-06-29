package com.roc.utils.r.sshclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class Shell {
	
	protected JSch jsch;
	protected Session session;
	protected Channel channel;
	protected PrintStream shellStreamSend;
	protected InputStream shellStreamReceive;

	protected String user;
	protected String host;
	protected String port; 
	protected String bashprompt_first;
	protected String bashprompt_userhost;
	protected String bashprompt_last;
	
	protected boolean isWaiting;
	
	protected boolean removeSpecialCharacters;
	protected boolean changeBashPromt;
	protected int responseTries;
	protected long responseTimeout;
	protected int responseMultFactorTries;
	protected long responseMultFactorTimeout;
	
	public Shell() {
		removeSpecialCharacters = true;
		changeBashPromt = true;
		responseTries = 3;
		responseTimeout = 200L;
		responseMultFactorTries = 2;
		responseMultFactorTimeout = 2L;
		isWaiting = true;
	}
	
	public String openConnection(String host, String port, String user, String pass, String bash_prompt, String privateKey, String strictHostKeyChecking) {
		try {
			if (host == null || host.equalsIgnoreCase("")) port = "localhost";
			if (port == null || port.equalsIgnoreCase("")) port = "22";
			if (pass == null) pass = "";
			
			if (strictHostKeyChecking != null && !strictHostKeyChecking.equalsIgnoreCase("") && strictHostKeyChecking.equalsIgnoreCase("no")) {
				JSch.setConfig("StrictHostKeyChecking", "no");
				System.out.println("[Warn] Not using strict host key checking.");
			}
			
			jsch=new JSch();  

			if (strictHostKeyChecking != null && !strictHostKeyChecking.equalsIgnoreCase("") && strictHostKeyChecking.equalsIgnoreCase("no")) {
				JSch.setConfig("StrictHostKeyChecking", "no");
				System.out.println("[Warn] Not using strict host key checking.");
			}
			
			if (privateKey != null && !privateKey.equalsIgnoreCase("")) {
				jsch.addIdentity(privateKey);
				System.out.println("[Identity] PPK added.");
			}
	            
			session = jsch.getSession(user, host, Integer.parseInt(port));
			session.setUserInfo(new UserPass(pass));
			session.connect();
			
			channel = session.openChannel("shell"); 
			shellStreamSend = new PrintStream(channel.getOutputStream());
			shellStreamReceive = channel.getInputStream();
			channel.connect(); 
			
			String strOutput = getConsoleResponse(shellStreamReceive, responseMultFactorTries*responseTries, responseMultFactorTimeout*responseTimeout, false);
			
			if (removeSpecialCharacters) {
				shellStreamSend.println("unalias ls"); 
				shellStreamSend.flush();
			}
			
			if (changeBashPromt) {
				if (bash_prompt==null || bash_prompt.equalsIgnoreCase("")) shellStreamSend.println("PS1=\"\\u@\\h \\w$ \""); 
				else shellStreamSend.println("PS1=\""+bash_prompt+"\""); 
				shellStreamSend.flush();
			}
			
			strOutput = strOutput + getConsoleResponse(shellStreamReceive, responseTries, responseTimeout, false);
			
			bashprompt_first = getLastLine(strOutput);
			bashprompt_last = bashprompt_first;
			bashprompt_userhost = getUserHostFromBashPrompt(bashprompt_first);
			
			if (
					jsch!=null 
					&& session!=null 
					&& session.isConnected()
					&& channel!=null 
					&& channel.isConnected()
				) {
				this.user = user;
				this.host = host;
				this.port = port;
				return strOutput;
			} else {
				return "Error ocurred";
			}
		} catch(Exception e) {
			e.printStackTrace();
			return "Error ocurred";
		}
	}
	
	public String closeConnection() {
		if (channel!=null) channel.disconnect();
		if (session!=null) session.disconnect();
		shellStreamSend = null;
		shellStreamReceive = null;
		channel = null;
		session = null;
		jsch = null;
		return "Connection closed";
	}
	
	
	public String sendCommand(String command) {
		try {
			if (jsch==null) return "jsch not inialitzed";
			if (session==null || !session.isConnected()) return "Session closed";
			if (channel==null || !channel.isConnected()) return "Channel closed";
			
			shellStreamSend.println(command); 
			shellStreamSend.flush();
			
			String strOutput = getConsoleResponse(shellStreamReceive, responseTries, responseTimeout, true);
			return strOutput;
		} catch(Exception e) {
			e.printStackTrace();
			return "Error ocurred";
		}
	}
	
	
	public String flush() {
		try {
			if (jsch==null) return "jsch not inialitzed";
			if (session==null || !session.isConnected()) return "Session closed";
			if (channel==null || !channel.isConnected()) return "Channel closed";
			
			String strOutput = getConsoleResponse(shellStreamReceive, responseTries, responseTimeout, true);
			return strOutput;
		} catch(Exception e) {
			e.printStackTrace();
			return "Error ocurred";
		}
	}
	
	public String flushwait() {
		try {
			String strOutput = "";
			while (!isWaiting) {
				strOutput = strOutput + flush();
			}
			return strOutput;
		} catch(Exception e) {
			e.printStackTrace();
			return "Error ocurred";
		}
	}
	
	
	public String sendSingleCommand(String command) {
		try {
			if (jsch==null) return "jsch not inialitzed";
			if (session==null || !session.isConnected()) return "Session closed";

			Channel channel = session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
			PrintStream shellStream = new PrintStream(channel.getOutputStream());
			InputStream in=channel.getInputStream();
			channel.connect(); 
			
			String strOutput = getConsoleResponse(in, responseMultFactorTries*responseTries, responseMultFactorTimeout*responseTimeout, false);
			if (
					command!=null && !command.equalsIgnoreCase("") 
					&& !command.equalsIgnoreCase("\\q") 
					&& !command.equalsIgnoreCase("exit")
			) {
				if (!command.equalsIgnoreCase("ssh-flush")) {
					shellStream.println(command); 
					shellStream.flush();
				}
				strOutput = strOutput + getConsoleResponse(in, responseTries, responseTimeout, false);
			}
			channel.disconnect();
			return strOutput;
		} catch(Exception e) {
			e.printStackTrace();
			return "Error ocurred";
		}
	}
	
	public String scpFromLocal (String lfile, String rfile) {
		FileInputStream fis = null;
		try {
			boolean ptimestamp = true;

			// exec 'scp -t rfile' remotely
			String command = "scp " + (ptimestamp ? "-p" :"") +" -t "+rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();
			channel.connect();

			if (checkAck(in) != 0) {
				return "Error ocurred checking Ack";
			}

			File _lfile = new File(lfile);

			if (ptimestamp) {
				command = "T "+(_lfile.lastModified()/1000)+" 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command += (" "+(_lfile.lastModified()/1000)+" 0\n"); 
				out.write(command.getBytes()); out.flush();
				if (checkAck(in) != 0) {
					return "Error ocurred checking Ack";
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
				return "Error ocurred checking Ack";
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
				return "Error ocurred checking Ack";
			}
			out.close();
			channel.disconnect();
			return "Done";
		} catch(Exception e) {
			e.printStackTrace();
			try{if(fis!=null)fis.close();}catch(Exception ee){}
			return "Error ocurred";
		}
	}
	
	public String scpToLocal (String rfile, String lfile) {
		FileOutputStream fos = null;
		try {
			if (jsch==null) return "jsch not inialitzed";
			if (session==null || !session.isConnected()) return "Session closed";
			
			String prefix = null;
			if(new File(lfile).isDirectory()){
				prefix = lfile+File.separator;
			}

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
					return null;
				}

				// send '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();
			}
			channel.disconnect();
			return "Done";
		} catch(Exception e) {
			e.printStackTrace();
			try{if(fos!=null)fos.close();}catch(Exception ee){}
			return "Error ocurred";
		}
	}
	
	private String getConsoleResponse(InputStream in, Integer retries, Long waitTimePerTry, boolean checkIsReady) throws IOException {
		String strOutput = "";
		if (retries==null || retries<1) retries=5;
		if (waitTimePerTry==null || waitTimePerTry<1) waitTimePerTry=200L;
		byte[] tmp = new byte[1024];
		while (0 < retries) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0) break;
				strOutput = strOutput + new String(tmp, 0, i);
			}
			try{Thread.sleep(waitTimePerTry);}catch(Exception e){}
			retries--;
		}
		if (checkIsReady) {
			isWaiting = isSameBeginningInLastLine(strOutput, bashprompt_userhost, isWaiting);
		}
		return strOutput;
	}

	private String getUserHostFromBashPrompt(String str) {
		if(str.contains("~")) return str.substring(0, str.indexOf("~")); 
		else if(str.contains(" ")) return str.substring(0, str.indexOf(" ")); 
		else if(str.contains(":")) return str.substring(0, str.indexOf(":")); 
		else if(str.contains("@")) return str.substring(0, str.indexOf("@"));
		else if(str.length()>7) return str.substring(0, 7);
		else return null;
	}
	
	private boolean isSameBeginningInLastLine(String str, String strBeginning, boolean bDefault) {
		String lines[] = str.split("\\r?\\n");
		boolean ready = bDefault;
		for (int i=0;i<lines.length;i++) {
			if (lines[i].length()>0) {
				if (lines[i].length()<strBeginning.length()) ready = false;
				else if (lines[i].substring(0, strBeginning.length()).equals(strBeginning)) {
					bashprompt_last = lines[i];
					ready = true;
				}
				else ready = false;
			}
		}
		return ready; 
	}
	
	private static String getLastLine(String str) {
		String lines[] = str.split("\\r?\\n");
		return lines[lines.length-1];
	}
	
	private static int checkAck(InputStream in) throws IOException{
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
				System.err.print(sb.toString());
			}
			if(b==2){ // fatal error
				System.err.print(sb.toString());
			}
		}
		return b;
	}

	
	private static class UserPass implements UserInfo, UIKeyboardInteractive {
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

	public String getUser() {
		return user;
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}
	
	public String getFirstBashPrompt() {
		return bashprompt_first;
	}

	public String getBashPrompt_UserHost() {
		return bashprompt_userhost;
	}
	
	public String getLastBashPrompt() {
		return bashprompt_last;
	}
	
	public boolean isRemoveSpecialCharacters() {
		return removeSpecialCharacters;
	}

	public void setRemoveSpecialCharacters(boolean removeSpecialCharacters) {
		this.removeSpecialCharacters = removeSpecialCharacters;
	}

	public boolean isChangeBashPromt() {
		return changeBashPromt;
	}

	public void setChangeBashPromt(boolean changeBashPromt) {
		this.changeBashPromt = changeBashPromt;
	}

	public int getResponseTries() {
		return responseTries;
	}

	public void setResponseTries(int responseTries) {
		this.responseTries = responseTries;
	}

	public long getResponseTimeout() {
		return responseTimeout;
	}

	public void setResponseTimeout(long responseTimeout) {
		this.responseTimeout = responseTimeout;
	}

	public int getResponseMultFactorTrie() {
		return responseMultFactorTries;
	}

	public void setResponseMultFactorTrie(int responseMultFactorTries) {
		this.responseMultFactorTries = responseMultFactorTries;
	}

	public long getResponseMultFactorTimeout() {
		return responseMultFactorTimeout;
	}

	public void setResponseMultFactorTimeout(long responseMultFactorTimeout) {
		this.responseMultFactorTimeout = responseMultFactorTimeout;
	}
	
	public boolean isWaiting() {
		return isWaiting;
	}
	
	public String getStatus() {
		if (jsch==null || session==null || channel==null) return "null";
		else if(!session.isConnected() || !channel.isConnected()) return "disconnected";
		else if(isWaiting) return "ready";
		else return "busy";
	}
	
	public String toString() {
		String strInfo;
		
		if (jsch!=null) strInfo = "jsch initialized";
		else strInfo = "jsch not initialized";
		
		if (session!=null && session.isConnected()) strInfo = strInfo + "\n" + "session connected";
		else strInfo = strInfo + "\n" + "session not connected";
		
		if (channel!=null && channel.isConnected()) strInfo = strInfo + "\n" + "channel connected";
		else strInfo = strInfo + "\n" + "channel not connected";

		if (user!=null && host!=null && port!=null) strInfo = strInfo + "\n" + "String connection: " + user+"@"+host+":"+port;
		else strInfo = strInfo + "\n" + "String connection: Unkown";
		
		if (bashprompt_userhost!=null) strInfo = strInfo + "\n" + "User@host: " + bashprompt_userhost;
		else strInfo = strInfo + "\n" + "User@host: Not found";
		
		if (bashprompt_first!=null) strInfo = strInfo + "\n" + "First Prompt: " + bashprompt_first;
		else strInfo = strInfo + "\n" + "First Prompt: Not found";
		
		if (bashprompt_last!=null) strInfo = strInfo + "\n" + "Last  Prompt: " + bashprompt_last;
		else strInfo = strInfo + "\n" + "Last  Prompt: Not found";
		
		strInfo = strInfo + "\n" + "Config:";
		strInfo = strInfo + "\n\t" + "removeSpecialCharacters="+String.valueOf(removeSpecialCharacters);
		strInfo = strInfo + "\n\t" + "changeBashPromt="+String.valueOf(changeBashPromt);
		strInfo = strInfo + "\n\t" + "response_tries="+String.valueOf(responseTries);
		strInfo = strInfo + "\n\t" + "response_timeout="+String.valueOf(responseTimeout);
		strInfo = strInfo + "\n\t" + "response_factor_tries="+String.valueOf(responseMultFactorTries);
		strInfo = strInfo + "\n\t" + "response_factor_timeout="+String.valueOf(responseMultFactorTimeout);
		
		strInfo = strInfo + "\n" + "Status: " + getStatus();
		
		return strInfo;
	}
	
	public static void main(String[] args) {
	}
}
