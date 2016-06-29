source("C:/Alabern/Data/ConnData/init_conndata.R")

con = ssh.shell()
ssh.open(con, 
         connData$server_da_ip, 
         connData$server_da_port, 
         connData$server_da_user, 
         connData$server_da_pass,
         privateKey = connData$server_da_pk_openssh,
         strictHostKeyChecking = "no")
ssh.sendCommand(con, strCMD)
ssh.close(con)