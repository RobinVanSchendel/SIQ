library(testthat)

# run from SIQPlotteR/: Rscript tests/testthat.R   (or testthat::test_dir("tests/testthat"))
test_dir("tests/testthat", reporter = "summary", stop_on_failure = TRUE)
