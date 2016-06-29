package com.roc.utils.r.sshclient;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.roc.utils.r.Parser;

public class ShellDialogBox {
	
	static private boolean default_removeSpecialCharacters = true;
	static private int default_tries = 2;
	static private long default_timeout = 150L;
	
	public ShellDialogBox() {
	}
	
	public static void main(String[] args) {
		openDialogBox(null, null, null, null, null, null);
	}
	
	public static void openDialogBox(String host, String port, String user, String strRemoveSpecialCharacters, String privateKey, String strictHostKeyChecking) {
		try {
			
			if (strictHostKeyChecking != null && !strictHostKeyChecking.equalsIgnoreCase("") && strictHostKeyChecking.equalsIgnoreCase("no")) {
				JSch.setConfig("StrictHostKeyChecking", "no");
				System.out.println("[Warn] Not using StrictHostKeyChecking.");
			}
			
			JSch jsch=new JSch();  
			
			if (strictHostKeyChecking != null && !strictHostKeyChecking.equalsIgnoreCase("") && strictHostKeyChecking.equalsIgnoreCase("no")) {
				JSch.setConfig("StrictHostKeyChecking", "no");
				System.out.println("[Warn] Not using StrictHostKeyChecking.");
			}
			
			if (privateKey != null && !privateKey.equalsIgnoreCase("")) {
				jsch.addIdentity(privateKey);
				System.out.println("[Identity] PPK added.");
			}
			
			if (host == null || host.equalsIgnoreCase("") || user==null || user.equalsIgnoreCase("")) {
				host = JOptionPane.showInputDialog("Enter username@hostname",
						System.getProperty("user.name")+
						"@localhost");
				user = host.substring(0, host.indexOf('@'));
				host = host.substring(host.indexOf('@')+1);
			}
			
			Boolean removeSpecialCharacters = Parser.toBoolean(strRemoveSpecialCharacters); 
			if (removeSpecialCharacters == null) removeSpecialCharacters = default_removeSpecialCharacters;
			if (port == null || port.equalsIgnoreCase("")) port = "22";
			
			Session session = jsch.getSession(user, host, Integer.parseInt(port));
			UserInfo ui = new InteractiveUserInfo();
			session.setUserInfo(ui);
			session.connect();
			
			Channel channel = session.openChannel("shell"); 
			PrintStream shellStream = new PrintStream(channel.getOutputStream());
			channel.connect(); 
			
			InputStream in=channel.getInputStream();
			String strOutput = getConsoleResponse(in, 2*default_tries, 2*default_timeout);
			System.err.print(strOutput);
			
			if (removeSpecialCharacters) {
				shellStream.println("unalias ls"); 
				shellStream.flush();
				getConsoleResponse(in, default_tries, default_timeout);
			}
			
			String command = JOptionPane.showInputDialog("Enter command", "ls");
			while (
					command!=null && !command.equalsIgnoreCase("") 
					&& !command.equalsIgnoreCase("\\q") 
					&& !command.equalsIgnoreCase("exit")
			) {
				if (!command.equalsIgnoreCase("ssh-flush")) {
					shellStream.println(command); 
					shellStream.flush();
				}
				strOutput = getConsoleResponse(in, default_tries, default_timeout);
				System.err.print(strOutput);
				command = JOptionPane.showInputDialog("Enter command", command);
			}
			channel.disconnect();
			session.disconnect();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static String getConsoleResponse(InputStream in, Integer retries, Long waitTimePerTry) throws IOException {
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
		return strOutput;
	}
	
	private static class InteractiveUserInfo implements UserInfo, UIKeyboardInteractive {
		public String getPassword(){ return passwd; }
		public boolean promptYesNo(String str) {
			Object[] options={ "yes", "no" };
			int foo=JOptionPane.showOptionDialog(null, 
					str,
					"Warning", 
					JOptionPane.DEFAULT_OPTION, 
					JOptionPane.WARNING_MESSAGE,
					null, options, options[0]);
			return foo==0;
		}

		String passwd;
		JTextField passwordField = (JTextField) new JPasswordField(20);

		public String getPassphrase(){ return null; }
		public boolean promptPassphrase(String message){ return true; }
		public boolean promptPassword(String message) {
			Object[] ob = {passwordField}; 
			int result =
					JOptionPane.showConfirmDialog(null, ob, message,
							JOptionPane.OK_CANCEL_OPTION);
			if(result == JOptionPane.OK_OPTION) {
				passwd = passwordField.getText();
				return true;
			} else { 
				return false; 
			}
		}
		public void showMessage(String message) {
			JOptionPane.showMessageDialog(null, message);
		}
		final GridBagConstraints gbc = 
				new GridBagConstraints(0,0,1,1,1,1,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.NONE,
						new Insets(0,0,0,0),0,0);
		private Container panel;
		public String[] promptKeyboardInteractive(String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo) {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			gbc.weightx = 1.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			panel.add(new JLabel(instruction), gbc);
			gbc.gridy++;

			gbc.gridwidth = GridBagConstraints.RELATIVE;

			JTextField[] texts = new JTextField[prompt.length];
			for(int i=0; i<prompt.length; i++) {
				gbc.fill = GridBagConstraints.NONE;
				gbc.gridx = 0;
				gbc.weightx = 1;
				panel.add(new JLabel(prompt[i]),gbc);

				gbc.gridx = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weighty = 1;
				if(echo[i]) {
					texts[i]=new JTextField(20);
				} else {
					texts[i]=new JPasswordField(20);
				}
				panel.add(texts[i], gbc);
				gbc.gridy++;
			}

			if(JOptionPane.showConfirmDialog(null, panel, 
					destination+": "+name,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE)
					==JOptionPane.OK_OPTION) {
				String[] response=new String[prompt.length];
				for(int i=0; i<prompt.length; i++) {
					response[i]=texts[i].getText();
				}
				return response;
			} else {
				return null;  
			}
		}
	}
}
