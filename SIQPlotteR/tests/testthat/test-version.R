siq_version <- load_app_function("siq_version")

test_that("siq_version reads and trims the first line", {
  f <- tempfile()
  writeLines(c("  2.5 ", "ignored"), f)
  expect_equal(siq_version(f), "2.5")
})

test_that("siq_version falls back to 'unknown' for missing or empty files", {
  expect_equal(siq_version(tempfile()), "unknown")
  empty <- tempfile(); file.create(empty)
  expect_equal(siq_version(empty), "unknown")
  blank <- tempfile(); writeLines("   ", blank)
  expect_equal(siq_version(blank), "unknown")
})

test_that("SIQPlotteR/VERSION matches siq.version in SIQ/pom.xml", {
  app_dir <- dirname(app_path())
  pom <- file.path(app_dir, "..", "SIQ", "pom.xml")
  skip_if_not(file.exists(pom), "SIQ/pom.xml not available")
  m <- regmatches(readLines(pom, warn = FALSE), regexec("<siq.version>([^<]+)</siq.version>", readLines(pom, warn = FALSE)))
  pom_version <- Filter(length, m)[[1]][2]
  expect_equal(siq_version(file.path(app_dir, "VERSION")), pom_version)
})
