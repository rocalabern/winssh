% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/ssh_scp.R
\name{scpToOther}
\alias{scpToOther}
\title{scpToOther}
\usage{
scpToOther(jShellServer, fromNameFile, toNameFile, userRemote, passRemote,
  servRemote, portRemote = "22", fromFile = fromNameFile,
  toFile = paste0(userRemote, "@", servRemote, ":", toNameFile),
  params = "-q", compression = FALSE,
  bandwithLimit = default.bandwithLimit, RUN = TRUE)
}
\description{
\code{scpToOther} Uses system scp through ssh connection to move a file from connected server to a different server.
}
\details{
Uses system scp through ssh connection to move a file from connected server to a different server.
\cr To move a whole folder, and all recursive subfolders, use \code{\link{scpToOther}} or \code{\link{scpFromOther}} with the following parameters: params="-q -r"
}
\seealso{
\code{\link{scpToLocal}} \code{\link{scpFromLocal}}
\cr \code{\link{scpToOther}} \code{\link{scpFromOther}}
\cr \code{\link{scp}}
}

