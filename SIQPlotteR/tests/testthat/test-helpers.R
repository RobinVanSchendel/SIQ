library(dplyr)

# ---- findColumn (MUSIC auto-config, case-insensitive column lookup) ---------

findColumn <- load_app_function("findColumn")

test_that("findColumn matches regardless of case", {
  cols <- c("Alias", "Barcode", "Barcode_And_Replicate", "music_screen")
  expect_equal(findColumn(cols, "barcode"), "Barcode")
  expect_equal(findColumn(cols, "barcode_and_replicate"), "Barcode_And_Replicate")
  expect_equal(findColumn(cols, "BARCODE"), "Barcode")
})

test_that("findColumn returns the actual spelling, lowercase too", {
  expect_equal(findColumn(c("barcode"), "Barcode"), "barcode")
})

test_that("findColumn returns NA_character_ when absent or input empty", {
  expect_true(is.na(findColumn(c("Alias", "Sample"), "barcode")))
  expect_true(is.character(findColumn(c("Alias"), "barcode")))
  expect_true(is.na(findColumn(character(0), "barcode")))
})

test_that("findColumn does not match partial names", {
  expect_true(is.na(findColumn(c("Barcode_And_Replicate"), "barcode")))
  expect_true(is.na(findColumn(c("Barcode"), "barcode_and_replicate")))
})

test_that("findColumn returns the first of several case variants", {
  expect_equal(findColumn(c("BARCODE", "barcode"), "Barcode"), "BARCODE")
})

test_that("findColumn works with the '-' placeholder in group choices", {
  expect_equal(findColumn(c("-", "Barcode"), "barcode"), "Barcode")
  expect_true(is.na(findColumn("-", "barcode")))
})

# ---- siq_local_path ---------------------------------------------------------

siq_local_path <- load_app_function("siq_local_path")

with_path_env <- function(map, windows, code){
  env <- new.env(parent = globalenv())
  env$siq_db_config <- list(output_path_map = map)
  env$siq_is_windows <- windows
  f <- siq_local_path
  environment(f) <- env
  code(f)
}

test_that("siq_local_path swaps the share prefix case-insensitively", {
  with_path_env(list(from = "\\\\server\\share", to = "/mnt/share"), FALSE, function(f){
    expect_equal(f("\\\\SERVER\\Share\\run1\\out.xlsx"), "/mnt/share/run1/out.xlsx")
  })
})

test_that("siq_local_path leaves paths outside the share alone (apart from slashes)", {
  with_path_env(list(from = "\\\\server\\share", to = "/mnt/share"), FALSE, function(f){
    expect_equal(f("\\\\other\\x\\y"), "//other/x/y")
  })
})

test_that("siq_local_path is vectorised and handles empty input", {
  with_path_env(list(from = "\\\\s\\a", to = "/m"), FALSE, function(f){
    expect_equal(f(c("\\\\s\\a\\1", "\\\\t\\b")), c("/m/1", "//t/b"))
    expect_equal(f(character(0)), character(0))
  })
})

test_that("siq_local_path keeps backslashes on Windows and works without a map", {
  with_path_env(NULL, TRUE, function(f){
    expect_equal(f("\\\\s\\a\\1"), "\\\\s\\a\\1")
  })
  with_path_env(NULL, FALSE, function(f){
    expect_equal(f("a\\b"), "a/b")
  })
})

# ---- siq_bind_rows ----------------------------------------------------------

siq_bind_rows <- load_app_function("siq_bind_rows")
environment(siq_bind_rows) <- globalenv()   # needs bind_rows / coalesce from dplyr

test_that("siq_bind_rows binds identical-typed frames", {
  r <- siq_bind_rows(list(data.frame(a = 1:2, b = c("x", "y")),
                          data.frame(a = 3L, b = "z")))
  expect_equal(nrow(r), 3)
  expect_equal(r$a, 1:3)
  expect_equal(r$b, c("x", "y", "z"))
})

test_that("siq_bind_rows: integer and numeric mix becomes numeric", {
  r <- siq_bind_rows(list(data.frame(a = 1L), data.frame(a = 2.5)))
  expect_type(r$a, "double")
  expect_equal(r$a, c(1, 2.5))
})

test_that("siq_bind_rows: all-NA logical column does not block a text column", {
  r <- siq_bind_rows(list(data.frame(a = c(NA, NA)),
                          data.frame(a = "txt", stringsAsFactors = FALSE)))
  expect_type(r$a, "character")
  expect_equal(r$a[3], "txt")
})

test_that("siq_bind_rows: numbers mixed with text become character; NA becomes empty string", {
  r <- siq_bind_rows(list(data.frame(a = c(1, NA)),
                          data.frame(a = "t", stringsAsFactors = FALSE)))
  expect_type(r$a, "character")
  expect_equal(r$a, c("1", "", "t"))
})

test_that("siq_bind_rows: a column missing from one frame is filled with NA", {
  r <- siq_bind_rows(list(data.frame(a = 1), data.frame(a = 2, b = "q", stringsAsFactors = FALSE)))
  expect_equal(nrow(r), 2)
  expect_true(is.na(r$b[1]))
  expect_equal(r$b[2], "q")
})

test_that("siq_bind_rows: column that is NA everywhere stays logical", {
  r <- siq_bind_rows(list(data.frame(a = 1, b = NA), data.frame(a = 2, b = NA)))
  expect_type(r$b, "logical")
  expect_true(all(is.na(r$b)))
})

test_that("siq_bind_rows: single frame and empty list", {
  d <- data.frame(a = 1:2)
  expect_equal(siq_bind_rows(list(d))$a, 1:2)
  expect_equal(nrow(siq_bind_rows(list())), 0)
})
