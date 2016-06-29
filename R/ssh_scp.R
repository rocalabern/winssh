# roxygen2::roxygenise()

#' @title scp
#' @description
#' \code{scp} Uses system scp through ssh connection to move a file from connected server to a different server.
#' \cr This function is exactly the same than \code{scpToOther}.
#' @details
#' Uses system scp through ssh connection to move a file from connected server to a different server.
#' \cr To move a whole folder, and all recursive subfolders, use \code{\link{scpToOther}} or \code{\link{scpFromOther}} with the following parameters: params="-q -r"
#' @seealso \code{\link{scpToLocal}} \code{\link{scpFromLocal}}
#' \cr \code{\link{scpToOther}} \code{\link{scpFromOther}}
#' \cr \code{\link{scp}}
#' @export
scp <- function(jShellServer,
                fromFile, toFile,
                passRemote,
                portRemote="22",
                params="-q",
                compression=FALSE,
                bandwithLimit=default.bandwithLimit,
                RUN=TRUE)
{
  if (default.bandwithLimit>0 && bandwithLimit>default.bandwithLimit) stop(paste0("Maximum bandwith is ",default.bandwithLimit," kbps"))
  if (bandwithLimit>0) params = paste0("-l ", format(bandwithLimit, scientific = FALSE), " ", params)
  if (compression) params = paste0("-C ", params)
  command = paste0("scp -oStrictHostKeyChecking=no"," -P ",portRemote," ",params," ",fromFile," ",toFile)
  if (RUN) {
    ssh.sendLogCommand(jShellServer, command)
    ssh.flush(jShellServer)
    ssh.sendCommand(jShellServer, passRemote)
  } else {
    ssh.showCommand(command)
  }
}

#' @title scpToOther
#' @description
#' \code{scpToOther} Uses system scp through ssh connection to move a file from connected server to a different server.
#' @details
#' Uses system scp through ssh connection to move a file from connected server to a different server.
#' \cr To move a whole folder, and all recursive subfolders, use \code{\link{scpToOther}} or \code{\link{scpFromOther}} with the following parameters: params="-q -r"
#' @seealso \code{\link{scpToLocal}} \code{\link{scpFromLocal}}
#' \cr \code{\link{scpToOther}} \code{\link{scpFromOther}}
#' \cr \code{\link{scp}}
#' @export
scpToOther <- function(jShellServer,
                       fromNameFile, toNameFile,
                       userRemote, passRemote,
                       servRemote, portRemote="22",
                       fromFile=fromNameFile,
                       toFile=paste0(userRemote,"@",servRemote,":",toNameFile),
                       params="-q",
                       compression=FALSE,
                       bandwithLimit=default.bandwithLimit,
                       RUN=TRUE)
{
  if (default.bandwithLimit>0 && bandwithLimit>default.bandwithLimit) stop(paste0("Maximum bandwith is ",default.bandwithLimit," kbps"))
  if (bandwithLimit>0) params = paste0("-l ", format(bandwithLimit, scientific = FALSE), " ", params)
  if (compression) params = paste0("-C ", params)
  command = paste0("scp -oStrictHostKeyChecking=no"," -P ",portRemote," ",params," ",fromFile," ",toFile)
  if (RUN) {
    ssh.sendLogCommand(jShellServer, command)
    ssh.flush(jShellServer)
    ssh.sendCommand(jShellServer, passRemote)
  } else {
    ssh.showCommand(command)
  }
}

#' @title scpFromOther
#' @description
#' \code{scpFromOther} Uses system scp through ssh connection to move a file from a different server to connected server.
#' @details
#' Uses system scp through ssh connection to move a file from a different server to connected server.
#' \cr To move a whole folder, and all recursive subfolders, use \code{\link{scpToOther}} or \code{\link{scpFromOther}} with the following parameters: params="-q -r"
#' @seealso \code{\link{scpToLocal}} \code{\link{scpFromLocal}}
#' \cr \code{\link{scpToOther}} \code{\link{scpFromOther}}
#' \cr \code{\link{scp}}
#' @export
scpFromOther <- function(jShellServer,
                         fromNameFile, toNameFile,
                         userRemote, passRemote,
                         servRemote, portRemote="22",
                         fromFile=paste0(userRemote,"@",servRemote,":",fromNameFile),
                         toFile=toNameFile,
                         params="-q",
                         compression=FALSE,
                         bandwithLimit=default.bandwithLimit,
                         RUN=TRUE)
{
  if (default.bandwithLimit>0 && bandwithLimit>default.bandwithLimit) stop(paste0("Maximum bandwith is ",default.bandwithLimit," kbps"))
  if (bandwithLimit>0) params = paste0("-l ", format(bandwithLimit, scientific = FALSE), " ", params)
  if (compression) params = paste0("-C ", params)
  command = paste0("scp -oStrictHostKeyChecking=no"," -P ",portRemote," ",params," ",fromFile," ",toFile)
  if (RUN) {
    ssh.sendLogCommand(jShellServer, command)
    ssh.flush(jShellServer)
    ssh.sendCommand(jShellServer, passRemote)
  } else {
    ssh.showCommand(command)
  }
}

#' @title scpFromLocal
#' @description
#' \code{scpFromLocal} It does not uses system scp. It uses java process. It moves a file from local server to connected server.
#' @details
#' It does not uses system scp. It uses java process. It moves a file from local server to connected server.
#' \cr To move a whole folder, and all recursive subfolders, use \code{\link{scpToOther}} or \code{\link{scpFromOther}} with the following parameters: params="-q -r"
#' @seealso \code{\link{scpToLocal}} \code{\link{scpFromLocal}}
#' \cr \code{\link{scpToOther}} \code{\link{scpFromOther}}
#' \cr \code{\link{scp}}
#' @examples
#' \dontrun{
#' con = ssh.shell()
#' ssh.open(con, connData$server_sas_ip, connData$server_sas_port, connData$server_sas_user, connData$server_sas_pass)
#' write.table(iris, file="iris.csv", append=FALSE, quote=FALSE, sep=";", row.names=FALSE)
#'
#' scpFromLocal(con, "iris.csv", paste0(connData$server_sas_tables,"iris.csv"))
#'
#' ssh.close(con)
#' }
#' @export
scpFromLocal <- function(jShellServer,
                         fromLocalFile, toRemoteFile)
{
  ssh.showCommand(paste0("JVM scp ", fromLocalFile," ",toRemoteFile))
  strOutput = rJava::.jcall(jShellServer, "S", method="scpFromLocal", fromLocalFile, toRemoteFile)
  message(strOutput)
  invisible(strOutput)
}

#' @title scpToLocal
#' @description
#' \code{scpToLocal} It does not uses system scp. It uses java process. It moves a file from connected server to local server.
#' @details
#' It does not uses system scp. It uses java process. It moves a file from connected server to local server.
#' \cr To move a whole folder, and all recursive subfolders, use \code{\link{scpToOther}} or \code{\link{scpFromOther}} with the following parameters: params="-q -r"
#' @seealso \code{\link{scpToLocal}} \code{\link{scpFromLocal}}
#' \cr \code{\link{scpToOther}} \code{\link{scpFromOther}}
#' \cr \code{\link{scp}}
#' @export
scpToLocal <- function(jShellServer,
                       fromRemoteFile, toLocalFile)
{
  ssh.showCommand(paste0("JVM scp ", fromRemoteFile," ",toLocalFile))
  strOutput = rJava::.jcall(jShellServer, "S", method="scpToLocal", fromRemoteFile, toLocalFile)
  message(strOutput)
  invisible(strOutput)
}