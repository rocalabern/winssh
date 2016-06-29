package com.roc.utils.r.exec_test;

import com.roc.utils.r.sshclient.Shell;

public class test01 {

	public static void main(String[] args) {
		try {
			Shell shell = new Shell();
			
			System.out.println("Status: "+shell.getStatus());
			
			String strOutput;
			String server = "localhost";
			String port = "22";
			String user = "ralabern";
			String pass = "ralabern";
			
			strOutput = shell.openConnection(server, port, user, pass, "", "", "");
			System.err.println(strOutput);
			
			System.out.println("Status: "+shell.getStatus());
			
			strOutput = shell.sendCommand("ls");
			System.err.println(strOutput);
			strOutput = shell.sendCommand("cd Alabern");
			System.err.println(strOutput);
			strOutput = shell.flush();
			System.err.println(strOutput);
			
			strOutput = shell.sendCommand("sleep 3");
			System.err.println(strOutput);
			System.out.println("ini");
			System.out.println("Status: "+shell.getStatus());
			while (!shell.isWaiting()) {
				Thread.sleep(500);
				strOutput = shell.flush();
				System.err.print(strOutput);
			}
			System.out.println("Status: "+shell.getStatus());
			System.out.println("end");
			
			System.out.println("ini");
			System.out.println(shell.getFirstBashPrompt());
			System.out.println(shell.getBashPrompt_UserHost());
			System.out.println(shell.getLastBashPrompt());
			System.out.println("end");
			
			System.err.println(shell);
			strOutput = shell.closeConnection();
			System.err.println(strOutput);
			System.err.println(shell);
			
			System.out.println("Status: "+shell.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
