default.bandwithLimit = 0

removeSpecialCharacters = TRUE

setVar <- function (var, value) {
  strValue = paste(capture.output(dump("value", file="")), collapse = "")
  if (substring(strValue, 1, 9)=="value <- ") {
    strValue = substring(strValue, 10)
  } else if (substring(strValue, 1, 8)=="value<- ") {
    strValue = substring(strValue, 9)
  } else if (substring(strValue, 1, 8)=="value <-") {
    strValue = substring(strValue, 9)
  } else if (substring(strValue, 1, 7)=="value<-") {
    strValue = substring(strValue, 8)
  } else if (substring(strValue, 1, 8)=="value = ") {
    strValue = substring(strValue, 9)
  } else if (substring(strValue, 1, 7)=="value= ") {
    strValue = substring(strValue, 8)
  } else if (substring(strValue, 1, 7)=="value =") {
    strValue = substring(strValue, 8)
  } else if (substring(strValue, 1, 6)=="value=") {
    strValue = substring(strValue, 7)
  }
  unlockBinding(var, env = asNamespace('sshtools'))
  eval(parse(text=paste0(var," <- ",strValue)), envir = asNamespace('sshtools'))
  lockBinding(var, env = asNamespace('sshtools'))
}

#' @title ssh.setParam.bandwithLimit
#' @export
ssh.setParam.bandwithLimit <- function (value=0) {
  sshtools:::setVar("default.bandwithLimit", value)
}

#' @title ssh.setRemoveSpecialCharactersAtR
#' @export
ssh.setRemoveSpecialCharactersAtR <- function (bool=TRUE) {
  sshtools:::setVar("removeSpecialCharacters", bool)
}