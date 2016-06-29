# roxygen2::roxygenise()

# library('rJava')
# .jinit()
# .jaddClassPath("inst/java/sshclient.jar")

ssh.x.message <- function (strText, windows=FALSE) {
  #   writeLines(strwrap(strText, width=73))
  if (windows) strText = unlist(strsplit(strText, "\r\n"))
  strText = unlist(strsplit(strText, "\n"))
  for (line in strText) message(line)
}

ssh.showCommand <- function(command) {
  message(paste0("[Command]:\n", command, "\n"))
}

#' @title ssh.shell
#' @description Creates ShellSSH Java Object.
#' @examples
#' \dontrun{
#' con = ssh.shell()
#' ssh.open(con, connData$server_sas_ip, connData$server_sas_port, connData$server_sas_user, connData$server_sas_pass)
#' ssh.sendCommand(con, strCMD)
#' ssh.close(con)
#' }
#' @export
ssh.shell <- function(
  jar_path = "",
  changeBashPromt = TRUE,
  responseTries = 3,
  responseTimeout = 200,
  responseMultFactorTries = 2,
  responseMultFactorTimeout = 2,
  removeSpecialCharactersAtJAVA = TRUE,
  removeSpecialCharactersAtR = TRUE,
  showConfig = FALSE
) {

  if (!is.null(jar_path) && jar_path!="") rJava::.jinit(jar_path)
  jShellServer <- rJava::.jnew("com.roc.utils.r.sshclient.Shell")

  if (!changeBashPromt) ssh.setChangeBashPromt(jShellServer, changeBashPromt)

  if (responseTries!=3) ssh.setResponseTries(jShellServer, responseTries)
  if (responseTimeout!=200) ssh.setResponseTimeout(jShellServer, responseTimeout)
  if (responseMultFactorTries!=2) ssh.setResponseMultFactorTries(jShellServer, responseMultFactorTries)
  if (responseMultFactorTimeout!=2) ssh.setResponseMultFactorTimeout(jShellServer, responseMultFactorTimeout)

  ssh.setRemoveSpecialCharacters(jShellServer, removeSpecialCharactersAtJAVA)
  ssh.setRemoveSpecialCharactersAtR(removeSpecialCharactersAtR)

  if (showConfig) smessage(rJava::.jcall(jShellServer, "S", method="toString"))

  return (jShellServer)
}

#' @title ssh.open
#' @examples
#' \dontrun{
#' con = ssh.shell()
#' ssh.open(con, connData$server_sas_ip, connData$server_sas_port, connData$server_sas_user, connData$server_sas_pass)
#' ssh.sendCommand(con, strCMD)
#' ssh.close(con)
#' }
#' @export
ssh.open <- function(
  jShellServer, 
  server, 
  port, 
  user, 
  pass, 
  bash_prompt="", 
  privateKey="", 
  strictHostKeyChecking="", 
  showConfig = FALSE
) {
  strOutput = rJava::.jcall(jShellServer, "S", method="openConnection", server, port, user, pass, bash_prompt, privateKey, strictHostKeyChecking)
  if (removeSpecialCharacters) strOutput = gsub("[^0-9A-Za-z///'\"\r\n\t ]", "", strOutput)
  if (showConfig) smessage(rJava::.jcall(jShellServer, "S", method="toString"))
  message(strOutput)
  invisible(strOutput)
}

#' @title ssh.setRemoveSpecialCharacters
#' @export
ssh.setRemoveSpecialCharacters <- function(jShellServer, choice=TRUE) {
  rJava::.jcall(jShellServer, "V", method="setRemoveSpecialCharacters", choice)
}

#' @title ssh.setChangeBashPromt
#' @export
ssh.setChangeBashPromt <- function(jShellServer, choice=TRUE) {
  rJava::.jcall(jShellServer, "V", method="setChangeBashPromt", choice)
}

#' @title ssh.setResponseTries
#' @export
ssh.setResponseTries <- function(jShellServer, ntries=3) {
  rJava::.jcall(jShellServer, "V", method="setResponseTries", as.integer(ntries))
}

#' @title ssh.setResponseTimeout
#' @export
ssh.setResponseTimeout <- function(jShellServer, timeout=200) {
  rJava::.jcall(jShellServer, "V", method="setResponseTimeout", .jlong(timeout))
}

#' @title ssh.setResponseMultFactorTries
#' @export
ssh.setResponseMultFactorTries <- function(jShellServer, responseMultFactorTries=2) {
  rJava::.jcall(jShellServer, "V", method="setResponseMultFactorTrie", as.integer(responseMultFactorTries))
}

#' @title ssh.setResponseMultFactorTimeout
#' @export
ssh.setResponseMultFactorTimeout <- function(jShellServer, responseMultFactorTimeout=2) {
  rJava::.jcall(jShellServer, "V", method="setResponseMultFactorTimeout", .jlong(responseMultFactorTimeout))
}

#' @title ssh.switchToTerminalMode
#' @export
ssh.switchToTerminalMode <- function(jShellServer, pwd=TRUE) {
  if (pwd) ssh.sendCommand(jShellServer, "pwd")
  command = ""
  while (command!="\\q" && command!="exit") {
    command = readline("")
    if (command=="\\q" || command=="exit") break
    else if (command=="flush") ssh.flush(jShellServer)
    else if (command=="flushwait") ssh.flushwait(jShellServer)
    else ssh.sendCommand(jShellServer, command)
  }
  invisible(NULL)
}

#' @title ssh.sendCommand
#' @examples
#' \dontrun{
#' con = ssh.shell()
#' ssh.open(con, connData$server_sas_ip, connData$server_sas_port, connData$server_sas_user, connData$server_sas_pass)
#' ssh.sendCommand(con, strCMD)
#' ssh.close(con)
#' }
#' @export
ssh.sendCommand <- function(jShellServer, command, flushwait=FALSE) {
  strOutput = rJava::.jcall(jShellServer, "S", method="sendCommand", command)
  if (flushwait) {
    strTemp = ssh.flushwait(jShellServer)
    strOutput = paste0(strOutput, strTemp)
  }
  if (removeSpecialCharacters) strOutput = gsub("[^0-9A-Za-z///'\"\r\n\t ]", "", strOutput)
  message(strOutput)
  invisible(strOutput)
}

#' @title ssh.sendLogCommand
#' @export
ssh.sendLogCommand <- function(jShellServer, command, flushwait=FALSE) {
  ssh.showCommand(command)
  strOutput = ssh.sendCommand(jShellServer=jShellServer, command=command, flushwait=flushwait)
  invisible(strOutput)
}

#' @title ssh.sendSingleCommand
#' @export
ssh.sendSingleCommand <- function(jShellServer, command) {
  strOutput = rJava::.jcall(jShellServer, "S", method="sendSingleCommand", command)
  if (removeSpecialCharacters) strOutput = gsub("[^0-9A-Za-z///'\"\r\n\t ]", "", strOutput)
  message(strOutput)
  invisible(strOutput)
}

#' @title ssh.flush
#' @export
ssh.flush <- function(jShellServer) {
  strOutput = rJava::.jcall(jShellServer ,"S",method="flush")
  if (removeSpecialCharacters) strOutput = gsub("[^0-9A-Za-z///'\"\r\n\t ]", "", strOutput)
  message(strOutput)
  invisible(strOutput)
}

#' @title ssh.flushwait
#' @export
ssh.flushwait <- function(jShellServer) {
  strOutput = rJava::.jcall(jShellServer ,"S",method="flushwait")
  if (removeSpecialCharacters) strOutput = gsub("[^0-9A-Za-z///'\"\r\n\t ]", "", strOutput)
  message(strOutput)
  invisible(strOutput)
}

#' @title ssh.charstatus
#' @export
ssh.charstatus <- function(jShellServer) {
  strStatus = rJava::.jcall(jShellServer ,"S",method="getStatus")
  return(strStatus)
}

#' @title ssh.status
#' @export
ssh.status <- function(jShellServer) {
  strStatus = rJava::.jcall(jShellServer ,"S",method="getStatus")
  return(factor(strStatus, levels=c("null","disconnected","ready","busy")))
}

#' @title ssh.isDisconnected
#' @export
ssh.isDisconnected <- function(jShellServer) {
  strStatus = rJava::.jcall(jShellServer ,"S",method="getStatus")
  "a" %in% c("a","b","c")
  return(strStatus %in% c("null","disconnected"))
}

#' @title ssh.isConnected
#' @export
ssh.isConnected <- function(jShellServer) {
  strStatus = rJava::.jcall(jShellServer ,"S",method="getStatus")
  "a" %in% c("a","b","c")
  return(strStatus %in% c("ready","busy"))
}

#' @title ssh.isBusy
#' @export
ssh.isBusy <- function(jShellServer) {
  strStatus = rJava::.jcall(jShellServer ,"S",method="getStatus")
  return(strStatus %in% c("busy"))
}

#' @title ssh.isReady
#' @export
ssh.isReady <- function(jShellServer) {
  strStatus = rJava::.jcall(jShellServer ,"S",method="getStatus")
  return(strStatus %in% c("ready"))
}

#' @title ssh.info
#' @export
ssh.info <- function(jShellServer) {
  strStatus = rJava::.jcall(jShellServer ,"S",method="toString")
  message(strStatus)
  invisible(strStatus)
}

#' @title ssh.close
#' @examples
#' \dontrun{
#' con = ssh.shell()
#' ssh.open(con, connData$server_sas_ip, connData$server_sas_port, connData$server_sas_user, connData$server_sas_pass)
#' ssh.sendCommand(con, strCMD)
#' ssh.close(con)
#' }
#' @export
ssh.close <- function(jShellServer) {
  strOutput = rJava::.jcall(jShellServer ,"S",method="closeConnection")
  if (removeSpecialCharacters) strOutput = gsub("[^0-9A-Za-z///'\"\r\n\t ]", "", strOutput)
  message(strOutput)
  invisible(strOutput)
}