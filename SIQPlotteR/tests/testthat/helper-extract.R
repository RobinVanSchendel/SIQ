# Loads single functions out of app.R without running the Shiny app
# (app.R installs packages, reads config and starts the app when sourced).

app_path <- function(){
  cands <- c("../../app.R", "app.R", "SIQPlotteR/app.R")
  hit <- cands[file.exists(cands)]
  if(length(hit) == 0) stop("app.R not found")
  hit[1]
}

# walk an expression tree and return the right-hand side of the first
# `name <- function...` / `name = function...` assignment found (also nested in server())
find_assigned_function <- function(expr, name){
  if(is.call(expr)){
    op <- as.character(expr[[1]])[1]
    if(op %in% c("<-", "=") && length(expr) == 3 && is.name(expr[[2]]) &&
       identical(as.character(expr[[2]]), name) &&
       is.call(expr[[3]]) && identical(as.character(expr[[3]][[1]]), "function")){
      return(expr[[3]])
    }
    parts <- as.list(expr)
    for(i in seq_along(parts)){
      # an empty argument (x[1, ]) is the empty symbol; it cannot be passed on
      if(identical(parts[[i]], quote(expr = ))) next
      r <- find_assigned_function(parts[[i]], name)
      if(!is.null(r)) return(r)
    }
  }
  NULL
}

# returns the function `name` defined in app.R, with `env` as its environment
load_app_function <- function(name, env = globalenv()){
  exprs <- parse(app_path(), keep.source = FALSE)
  for(e in exprs){
    f <- find_assigned_function(e, name)
    if(!is.null(f)) return(eval(f, env))
  }
  stop("function ", name, " not found in app.R")
}
