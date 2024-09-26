#
# 
# Author: rvanschendel
###############################################################################


#
# This is a Shiny web application. You can run the application by clicking
# the 'Run App' button above.
#
# Find out more about building applications with Shiny here:
#
#    http://shiny.rstudio.com/
#

library(shiny)
library(IRanges)
if(!require(lobstr)){
  install.packages("lobstr", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(lobstr)
}
if(!require(colourpicker)){
  install.packages("colourpicker", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(colourpicker)
}
if(!require(grid)){
  install.packages("grid", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(grid)
}
if(!require(gridExtra)){
  install.packages("gridExtra", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(gridExtra)
}
if(!require(ggplot2)){
  install.packages("ggplot2", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(ggplot2)
}
if(!require(readxl)){
  install.packages("readxl", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(readxl)
}
if(!require(shinyWidgets)){
  install.packages("shinyWidgets", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(shinyWidgets)
}
if(!require(dplyr)){
  install.packages("dplyr", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(dplyr)
}
if(!require(tidyr)){
  install.packages("tidyr", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(tidyr)
}
if(!require(RColorBrewer)){
  install.packages("RColorBrewer", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(RColorBrewer)
}
if(!require(sortable)){
  install.packages("sortable", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(sortable)
}
if(!require(ggpubr)){
  install.packages("ggpubr", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(ggpubr)
}
if(!require(ggrepel)){
  install.packages("ggrepel", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(ggrepel)
}
if(!require(DT)){
  install.packages("DT", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(DT)
}
if(!require(gplots)){
  install.packages("gplots", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(gplots)
}
if(!require(FactoMineR)){
  install.packages("FactoMineR", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(FactoMineR)
}
if(!require(factoextra)){
  install.packages("factoextra", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(factoextra)
}
if(!require(umap)){
  install.packages("umap", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(umap)
}
if(!require(data.table)){
  install.packages("data.table", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(data.table)
}
if(!require(spatstat)){
  install.packages("spatstat", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(spatstat)
}
if(!require(dbplyr)){
  install.packages("dbplyr", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(dbplyr)
}
if(!require(RSQLite)){
  install.packages("RSQLite", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(RSQLite)
}
if(!require(shinyauthr)){
  install.packages("shinyauthr", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(shinyauthr)
}
if(!require(sodium)){
  install.packages("sodium", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(sodium)
}
if(!require(heatmaply)){
  install.packages("heatmaply", repos = "https://mirror.lyrahosting.com/CRAN/")
  library(heatmaply)
}



source("user_base.R")

##Music DB name
dbname = "data/MBCrisprMBAgain_1_1.db"
dbnameSubScreen = "data/MBCrisprMBSubscreeen_Full_1.db"
dbNameSubScreenTUDelft = "data/MBCrisprMBSubscreeen_TUDelft_1.db"

options(shiny.maxRequestSize=4048*1024^2)

#exampleExcel = "data/20210305_093157_SIQ.xlsx"
#exampleExcel = "data/20220127_103430_SIQ_complete.xlsx"
exampleExcel = "data/20220609_220725_SIQ.xlsx"  ## for testing
#exampleExcel = "Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\GenomeScan104596\\Analysis\\20200928_GenomeScan104269_104596_NMS_part_for_siq_testing.xlsx"
#exampleExcel = "Z:\\Projects\\2023_HPRT_sites\\SIQ\\Old\\20230829_SIQ_NMS_total_gt1000_p404_405_mm.xlsx"
exampleData = read_excel(exampleExcel, sheet = "rawData", guess_max = 100000)

seleced_input = 2
selected_tab = "Tornado"

debug = FALSE

###test
TranslocationColorReal = "Translocation"


# Define UI for application that draws a histogram
ui <- fluidPage(
  
  # logout button
  div(class = "pull-right", shinyauthr::logoutUI(id = "logout")),
  
  # Sidebar with a slider input for number of bins 
  sidebarLayout(
    sidebarPanel(
      #h1("SIQPlotteR"),
      img(src="SIQ_title.png",width=200),
      radioButtons(
        "data_input", "",
        choices = 
          list("Load example SIQ paper data" = 1,
               "Upload file (TSV, Text, Excel)" = 2
          )
        ,
        selected = seleced_input),
      conditionalPanel(
        condition = "input.data_input=='2'",
        fileInput("file1",
                  "Select Excel or tab-separated File:",
                  accept=c(".xlsx",".txt")),
      ),
      radioButtons(
        "keepOverlap",
        "Filter reads by distance from expected cut site:",
        c("disabled","≤10bp", "≤2bp"),
        selected = "disabled",
        inline = T),
      
      pickerInput(
        inputId = "Types", 
        label = "Select Mutation Type(s):",
        choices = NULL,
        options = list(
          `actions-box` = TRUE, 
          size = 10,
          #`live-search`=TRUE,
          `selected-text-format` = "count > 3"
        ), 
        multiple = TRUE
      ),
      pickerInput(
        inputId = "Aliases", 
        label = "Select Sample(s):",
        choices = NULL,
        options = list(
          `actions-box` = TRUE, 
          size = 10,
          `live-search`=TRUE,
          `selected-text-format` = "count > 3"
        ), 
        multiple = TRUE
      ),
      pickerInput(
        inputId = "Subject", 
        label = "Select Target(s):",
        choices = NULL,
        options = list(
          `actions-box` = TRUE, 
          size = 10,
          `live-search`=TRUE,
          `selected-text-format` = "count > 3"
        ), 
        multiple = TRUE
      ),
      selectInput(
        "GroupColumn",
        "Select Grouping/Replicate Column:", 
        c("-"),
        selected = "-"
      ),
      sliderInput("plotHeight", "Plot height (# pixels): ",
                  value = 600,
                  min = 100,
                  max = 5000,
                  step = 50
      ),
      sliderInput("plotWidth", "Plot width (# pixels):", 
                  value = 800,
                  min = 100,
                  max = 5000,
                  step = 50
      ),
      conditionalPanel(
        condition = "input.tabs == 'Efficiency'",
        checkboxInput(inputId = "mutFreqBoxPlot", label = "Show boxplot",value = T),
        checkboxInput(inputId = "mutFreqViolinPlot", label = "Show violin", value = T),
        checkboxInput(inputId = "mutFreqTableSummary", label = "Show data summary", value = T)
      ),
      ##type plot only ###
      conditionalPanel(
        condition = "input.tabs == 'Type'",
        downloadButton('exportType',"Export to PDF"),
        #uiOutput("subject_selectionType"),
        radioButtons(
          "fraction",
          "Set fraction:",
          c("relative","absolute"),
          inline = T),
        radioButtons(
          "datatableFraction",
          "Set counts:",
          c("relative","absolute"),
          inline = T),
        radioButtons(
          "datatableGrouped",
          "Set display:",
          c("per Alias","by Group"),
          inline = T),
      ),
      ##type plot only ###
      conditionalPanel(
        condition = "input.tabs == 'Homology'",
        downloadButton('exportHom',"Export to PDF"),
      ),
      conditionalPanel(
        condition = "input.tabs == '1bp insertion'",
        downloadButton('export1bpInsertion',"Export to PDF"),
      ),
      
      
      ###hom insert plot
      conditionalPanel(
        condition = "input.tabs == 'HomologyInsert'",
        #downloadButton('exportHom',"Export to PDF"),
        #uiOutput("subject_selectionHomologyIns"),
        uiOutput("homRange"),
        radioButtons(
          "display",
          "display selection:",
          c("total","fraction"),
          selected = "total",
          inline = TRUE),
      ),
      ##Target plot only ###
      #conditionalPanel(
      #  condition = "input.tabs == 'Target'",
      #),
      ##sizeDiff plot only ###
      conditionalPanel(
        condition = "input.tabs == 'SizeDiff'",
        radioButtons(
          "fractionSizeDiff",
          "Set fraction:",
          c("relative","absolute"),
          inline = T),
        uiOutput("xminmaxRangeSizeDiff"),
        radioButtons(
          "colors",
          "# of colors:",
          c(3,5),
          selected = 3,
          inline = TRUE),
        downloadButton('exportSizeDiff',"Export to PDF"),
      ),
      ##TORNADO plot only stuff##
      conditionalPanel(
        condition = "input.tabs == 'Tornado'",
        uiOutput("column_selection"),
        sliderInput("nrCols",
                    "Number of columns:",
                    min = 1,
                    max = 10,
                    value = 4),
        sliderInput(inputId = "xminmaxRange",
                    "X-axis range:",
                    min = -1000,
                    max = 1000,
                    value = c(-300,300),
                    step = 10
        ),
        #uiOutput("xminmaxRange"),
        #uiOutput("xmax"),
        radioButtons(
          "yaxis",
          "Set y-value",
          c(1,"not set", "max of plots"),
          inline = T),
        radioButtons(
          "Sort",
          "Select Sort:",
          c("Start position","End position","Mid position","Size","Type","Closest position"),
          selected = "Start position",
          inline = TRUE),
        radioButtons(
          "Type",
          "Select Tornado type:",
          c("Regular","Inverted","Inverted - show middle", "Closest to 0"),
          selected = "Regular",
          inline = TRUE),
        radioButtons(
          "YaxisValue",
          "Y-axis value:",
          c("Fraction","#Reads"),
          selected = "Fraction",
          inline = TRUE),
        radioButtons(
          "tornadoSubjectAlias",
          "Order by:",
          c("Target - Alias","Alias - Target"),
          inline = TRUE),
        checkboxInput(
          "expandYaxis",
          "Expand y-axis",
          value = T
        ),
        downloadButton('export',"Export to PDF"),
      ),
      conditionalPanel(
        condition = "input.tabs == 'Size'",
        checkboxInput("ymaxRangeDiffLimitaxis","Set manual y-axis", value = F),
        uiOutput("ymaxRangeDiff"),
        radioButtons(
          "Column",
          "Select Column to plot :",
          c("both","delSize","insSize"),
          selected = "both",
          inline = TRUE),
        radioButtons(
          "TypePlot",
          "Select type of plot :",
          c("heatmap","violin", "lineplot", "median size", "boxplot"),
          selected = "heatmap",
          inline = TRUE),
        radioButtons(
          "fractionSize",
          "Set fraction:",
          c("relative","absolute"),
          inline = T),
        downloadButton('exportSize',"Export to PDF"),
      ),
      conditionalPanel(
        condition = "input.tabs == 'SNVs'",
        radioButtons(
          "sepBySubject",
          "Plot type:",
          c("Combined plot","Per Alias"),
          selected = "Per Alias",
          inline = T),
        radioButtons(
          "yaxisSNV",
          "Set y-value",
          c("not set", "max of plots"),
          inline = T),
        sliderInput(inputId = "snvrange",
                    "SNVS of size:",
                    min = 1,
                    max = 5,
                    value = 1,
                    step = 1
        ),
        checkboxInput("snvrangesplit","Split SNVs ≥2bp", value = F),
        downloadButton('exportSNVs',"Export to PDF"),
      ),
      
      conditionalPanel(
        condition = "input.tabs == 'Target'",
        downloadButton('exportTarget',"Export to PDF"),
      ),
      conditionalPanel(
        condition = "input.tabs == 'Templated Insertions'",
        radioButtons(
          "sortTypeTI",
          "Select Sort:",
          c("Start position","End position"),
          selected = "Start position",
          inline = TRUE),
        radioButtons(
          "yaxisTINS",
          "Set y-value",
          c("not set", "max of plots", "max of junction"),
          inline = T),
        radioButtons(
          "posTINS",
          "Set calculated position",
          c("relative to junction", "relative to reference"),
          inline = T),
        downloadButton('exportTI',"Export to PDF"),
      ),
      conditionalPanel(
        condition = "input.tabs == 'Sample Info'",
        radioButtons("sampleInfoRelative",
                     "Set counts:",
                     c("relative","absolute"),
                     inline = T),
        downloadButton('exportSampleInfo',"Export to PDF"),
      ),
      conditionalPanel(
        condition = "input.tabs == 'Alleles'",
        radioButtons("alleleFractionBasedOn",
                     "Set fraction:",
                     c("relative","absolute","mutagenic"),
                     inline = T),
        numericInput("alleleTopOutcomes","Set the number of alleles to be shown:", 
                     min =0, max=100, value = 10),
        numericInput("alleleDecimals","Set the number of decimals in the table:", 
                     min =3, max=20, value = 4),
        radioButtons("alleleTopOutcomesChoice",
                     "Set top alleles based on:",
                     c("Total","Sample"),
                     inline = T),
        downloadButton('exportAlleles',"Export to PDF")
      ),
      conditionalPanel(
        condition = "input.tabs == 'Table'",
        radioButtons("tableFraction",
                     "Set fraction of events:",
                     c("Absolute","Relative"),
                     inline = TRUE),
        checkboxInput("tableReference",
                      "Add Reference to table",
                      value = F),
        pickerInput(
          inputId = "tableColumn", 
          label = "Show columns:",
          choices = NULL,
          options = list(
            `actions-box` = TRUE, 
            size = 10,
            `live-search`=TRUE,
            `selected-text-format` = "count > 3"
          ), 
          multiple = T
        ),
        numericInput("alleleDecimalsTable","Set the number of decimals in the table (-1 = all):", 
                     min=-1, max=20, value = -1)
      ),
      conditionalPanel(
        condition = "input.alleleTopOutcomesChoice == 'Sample'",
        pickerInput(
          inputId = "alleleTopOutcomesSample", 
          label = "Show outcomes based on Sample:",
          choices = NULL,
          options = list(
            `actions-box` = TRUE, 
            size = 10,
            `live-search`=TRUE,
            `selected-text-format` = "count > 3"
          ), 
          multiple = F
        ),
      ),
      conditionalPanel(
        condition = "input.tabs == 'HeatmapEnds'",
        radioButtons("HeatmapEndsRelative",
                     "Set counts:",
                     c("relative","absolute"),
                     inline = T),
        radioButtons("HeatmapEndsFilter",
                     "Set max distance to Cas9 target site(s):",
                     c("disabled","-2bp"),
                     inline = T)
      ),
      conditionalPanel(
        condition = "input.tabs == 'Alleles'",
        sliderInput("alleleOutcomeTable","Set the size around the flanks:", 
                     min=-500, max=500, value = c(-20,20), step = 5),
      ),
      conditionalPanel(
        condition = "input.tabs == 'Outcomes'",
        dropdown(
          label = "Outcomes",
          icon = icon("barcode"),
          tags$h3("How to combine outcomes"),
          checkboxGroupInput(
            inputId = "outcomeDEL",
            label = "DEL",
            choices = c("by size" = "size","by homology" = "homology" ,"all combined" = "allcombined"),
            inline = T,
          ),
          checkboxGroupInput(
            inputId = "outcomeDELINS",
            label = "DELINS",
            choices = c( "by size" = "size","all combined" = "allcombined"),
            inline = T,
          ),
        ),
        pickerInput(
          inputId = "controls", 
          label = "Select Control Sample(s):",
          choices = NULL,
          options = list(
            `actions-box` = TRUE, 
            size = 10,
            `live-search`=TRUE,
            `selected-text-format` = "count > 3"
          ), 
          multiple = T
        ),
        pickerInput(
          inputId = "controlsX", 
          label = "Select Type X:",
          choices = NULL,
          options = list(
            `actions-box` = TRUE, 
            size = 10,
            `live-search`=TRUE,
            `selected-text-format` = "count > 3"
          ), 
          multiple = F
        ),
        pickerInput(
          inputId = "controlsY", 
          label = "Select Type(s) Y:",
          choices = NULL,
          options = list(
            `actions-box` = TRUE, 
            size = 10,
            `live-search`=TRUE,
            `selected-text-format` = "count > 3"
          ), 
          multiple = T
        ),
        numericInput(
          inputId = "numberOfOutcomes",
          label = "Set number of outcomes",
          value = 10,
          min = 1,
          max = 100
        ),
        radioButtons(
          "OutcomeResolve",
          "What to do with outcomes?",
          c("nothing","remove non-informative","merge non-informative"),
          selected = "nothing",
          inline = TRUE),
        radioButtons(
          "OutcomeCalc",
          "Calculate frequency by?",
          c("selected types" = 0,"mutagenic reads" = 1),
          selected = 0,
          inline = TRUE),
        conditionalPanel(
          condition = "input.OutcomeResolve == 'remove non-informative'",
          numericInput(
            inputId = "OutcomeMinimumPerGroup",
            label = "Set minimum number of Alias per group",
            value = 1,
            min = 1,
            max = Inf
          ),
        ),
        radioButtons(
          "typePlotOutcome",
          "Select type of plot :",
          c("line","heatmap","pca","XY", "umap","volcano"),
          selected = "volcano",
          inline = TRUE),
        radioButtons(
          "outcome_volcano_type",
          "Select calculation type :",
          c("mean", "log2fraction"),
          selected = "mean",
          inline = TRUE),
        checkboxInput(
          "OutcomeLegend",
          "Legend",
          value = F),
        numericInput(
          inputId = "OutcomeSize",
          label = "Set text size",
          value = 4,
          min = 0,
          max = 20
        ),
        numericInput(
          inputId = "OutcomeDotSize",
          label = "Set dot size",
          value = 4,
          min = 1,
          max = 20
        ),
        numericInput(
          inputId = "OutcomeStrokeSize",
          label = "Set stroke size",
          value = 1,
          min = 0.25,
          max = 20
        ),
        numericInput(
          inputId = "OutcomeAlpha",
          label = "Set alpha",
          value = 1,
          min = 0,
          max = 1
        ),
        sliderInput("OutcomeNrCols",
                    "Number of columns:",
                    min = 1,
                    max = 10,
                    value = 2),
        numericInput(
          inputId = "OutcomePartQuartile",
          label = "Set number of SDs",
          value = 3,
          min = 1,
          max = 100
        ),
        numericInput(
          inputId = "heatmapLimits",
          label = "Set log2 limits",
          value = 5,
          min = 0,
          max = 100
        ),
        checkboxInput(
          "OutcomeUmapLabels",
          "umap show only 1 label per group",
          value = F),
        checkboxInput(
          "OutcomePCAScale",
          "Scale",
          value = F),
        numericInput(
          inputId = "pca_max_overlap",
          label = "Set max overlap for labels:",
          value = 100,
          step = 10
        ),
        downloadButton('exportOutcome',"Export to PDF"),
      ),
      h3("Advanced Settings"),
      selectInput(
        "AliasColumn",
        "Select Sample Column:", 
        c("Alias"),
        selected = "Alias"
      ),
      checkboxGroupInput(
        inputId = "CollapseTypes", 
        label = "how to treat Type(s):",
        choices = c("collapse TDs" = "collapseTD", "split TINS" = "splitTINS" ),
        inline = T
      ),
      checkboxInput("facet_wrap",
                    "Separate targets",
                    value=T),
      uiOutput("minEvents"),
      radioButtons(
        "minReadsOn",
        "Filter number of reads on:",
        c("all reads","mutagenic reads", "selected types"),
        inline = T
      ),
      radioButtons(
        "homologyColumn",
        "homologyLength:",
        c("homologyLength","homologyLengthMismatch10%"),
        inline = TRUE),
      uiOutput("type_list"),
      uiOutput("multi_list"),
      uiOutput("multi_list_group"),
      uiOutput("color_test"),
    ),
    
    # Show a plot of the generated distribution
    mainPanel(
      tabsetPanel(id="tabs", type = "tabs",
                  tabPanel("Tornado",
                           h3("Tornado Plot"),
                           p("A newly designed interactive plot to show all different mutation types and weights in one.
														The plot can be customized by altering the settings on the left. NOTE: max 100 plots are shown"),
                           uiOutput("ui_plot"),
                           uiOutput("hover_info")),
                  tabPanel("Alleles",
                           h3("Mutational Outcomes"),
                           uiOutput("ui_alleles"),
                           div(DT::dataTableOutput("allele_data",width = 8), style = "font-family: Courier,courier")
                           ),
                  tabPanel("Efficiency",h3("Targeting efficiency"),
                           p("For each sample the fraction of non wild-type reads are shown."),
                           uiOutput("ui_info"),
                           DT::dataTableOutput("mutFreqTable",width = 8)
                           ),
                  tabPanel("Type",
                           h3("Mutation types"),
                           p("for each sample the mutation types are shown in this interactive plot. Both relative and absolute fractions can be shown."),
                           uiOutput("ui_typeplot"),
                           DT::dataTableOutput("plot1_data",width = 8)),
                  tabPanel("Homology",
                           h3("Homology plot - Deletions and Tandem duplications only"),
                           p("the homology that was used for repair. NOTE: only deletions and tandem duplications can be included in this plot."),
                           uiOutput("ui_homplot"),
                           DT::dataTableOutput("plot_hom_data",width = 8)),
                  tabPanel("Size",
                           h3("Size Plot"),
                           p("a representation of the sizes of all events. Deletion size or Insertion size can be specified in a heatmap or violin plot representation."),
                           uiOutput("ui_sizeplot"),
                  ),
                  tabPanel("SNVs",
                           h3("Single-nucleotide variations (SNVs)"),
                           p("The frequency of SNVs at each position is shown. SNVs can be combined in one plot to compare rates at each position.
														NOTE: the SNV fraction is based on the mutation type(s) selected."),
                           uiOutput("ui_snvplot")),
                  #tabPanel("Correlation",uiOutput("ui_corplot")),
                  tabPanel("Target", 
                           h3("Target Alteration plot"),
                           p("For each location relative to the target site the fraction of alteration is shown. Note: Insertions and TDs are not included in the plot."),
                           uiOutput("ui_SizeFreq")),
                  tabPanel("Sample Info",
                           h3("Sample Information plots"),
                           p("This page contains a number of plots to help you assess which samples should be included in the analysis.
														Samples with low number of reads can be excluded using the slider on the left.
														A second plot is shown with the fraction of correct reads from the total of merged reads. 
														This gives an indication of how many merged reads pass various filter (e.g. primers included, minimum quality).
														A third plot shows for all the samples analyzed the number of merged reads from the total reads."
                           ),
                           uiOutput("ui_statplot")),
                  tabPanel("Templated Insertions",
                           h3("Tornado Plot - Templated Insertions"),
                           p("A newly designed interactive plot to show the origin of templated flank insertions.
														The plot can be customized by altering the settings on the left.
                             NOTE: only deletions with templated inserts are shown"),
                           uiOutput("ui_tornadplot_flankinsertions")),
                  tabPanel("Outcomes",
                           h3("Outcomes"),
                           p("The outcomes plot is specifically designed to view all your data simultaneously. Especially when you have many samples
                           this is a powerful method to look at differences between samples. It currently
                             supports: UMAP, PCA, XY scatter, heatmap  and Top X alleles."),
                           uiOutput("ui_outcome"),
                           DT::dataTableOutput("outcome_data",width = 8)
                           ),
                  tabPanel("HeatmapEnds",
                           h3("HeatmapEnds"),
                           p("Not sure yet what will be put here"),
                           uiOutput("ui_heatmapend")
                  ),
									tabPanel("1bp insertion",
									         h3("1bp insertion"),
									         p("display the contribution of the 1bp insertions"),
									         uiOutput("ui_1bpinsertion"),
									         DT::dataTableOutput("plot_1bp_data",width = 8)
									),
									tabPanel("Table",
									         h3("Note: to download all data, please select Show 'All' entries (slow on large sets)", style="color:red"),
									         DT::dataTableOutput("datatable",width = "100%"),
									),
                  tabPanel("About",
                           h3("About SIQPlotteR"),
                           p("The SIQPlotteR web app is a dedicated tool for exploring data that has been generated by SIQ. Users can explore their data by generating different kind
                              of plots. Every plot can be adjusted in terms of filtering of type of events, changing sort order, altering colors. There is also the possibility to 
                             explore data from the SIQ paper."),
                           p("You can find more information on SIQ and SIQPlotter, including a detailed ",shiny::tags$b("user guide")," and ",
                             shiny::tags$b("video tutorials "), a(href="https://github.com/RobinVanSchendel/SIQ", "here", target="_blank")),
                           p("SIQPlotteR Version - 1.0 created by Robin van Schendel"),
                           a(href="https://github.com/RobinVanSchendel/SIQ/releases","Download SIQ here", target="_blank")),
                  tabPanel(
                    id = 'login_tab',
                    title = 'Login',
                    shinyauthr::loginUI("login")
                  ),
                  selected = selected_tab
      )
    )
  ),
  fluidRow(
    tableOutput("your_data")
  )
)

# Define server logic required to draw a histogram
server <- function(input, output, session) {
  
  
  observe({
    req(credentials())
    if(credentials()$user_auth){
      updateRadioButtons(session,
                         inputId = "data_input",
                         choices =
                           list("Load example SIQ paper data" = 1,
                                "Upload file (TSV, Text, Excel)" = 2,
                                "MUSIC screen" = 3,
                                "MUSIC subscreen" = 4,
                                "MUSIC subscreen TUDelft" = 5
                           ),
                         selected = as.numeric(input$data_input),
                         )
    }
    else{
      updateRadioButtons(session,
                         inputId = "data_input",
                         choices =
                           list("Load example SIQ paper data" = 1,
                                "Upload file (TSV, Text, Excel)" = 2
                           ),
                         selected = seleced_input,
      )
    }
      
  })
  
  credentials <- shinyauthr::loginServer(
    id = "login",
    data = user_base,
    user_col = user,
    pwd_col = password,
    sodium_hashed = TRUE,
    log_out = reactive(logout_init())
  )
  
  # Logout to hide
  logout_init <- shinyauthr::logoutServer(
    id = "logout",
    active = reactive(credentials()$user_auth)
  )
  
  hardcodedTypesDFnonreactive <- function(){
    ###THIS NEEDS TO BE RESTRUCTURED
    bp0Color = "#DAE8F5"
    bp1Color = "#B9D5E9"
    bp2Color = "#88BDDC"
    bp3Color = "#539CCB"
    bp4Color = "#2A7ABA"
    bp5Color = "#0E559F"
    bp6Color = "#0e2b9f"
    snvColor = "#8B4500"
    wtColor = "green"
    insColor = "#B3B3B3"
    insColor1 = "#9400D3"
    delColor = "#1E90FF"
    delinsColor = "#4D4D4D"
    hdrColor = "#FF6A6A"
    hdr1mmColor = "#F08D8D"
    tinsColor = "#FF0000"
    tinsColorRC = "#AA0000"
    tdColor = "#FF7F00"
    tdCColor = "#F4A460"
    delins_dual = "grey50"
    delins_snv = "grey30"
    ##
    colourCode <- c("WT" = wtColor, "DELETION" = delColor, "INSERTION" = insColor, "INSERTION_1bp" = insColor1, "DELINS" = delinsColor,
                    "TINS" = tinsColor, "TANDEMDUPLICATION" = tdColor, 
                    "TANDEMDUPLICATION_COMPOUND" = tdCColor,"SNV" = snvColor, "HDR" = hdrColor,"HDR1MM" = hdr1mmColor, "0bp_homology" = bp0Color,
                    "1bp_homology" = bp1Color,"2bp_homology" = bp2Color,"3bp_homology" = bp3Color,
                    "4bp_homology" = bp4Color, "5-15bp_homology" = bp5Color, "15bp_homology" = bp6Color, "white" = "white",
                    "TINS_FW" = tinsColor,"TINS_RC" = tinsColorRC, "DELINS_DUAL" = delins_dual, "DELINS_SNV" = delins_snv
    ) 
    
    hardcodedTypes = c("WT" = "wild-type","INSERTION" = "insertion", "INSERTION_1bp" = "1bp insertion",
                       "DELINS" = "deletion with insert", "TINS" = "deletion with templated insert","TANDEMDUPLICATION" = "tandem duplication (td)",         
                       "TANDEMDUPLICATION_COMPOUND" = "tandem duplication plus (td+)", "SNV" = "snv", "0bp_homology" = "deletion/td 0bp microhomology",              
                       "1bp_homology" = "deletion/td 1bp microhomology", "2bp_homology" = "deletion/td 2bp microhomology",
                       "3bp_homology" = "deletion/td 3bp microhomology", "4bp_homology" = "deletion/td 4bp microhomology",
                       "5-15bp_homology" = "deletion/td 5-15bp microhomology", "DELETION" = "deletion", "HDR" = "homology-directed repair"
                       ,"HDR1MM" = "homology-directed repair mismatch", "15bp_homology" = "deletion/td >15bp microhomology",
                       "TINS_FW" = "deletion with templated insert (FW)","TINS_RC" = "deletion with templated insert (RC)",
                       "DELINS_DUAL" = "delins (likely two events)", "DELINS_SNV" = "deletion plus snv"
                       )
    
    hardcodedTypesDF = data.frame(names(hardcodedTypes), unname(hardcodedTypes), stringsAsFactors = FALSE)
    colnames(hardcodedTypesDF) = c("Type", "Text")
    colourCodeDF = data.frame(names(colourCode), unname(colourCode), stringsAsFactors = FALSE)
    colnames(colourCodeDF) = c("Type", "Color")
    
    hardcodedTypesDF = merge(hardcodedTypesDF, colourCodeDF, by = "Type")
    return(hardcodedTypesDF)
  }
  
  hardcodedTypesDF <- reactive({
    hardcodedTypesDF = hardcodedTypesDFnonreactive()
    #set colours from input
    for(colour in hardcodedTypesDF$Type){
      if(!is.null(input[[paste0(colour,"Picker")]])){
        #print(paste("print",colour,input[[paste0(colour,"Picker")]]))
        hardcodedTypesDF$Color[hardcodedTypesDF$Type==colour] <- input[[paste0(colour,"Picker")]] 
      }
    }
    hardcodedTypesDF
  })
  output$datatable <- DT::renderDataTable({
    req(filter_in_data())
    el = filter_in_data()
    
    el = el |>
      select_at(input$tableColumn)
    
    ##remove the Reference?
    if(!input$tableReference){
      el = el |> filter(Type != "Reference")
    }
    
    ##display the data as absolute or as fraction
    if(input$tableFraction == "Relative"){
      el = el |> group_by(Subject, Alias) |>
        mutate(fraction = fraction/sum(fraction))
    }
    
    ##added a safety for the colnames to be present
    if(input$alleleDecimalsTable != -1 & "fraction" %in% colnames(el)){
      el = el |> mutate(fraction = round(fraction,input$alleleDecimalsTable))
    }
    
    ##change the order a bit
    ordered = c("Subject","Alias","fraction","countEvents","delSize", "insSize", "Type")
    ordered = intersect(ordered, input$tableColumn)
    
    el = el %>% relocate(all_of(ordered))
    
    dt = DT::datatable(el,rownames = FALSE,extensions = 'Buttons', 
                       filter = "top",
                       class = "display nowrap",
                       options = list(
                         pageLength = 10,
                         lengthMenu = list( c(10,25,50,100, 1000, 5000, -1) # declare values
                                            , c(10,25,50,100, 1000, 5000, "All")), # declare titles
                         scrollX = TRUE,  
                         scrollY = TRUE,
                         autoWidth = TRUE,
                         dom = 'lftpB',
                         buttons = 
                           list("copy", "excel","csv")
                       ))
    dt
  })
  
  d_xminmaxRange <- reactive({
    input$xminmaxRange
  }) %>% debounce(1000)
  
  d_minEvents <- reactive({
    input$minEvents
  }) %>% debounce(500)
  
  in_data <- reactive({
    req(input$data_input)
    if(input$data_input == 1){
      el = exampleData
    }
    ##upload a file
    else if(input$data_input == 2){
      fileNameXLS = input$file1
      req(fileNameXLS)
      if(grepl('xlsx$', fileNameXLS$datapath)){
        sheets = excel_sheets(fileNameXLS$datapath)
        sheet = sheets[1]
        if("rawData" %in% sheets){
          sheet = "rawData"
        }
        el = read_excel(fileNameXLS$datapath, sheet = sheet, guess_max = 100000)
      }
      else{
        ## <200Mb
        #if(fileNameXLS$size<200*1024*1024){
          el = read.csv(fileNameXLS$datapath, header=T, stringsAsFactors = FALSE, sep = "\t")
        #}
        #else{
        #  start_time = start_time <- Sys.time()
        #  el = fread(fileNameXLS$datapath, header=T, stringsAsFactors = FALSE, data.table = F)
        #  end_time <- Sys.time()
        #  print(paste("fread",end_time-start_time, "seconds"))
        #}
      }
    }
    ## MUSIC screen and subscreen
    ##all >2 is a DB
    else if(input$data_input > 2){
      
      dbnameCurrent = get_current_dbname()
      con <- dbConnect(RSQLite::SQLite(), dbname = dbnameCurrent)
      geneTable <- tbl(con, "genes")
    
      #el = fread(file, header = T, stringsAsFactors = FALSE, data.table = FALSE)
      el = geneTable %>% collect()
      el = el %>% mutate(Subject = Alias)
      
      dbDisconnect(conn = con)
      
      ##needed to not break down the process below
      el$Type = "dummy"
      el$insSize = 0
    }
    
    
    if("Remarks" %in% colnames(el)){
      el = el %>% filter(is.na(Remarks) | Remarks == "NA")
    }
    el = el[el$Type!="",]
    el$Alias <- as.character(el$Alias)
    
    
    if(!"countEvents" %in% colnames(el)){
      el$countEvents = 1
      el$fraction = 1
      ##the ref seq needs to have 0 count
    }
    if("Name" %in% colnames(el)){
      el = el %>% mutate(countEvents = ifelse(el$Name == "wt_query",0, countEvents))
      el = el %>% mutate(fraction = ifelse(el$Name == "wt_query",0, fraction))
    }
    #overwrite insertions of 1bp
    el = el %>% mutate(Type = ifelse(Type == "INSERTION" & insSize == 1,"INSERTION_1bp",Type))
    
    #collapse TDs?
    if("collapseTD" %in% input$CollapseTypes){
      el = el %>% mutate(Type = ifelse(Type == "TANDEMDUPLICATION" | Type == "TANDEMDUPLICATION_COMPOUND","INSERTION",Type))
    }
    ##somehow that makes the type dissappear probably move to another location!
    el = split_tins(el)

    print("endOf in_data")
    return(el)
  })
  
  split_tins <- function(df){
    ##somehow that makes the type dissappear probably move to another location!
    if("splitTINS" %in% input$CollapseTypes & "isFirstHit" %in% colnames(df)){
      df = df %>%
        mutate(rc = ifelse(grepl("rc",isFirstHit),"RC","FW")) %>% 
        mutate(Type = ifelse(Type == "TINS",paste0(Type,"_",rc),Type)) %>%
        select(-rc)
    }  
    return(df)
  }
  
  get_current_dbname <- function(){
    if(input$data_input == 3){
      return(dbname)
    }
    if(input$data_input == 4){
      return(dbnameSubScreen)
    }
    if(input$data_input == 5){
      return(dbNameSubScreenTUDelft)
    }
    stop("Not an option")
  }
    
  pre_pre_filter_in_data <- reactive({
    req(in_data())
    if(!is.null(input$AliasColumn)){
      print(paste("startOf in_data",input$AliasColumn))
    } else{
      print(paste("startOf in_data","null"))
    }
    el = in_data()
    ##overwrite Alias column if requested
    if(!is.null(input$AliasColumn) && input$AliasColumn %in% colnames(el)){
      el$Alias = el[[input$AliasColumn]]
    }
    #make sure the fractions will be set to 1 here already
    el = el %>% group_by(Alias, Subject) %>% 
      mutate(totalFraction = sum(fraction), fraction = fraction/totalFraction)
    return(el)
    
  })
  pre_filter_in_data <- reactive({
    req(pre_pre_filter_in_data())
    print("pre_filter_in_data")
    el = pre_pre_filter_in_data()
    #filter data if required
    if(input$keepOverlap!="disabled"){
      if(input$keepOverlap == "≤2bp"){
        size = 2
      }
      else{
        size = 10
      }
      #incorrect for TDs => fixed
      #+1 needed
      
      el = el[el$delRelativeStartRight<=size & el$delRelativeEnd>=-size+1,]
      #for SNVs be more stringent
      el = el[el$Type != "SNV" | (el$Type == "SNV" & ((el$delRelativeStartRight<=size & el$delRelativeStartRight>=-size
                                                       &el$delRelativeStartRight<=size) | 
                                                        (el$delRelativeEnd>=-size+1 & el$delRelativeEnd<=size))),]
    }
    return(el)
  })
  
  ##function to retrieve the mutagenic fraction per Alias/Subject
  ##hooks into pre_filter_in_data as all events are still in here
  mutagenic_fractions <- reactive({
    req(pre_filter_in_data())
    req(input$Aliases)
    df = pre_filter_in_data()
    df = df %>% filter(Type != "WT") %>%
      group_by(Subject, Alias) %>%
      summarise(mutagenicFraction = sum(fraction))
    df
  })
  
  total_reads <- reactive({
    req(pre_filter_in_data())
    req(input$Aliases)
    df = pre_filter_in_data()
    group_col = c("Subject", "Alias")
    if(is_grouped()){
      group_col = c(group_col,get_group_column())
    }
    df = df %>% group_by_at(group_col) %>%
      summarise(counts = sum(countEvents), counts_mut = sum(countEvents[Type!="WT"]),
                fraction_mut = sum(fraction[Type!="WT"]))
    df
  })
  
  get_db_data <- reactive({
    dbnameCur = get_current_dbname()
    
    con <- dbConnect(RSQLite::SQLite(), dbname = dbnameCur)
    geneTable <- tbl(con, "tornado")
    
    genes = input$Aliases
    print("quering DB...")
    el = geneTable %>% filter(!!as.symbol(input$AliasColumn) %in% genes) %>% collect()
    
    ##split tins if needed
    
    print("quering done...")
    dbDisconnect(conn = con)
    el
  })
  
  
  filter_in_data <- reactive({
    req(pre_filter_in_data())
    req(input$Aliases)
    
    if((input$data_input > 2) & input$AliasColumn == "Alias"){
      return()
    }
    ##remove aliases not plotted
    el = pre_filter_in_data()
    
    ##so now load in the data
    if(input$data_input > 2){
      el = get_db_data()
      
      ##alter following columns
      ##change this as this is undesired in the end
      el = el %>% 
        mutate(Subject = Alias) %>%
        mutate(Alias = !!as.symbol(input$AliasColumn)) %>%
        mutate(Type = SubType)
      
      el = split_tins(el)
      
      el = el %>% group_by(Alias) %>% mutate(totalFraction = sum(fraction))
    }
    
    
    #rename the references accordingly
    refType = "Reference"
    el = el %>% mutate(Type = ifelse(countEvents == 0 & Type == "WT",refType,Type))
    
    el = el[el$Alias %in% input$Aliases & el$Type %in% c(input$Types,refType) & el$Subject %in% input$Subject,]
    
    ##alter 1bp insertions
    #if("getHomologyColor" %in% colnames(el)){
      el = el %>% mutate(getHomologyColor = ifelse(Type == "INSERTION_1bp","INSERTION_1bp",getHomologyColor))
    #}
    
    
    ##add dummy rows
    aliases = unique(el$Alias)
    remAliases = setdiff(input$Aliases, aliases)
    ##do not do this for the MUSIC screen as that will remove the entire data frame
    if(input$data_input != 3 && length(aliases)>0 && length(remAliases)>0){
      req(in_stat())
      dummies = in_stat()[in_stat()$Alias %in% remAliases,]
      bind_rows(el,dummies)
    }
    
    ##alter homology Column as required
    if(input$homologyColumn == "homologyLengthMismatch10%"){
      ##only update the homologies if the mismatch amount is larger
      if("homologyLengthMismatch10.ref" %in% colnames(el)){
        el = el %>% mutate(
          homologyLength = ifelse( homologyLengthMismatch10.ref > homologyLength,homologyLengthMismatch10.ref,homologyLength) 
        )
      }
      else{
        el = el %>% mutate(
          homologyLength = ifelse( `homologyLengthMismatch10%ref` > homologyLength,`homologyLengthMismatch10%ref`,homologyLength) 
        )
      }
    }
    
    print(paste("filter_in_data",Sys.time()))
    return(el)
  })
  
  in_stat <- reactive({
    if(input$data_input == 1){
      fileNameXLS = exampleExcel
    }else{
      fileNameXLS = input$file1$datapath
    }
    if(!is.null(fileNameXLS)){
      if(grepl('xlsx$', fileNameXLS)){
        sheets = excel_sheets(fileNameXLS)
        if("Information" %in% sheets){
          el = read_excel(fileNameXLS, sheet = "Information")
          if("File" %in% colnames(el)){
            el$File = gsub(".fastq.gz","",el$File)
            el$File = gsub(".assembled","",el$File)
            el$File = gsub(".fastq","",el$File)
            el$File = gsub(".extendedFrags","",el$File)
            return (el)
          }
        }
      }
    }
    return(NULL)
  })
  
  ##for the plots
  plotsForDownload <- reactiveValues(tornados=NULL, homs=NULL,sizeDiffs=NULL,types=NULL, size=NULL
                                     , snvs=NULL, target=NULL, tornadoTI=NULL, tornadoTIcols=NULL, outcomes=NULL, samples=NULL,
                                     alleles=NULL, plot1bpInsertion = NULL)
  applyColor <- function(el){
    start_time <- Sys.time()
    if(!is.data.frame(el)){
      return()
    }
    print("applyColor")
    el = el %>% mutate(getHomologyColor = case_when(
      Type=="SNV" ~ "SNV",
      Type=="WT" ~ "WT",
      Type=="INSERTION_1bp" ~ "INSERTION_1bp",
      Type=="INSERTION" ~ "INSERTION",
      Type=="DELINS" ~ "DELINS",
      Type=="HDR" ~ "HDR",
      Type=="HDR1MM" ~ "HDR1MM",
      Type=="TINS" ~"TINS",
      Type=="TINS_FW" ~"TINS_FW",
      Type=="TINS_RC" ~"TINS_RC",
      Type=="DELINS_SNV" ~"DELINS_SNV",
      Type=="DELINS_DUAL" ~"DELINS_DUAL",
      Type=="TANDEMDUPLICATION_COMPOUND" ~ "INSERTION",
      homologyLength<=4 ~ paste0(homologyLength,"bp_homology"),
      homologyLength>=5 & homologyLength<15 ~ "5-15bp_homology",
      homologyLength>=15 ~ "15bp_homology"
      ##these were there for Jip, but now not anymore
      #homologyLength>=5 & homologyLength<11 ~ "5-15bp_homology",
      #homologyLength>=11 ~ "15bp_homology"
    ))
    
    #el$getHomologyColor[el$homologyLength==0] <- "0bp_homology"
    #el$getHomologyColor[el$homologyLength==1] <- "1bp_homology"
    #el$getHomologyColor[el$homologyLength==2] <- "2bp_homology"
    #el$getHomologyColor[el$homologyLength==3] <- "3bp_homology"
    #el$getHomologyColor[el$homologyLength==4] <- "4bp_homology"
    #el$getHomologyColor[el$homologyLength>=5 & el$homologyLength<15] <- "5-15bp_homology"
    #el$getHomologyColor[el$homologyLength>=15] <- "15bp_homology"
    #el$getHomologyColor[el$Type=="SNV"] <- "SNV"
    #el$getHomologyColor[el$Type=="WT"] <- "WT"
    #el$getHomologyColor[el$Type=="INSERTION"] <- "INSERTION"
    #el$getHomologyColor[el$Type=="INSERTION" & el$insSize==1] <- "INSERTION_1bp"
    #el$getHomologyColor[el$Type=="DELINS"] <- "DELINS"
    #el$getHomologyColor[el$Type=="HDR"] <- "HDR"
    #el$getHomologyColor[el$Type=="HDR1MM"] <- "HDR1MM"
    #el$getHomologyColor[el$Type=="TINS"] <- "TINS"
    #el$getHomologyColor[el$Type=="TANDEMDUPLICATION_COMPOUND"] <- "INSERTION"
    el$TDcolor = "white"
    el$TDcolor[el$Type=="TANDEMDUPLICATION"] <- "TANDEMDUPLICATION"
    el$TDcolor[el$Type=="TANDEMDUPLICATION_COMPOUND"] <- "TANDEMDUPLICATION_COMPOUND"
    el$TypeHom = paste(el$Type,el$homologyLength)
    el$TypeHom[el$Type=="INSERTION" & el$insSize==1] <- "INSERTION_1bp"
    el$TypeHom <- gsub("-1", "", el$TypeHom)
    el$TypeTD = "other"
    el$TypeTD[el$Type=="TANDEMDUPLICATION_COMPOUND"] <- "TANDEMDUPLICATION_COMPOUND"
    el$TypeTD[el$Type=="TANDEMDUPLICATION"] <- "TANDEMDUPLICATION"
    
    end_time <- Sys.time()
    print(paste("applyColor",end_time-start_time, "seconds"))
    return(el)
  }
  ###
  
  renewPlotData <- function(el){
    ##remove in newer version as del and insertions should always be there
    if(!"del" %in% colnames(el)){
      el$del = ""
    }
    if(!"insertion" %in% colnames(el)){
      el$insertion = ""
    }
    ##for Lejon
    if(!"Translocation" %in% colnames(el)){
      el$Translocation = FALSE
    }
    ##end of to be removed###
    if("fraction" %in% colnames(el)) {
      plot.data <- data.frame(size = el$delRelativeEndTD-el$delRelativeStartTD, start.points = el$delRelativeStartTD, 
                              end.points = el$delRelativeEndTD, type=el$TypeHom, typeTD=el$TypeTD, color=el$getHomologyColor, code=el$Alias, 
                              yheight = el$fraction, typeOrig = el$Type, left = el$delRelativeStartTD+(el$delRelativeEndTD-el$delRelativeStartTD)/2, 
                              startTD=el$delRelativeStart, countEvents = el$countEvents, Pool = el$Subject, tdColor = el$TDcolor, insSize = el$insSize,
                              Subject = el$Subject, Alias = el$Alias, del = el$del, insert = el$insertion, homology = el$homologyLength, Translocation = el$Translocation)
      return(plot.data)
    }
    else{
      plot.data <- data.frame(size = el$delRelativeEndTD-el$delRelativeStartTD, start.points = el$delRelativeStartTD, 
                              end.points = el$delRelativeEndTD, type=el$TypeHom, typeTD=el$TypeTD, color=el$getHomologyColor, code=el$Alias, 
                              typeOrig = el$Type, left = el$delRelativeStartTD+(el$delRelativeEndTD-el$delRelativeStartTD)/2, 
                              startTD=el$delRelativeStart, Pool = el$Subject, tdColor = el$TDcolor, insSize = el$insSize,
                              Subject = el$Subject, Alias = el$Alias, del = el$del, insert = el$insertion, homology = el$homologyLength)
      plot.data$yheight <- 1
      plot.data$countEvents <- 1
      return(plot.data)  
    }
  }
  
  ##the function that handles calculations of the 1bp insert data
  get1bpinsertion_data <- function(df){
    ##recalculate fraction per Alias
    df = df %>% group_by(Subject, Alias) %>%
      mutate(fraction = fraction/sum(fraction))
    
    
    
    columns = c("Subject","Alias", "insertion")
    if(is_grouped()){
      columns = c(columns, get_group_column())
    }
    
    df = df %>% filter(Type == "INSERTION_1bp") %>%
      #complete() %>%
      group_by_at(columns) %>%
      summarise(total = sum(fraction)) 
    
    ##make complete
    samples = total_reads() %>% select(Subject, Alias)  %>%
      filter(Alias %in% input$Aliases)
    df = dplyr::left_join(samples, df, by = c("Subject","Alias"))
    group_by = c("Subject")
    if(is_grouped()){
      group_by = c("Subject",get_group_column())
    }
    df = df %>% as.data.frame() %>% group_by_at(group_by) %>%
      complete(Alias, insertion, fill = list(total = 0)) %>%
      filter(!is.na(insertion))
    
    
    if(is_grouped()){
      group_now = c("Subject","insertion", get_group_column())
      group_now_sd_pos = c("Subject",get_group_column())
      ##missing values
      df = df %>% group_by_at(group_now) %>%
        summarise(mean = mean(total), sd = sd(total), samples = n()) %>%
        ungroup() %>%
        group_by_at(group_now_sd_pos) %>%
        arrange(desc(insertion)) %>%
        mutate(sdpos = cumsum(mean))
      
      #make group a factor
      df[[get_group_column()]] = factor(df[[get_group_column()]], levels = input$multiGroupReplicateOrder)
    }
    
    df
  }
  
  ##get the grouping column
  get_group_column <- function(){
    return(input$GroupColumn)
  }
  
  ##is the data supposed to be grouped?
  is_grouped <- function(){
    return(input$GroupColumn != "-")
  }
  
  output$plot1bpinsertion <- renderPlot({
    req(filter_in_data())
    data = filter_in_data()
    data = get1bpinsertion_data(data)
    
    x_axis_value = "Alias"
    y_axis_value = "total"
    if(is_grouped()){
      x_axis_value = get_group_column()
      y_axis_value = "mean"
    }
    
    plot = ggplot(data, aes(x=!!as.symbol(x_axis_value), y = !!as.symbol(y_axis_value), fill = insertion)) +
      theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust=1)) +
      geom_bar(stat = "identity") +
      facet_wrap(Subject ~ ., scales = "free")+
      NULL
    ##for grouped add SD
    if(is_grouped()){
      ##need to recalculate the sdPos
      ##perhaps this needs to be done somewhere else
      plot = plot + geom_errorbar(aes(ymin=sdpos-sd, ymax=sdpos+sd), width=.2, stat = "identity") 
      
    }
    ##save for PDF
    plotsForDownload$plot1bpInsertion = plot
    plot
  })
  
  output$outcomeHeatmapEnd <- renderPlot({
    req(filter_in_data())
    req(input$Aliases)
    data = filter_in_data()
    
    ##filter for events that are outside of the region of interest
    if(input$HeatmapEndsFilter == "-2bp"){
      data = data %>% filter(delRelativeEndTD >= -2) 
    }
    
    
    ####TAKE THE RIGHT column. These ones are not correct!!!
    counts1 = data %>% group_by(Alias, Subject, delRelativeStartTD) %>% summarise(fraction = sum(fraction))
    counts2 = data %>% group_by(Alias, Subject, delRelativeEndTD) %>% summarise(fraction = sum(fraction))
    if(input$HeatmapEndsRelative == "relative"){
      ##make the counts relative to the total selected fraction
      counts1 = counts1 %>% ungroup() %>% group_by(Alias, Subject) %>% mutate(fraction = fraction/sum(fraction))
      counts2 = counts2 %>% ungroup() %>% group_by(Alias, Subject) %>% mutate(fraction = fraction/sum(fraction))
    }
    counts1$Alias = factor(counts1$Alias, levels = rev(input$multiGroupOrder))
    counts2$Alias = factor(counts2$Alias, levels = rev(input$multiGroupOrder))
    
    plot1 = ggplot(counts1, aes(x=delRelativeStartTD, y = Alias , fill = fraction)) + 
      geom_tile() +
      #scale_fill_viridis_c()
      scale_fill_gradientn(colours = c("white", "black", "red")) +
      theme_classic() +
      NULL
    plot2 = ggplot(counts2, aes(x=delRelativeEndTD, y = Alias , fill = fraction)) + 
      geom_tile() +
      scale_fill_gradientn(colours = c("white", "black", "red")) +
      theme_classic() +
      NULL
    if(length(unique(data$Subject))>1){
      plot1 = plot1 + facet_wrap(Subject ~., ncol = 1, scales = "free")
      plot2 = plot2 + facet_wrap(Subject ~., ncol = 1, scales = "free")
    }
    grid.arrange(plot1, plot2, ncol=2)
  })
  
  ##this function still needs to be implemented
  ##will save a lot of time
  outcomePlotData <- reactive({
    req(filter_in_data())
    req(input$Aliases)
    data = filter_in_data()
    ##calculate fraction from selection
    if(input$OutcomeCalc == 0){
      data <- data %>%
        group_by(Subject, Alias) %>%
        mutate(fraction = fraction/sum(fraction))
    } else if(input$OutcomeCalc == 1){
      totalDF = total_reads() %>% select(-counts, -counts_mut)
      ##merge goes wrong if group column is there
      if(is_grouped()){
        totalDF = totalDF %>% select(-!!sym(get_group_column()))
      }
      data <- dplyr::left_join(data, totalDF, by = c("Subject", "Alias")) %>%
        mutate(fraction = fraction / fraction_mut)
    }
      
    if(nrow(data)==0){
      return()
    }
    ##add Outcomes
    data = data %>% mutate(Outcome = paste(Type,delRelativeStart,delRelativeEnd,insSize,sep = "|"))
    
    if(!"insertion" %in% colnames(data)){
      data$insertion = ""
    }
    #specify outcomes
    ##in the future the rules might change dynamically
    #keep DELINS separate already
    data = data %>%
      mutate(Outcome = case_when(
        Type == "DELETION" ~ paste(Type,delRelativeStart,delRelativeEnd,paste0(homologyLength,"bp"),sep = "|"),
        Type == "DELINS" ~ paste(Type,delRelativeStart,delRelativeEnd,insertion,sep = "|"),
        Type == "SNV" ~ paste(Type,delRelativeStart,delRelativeEnd,insertion,sep="|"),
        Type == "INSERTION" & insSize < 7 ~ paste(Type,delRelativeStart,delRelativeEnd,insertion,sep="|"),
        Type == "INSERTION_1bp" ~ paste(Type,delRelativeStart,delRelativeEnd,insertion,sep="|"),
        ##combine TD, TINS and DELINS
        Type == "TANDEMDUPLICATION" | 
          Type == "TANDEMDUPLICATION_COMPOUND" | 
          Type == "TINS" |
          Type == "TINS_FW" | 
          Type == "TINS_RC" ~ paste(Type,sep="|"),
        TRUE ~ Outcome
      ))
    
    if(!is.null(input$outcomeDEL)){
      delSizeLabels = c(0,1,2,5,10,15,20,25,30,max(data$delSize))
      homologySteps = c(0,0.9,2.1,5.1,100)
      if("size" %in% input$outcomeDEL & "homology" %in% input$outcomeDEL){
        data = data %>%
          mutate(Outcome = case_when(
            Type == "DELETION" ~ paste(Type, "size:",cut(delSize, breaks = delSizeLabels),
                                       "hom:",cut(homologyLength, breaks = homologySteps,include.lowest = T)),
            TRUE ~ Outcome
          ))
      }
      else if("size" %in% input$outcomeDEL){
        data = data %>%
          mutate(Outcome = case_when(
            Type == "DELETION" ~ paste(Type, "size:",cut(delSize, breaks = delSizeLabels)),
            TRUE ~ Outcome
          ))
      }
      else if("homology" %in% input$outcomeDEL){
        data = data %>%
          mutate(Outcome = case_when(
            Type == "DELETION" ~ paste(Type, "hom:",cut(homologyLength, breaks = homologySteps, include.lowest = T)),
            TRUE ~ Outcome
          ))
      }
    }
    ##DELINS
    if(!is.null(input$outcomeDELINS)){
      if("size" %in% input$outcomeDELINS){
        data = data %>%
          mutate(Outcome = case_when(
            Type == "DELINS" ~ paste(Type, "size:",cut(delSize, breaks = delSizeLabels)),
            TRUE ~ Outcome
          ))
      }
      else if("allcombined" %in% input$outcomeDELINS){
        data = data %>%
          mutate(Outcome = case_when(
            Type == "DELINS" ~ paste(Type),
            TRUE ~ Outcome
          ))
      }
    }
    
    
    print("outcomePlotData")
    data
  })
  
  outcomePlotDataFiltered <- reactive({
    req(outcomePlotData())
    req(input$controls)
    data = outcomePlotData()
    
    ##this option processes its own info, so leave it alone here
    if(input$OutcomeResolve == "merge non-informative"){
      data = redoOutcomes(data)
      return(data)
    }

    ##do we need to add the grouping column
    select_var = c("Subject","Alias")
    group_var = c("Subject","Outcome")
    group_var_all = c("Subject","Outcome","Alias")
    if(is_grouped()){
      select_var = c(select_var,get_group_column())
      group_var = c(group_var,get_group_column())
      group_var_all = c(group_var_all,get_group_column())
    }
    
    ##we need this dummy because in some cases we lose Aliases because
    ##they do not have outcomes and if we select a single outcome they are gone
    
    dummyOutcome = data %>% select_at(select_var) %>% distinct() %>% mutate(Outcome = "dummy", fraction = 0)
    
    ##first get the top outcomes
    dataOutcomeControls = data %>% 
      filter(Alias %in% input$controls) %>%
      group_by_at(group_var_all) %>%
      summarise(fraction = sum(fraction)) %>% 
      data.frame()
    
    ##here we already need to add missing values I think!
    
    dataOutcomeControlsTotal = dataOutcomeControls %>%
      ungroup() %>%
      group_by(Subject, Outcome) %>%
      summarise(fraction = sum(fraction)) %>% 
      data.frame()
    
    ##select the top outcomes
    keepOutcomes = dataOutcomeControlsTotal %>% group_by(Subject) %>%
      slice_max(fraction, n = input$numberOfOutcomes, with_ties = TRUE) %>%
      arrange(desc(fraction)) %>%
      select(-fraction)
    
    ##now subselect the entire data
    ##and because some outcomes are not unique you need to calculate them now!
    dataPlot1 = keepOutcomes %>%
      left_join(data) %>%
      group_by_at(group_var_all) %>%
      summarise(fraction = sum(fraction)) %>%
      data.frame() 
    
    ##the magic happens here: bind the dummy row to get all Aliases per Subject in
    ##then complete the outcomes and remove the dummy
    dataPlot1 = dplyr::bind_rows(dataPlot1,dummyOutcome) %>%
      group_by(Subject) %>%
      complete(Outcome, Alias, fill = list(fraction = 0)) %>%
      filter(Outcome != "dummy")
    
    if(is_grouped()){
      #need to put the group column back in through the dummy
      groupsDF = dummyOutcome %>% select(!!as.symbol(get_group_column()), Alias)
      dataPlot1 = dataPlot1 %>% select(-!!as.symbol(get_group_column()))
      dataPlot1 = dplyr::left_join(dataPlot1, groupsDF)      
    }
    dataPlot1 = redoOutcomes(dataPlot1)
    dataPlot1
  })
  
  getUMAPDataDF <- reactive({
    req(outcomePlotDataFiltered())
    if(input$typePlotOutcome != "umap"){
      return()
    }
    dfFull = outcomePlotDataFiltered()
    ##add a call to redo here?
    
    
    if(nrow(dfFull) == 0){
      return()
    }
    subjects = sort(unique(dfFull$Subject))
    
    ##init umap
    custom.config = umap.defaults
    custom.config$random_state = 123
    custom.config$min_dist = 0.01
    
    dfUmap = NULL
    for(subject in subjects ){
      df = dfFull %>% 
        ungroup() %>%
        filter(Subject == subject) %>%
        select (Alias, Outcome, fraction) %>%
        spread(Outcome,fraction) %>%
        as.data.frame()
      rownames(df) = df[,1]
      df = df[,-1]
      df[is.na(df)] <- 0
      
      if(nrow(df)<15){
        custom.config$n_neighbors = 2
      } else{
        #default value
        custom.config$n_neighbors = 15
      }
      test = umap(df, config = custom.config)
      dfTest = data.frame('Subject' = subject, 'Alias' = rownames(df),test$layout)
      ##get the grouping column in there if present
      if(is_grouped()){
        dfParts = dfFull %>% filter(Subject == subject) %>%
          ungroup() %>%
          select (Alias, !!as.symbol(get_group_column())) %>% 
          distinct()
        dfTest = dplyr::left_join(dfTest, dfParts, by = c("Alias"))
      }
      dfUmap = dplyr::bind_rows(dfUmap, dfTest)
    }
    
    if(is_grouped()){
      dfUmap$label = dfUmap[[get_group_column()]]
    } else{
      dfUmap$label = dfUmap$Alias
    }
    dfUmap
  })
  
  getSD_From_OutcomeDF <- function(df){
    ##these are log2 based
    ##for the plot of the mean you need to get the mean of the mean
    if(input$outcome_volcano_type == "log2fraction"){
      sdControls = df %>% 
        filter(Alias %in% input$controls) %>%
        group_by(Subject, Outcome) %>%
        summarise(lower = mean(log2fraction)-input$OutcomePartQuartile*sd(log2fraction),
                  upper = mean(log2fraction)+input$OutcomePartQuartile*sd(log2fraction))
    } else if(input$outcome_volcano_type == "mean"){
      sdControls = df %>% 
        filter(Alias %in% input$controls) %>%
        group_by(Subject, Outcome) %>%
        summarise(lower = mean(fraction)-input$OutcomePartQuartile*sd(fraction),
                  upper = mean(fraction)+input$OutcomePartQuartile*sd(fraction))
    }
    return(sdControls)
  }
  ##place function to filter outcomes
  redoOutcomes <- function(dataPlot1){
    start_time = Sys.time()
    dataPlot1 = dataPlot1 %>% 
      group_by(Subject, Outcome) %>% 
      mutate(meanControls = mean(fraction[Alias %in% input$controls], na.rm = T)) %>% 
      mutate(log2fraction = log2(fraction/meanControls))
    
    if(input$OutcomeResolve == "remove non-informative"){
      if(input$GroupColumn == "-"){
        return(as_ggplot(text_grob("Please select a Grouping column for this to work", size = 15)))
      }
      
      sdControls = getSD_From_OutcomeDF(dataPlot1)
      
      if(input$outcome_volcano_type == "mean"){
        x_name = "fraction"
      } else if(input$outcome_volcano_type == "log2fraction"){
        x_name = "log2fraction"
      }
      
      ##which ones are non-informative?
      comparisonDF = dplyr::left_join(dataPlot1, sdControls, by = c("Subject","Outcome"))
      comparisonDF = comparisonDF %>% filter(!!as.symbol(x_name) < lower | !!as.symbol(x_name) > upper) %>%
        group_by_at(c("Subject","Outcome",input$GroupColumn)) %>%
        count() %>% filter(n>=input$OutcomeMinimumPerGroup) %>%
        ungroup %>%
        select(Subject, Outcome) %>% distinct()
      ##only keep stuff that is in comparisonDF
      dataPlot1 =  dplyr::inner_join(dataPlot1, comparisonDF, by = c("Subject","Outcome"))
      sdControls = dplyr::inner_join(sdControls, comparisonDF, by = c("Subject","Outcome"))
    } else if(input$OutcomeResolve == "merge non-informative"){
      ##all the aggregation takes place here
      ##assumption is that dataPlot1 already has an initial outcome
      
      
      
      dfCombined = combineOutcomes(dataPlot1)
      
      dfComplete = completeOutcomes(dfCombined)
      
      controls = input$controls
      
      minAlias = 3
      sigLevel = 1
      
      dfComplete = calculateControls(dfComplete, controls)
      
      deviantOutcomes = getDeviantOutcomes(dfComplete, minAlias)
      
      dataPlot1 = markOutcomesAsDone(dataPlot1, deviantOutcomes)
      
      ##adjust Outcomes
      dataPlot1 = changeOutcome(dataPlot1, sigLevel) 
      
      dfRemain = dataPlot1 %>%
        filter(deviant == F) %>%
        combineOutcomes() %>%
        completeOutcomes() %>%
        calculateControls(controls) 
      
      deviantOutcomes = dfRemain %>%
        getDeviantOutcomes(minAlias)
      
      dataPlot1 = markOutcomesAsDone(dataPlot1, deviantOutcomes)
      
      ##adjust outcomes
      sigLevel = 2
      
      ##adjust Outcomes
      dataPlot1 = changeOutcome(dataPlot1, sigLevel) 
      
      dfRemain = dataPlot1 %>%
        filter(deviant == F) %>%
        combineOutcomes() %>%
        completeOutcomes() %>%
        calculateControls(controls) 
      
      deviantOutcomes = dfRemain %>%
        getDeviantOutcomes(minAlias)
      
      dataPlot1 = markOutcomesAsDone(dataPlot1, deviantOutcomes)
      
      groupColumn = get_group_column()
      
      dataPlot1 = dataPlot1 %>% filter(deviant == T) %>%
        group_by(Subject, !!sym(groupColumn), Alias, Outcome) %>%
        summarise(fraction = sum(fraction)) %>%
        ungroup() %>%
        completeOutcomes() %>% 
        ##get the meanControls in there
        group_by(Subject, Outcome) %>% 
        mutate(meanControls = mean(fraction[Alias %in% input$controls])) %>%
        mutate(log2fraction = log2(fraction/meanControls))
      
    }
    end_time = Sys.time()-start_time
    print(paste("redoOutcomes took",end_time))
    
    return(dataPlot1)
  }
  
  
  output$outcomePlot <- renderPlot({
    #req(filter_in_data())
    #req(input$Aliases)
    #req(outcomePlotData)
    if(is.null(input$controls) | length(input$controls) == 0){
      as_ggplot(text_grob("Please select a control sample on the left", size = 15))
    }else{
      data = outcomePlotData()
      
      #scale data to fraction
      start_time = Sys.time()
      print("outcomePlot")
      if(nrow(data)==0){
        return()
      }
      
      ###not for all needed
      if(input$typePlotOutcome!="XY"){
        end_time = Sys.time()-start_time
        print(paste("half2: outcomePlot",end_time))
        
        dataPlot1 = outcomePlotDataFiltered()
        
        #dataPlot1 = redoOutcomes(dataPlot1)
        
        # Order top outcomes
        #maxSizeString = 50
        #dataNT1TopOutcomes = dataNT1TopOutcomes %>%
        #  mutate(Outcome=stringr::str_trunc(Outcome,maxSizeString))
        #dataPlot1 = dataPlot1 %>%
          #mutate(Outcome=stringr::str_trunc(Outcome,maxSizeString))
        
        #ordered <- unique(dataNT1TopOutcomes$Outcome)
        
        end_time = Sys.time()-start_time
        print(paste("outcomePlot took",end_time))
        
      }
      plots = list()
      if(input$typePlotOutcome=="line"){
        ##get the SDs        
        sdControls = getSD_From_OutcomeDF(dataPlot1)
        
        dataPlot1 = dplyr::left_join(dataPlot1,sdControls, by = c("Subject","Outcome"))
        
        displayName = "Alias"
        if(is_grouped()){
          displayName = get_group_column()
        }
        
        dataPlot1 = dataPlot1 %>% group_by(Outcome) %>%
          mutate(rank = ifelse(fraction<lower | fraction > upper, !!sym(displayName), NA))
        
        
        #outliers = unique(dataPlot1$Alias[!is.na(dataPlot1$rank)])
        #dataPlot1$rank[dataPlot1$Alias %in% outliers] = dataPlot1$Alias[dataPlot1$Alias %in% outliers]
        
        if(is_grouped()){
          
          
          remainder = dataPlot1 |> group_by(Subject, Alias, !!sym(get_group_column())) |> 
            summarise(fraction = 1-sum(fraction)) |>
            mutate(Outcome = "remainder")
          
          dataPlot1 = dataPlot1 %>% bind_rows(remainder)
          
          #adjust the outcomes if multiple subject
          if(length(unique(dataPlot1$Subject)) > 1){
            dataPlot1 = dataPlot1 %>% mutate(Outcome = paste(Subject, Outcome))
          }
          
          ordered = dataPlot1 %>% select(Subject, Outcome, meanControls) %>% distinct() %>% arrange(meanControls)
          dataPlot1$Outcome = factor(dataPlot1$Outcome, levels = ordered$Outcome)
          
          dataPlot1Grouped = dataPlot1 %>% group_by(Subject, Outcome, !!sym(displayName)) %>%
            summarise(meanFraction = mean(fraction), sdFraction = sd(fraction),
                      meanSDmin = meanFraction-sdFraction,
                      meanSDmax = meanFraction+sdFraction) %>%
            #ensure that the minimum is 0
            mutate(meanSDmin = ifelse(meanSDmin<0,NA,meanSDmin))
          
          ##for control samples
          dataPlot1GroupedControl = dataPlot1 %>% 
            filter(Alias %in% input$controls) %>%
            group_by(Subject, Outcome, !!sym(displayName)) %>%
            summarise(meanFraction = mean(fraction), sdFraction = sd(fraction),
                      meanSDmin = meanFraction-sdFraction,
                      meanSDmax = meanFraction+sdFraction) %>%
            #ensure that the minimum is 0
            mutate(meanSDmin = ifelse(meanSDmin<0,0,meanSDmin))
          
          plot <- ggplot(dataPlot1Grouped,aes(x = Outcome, y = meanFraction, group = !!sym(displayName), color = !!sym(displayName))) +
            geom_line(size=0.25, alpha=0.4) +
            geom_point(size=input$OutcomeDotSize, alpha=input$OutcomeAlpha) +
            #geom_errorbar(aes(ymin = meanSDmin, ymax = meanSDmax))+
            geom_ribbon(data = dataPlot1GroupedControl, aes(ymin = meanSDmin, ymax = meanSDmax,
                            ),
                        fill = "darkgrey",
                        linetype = 2, alpha = .6)+
            scale_y_log10() +
            theme_grey()+
            theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust=1)) +
            coord_flip()+
            facet_wrap(Subject ~ ., ncol = input$OutcomeNrCols, scales = "free")+
            NULL
          plots[['line']] <- plot
          
          plotsForDownload$outcomes <- plots
          render = grid.arrange(grobs = plots, ncol=1)
          return(render)
        }
        
        #order the outcomes
        ordered = dataPlot1 %>% select(Subject, Outcome, meanControls) %>% distinct() %>% arrange(desc(meanControls))
        
        dataPlot1$Outcome = factor(dataPlot1$Outcome, levels = ordered$Outcome)
        
        
        ##only draw colors if we have something to color
        if(length(na.omit(dataPlot1$rank))>0){
          plot <- ggplot(dataPlot1,aes(x = Outcome, y = fraction, group = Alias, color = rank))
          if(input$OutcomeSize > 0){
            plot <- plot + geom_text_repel(aes(label=rank, color = rank), na.rm = T, size=input$OutcomeSize, position = position_dodge(0.5), max.overlaps = Inf)
          }
        }
        ##if there is no rank, do not use color as rank
        else{
          plot <- ggplot(dataPlot1,aes(x = Outcome, y = fraction, group = Alias, color = Alias))
        }
          #scale_colour_manual()
          plot <- plot + geom_line(size=1.2, alpha=0.4) +
          scale_y_log10() +
          theme_grey() +
          #scale_color_manual(values = colValues) +
          theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust=1)) +
          #ggtitle(paste(Title, "Most frequent outcomes in RVsg_mmNT-1", sep=": ")) +
          theme(plot.title = element_text(hjust = 0.5)) +
          labs(
            x = "Outcome",
            y = "Fraction"
          )
          if(!input$OutcomeLegend){
            plot <- plot + theme(legend.position = "none")
          }
          plots[['line']] <- plot
          
      } else if(input$typePlotOutcome=="volcano"){
        
        ##add the read counts to the Aliases
        total_reads = total_reads()
        if(is_grouped()){
          ##remove the Gene here as it breaks the join
          total_reads = total_reads |> select(-!!sym(get_group_column()))
        }
        ##get the total number of mutagenic reads in
        dataPlot1 = dplyr::left_join(dataPlot1, total_reads, by = c("Subject", "Alias"))
        ##overwrite WT with the counts and not the mutagenic counts
        dataPlot1 = dataPlot1 %>% mutate(counts_mut = ifelse(grepl("WT",Outcome),counts,counts_mut))
        
        ##ensure the outcomes are ordered by abundance
        orderOutcome = dataPlot1 |> select(Outcome, meanControls) %>% distinct() %>% arrange(desc(meanControls))
        dataPlot1$Outcome = factor(dataPlot1$Outcome, levels = unique(orderOutcome$Outcome))
        
        ##set limits for the log2
        dataPlot1 = dataPlot1 %>% mutate(log2fraction = ifelse(log2fraction < (-input$heatmapLimits),-input$heatmapLimits,
                                                               log2fraction))
        dataPlot1 = dataPlot1 %>% mutate(log2fraction = ifelse(log2fraction > (input$heatmapLimits),input$heatmapLimits,
                                                               log2fraction))
        
        sdControls = getSD_From_OutcomeDF(dataPlot1)
        
        if(input$outcome_volcano_type == "mean"){
          x_name = "fraction"
        } else if(input$outcome_volcano_type == "log2fraction"){
          x_name = "log2fraction"
        }
        
        color_column = get_group_column()
        if(!is_grouped()){
          color_column = "Alias"
        }
          plot = ggplot(dataPlot1, aes(x = !!as.symbol(x_name), y = counts_mut, color = !!as.symbol(color_column) )) +
            geom_point(size = input$OutcomeDotSize) +
            geom_vline(aes(xintercept = lower), data = sdControls)+
            geom_vline(aes(xintercept = upper), data = sdControls)+
            scale_y_log10()+
            facet_wrap(Subject ~ Outcome, scales = "free", ncol = input$OutcomeNrCols)+
            NULL
          
        return(plot)
      } else if(input$typePlotOutcome=="heatmap"){
        
        #dataPlot1 = dataPlot1 %>% group_by(Subject) %>% 
        #  complete(Alias, Outcome, fill = list(fraction = 0))
        #dataPlot1 = dataPlot1 %>% group_by(Subject) %>% 
         #   complete(Alias, Outcome)
          ##perhaps this needs to be done in a different manner
          #mutate(fraction = fraction + (min(fraction)/10))
        
        #get a readcount for each sample
        #total_reads = total_reads()
        #dataPlot1 = dplyr::left_join(dataPlot1, total_reads, by = c("Subject", "Alias"))
        #add the total fraction of 1 read for that sample to the outcome
        #dataPlot1 = dataPlot1 %>% mutate(fraction = ifelse(is.na(fraction),1/counts,fraction)) %>%
        #  select(-counts)
          
        
        ##get the log2 difference
        #dataPlot1 = dataPlot1 %>% 
         # group_by(Outcome) %>% 
        #  mutate(meanControls = mean(fraction[Alias %in% input$controls], na.rm = T)) %>% 
        #  mutate(fraction = log2(fraction/meanControls)) %>%
          ##get rid of that column again
        #  select(-meanControls) %>%
        #  as.data.frame()
        
        ##set a maximum and minimum for the log2
        minimum = -input$heatmapLimits
        maximum = input$heatmapLimits
        dataPlot1 = dataPlot1 %>% mutate(log2fraction = ifelse(log2fraction < minimum,
                                         minimum, log2fraction))
        dataPlot1 = dataPlot1 %>% mutate(log2fraction = ifelse(log2fraction > maximum,
                                                           maximum, log2fraction))
          
        
        nrSubject = length(unique(dataPlot1$Subject))
        
        col_names = c("Alias", "Outcome", "log2fraction" )
        if(nrSubject >1 ){
          col_names = c(colnames, "Subject")
        }
        
        df = dataPlot1 %>%
          select_at(col_names) %>%
          spread(Outcome,log2fraction) %>%
          as.data.frame()
        
        if(nrSubject == 1){
          rownames(df) = df[["Alias"]]
        } else{
          rownames(df) = paste(df[["Subject"]],df[["Alias"]])
        }
        #remove two columns
        df = df[,-1]
        df = df[,-1]
        
        df = as.matrix(df)
        #df[is.na(df)] <- 0
        scale = "none"
        if(input$OutcomePCAScale){
          #is that useful?
          scale = "row"
        }
        plot = heatmap.2(as.matrix(df), trace = "none",
                  col=rev(RdBu(100)),
                  scale = scale,
                  cexRow = input$OutcomeSize/4,
                  cexCol = input$OutcomeSize/4,
                  margins=c(12,12),
                  na.color = "black"
                  #cexRow = 0.5,
                  #cexCol = 0.5,
                  #scale = "row"
                  )
        return(plot)
      }else if(input$typePlotOutcome=="pca"){
        if(length(unique(dataPlot1$Subject))>1){
          dataPlot1 = dataPlot1 %>% mutate(Outcome = paste(Outcome, Subject))
        } 
        
        df = dataPlot1 %>% 
          ungroup() %>%
          select (Alias, Outcome, fraction) %>%
          spread(Outcome,fraction) %>%
          as.data.frame()
        rownames(df) = df[,1]
        df = df[,-1]
        df[is.na(df)] <- 0
        pca_res <- prcomp(df, scale. = input$OutcomePCAScale)
        
        dtp <- data.frame('Alias' = rownames(df), pca_res$x[,1:2]) # the first two components are selected (NB: you can also select 3 for 3D plottings or 3+)
        
        if(is_grouped()){
          dfPart = data %>% select(Alias, Subject,!!as.symbol(get_group_column())) %>% distinct(Alias, .keep_all = T)
          dtp = merge(dtp,dfPart,by = "Alias")
          dtp$label = dtp[[get_group_column()]]
          col_column = get_group_column()
        } else{
          dtp$label = dtp$Alias
          col_column = "Alias"
        }
        
        
        plot1 = ggplot(data = dtp,aes_string(x = "PC1", y = "PC2", fill = col_column)) + 
          geom_point(shape = 21, size = input$OutcomeDotSize, stroke = input$OutcomeStrokeSize) +
          geom_text_repel(size = input$OutcomeSize,aes(label = label), max.overlaps = input$pca_max_overlap)+
          #geom_label(size = input$OutcomeSize,aes(label = rownames(df)))+
          theme_minimal()+
          theme(legend.position = "none")
        plots[['pca']] = plot1
        fviz = fviz_pca_var(pca_res, col.var = "cos2",
                            gradient.cols = c("#00AFBB", "#E7B800", "#FC4E07"), 
                            repel = TRUE # Avoid text overlapping
        )
        plots[['pcaFviz']] = fviz
      } else if(input$typePlotOutcome=="XY"){
        start_time = Sys.time()
        ##get the fraction per type
        dataDF = data %>%
          group_by(Alias,Type) %>%
          count(wt=fraction, name = "fraction")
        test = 1
        dataSpread = dataDF %>%
          spread(Type,fraction)
        
        #add mean homology usage from deletions as needed
        if("mean_homology_deletion" %in% input$controlsY | "mean_homology_deletion" %in% input$controlsX){
          dataDF = data %>%
            filter(Type=="DELETION")%>%
            group_by(Alias) %>%
            summarise(mean_homology_deletion = weighted.mean(homologyLength,countEvents))
          dataSpread = merge(dataSpread,dataDF,by="Alias", all.x=T)
        }
        if("mean_homology_TD" %in% input$controlsY | "mean_homology_TD" %in% input$controlsX){
          dataDF = data %>%
            filter(Type=="TANDEMDUPLICATION")%>%
            group_by(Alias) %>%
            summarise(mean_homology_TD = weighted.mean(homologyLength,countEvents))
          dataSpread = merge(dataSpread,dataDF,by="Alias", all.x=T)
        }
        
        print(paste("means",Sys.time()-start_time))
        
        ##color samples based on SD difference
        dfForOutliers = dataSpread %>%
          filter(Alias %in% input$controls) %>%
          select(!!as.name(input$controlsX)) %>%
          ungroup() %>%
          summarise(avg=mean(!!as.name(input$controlsX),na.rm = T), sd=sd(!!as.name(input$controlsX),na.rm = T))
        
        print(paste("outliers",Sys.time()-start_time))
        
        ##replace sd with 0 as required
        dfForOutliers[is.na(dfForOutliers)] = 0
        
        minValue = dfForOutliers$avg- input$OutcomePartQuartile*dfForOutliers$sd
        maxValue = dfForOutliers$avg+ input$OutcomePartQuartile*dfForOutliers$sd
        print(paste(minValue,maxValue))
        dataSpread = dataSpread %>%
          mutate(rank = ifelse(!!as.name(input$controlsX)<minValue | !!as.name(input$controlsX)>maxValue,Alias,NA))
       
        print(paste("mutate outliers",Sys.time()-start_time))
        
        for(controlY in input$controlsY){
          plot = ggplot(dataSpread, aes_string(x=input$controlsX,y=controlY)) +
            geom_point() +
            geom_vline(xintercept = c(minValue,maxValue), linetype="dashed",color="grey")
          ##only draw colors if we have something to color
          if(length(na.omit(dataSpread$rank))>0){
             plot = plot+geom_text_repel(aes(label = rank,color = rank), size = input$OutcomeSize)
          }
          plot = plot +theme_minimal()+
            ggtitle(paste(input$controlsX, controlY))
           if(!input$OutcomeLegend){
             plot <- plot + theme(legend.position = "none")
           }
          plots[[controlY]] <- plot
        }
        end_time = Sys.time()
        print(paste("outcomeXY ",end_time-start_time, "seconds"))
      } else if(input$typePlotOutcome=="umap"){
        
        dfTest = getUMAPDataDF()
        if(input$OutcomeUmapLabels){
          dfTest = dfTest %>% group_by_at(c("Subject",get_group_column())) %>%
            mutate(displayLabel = ifelse(row_number() == 1,label,NA))
        } else{
          dfTest = dfTest %>% mutate(displayLabel = label)
        }
        
        plot = ggplot(dfTest,aes(x=X2,y=X1, fill = label))+
          geom_point(shape=21, size = input$OutcomeDotSize, stroke = input$OutcomeStrokeSize,
                     alpha = input$OutcomeAlpha) +
          theme_minimal()
        if(input$OutcomeSize>0){
          plot <- plot + geom_text_repel(size = input$OutcomeSize,aes(label = displayLabel), max.overlaps = Inf)
        }
        if(!input$OutcomeLegend){
          plot <- plot + theme(legend.position = "none")
        }
        plot = plot + facet_wrap(Subject ~ ., scales = "free", ncol = input$OutcomeNrCols)
        
        plots[['umap']] <- plot
      }
      plotsForDownload$outcomes <- plots
      grid.arrange(grobs = plots, ncol=1)
    }
    
  })
  
  is_outlier <- function(x) {
    return(x < quantile(x, 0.25) - 1.5 * IQR(x) | x > quantile(x, 0.75) + 1.5 * IQR(x))
  }
  
  output$tornadoPlotFI <- renderPlot({
      req(filter_in_data())
      req(input$yaxis)
      req(input$Aliases)
      req(d_xminmaxRange())
      #speedup multigroup order is lagging behind Aliases
      if(length(input$Aliases) != length(input$multiGroupOrder)){
        return()
      }
      el = filter_in_data()
      if(!"isFirstHit" %in% colnames(el)){
        plots = list()
        plot = text_grob(paste("Columns with specific information is missing from your data that prevents showing Templated Insertions. \nYou need to rerun SIQ as some columns were stripped from your data."), size = 15)
        plots[["empty"]] = plot
        return(grid.arrange(grobs=plots, ncol=1))
      }
      el = el[el$Type=="TINS" | el$Type == "DELINS",]
      plots=list()
      el$junction = el$isFirstHit
      
      el$junction = gsub("rc","",el$junction)
      el$junction = gsub("<0L>","L",el$junction)
      el$junction = gsub("<R0>","R",el$junction)
      el$junction = gsub("<","",el$junction)
      el$junction = gsub(">","",el$junction)
      el$junctionType = "FW"
      el$junctionType[grep("rc",el$isFirstHit)] <- "RC"
      
      #set the positions and length
      el$position = el$getFirstPos
      el$length = el$isGetLargestMatch
      
      #change positions and junctions as required
      if(input$posTINS == "relative to reference"){
        el = el %>% mutate(position =
                             case_when(junction == "L" ~ isStartPosRel, 
                                       junction == "R"  ~ isStartPosRel,
                                       TRUE ~ position)
        )
        el$junction[el$junction=="L" | el$junction=="R"] <- "ref"
      }
      ##added plot based on secondary Type
      el = el %>% mutate(SecondaryTypeHit = paste(SecondaryType,junction,sep = "_"))
      elCount = el %>% filter(Type == "DELINS" | Type == "TINS") %>% group_by(Alias) %>% count(SecondaryTypeHit,wt=fraction)
      plotBars = ggplot(elCount, aes(x = Alias, y=n, fill = SecondaryTypeHit)) + geom_bar(stat = "identity") + scale_fill_viridis_d() + theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust=1))
      plots[['bars']] = plotBars
      
      #drop delins
      el = el[el$Type=="TINS",]
      
      
      
      junctions = unique(el$junction)
      counts = el %>% group_by(Alias,junction) %>% count(wt=fraction)
      ymax=-1
      if(input$yaxisTINS == "max of plots"){
        ymax = max(counts$n)
      }
      
      
      
      for(alias in input$multiGroupOrder){
        for(junction in junctions){
          if(input$yaxisTINS == "max of junction"){
            ymax = max(counts$n[counts$junction == junction])
          }
          xmin = min(el$position[el$junction == junction])-20
          xmax = max(el$position[el$junction == junction])+20
          
          dfSub = el[el$Alias ==alias & el$junction == junction,]
          if(nrow(dfSub)>0){
            plot = tornadoPlatFlankInsert(dfSub,ymax, xmin, xmax, sortType = input$sortTypeTI)
          }else{
            plot = text_grob(paste(alias,"\nNO DATA"), size = 10)
          }
          plots[[paste0(alias,junction)]] <- plot
        }
      }
      plotsForDownload$tornadoTI <- plots
      plotsForDownload$tornadoTIcols <- length(junctions)
      grid.arrange(grobs=plots, ncol=length(junctions))
  })
  
  tornadoPlatFlankInsert <- function(df, ymax, xmin, xmax, sortType = "Start postion"){
    junction = unique(df$junction)
    title = paste(unique(df$Alias)," ",junction)
    
    if(sortType == "Start position"){
      df <- df[order(-df$position, -df$length), ]
    } else if(sortType == "End position"){
      df <- df[order(-(df$position+df$length), -df$length), ]
    }
    
    df$y_end = cumsum(df$fraction)
    df$y_start = df$y_end - df$fraction
    
    plot <- ggplot(df) + 
      geom_rect(aes(xmin=position,xmax=position+length,ymin=y_start,ymax=y_end, fill=junctionType))+
      ggtitle(title)
    
    if(ymax!=-1){
      plot <- plot + ylim(c(0,ymax))
    }
    if(junction=="L"){
        #xmax before xmin otherwise it break!
        plot <- plot + scale_x_reverse(lim = c(xmax,xmin))
    }else{
      if(xmin!=-1 & xmax!=-1){
        plot <- plot + xlim(c(xmin,xmax))
      }
    }
    plot <- plot +
      theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
            legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title=element_blank(), legend.title = element_text(size = 10))
    
    return(plot)
  }
  
  tornadoPlotData <- reactive({
    req(filter_in_data())
    #speedup multigroup order is lagging behind Aliases
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    el = filter_in_data()
    if(nrow(el) == 0){
      return()
    }
    
    print("start: tornadoPlotData")
    start_time = Sys.time()
    
    ##respect the grouping if selected
    if(is_grouped()){
      el = el %>% mutate(Alias = !!as.name(input$GroupColumn))
    }
    
    # FIX this 100 limit
    maxTornadoes = 100
    subjectAliasList = el %>% select(Subject,Alias) %>% distinct() %>% ungroup()
    if(nrow(subjectAliasList) > maxTornadoes){
      subjectAliasListSub = subjectAliasList %>% slice_head(n = maxTornadoes)
      el = el %>% filter(Subject %in% subjectAliasListSub$Subject, Alias %in% subjectAliasListSub$Alias)
    }
    
    el = applyColor(el)
    if(input$yaxis == 1 & input$YaxisValue != "#Reads"){
      el = el %>%
        group_by(Alias, Subject) %>%
        mutate (fraction = fraction/sum(fraction))
    } else if(input$YaxisValue == "#Reads"){
      el = el %>% ungroup() %>% mutate(fraction = countEvents)
    }
    newdata = renewPlotData(el)
    
    end_time = Sys.time()-start_time
    print(paste("half: tornadoPlotData",end_time))
    
    ##this code compresses events that are the same, but come from different files
    ##into a single event
    ##perhaps it would be wise to only do that if the Alias is combined of several files
    ##but since the Alias column is overwritten we ware not sure at this moment if the data
    ##is from multiple files
    maxFraction = max(el$totalFraction)
    ##this expensive call is usually not needed, but sometimes it is
    if(maxFraction > 1){
      newdata = newdata %>% group_by(across(c(-yheight, -countEvents))) %>%
        summarise(yheight = sum(yheight), countEvents = sum(countEvents))
    }
    
    end_time = Sys.time()-start_time
    print(paste("half2: tornadoPlotData",end_time))
    
    
    sortType = input$Sort
    
    newdata$y.start <- 0
    newdata$y.end <- 0
    yoffset = 0
    #get the right column
    newdata$closestto0[abs(newdata$start.points)<=abs(newdata$end.points)] = newdata$start.points[abs(newdata$start.points)<=abs(newdata$end.points)]
    newdata$closestto0[abs(newdata$start.points)>abs(newdata$end.points)] = newdata$end.points[abs(newdata$start.points)>abs(newdata$end.points)]
    #sorting
    if(sortType == "Start position"){
      newdata <- newdata[order(newdata$start.points, -newdata$size,newdata$typeOrig), ]
    } else if(sortType == "End position"){
      newdata <- newdata[order(-newdata$end.points, -newdata$size,newdata$typeOrig), ]
    } else if(sortType == "Mid position"){
      newdata$middle = (newdata$end.points + newdata$start.points)/2
      newdata <- newdata[order(newdata$middle, -newdata$size,newdata$typeOrig), ]
    }
    else if(sortType == "Closest position"){
      newdata <- newdata[order(-newdata$closestto0, -newdata$size,newdata$typeOrig), ]
    }
    else if(sortType == "Size"){
      newdata <- newdata[order(-newdata$size, newdata$typeOrig, newdata$start.points), ] 
    } else if(sortType == "Type"){
      ##added this merge to allow the Type to be influenced by the order of the types
      newdata = merge(newdata, hardcodedTypesDFnonreactive(), by.x ="typeOrig", by.y = "Type")
      newdata$Text = factor(newdata$Text, levels = rev(input$multiType$order))
      newdata <- newdata[order(newdata$Text,-newdata$size), ] 
    }
    
    newdata = newdata %>% group_by(Alias, Subject) %>% mutate(y.end = cumsum(yheight), y.start = y.end-yheight)
    
    ##added this for hover later
    if(length(unique(newdata$Subject)) > 1){
      newdata <- newdata %>% mutate(SubjectAlias = paste(Subject, Alias, sep = " - "))
    }
    else{
      ##keep it as Alias to ensure it can still be sorted
      newdata <- newdata %>% mutate(SubjectAlias = Alias)
    }
    end_time = Sys.time()-start_time
    print(paste("end: tornadoPlotData",end_time))
    
    newdata
  })
  
  output$subjectPlot <- renderPlot({
    req(tornadoPlotData())
    req(input$yaxis)
    #req(input$Aliases)
    req(d_xminmaxRange())
    start_time = Sys.time()
    #speedup multigroup order is lagging behind Aliases
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    if(debug){
      print("subjectPlot renderPlot")
      print(Sys.time())
    }
    plot.data = tornadoPlotData()
    
    end_time = Sys.time()-start_time
    print(paste("half: subjectPlot",end_time))
    
    
    ##recalculate the frequencies here:
    ### done
    if(input$yaxis == "not set"){
      ymaxInput = input$yaxis
    }
    else if(input$yaxis =="1"){
      ymaxInput = 1
    }
    else{
      #get the maximum ymax value
      testDF = plot.data %>% group_by(code) %>% summarise(total = sum(yheight))
      yAllMax = max(testDF$total)
      ymaxInput = yAllMax
    }
    end_time = Sys.time()-start_time
    print(paste("half2: subjectPlot",end_time))
    
        
    newdata <- plot.data
    if(nrow(newdata)>0){
      ##ensure the factor is from the grouping column if that is set
      if(is_grouped()){
        newdata$Alias = factor(newdata$Alias, levels = input$multiGroupReplicateOrder)
      } else{
        newdata$Alias = factor(newdata$Alias, levels = input$multiGroupOrder)
      }
        plot = tornadoplot(newdata, ymax = ymaxInput, xmin = d_xminmaxRange()[1],
                             xmax = d_xminmaxRange()[2],
                             Type = input$Type)
    }
    end_time = Sys.time()-start_time
    print(paste("half3: subjectPlot",end_time))
    plotsForDownload$tornados <- plot
    end_time = Sys.time()-start_time
    print(paste("end: subjectPlot",end_time))
    
    plot
  })
  
  output$exportSizeDiff = downloadHandler(
    filename = function() {"plotsSizeDiff.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$sizeDiffs)){
        ggsave(file, plotsForDownload$sizeDiffs,height=input$plotHeight/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")
        
      }
    }
  )
  output$exportSNVs = downloadHandler(
    filename = function() {"plotsSNVs.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$snvs)){
        if(input$sepBySubject == "Combined plot"){
          ggsave(file, plotsForDownload$snvs,height=input$plotHeight/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")
        }
        else{
          nrow = length(plotsForDownload$snvs)
          #print(nrow)
          ggsave(file, arrangeGrob(grobs=plotsForDownload$snvs, ncol=1, nrow = nrow),height=(nrow*input$plotHeight)/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")
        }
        
      }
    }
  )
  output$exportTarget = downloadHandler(
    filename = function() {"plotsTarget.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$target)){
        plots = length(input$multiGroupOrder)
        if(is_grouped()){
          plots = length(input$multiGroupReplicateOrder)
        }
        ggsave(file, arrangeGrob(grobs=plotsForDownload$target, ncol=1, nrow = plots),height=(plots*input$plotHeight)/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")  
      }
    }
  )
  
  
  output$exportSize = downloadHandler(
    filename = function() {"plotsSize.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$size)){
        #print(paste("height: ",input$plotHeight/72))
        #print(paste("width: ",input$plotWidth/72))
        ggsave(file, plotsForDownload$size,height=input$plotHeight/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")
        
      }
    }
  )
  output$exportType = downloadHandler(
    filename = function() {"plotsType.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$types)){
        ggsave(file, plotsForDownload$types[["type"]],height=input$plotHeight/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")
      }
    }
  )
  output$exportOutcome = downloadHandler(
    filename = function() {"plotsOutcome.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$outcomes)){
        nrow = length(plotsForDownload$outcomes)
        ncol = 1
        height = nrow*input$plotHeight/72
        width = input$plotWidth/72
        useMatrix = matrix(1:(nrow*ncol),nrow=nrow,ncol = ncol, byrow=TRUE)
        ggsave(file, arrangeGrob(grobs=plotsForDownload$outcomes, ncol=ncol, nrow = nrow, layout_matrix = useMatrix),height=height, width = width,limitsize = FALSE, device = "pdf")
      }
    }
  )
  
  output$exportHom = downloadHandler(
    filename = function() {"plotsHom.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$homs)){
        ggsave(file, plotsForDownload$homs,height=input$plotHeight/72, width=input$plotWidth/72, limitsize = FALSE)
      }
    }
  )
  
  output$export1bpInsertion = downloadHandler(
    filename = function() {"plots1bpInsertion.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$plot1bpInsertion)){
        ggsave(file, plotsForDownload$plot1bpInsertion,height=input$plotHeight/72, width=input$plotWidth/72, limitsize = FALSE)
      }
    }
  )
  
  output$export = downloadHandler(
    filename = function() { paste0(format(Sys.time(), "%Y%m%d_%H%M%S_"), "plotsTornado.pdf")},
    content = function(file) {
      if(!is.null(plotsForDownload$tornados)){
        parts = plotsForDownload$tornados$data %>% select(Alias, Subject) %>% distinct() %>% nrow
        ncol = input$nrCols
        nrow = ceiling(parts/ncol)
        height = nrow*input$plotHeight/72
        width = input$plotWidth/72
        ggsave(file, plotsForDownload$tornados,height=height, width=width,limitsize = FALSE, device = "pdf")  
      }
    }
  )
  output$exportAlleles = downloadHandler(
    filename = function() {"plotsAlleles.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$alleles)){
        parts = length(plotsForDownload$alleles)
        height = parts*input$plotHeight/72
        width = input$plotWidth/72
        ggsave(file, arrangeGrob(grobs=plotsForDownload$alleles, ncol=1, nrow=parts), height=height, width = width,limitsize = FALSE, device = "pdf")
      }
    }
  )
  
  output$exportTI = downloadHandler(
    filename = function() {"plotsTornadoTI.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$tornadoTI)){
        nrow = ceiling(length(plotsForDownload$tornadoTI)/plotsForDownload$tornadoTIcols)
        ncol = plotsForDownload$tornadoTIcols
        height = nrow*input$plotHeight/72
        width = input$plotWidth/72
        useMatrix = matrix(1:(nrow*ncol),nrow=nrow,ncol = ncol, byrow=TRUE)
        ggsave(file, arrangeGrob(grobs=plotsForDownload$tornadoTI, ncol=ncol, nrow = nrow, layout_matrix = useMatrix),height=height, width = width,limitsize = FALSE, device = "pdf")
      }
    }
  )
  
  output$exportSampleInfo = downloadHandler(
    filename = function() {"SIQ_SampleInfo.pdf"},
    content = function(file) {
      if(!is.null(plotsForDownload$samples)){
        if(!is.null(in_stat())){
          plots = 3
          ggsave(file, arrangeGrob(grobs=plotsForDownload$samples, ncol=1, nrow = plots),height=(plots*input$plotHeight)/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")  
        } else{
          plots = 1
          ggsave(file, plotsForDownload$samples,height=input$plotHeight/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")  
        }
      }
    }
  )
  
  output$allele_data <- DT::renderDataTable({
    req(allelePlotData())
    el = allelePlotData()
    colnames = c("OutcomeText", "insertion", "Outcome", "Alias", "fraction")
    
    if(length(unique(el$Subject))>1){
      colnames = c("Subject", colnames)
    }
    
    el = el %>% select_at(colnames)
    el = el %>% mutate(insertion = ifelse(nchar(insertion)<60, insertion, paste(nchar(insertion),"bp")))
    
    ##summarise same events for spread
    el = el %>% group_by_at(vars(-fraction)) %>% summarise(fraction = sum(fraction)) %>% ungroup()
    
    el = el %>%
      mutate(fraction = round(fraction,input$alleleDecimals)) %>%
      spread(key = Alias, value = fraction)
    
    if(input$alleleTopOutcomesChoice == "Total"){
      if("Subject" %in% colnames(el)){
        el = el %>% mutate(Total = rowSums(select_if(., is.numeric), na.rm = T)) %>% arrange(Subject, desc(Total)) %>% select(-Total)
      } else{
        el = el %>% mutate(Total = rowSums(select_if(., is.numeric), na.rm = T)) %>% arrange(desc(Total)) %>% select(-Total)
      }
    } else {
      input$alleleTopOutcomesSample
      if("Subject" %in% colnames(el)){
        el = el %>% arrange(Subject, desc(!!as.name(input$alleleTopOutcomesSample)))
      } else{
        el = el %>% arrange(desc(!!as.name(input$alleleTopOutcomesSample)))
      }
    }
    
    dt = DT::datatable(el,rownames = FALSE,extensions = 'Buttons', options = list(
      pageLength = -1,
      #columnDefs = list(list( targets = target, width = '600px')),
      #scrollX = TRUE,
      dom = 'tB',
      buttons = c('copy', 'excelHtml5')
    ))
    dt %>% formatStyle(names(el),"white-space"="nowrap")
  })
  
  
  output$plot_1bp_data <- DT::renderDataTable({
    req(filter_in_data())
    data = filter_in_data()
    df = get1bpinsertion_data(data)
    
    ##round
    df <- df %>% mutate(across(where(is.numeric), round, 8))
    
    if(is_grouped()){
      if("sdpos" %in% colnames(df)){
        df <- df %>% select(-sdpos)
      }
      values_from_columns = c("mean", "sd","samples")
      names_glue = "{insertion}_{.value}"
    } else{
      values_from_columns = c("total")
      names_glue = NULL
    }
      
    df = df %>% pivot_wider(names_from = insertion, values_from = values_from_columns, 
                                                   names_glue = names_glue,
                                                   names_vary = "slowest")
    
    dt = DT::datatable(df,rownames = FALSE,extensions = 'Buttons', options = list(
      pageLength = -1,
      dom = 'tB',
      buttons = c('copy', 'excelHtml5')
    ))
    dt 
  })
  
  
  ## dispay table for the homology
  output$plot_hom_data <- DT::renderDataTable({
    req(homologyData())
    ##get and round the data to 8 decimals
    df = homologyData() %>% 
      mutate(across(where(is.numeric), round, 8))
      
    
    if(!is_grouped()){
      dfSpread = df %>% 
        select(-n) %>%
        pivot_wider(names_from = Alias, values_from = fraction, values_fill = 0)
    } else{
      if("sdpos" %in% colnames(df)){
        df <- df %>% select(-sdpos)
      }
      values_from_columns = c("mean", "sd","samples")
      names_glue = paste0("{",get_group_column(),"}_{.value}")
      dfSpread = df %>% pivot_wider(names_from = get_group_column(), values_from = values_from_columns, 
                         names_glue = names_glue,
                         names_vary = "slowest")
    }
    
    dt = DT::datatable(dfSpread,rownames = FALSE,extensions = 'Buttons', options = list(
      pageLength = -1,
      dom = 'tB',
      buttons = c('copy', 'excelHtml5')
    ))
    dt 
  })
  
  ##outcome data table
  output$outcome_data <- DT::renderDataTable({
    req(getUMAPDataDF())
    df = getUMAPDataDF()
    
    dt = DT::datatable(df,rownames = FALSE,extensions = 'Buttons', options = list(
      pageLength = -1,
      dom = 'tB',
      buttons = c('copy', 'excelHtml5')
    ))
  })
  
  ##type data table
  output$plot1_data <- DT::renderDataTable({
    req(filter_in_data())
    el = filter_in_data()
    
    group_names = c("Alias","Subject")
    if(input$GroupColumn != "-")  {
      group_names = c(input$GroupColumn, group_names)
    }
    
    if(input$datatableFraction == "relative"){
      countDF = el %>% group_by_at(group_names) %>%  
        dplyr::count(Type = Type, wt = fraction, .drop = FALSE)
      
      ##for relative recalculate the fractions
      if(input$fraction=="relative"){
        countDF = countDF %>%
          mutate(n = n/sum(n))
      }
    }
    ##show the actual read counts
    else if(input$datatableFraction == "absolute"){
      countDF = el %>% 
        group_by_at(group_names) %>%  
        dplyr::count(Type = Type, wt = countEvents, .drop = FALSE) 
    }
    ##remove the Reference type (used for alleles)
    countDF = countDF %>% 
      filter(Type != "Reference")
    
    names_glue = NULL
    values_from_columns = c("n")
    
    if(input$datatableGrouped == "by Group"){
      group_by_name = c(input$GroupColumn, "Subject","Type")
      group_by_complete = c("Subject","Type","Alias")
      countDF = countDF %>% as.data.frame() %>%
        group_by_at(input$GroupColumn) %>%
        complete(Subject,Type, Alias, fill = list(n = 0)) %>%
        group_by_at(group_by_name) %>%
        summarise(mean = mean(n, na.rm=T), sd = sd(n, na.rm=T), samples = n())
      ##these are for the spread later
      values_from_columns = c("mean","sd","samples")
      names_glue = "{Type}_{.value}"
    }
    
    ##spread for viewing
    countDFSpread = countDF %>% pivot_wider(names_from = Type, values_from = values_from_columns, 
                                            names_glue = names_glue,
                                            names_vary = "slowest")
    columns = ncol(countDFSpread)
    
    ##round the numbers because the table will get too large otherwise
    if(input$datatableFraction == "relative"){
      countDFSpread <- countDFSpread %>% mutate(across(where(is.numeric), round, 8))
    }
    
    dt = DT::datatable(countDFSpread,rownames = FALSE,extensions = 'Buttons', options = list(
      pageLength = -1,
      dom = 'tB',
      buttons = c('copy', 'excelHtml5')
    ))
    dt 
  })
  
  ## type plot
  output$typePlot <- renderPlot({
    req(filter_in_data())
    req(input$Aliases)
    
    el = filter_in_data()
    if(nrow(el) == 0){
      return()
    }
    plot = typeplot(el, types = input$Types, fraction = input$fraction)
    #plot <- plot + coord_flip()
    plots=list()
    plots[["type"]] <- plot
    plotsForDownload$types <- plots
    plot
  })
  
  homologyData <- reactive({
    req(filter_in_data())
    
    df = filter_in_data()
    group_columns = c("Alias", "Subject")
    if(is_grouped()){
      group_columns = c(group_columns,get_group_column())
    }
    
    test = df %>% filter(Type == "DELETION" | Type == "TANDEMDUPLICATION") %>% 
      group_by_at(group_columns) %>% 
      dplyr::count(homologyLength = homologyLength, wt = countEvents) %>%   
      mutate(fraction = n / sum(n))
    
    if(is_grouped()){
      columns = c("Subject","homologyLength",get_group_column())
      group_now_sd_pos = c("Subject",get_group_column())
      test = test %>% group_by_at(group_now_sd_pos) %>%
        ##ensure all values are filled in when absent
        complete(Alias, homologyLength, fill = list(fraction = 0, n = 0)) %>%
        group_by_at(columns) %>%
        summarise(mean = mean(fraction), sd = sd(fraction), samples = n()) %>%
        ungroup() %>%
        group_by_at(group_now_sd_pos) %>%
        arrange(homologyLength) %>%
        mutate(sdpos = cumsum(mean))
      
      ##ensure grouped column is now a factor
      ##perhaps this slows down too much here?
      test[[get_group_column()]] = factor(test[[get_group_column()]], levels = input$multiGroupReplicateOrder)
    }
    
    test
  })
  
  #homology plot call
  output$homPlot <- renderPlot({
    req(filter_in_data())
    el = filter_in_data()
    ##now use this data instead
    ##also add a datatable
    dfTest = homologyData()
    
    if(!is_grouped()){
      dfTest$Alias = factor(dfTest$Alias, levels = input$multiGroupOrder)
      x_axis = "Alias"
      y_axis = "fraction"
    } else{
      x_axis = get_group_column()
      y_axis = "mean"
    }
    
    dfTest$homologyLength <- factor(dfTest$homologyLength, levels = unique(sort(dfTest$homologyLength)))
    
    ##get the colors correct, also if there are missing values in homologyLength
    getPalette = colorRampPalette(brewer.pal(9, "PuBu"), bias = 1.5)
    colourCount2 = max(as.numeric(levels(dfTest$homologyLength)),na.rm = T)
    colValues = getPalette(colourCount2+1)
    colValues = setNames(colValues,0:colourCount2)
    
    p = ggplot(dfTest, aes(x=!!as.symbol(x_axis), y=!!as.symbol(y_axis), fill=homologyLength))
    p <- p + geom_bar(stat="identity",position = position_stack(reverse = TRUE))
    
    if(is_grouped()){
      ##add error bars
      p = p + geom_errorbar(aes(ymin=sdpos-sd, ymax=sdpos+sd), width=.2, stat = "identity") 
    }
    
    p<- p +scale_fill_manual(values = colValues)
    #get the names of the types displayed
    typesInData = intersect(c("DELETION", "TANDEMDUPLICATION"),unique(el$Type))
    hdDF = hardcodedTypesDF() %>% filter(Type %in% typesInData)
    names = paste("Type(s) in plot:",paste(hdDF$Text, collapse=", "))
    
    p<- p + theme(plot.title = element_text(size=14, hjust=0.5),panel.border = element_blank(), panel.grid.major = element_blank(),
                  panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
                  legend.text = element_text( size = 10), legend.key.size = unit(5, "mm"), axis.title=element_blank(), legend.title = element_text(size = 10), axis.text.y=element_text(size = 8))  +
      ggtitle(names)
    if(input$facet_wrap == TRUE){
      p<- p + facet_grid(~Subject, scales = "free_x", space = "free_x")
    }
    ###save for the PDF export
    plotsForDownload$homs = p
    return(p)
  })
  
  #homology plot call
  output$homInsPlot <- renderPlot({
    req(input$homRange)
    req(filter_in_data())
    el = filter_in_data()
    el = el[el$homologyLength>=input$homRange,]
    el$Alias = factor(el$Alias, levels = input$multiGroupOrder)
    plot = homplot(el, flipped = TRUE, types = c("DELETION","INSERTION"), fraction = input$display)
    plots=list()
    plots[["hom"]] <- plot
    #plotsForDownload$homsIns <- plots
    plot
  })
  
  
  
  #sizeDiffPlot calls
  output$sizeDiffPlot <- renderPlot({
    req(input$fractionSizeDiff)
    el = filter_in_data()
    el$Alias = factor(el$Alias, levels = input$multiGroupOrder)
    plot = sizeDiffPlot(el, xmin = input$xminmaxRangeSizeDiff[1], xmax = input$xminmaxRangeSizeDiff[2], colors = input$colors, fraction = input$fractionSizeDiff)
    plotsForDownload$sizeDiffs <- plot
    plot
  })
  output$sizePlot <- renderPlot({
    req(input$Column)
    req(input$fractionSize)
    
    el = filter_in_data()
    el$Alias = factor(el$Alias, levels = input$multiGroupOrder)
    plot = sizePlot(el, column = input$Column, fraction = input$fractionSize, ymin = input$ymaxsize[1], ymax = input$ymaxsize[2], useylimit = input$ymaxRangeDiffLimitaxis)
    plotsForDownload$size = plot 
    plot
  })
  output$corPlot <- renderPlot({
    req(filter_in_data())
    req(input$Aliases)
    
    el = filter_in_data()
    el = el[el$Alias %in% input$Aliases,]
    corPlot(el)
  })
  output$statPlot <- renderPlot({
    req(filter_in_data())
    
    #speedup multigroup order is lagging behind Aliases
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    plots=list()
    
    ## to plot at least something if the stats df is not present
    if(is.null(in_stat())){
      el = filter_in_data()
      testDF = el %>% group_by(Alias) %>% dplyr::count(wt=countEvents)
      
      AliasSubject = getAliasSubjectDF(in_stat(),input$multiGroupOrder)
      testDF = merge(AliasSubject,testDF,by = "Alias",all.x=T)
      
      testDF$Alias = factor(testDF$Alias, levels = union(input$multiGroupOrder,testDF$Alias))
      
      plot <- ggplot(testDF, aes(x=Alias, y=n)) + geom_bar(stat = "identity") +
        geom_hline(yintercept=d_minEvents(), color = "red")+
        theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
              panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
              legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title.x=element_blank(), legend.title = element_text(size = 10))+
        ylab("CountEvents")
      
      if(input$facet_wrap == TRUE){
        plot <- plot + facet_grid(~Subject, scales = "free_x", space = "free_x")
      }
      
      plotsForDownload$samples = plot
      
      plots[["CountEvents"]] <- plot
    }
    if(!is.null(in_stat())){
      df = in_stat()
      
      if("Alias" %in% colnames(df)){
        df = df %>% filter(Alias %in% input$multiGroupOrder)
      }
      
      
      testDF = df %>% spread(key = Type, value = Reads)
      testDF$Unmerged = testDF$TotalReads-testDF$MergedReads
      testDF$FailedFilter = testDF$MergedReads-testDF$MergedCorrect
      testDF = testDF %>% select(Alias,File,Subject,Unmerged,FailedFilter,MergedCorrect) %>% 
        rename(correct = MergedCorrect, "not merged" = Unmerged, "failed filter" = FailedFilter) %>% 
        gather("Type","n",4:6)
      
      testDF$Type = factor(testDF$Type, levels = c("failed filter","not merged","correct"))
      colors = c("correct" = "black", "not merged" = "#D4D4D4", "failed filter" = "#FDE0DF")
      
      if(input$sampleInfoRelative == "relative"){
        testDF = testDF %>% group_by(Alias, Subject) %>% mutate(n = n/sum(n))
      }
      
      plotTest = ggplot(testDF, aes(x=Alias,y=n,fill=Type)) + 
        geom_bar(stat="identity", position = position_stack()) +
        scale_fill_manual(values = colors) +
        theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
              panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
              legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title.x=element_blank(), legend.title = element_text(size = 10))
      if(input$facet_wrap == TRUE){
        plotTest <- plotTest + facet_grid(~Subject, scales = "free_x", space = "free_x")
      }
      if(input$sampleInfoRelative == "absolute"){
        plotTest <- plotTest + geom_hline(yintercept=d_minEvents(), color = "red")
      }
      plots[["CountEvents2"]] <- plotTest
      
      #don't do this for now as sometimes there is a file here with no reads
      if("Alias" %in% colnames(df)){
        df = df[df$Alias %in% input$multiGroupOrder,]
      }
      else{
        df$Alias = df$File
      }
      if(nrow(df)==0){
        return()
      }
      #df$File = factor(df$File, levels = unique(df$File))
      df$Alias = factor(df$Alias, levels = union(input$multiGroupOrder,df$Alias))
      if("UnmergedCorrectPositionFR" %in% df$Type){
        keepNames = c("TotalReads","MergedReads","MergedCorrect","MergedBadQual")
        dfPart = df[grepl("Fraction", df$Type ,fixed = TRUE),] ##works
        fraction <- ggplot(dfPart, aes(x=Alias,y=Reads, fill=Type))+geom_bar(stat = "identity", position = position_dodge())+
          theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
                panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
                legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title.x=element_blank(), legend.title = element_text(size = 10))+
          ylab("Fraction correct reads")
        if("Subject" %in% colnames(dfPart)){
          fraction = fraction + facet_grid(~Subject, scales = "free_x", space = "free_x")
        }
      }
      else{
        keepNames = c("TotalReads","MergedCorrect","MergedBadQual")
        dfPart = df[grepl("CorrectFractionTotal", df$Type ,fixed = TRUE),] ##works
        fraction <- ggplot(dfPart, aes(x=Alias,y=Reads))+geom_bar(stat = "identity", position = position_dodge())+
          theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
                panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
                legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title.x=element_blank(), legend.title = element_text(size = 10))+
          ylab("Fraction correct reads")
        if("Subject" %in% colnames(dfPart)){
          fraction = fraction + facet_grid(~Subject, scales = "free_x", space = "free_x")
        }
      }
      plots[["fraction"]] <- fraction
      
      dfPart = df[df$Type %in% keepNames ,] ##works
      dfPart$Type = factor(dfPart$Type,levels=keepNames)
      #sometimes we have the same names...
      if("Subject" %in% colnames(dfPart)){
        dfPart = dfPart %>% group_by(Subject, Alias, Type) %>% count(wt = Reads, name = "Reads")
        dfPart$Alias = factor(dfPart$Alias, levels = union(input$multiGroupOrder,dfPart$Alias))
        total <- ggplot(dfPart, aes(x=Alias,y=Reads,fill=Type))+geom_bar(stat = "identity", position = position_dodge())+
          theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
                panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
                legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title.x=element_blank(), legend.title = element_text(size = 10))+
          ylab("Number of reads")+
          facet_grid(~Subject, scales = "free_x", space = "free_x")
        plots[["total"]] <- total
      } else {
        dfPart = dfPart %>% group_by(Alias, Type) %>% count(wt = Reads, name = "Reads")
        dfPart$Alias = factor(dfPart$Alias, levels = union(input$multiGroupOrder,dfPart$Alias))
        total <- ggplot(dfPart, aes(x=Alias,y=Reads,fill=Type))+geom_bar(stat = "identity", position = position_dodge())+
          theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
                panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
                legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title.x=element_blank(), legend.title = element_text(size = 10))+
          ylab("Number of reads")
        plots[["total"]] <- total
      }
    }
    plotsForDownload$samples = plots
    grid.arrange(grobs=plots, ncol=1)
  })
  
  observeEvent(
    rv$subjects,{
    choices = rv$subjects
    #data_input == 1 = example data
    if(input$data_input == 1){
      selected = choices
    } else{
      selected = choices[1]
    }
    print("updatePickerInput Subject")
    updatePickerInput(session, "Subject", choices = choices, selected = selected )
  })
  
  observe({
    req(input$data_input)
    if(debug){
      print("just an observe AliasColumn")
    }
    if(input$data_input == 3){
      updatePickerInput(session, inputId = "AliasColumn", selected = "Gene")
    }
  })
  
  
  observe({
    req(in_data())
    if(debug){
      print(paste("updateSelectInput","GroupColumn"))
    }
    df = in_data()
    if(is.null(input$AliasColumn)){
      selected = "Alias"
    }
    else{
      selected = input$AliasColumn
    }
    colNamesChar <- df[,sapply(df, is.character)]
    colNamesAll = colnames(colNamesChar)
    updateSelectInput(session,"AliasColumn",
        choices = colNamesAll,
        selected = selected
    )
    colNamesAll = c("-",colNamesAll)
    
    #keeep current selection
    updateSelectInput(session,"GroupColumn",
                      choices = colNamesAll,
                      selected = input$GroupColumn
    )
    
  })
  
  observeEvent(
    rv$types,
    {
      if(debug){
        print("updatePickerInput Types observeEvent")
      }
      updatePickerInput(session, "Types", choices = rv$types, selected = rv$types) 
    }
  )
  
  observe({
    req(in_data())
    req(hardcodedTypesDF())
    if(debug){
      print(paste("updatePickerInput Types observe"))
    }
    ##need to set the types for the screen
    if(input$data_input > 2){
      ##perhaps change the TINS still, depending on presence of 'split TINS'
      types = c("HDR","DELETION","DELINS","INSERTION","TINS","WT","INSERTION_1bp", "TINS_FW", "TINS_RC")
      choices = hardcodedTypesDF()[hardcodedTypesDF()$Type %in% types,]
    } else{
      choices = hardcodedTypesDF()[hardcodedTypesDF()$Type %in% unique(in_data()$Type),]
    }
    choices = choices[order(choices$Text), ]
    choicesInv = setNames(choices$Type, choices$Text)
    rv$types = choicesInv
  })
  
  observe({
    req(filter_in_data())
    if(debug){
      print("updatePickerInput alleleTopOutcomesSample")
    }
    el = filter_in_data() %>% select(Alias) %>% distinct()
    updatePickerInput(session, "alleleTopOutcomesSample", choices = el$Alias )
    
  })
  
  observe({
    req(filter_in_data())
    if(debug){
      print("updatePickerInput tableColumn")
    }
    keepColumns = c("countEvents","fraction","Alias", "Subject", "leftFlank","del","rightFlank","insertion", "Type",
                    "delRelativeStart", "delRelativeEnd", "delSize","insSize")
    cols = colnames(filter_in_data())
    updatePickerInput(session, "tableColumn", choices = cols, selected = keepColumns)
  })
  
  output$minEvents <- renderUI({
    #req(in_data())
    #df = in_data()
    #testDF = df %>% group_by(Alias) %>% dplyr::count(wt=countEvents)
    #maxSize = max(testDF$n)
    maxNr = 6
    choices = c(0,1,10,100,1000,2000,3000,4000,5000,6000,7000,8000,9000,10^(4:maxNr))
    sliderTextInput(
      inputId = "minEvents",
      label = "Minimum number of reads for Alias to be shown:", 
      choices = choices,
      selected = c(1),
      grid = TRUE
    )
  })
  
  ##I need this one to store rv$aliases in
  
  rv <- reactiveValues()
  
  ##set the target/subject options
  observe({
    req(in_data())
    if(debug){
      print("set the target/subject options")
    }
    rv$subjects = sort(unique(in_data()$Subject))
  })
  
  ##set the Aliases
  observe({
    req(pre_filter_in_data())
    req(d_minEvents())
    if(debug){
      print("set the aliases")
    }
    totalAliases = in_stat()
    if("Alias" %in% colnames(totalAliases) & "Subject" %in% colnames(totalAliases)){
      aliases = totalAliases$Alias[totalAliases$Subject %in% input$Subject]
      aliases = sort(unique(aliases))
    } else {
      subAliases = in_data()[in_data()$Subject %in% input$Subject,]
      aliases = sort(unique(subAliases$Alias))
    }
    pre_filter_DF = pre_filter_in_data()
    ##additionalFilter for the max fraction to be included
    #remove Aliases that don't have enough counts
    ##for the MUSIC screen this filtering does not work
    ##the data is not there yet and causes a major slow down
    ##You might have to fix this later on
    if(input$data_input == 3){
      plotAliases = unique(pre_filter_DF$Alias)
    }
    else if(d_minEvents()>0 && input$data_input != 3){
      if(input$minReadsOn == "mutagenic reads"){
        pre_filter_DF = pre_filter_DF %>%
          filter(Type !="WT")
      } else if(input$minReadsOn == "selected types"){
        pre_filter_DF = pre_filter_DF %>%
          filter(Type %in% input$Types)
      }
      testDF = pre_filter_DF %>% 
        filter(Subject %in% input$Subject) %>%
        group_by(Alias) %>% 
        count(wt = countEvents) %>% 
        filter(n>=d_minEvents())
      plotAliases = testDF$Alias
    } else{
      ##==0 add all
      plotAliases = aliases
    }
    if(input$AliasColumn == "Alias"){
      aliases = aliases[aliases %in% plotAliases]
    } else{
      aliases = plotAliases
    }
    rv$aliases = aliases
  })
  
  ##only trigger a change in the picker if Alias is actually changed
  ##also only update the selected part if the selected Alias is not there anymore
  observeEvent(
    rv$aliases,
    {
      req(rv$aliases)
      aliases = rv$aliases
      print("##########changeAliases")
      
      ##not safe to update the selected aliases as that potentially results in an infinite loop
      intersect = intersect(aliases, input$Aliases)
      #print(aliases)
      #if(!is.null(input$Aliases) && length(intersect) == length(input$Aliases)){
      #  ##do not update
      #  return()
      #}
      if(length(intersect) > 0 & length(intersect)<100){
        selected = intersect
      }
      #else 
      else if(length(aliases) >100){
          selected = NULL
      } 
      ##if no overlap between selected and these aliases, select them all
      else{
        selected = aliases
      }
      ##this does not contain the samples that have 0 reads anymore
      ##which may be an issue for the SampleInfo tab
      print("updatePickerInput Aliases")
      updatePickerInput(session, "Aliases", choices = aliases, selected = selected)
      #updatePickerInput(session, "Aliases", choices = aliases)
    }
  )
  
  observe({
    req(input$Aliases)
    ##for testing
    controls = input$Aliases[grepl("wild-type",input$Aliases)]
    updatePickerInput(session, "controls", choices = input$Aliases, selected = controls)
  })
  
  observe({
    req(input$Types)
    choices = c(input$Types,"mean_homology_deletion","mean_homology_TD")
    updatePickerInput(session, "controlsX", choices = choices)
    updatePickerInput(session, "controlsY", choices = choices, selected = choices[1])
  })
  
  observe({
    req(in_data())
    df = in_data()
    updatePickerInput(session, "genotype", choices = colnames(df), selected = "Gene")
    updatePickerInput(session, "dose", choices = colnames(df), selected = "Gene")
  })
  
  
  
  output$color_test <- renderUI({
    start_time <- Sys.time()
    colours <- list()
    hardCodedTypes = hardcodedTypesDFnonreactive()
    for(colour in hardCodedTypes$Type){
      #print(paste0(colour," ",colourCode[[colour]]))
      if(grepl("bp",colour, fixed = TRUE) | grepl("HDR1MM",colour, fixed = TRUE)){
        palette = "square" 
      }
      else{
        palette = "limited"
      }
      colours[[colour]]<- colourInput(
        paste0(colour,"Picker"),
        colour,
        value = hardCodedTypes$Color[hardCodedTypes$Type==colour],
        showColour = "background",
        palette = palette
      )
    }
    end_time <- Sys.time()
    dropdown(
      tags$h3("Select colors"),
      colours,
      icon = icon("palette"),
      label = "Select colors"
    )
  })
  
  plot_rows <- function() {
    req(filter_in_data())
    
    if(length(input$Aliases) == 0){
      return(1)
    }
    if(debug){
      print("plot_rows")
    }
    aliasColumn = "Alias"
    if(is_grouped()){
      aliasColumn = get_group_column()
    }
    df = filter_in_data() %>%
      group_by_at(aliasColumn) %>%
      summarise(Subjects = n_distinct(Subject))
    nrows = sum(df$Subjects)
    return(ceiling((nrows)/input$nrCols))
  }
  plot_rows_no_col <- function() {
      req(filter_in_data())
      if(length(input$Aliases) == 0){
        return(1)
      }
      df = filter_in_data() %>%
        group_by(Alias) %>%
        summarise(Subjects = n_distinct(Subject))
      nrows = sum(df$Subjects)
      ##add one for the legend
      return(nrows+1)
  }
  
  output$ui_plot <- renderUI({
    req(plot_rows())
    if(debug){
      print(paste("subjectPlot",plot_rows()))
    }
    plotOutput("subjectPlot", height = plot_rows()*input$plotHeight, width = input$plotWidth,
               hover = hoverOpts("plot_hover", delay = 10, delayType = "debounce"))
  })
  
  output$ui_tornadplot_flankinsertions <- renderUI({
    plotOutput("tornadoPlotFI", height = plot_rows_no_col()*input$plotHeight, width = input$plotWidth)
  })
  
  output$ui_outcome <- renderUI({
    if(input$typePlotOutcome=="XY"){
      height = input$plotHeight*length(input$controlsY)
    } else if(input$typePlotOutcome=="pca"){
      height = input$plotHeight*2
    } else{
      height = input$plotHeight
    }
      plotOutput("outcomePlot", height = height, width = input$plotWidth)
  })
  output$ui_1bpinsertion <- renderUI({
    height = input$plotHeight
    plotOutput("plot1bpinsertion", height = height, width = input$plotWidth)
  })
  
  output$ui_heatmapend <- renderUI({
    height = input$plotHeight
    plotOutput("outcomeHeatmapEnd", height = height, width = input$plotWidth)
  })
  
  output$ui_homplot <- renderUI({
    plotOutput("homPlot", height = input$plotHeight, width = input$plotWidth)
  })
  
  output$ui_hominsplot <- renderUI({
    plotOutput("homInsPlot", height = input$plotHeight, width = input$plotWidth)
  })
  
  output$ui_typeplot <- renderUI({
    plotOutput("typePlot", height = input$plotHeight, width = input$plotWidth)
  })
  
  output$ui_sizediffplot <- renderUI({
    plotOutput("sizeDiffPlot", height = input$plotHeight, width = input$plotWidth)
  })
  
  output$ui_sizeplot <- renderUI({
    plotOutput("sizePlot", height = input$plotHeight, width = input$plotWidth)
  })
  output$ui_corplot <- renderUI({
    plotOutput("corPlot", height = input$plotHeight, width = input$plotWidth)
  })
  output$ui_statplot <- renderUI({
    #2 graphs
    if(is.null(in_stat())){
      plotOutput("statPlot", height = input$plotHeight, width = input$plotWidth)
    }
    else{
      plotOutput("statPlot", height = 3*input$plotHeight, width = input$plotWidth)
    }
  })
  output$ui_alleles <- renderUI({
    req(filter_in_data())
    nrSubjects = length(unique(filter_in_data()$Subject))
    if(nrSubjects >0){
      #actually we have only one subject if we base it on Sample
      if(input$alleleTopOutcomesChoice == "Sample"){
        nrSubjects = 1
      }
      plotOutput("allelePlot", height = nrSubjects*input$plotHeight, width = input$plotWidth)
    }
  })
  output$ui_info <- renderUI({
    plotOutput("mutFreq", height = input$plotHeight, width = input$plotWidth)
  })
  output$ui_snvplot <- renderUI({
    if(input$sepBySubject =="Combined plot"){
      plotOutput("snvplot", height = input$plotHeight, width = input$plotWidth)
    }
    else{
      plotOutput("snvplot", height = (plot_rows_no_col()-1)*input$plotHeight, width = input$plotWidth)
    }
  })
  output$ui_SizeFreq <- renderUI({
    req(filter_in_data())
    plots = length(input$multiGroupOrder)
    if(is_grouped()){
      ##overwrite with number of groups
      plots = length(input$multiGroupReplicateOrder)
    }
    plotOutput("SizeFreq", height = plots*input$plotHeight, width = input$plotWidth)
  })
  
  sizeFreqData <- reactive({
    req(filter_in_data())
    start = Sys.time()
    el = filter_in_data() %>% 
      mutate(range = delRelativeEnd-1-delRelativeStart) %>%
      filter(range>=0)
    
    ##calculate shift size
    coverageTotal = NULL
    shiftSize = min(el$delRelativeStart)
    if(shiftSize < 1){
      shiftSize = -shiftSize+1
    }
    
    ##let's make the df ready
    if(is_grouped()){
      for(subject in unique(el$Subject)){
        elSub = el %>% filter(Subject == subject)
        for(groupName in unique(elSub[[get_group_column()]])){
          elGroup = elSub %>% filter(!!as.symbol(get_group_column()) == groupName)
          ##how many samples in this group?
          nrAlias = length(unique(elGroup$Alias))
          ir = IRanges(start = elGroup$delRelativeStart+shiftSize, end = elGroup$delRelativeEnd-1+shiftSize)
          ##divide the fraction by the number of Aliases to get to max 1
          coverage_ir <- coverage(ir, weight = elGroup$fraction/nrAlias)
          coverage_df <- as.data.frame(coverage_ir)
          coverage_df$pos <- as.numeric(rownames(coverage_df))
          coverage_df$total_fraction <- coverage_df$value
          ##this may be a problem at some point!!
          coverage_df$Alias = groupName
          coverage_df$Subject = subject
          coverageTotal = dplyr::bind_rows(coverageTotal, coverage_df)
        }
      }
      
    } else{
      for(subject in unique(el$Subject)){
        elSub = el %>% filter(Subject == subject)
        for(alias in unique(elSub$Alias)){
          elAlias = elSub %>% filter(Alias == alias)
          ##coverage only works on >0 locations, so shift the delRelativeStart by shiftSize
          ##use the elAlias df here
          ir = IRanges(start = elAlias$delRelativeStart+shiftSize, end = elAlias$delRelativeEnd-1+shiftSize)
          coverage_ir <- coverage(ir, weight = elAlias$fraction)
          coverage_df <- as.data.frame(coverage_ir)
          coverage_df$pos <- as.numeric(rownames(coverage_df))
          coverage_df$total_fraction <- coverage_df$value
          coverage_df$Alias = alias
          coverage_df$Subject = subject
          coverageTotal = dplyr::bind_rows(coverageTotal, coverage_df)
        }
      }
    }
    ##ensure the shiftSize is removed now
    coverageTotal$pos = coverageTotal$pos - shiftSize
    pos = coverageTotal
    end = Sys.time()
    totalTime = end - start
    print(paste("Took",totalTime))
    pos
  })
  
  output$SizeFreq <- renderPlot({
    req(filter_in_data())
    
    el = filter_in_data()
    el = el %>% 
      mutate(range = delRelativeEnd-1-delRelativeStart) %>%
      filter(range>=0)
                       
    
    plots = list()
    dfs = list()
    xmin = min(el$delRelativeStart)
    xmax = max(el$delRelativeEnd)
    
    ##let's get the data
    pos = sizeFreqData()
    
    p = ggplot(pos,aes(x = pos, y = total_fraction)) + 
      geom_bar(stat = "identity") + 
      theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
            legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), legend.title = element_text(size = 10)) +
      facet_wrap(Subject ~ Alias, ncol = 1,scales = "free") +
      xlab("location")+ylab("fraction of total") +
      NULL
    
    plots[["test"]] = p
    
    plotsForDownload$target = plots
    grid.arrange(grobs = plots, ncol=1)
  })
  
  
  observe({
    req(filter_in_data())
    ##only if the tab is on tornado
    if(input$tabs != "Tornado"){
      return()
    }
    el = filter_in_data()
    if(nrow(el)==0){
      return()
    }
    if(is.null(input$Subject)){
      maxValue = min(el$delRelativeStartTD)
      minValue = max(el$delRelativeEndTD)
    }
    else{
      maxValue = min(el$delRelativeStartTD[el$Subject %in% input$Subject])
      minValue = max(el$delRelativeEndTD[el$Subject %in% input$Subject])
    }
    if(maxValue<(-1000)){
      minValueX = min(-2000, maxValue-20)
    }
    else{
      minValueX = min(-1000,maxValue-20)
    }
    if(minValue>500){
      maxValueX = max(2000, minValue+20)
    }
    else{
      maxValueX = max(1000,minValue+20)
    }
    print(paste("updateSliderInput", minValueX, maxValueX))
    updateSliderInput(session, "xminmaxRange", min = minValueX, max = maxValueX)
  })
  
  #output$homRange <- renderUI({
 #   req(in_data())
  #  req(input$Subject)
  #  sliderInput("homRange",
  #              "from Homology:",
  #              min = 0,
#max = max(in_data()[in_data()$Type=="DELETION",]$homologyLength,na.rm=T),
  #              value = 2,
  #              step = 1
  #  )
    
  #})
  #output$xminmaxRangeSizeDiff <- renderUI({
  #  el = in_data()
  #  if(is.null(input$Subject)){
  #    minValue = -max(el$delSize)
  #    maxValue = max(el$insSize)
  #  }
  #  else{
  #    minValue = -max(el$delSize[el$Subject %in% input$Subject])
  #    maxValue = max(el$insSize[el$Subject %in% input$Subject])
  #  }
  #  print(minValue)
  #  print(maxValue)
  #  sliderInput("xminmaxRangeSizeDiff",
  #              "x-axis minimum",
  #              min = minValue,
  #              max = maxValue,
  # #             value = c(minValue, maxValue)
  #  )
 #   
 # })
  output$ymaxRangeDiff <- renderUI({
    el = filter_in_data()
    if(nrow(el)==0){
      return()
    }
    if(is.null(input$Subject)){
      maxValue = max(el$insSize)
      minValue = -max(el$delSize)
    }
    else{
      maxValue= max(el$insSize[el$Subject %in% input$Subject])
      minValue = -max(el$delSize[el$Subject %in% input$Subject])
    }
    sliderInput("ymaxsize",
                "y-axis maximum",
                min = minValue,
                max = maxValue,
                value = c(minValue,maxValue)
    )
  })
  
  filterAliases <- function(df, minEvents, aliases){
    if(minEvents==0){
      return(aliases)
    } else {
      countDF = df %>% group_by(Alias) %>% count(wt=countEvents)
      countDFAliasesQualify = countDF$Alias[countDF$n>=minEvents]
      return(aliases[aliases %in% countDFAliasesQualify])
    }
  }
  
  getAliasSubjectDF <- function(df, aliases){
    if("Subject" %in% colnames(df)){
      df = df[df$Alias %in% aliases,]
      return(unique(df[c("Alias", "Subject")]))
    } else {
      #legacy option
      req(in_data())
      el = in_data()
      tempDF = unique(el[c("Alias", "Subject")])
      tempDF = tempDF[tempDF$Alias %in% aliases,]
      return(tempDF)
    }
  }
  
  typeplot <- function(el, types = c(), fraction = "relative"){
    addGroup = F
    if(input$GroupColumn != "-" & input$GroupColumn %in% colnames(el)){
      addGroup = T
      group_by_columns = c("Subject", "Alias", input$GroupColumn)
    } else{
      group_by_columns = c("Subject", "Alias")
    }
    ##to enable sorting the levels have to be set
    ##but not if we will display the group as that removes the error bars
    el$Alias = factor(el$Alias, levels = input$multiGroupOrder)
    if(addGroup){
      el[[input$GroupColumn]] = factor(el[[input$GroupColumn]], levels = input$multiGroupReplicateOrder)      
    }
    
    if(fraction=="relative"){
      test2 = el %>% group_by_at(group_by_columns) %>%  dplyr::count(Type = Type, wt = fraction) %>%   mutate(fraction = n / sum(n))
    }
    else{
      test2 = el %>% group_by_at(group_by_columns) %>%  dplyr::count(Type = Type, wt = fraction) %>%   mutate(fraction = n ) 
    }
    #convert
    #keep all.x in case of 0 events in a sample
    test2 = merge(test2,hardcodedTypesDF(),by = "Type", all.x=T)
    colorType = hardcodedTypesDF()$Color
    colorType = setNames(colorType,hardcodedTypesDF()$Text)
    ## remove the unused values as scale_fill_manual now show all of them
    colorType = colorType[names(colorType) %in% unique(test2$Text)]
    
    
    test2$Text = factor(test2$Text, levels = input$multiType$order)
    
    ##experimental way to show erro bars on grouping variables
    if(addGroup){
      
      test2 = test2 %>% group_by_at(c("Subject", input$GroupColumn,"Text")) %>% 
        summarise(meanFraction = mean(fraction, na.rm=T), sdFraction = sd(fraction, na.rm=T))
      test3 = test2 %>% ungroup() %>% group_by_at(c("Subject", input$GroupColumn)) %>% arrange(desc(Text)) %>%
        mutate(sdpos = cumsum(meanFraction))
      
      plot <- ggplot(test3, aes_string(x=input$GroupColumn, y="meanFraction", fill="Text")) +
        geom_bar(stat="identity") +
        geom_errorbar(aes(ymin=sdpos-sdFraction, ymax=sdpos+sdFraction), width=.2, stat = "identity") +
        NULL
      if(input$facet_wrap == TRUE){
        plot <- plot + facet_grid(~Subject, scales = "free_x", space = "free_x")
      }
      
    } else {
      
      
      plot <- ggplot(test2, aes(fill=Text,y=fraction, x=Alias)) + geom_bar(stat = "identity") +
        NULL
        
      if(input$facet_wrap == TRUE){
        plot <- plot + facet_grid(~Subject, scales = "free_x", space = "free_x")
      }
    }
    plot = plot +
      scale_fill_manual(values = colorType) + 
      theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
            legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title=element_blank(), legend.title = element_text(size = 10))
      
    
    return(plot)
  }
  
  homplot <- function(el,types = c(), flipped = FALSE, fraction = "total"){
    if(nrow(el)==0){
      return()
    }
    
    el = el[el$Type %in% types,]
    el$homologyLength <- factor(el$homologyLength, levels = unique(sort(el$homologyLength)))
    if(fraction=="total"){
      test = el[el$Type %in% types,] %>% group_by(Alias, Subject) %>% dplyr::count(homologyLength = homologyLength, wt = fraction) %>%   mutate(fraction = n / sum(n))
    }
    else{
      test = el[el$Type %in% types,] %>% group_by(Alias, Subject) %>% dplyr::count(homologyLength = homologyLength, wt = fraction) %>%   mutate(totalFraction = sum(n))
    }
    
    colourCount = length(unique(test$homologyLength))
    getPalette = colorRampPalette(brewer.pal(9, "PuBu"), bias = 1.5)
    
    colourCount2 = max(as.numeric(levels(el$homologyLength)),na.rm = T)
    
    colValues = getPalette(colourCount2+1)
    colValues = setNames(colValues,0:colourCount2)
    
    #title = paste0(unique(el$Subject)," ",unique(el$Type))
    
    #for data labels
    test$n[test$n==0] <- ""
    
    if(!is.null(test[['totalFraction']])){
      p = ggplot(test, aes(x=reorder(Alias,totalFraction), y=n, fill=homologyLength))
    } else{
      p = ggplot(test, aes(x=Alias, y=fraction, fill=homologyLength))
    }
    p <- p + geom_bar(stat="identity",position = position_stack(reverse = TRUE))
      
    if("INSERTION" %in% types){
      p<- p +scale_fill_manual(values = c(colourCode[["INSERTION"]],getPalette(colourCount-1)))
    }
    else{
      p<- p +scale_fill_manual(values = colValues)
    }
    #get the names of the types displayed
    hdDF = hardcodedTypesDF()
    hdDF = hdDF[hdDF$Type %in% unique(el$Type),]
    names = paste(hdDF$Text, collapse=", ")
    names = paste("Type(s) in plot:",names)
    
    p<- p + theme(plot.title = element_text(size=14, hjust=0.5),panel.border = element_blank(), panel.grid.major = element_blank(),
                  panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
                  legend.text = element_text( size = 10), legend.key.size = unit(5, "mm"), axis.title=element_blank(), legend.title = element_text(size = 10), axis.text.y=element_text(size = 8))  +
      ggtitle(names)
    if(flipped){
      #p = p + coord_flip()
    }
    #this is broken now as it only displays relative labels. I want to avoid using countEvents as it might not be correct
    if(input$data_labels_hom==TRUE){
      p <- p + geom_text(aes(label = n),
                         position=position_stack(0.5, reverse = TRUE))  
    }
    if(input$facet_wrap == TRUE){
      p<- p + facet_grid(~Subject, scales = "free_x", space = "free_x")
    }
    
    return(p)
  }
  
  sizeDiffPlot <- function(el, xmin = xmin, xmax = xmax, colors = 3, fraction = "absolute"){
    testData = el %>% group_by(Alias) %>% dplyr::count(sizeDiff = insSize-delSize, wt = fraction)
    if(fraction == "relative"){
      testData = testData %>% group_by(Alias) %>% mutate(n = n / sum(n))
    }
    testData = testData %>% mutate(n = if_else(sizeDiff < 0, -1*n, n))
    #title = paste0(unique(el$Subject)," sizeDiff ",unique(el$Type))
    
    #get the subject in there
    AliasSubject = unique(el[c("Alias", "Subject")])
    testData = merge(testData,AliasSubject,by = "Alias")
    
    
    p <- ggplot(testData)+geom_tile(aes(x=Alias,y=sizeDiff, fill=n))+
      theme(axis.text.x = element_text(angle = 90, size = 10), axis.text.y = element_text(size=10), panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(), panel.background = element_blank(), axis.title=element_blank(), legend.title = element_text(size = 8),legend.text = element_text( size = 6)) + 
      scale_y_continuous(limits = c(xmin,xmax)) +
      #facet_grid(. ~ Subject, scales = "free_y", space = "free_y") 
      facet_grid(. ~ Subject)
    if(colors == 3){
      p<- p+ scale_fill_gradientn(colours=c("blue4","white", "red4"), values = scales::rescale(c(min(testData$n),0,max(testData$n)))) 
    }
    else if(colors == 5){
      p<- p + scale_fill_gradientn(colours=c("blue4","lightblue","white", "lightcoral", "red4"), values = scales::rescale(c(min(testData$n),0-.Machine$double.eps,0,0+.Machine$double.eps,max(testData$n)))) 
    }
    return(p)
  }
  
  allelePlotData <- reactive({
    req(filter_in_data())
    req(input$Aliases)
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    el = filter_in_data()

        ##recalculate fraction as this is not done within filter_in_data
    if(input$alleleFractionBasedOn == "relative"){
      el = el %>% filter(fraction != Inf) %>% group_by(Alias) %>% mutate(fraction = fraction/sum(fraction))
    } else if(input$alleleFractionBasedOn == "mutagenic"){
      ##Get the mutagenic fraction per Alias/Subject through this call
      mutFractions = mutagenic_fractions()
      el = merge(el, mutFractions, by = c("Subject","Alias")) %>% mutate(fraction = fraction/mutagenicFraction)
    }
    ##bug in the next part of the code, you need to determine the actual outcomes to be shown asap
    dnaRefStrings = getDNARefStrings(el) %>% mutate(Outcome = "Reference", totalFraction = Inf, fraction = Inf)
    el = el %>% mutate(OutcomeDigital = paste(Subject, delRelativeStart, delRelativeEnd, insertion))
    if(input$alleleTopOutcomesChoice == "Total"){
      keepOutcomes = el %>% select(OutcomeDigital, Subject, fraction) %>% group_by(Subject, OutcomeDigital) %>% 
        summarise(totalFraction = sum(fraction)) %>% slice_max(totalFraction, n = input$alleleTopOutcomes + 1)
    } else{
      subject = el %>% select(Alias, Subject) %>% distinct() %>% filter(Alias == input$alleleTopOutcomesSample)
      #only take one subject here
      subject = subject$Subject[1]
      #and filter test2 otherwise it still does not work
      el = el %>% filter(Subject == subject)
      
      keepOutcomes = el %>% filter(Alias == input$alleleTopOutcomesSample & Subject == subject) %>% 
        mutate(totalFraction = fraction) %>%
        select(Subject, OutcomeDigital, totalFraction) %>% 
        slice_max(totalFraction, n = input$alleleTopOutcomes + 1)
    }
    elSub = el %>% filter(OutcomeDigital %in% keepOutcomes$OutcomeDigital)
    
    
    #dnaRefStrings = getDNARefStrings(el) %>% mutate(Outcome = "Reference", totalFraction = Inf, fraction = Inf)
    #bug if Alias is a merge of more samples, we should select more events, so File was added to grouping
    #if("File" %in% colnames(el)){
    #  elSub = el %>% ungroup() %>% group_by(Subject, Alias, File) %>% slice_max(fraction, n = input$alleleTopOutcomes)
    #} else{
    #  elSub = el %>% ungroup() %>% group_by(Subject, Alias) %>% slice_max(fraction, n = input$alleleTopOutcomes)
    #}
    el = rbind(elSub, dnaRefStrings)
    
    el = el %>% ungroup() %>% mutate(Category = paste(Type,delSize))
    
    colorType = hardcodedTypesDF()$Color
    colorType = setNames(colorType,hardcodedTypesDF()$Text)
    
    ###only keep the important ones
    test2 = merge(el,hardcodedTypesDF(),by = "Type", all.x=T)
    
    ##changed into case_when and added missing type
    test2 = test2 %>% mutate(Outcome = case_when(
      Type == "DELETION" & homologyLength == 0 ~ paste(Text, delSize, "bp, pos:",delRelativeStart),
      Type == "DELETION" ~ paste(Text, delSize,"bp, hom:", paste0(homologyLength,"bp"), homology, ", pos:",delRelativeStart),
      Type == "WT" | Type == "HDR" ~ paste(Text),
      Type == "INSERTION_1bp" ~ paste(Text, insertion),
      Type == "SNV" ~ paste(Text, del,">", insertion,"pos:",delRelativeStart),
      Type == "DELINS" & insSize < 6 ~ paste(Text, "del:" ,delSize,", ins:", insSize, " ,ins:",insertion, ", pos:",delRelativeStart),
      Type == "TINS" | Type == "DELINS" ~ paste(Text, "del:" ,delSize,", ins:", insSize, ", pos:",delRelativeStart),
      Type == "TANDEMDUPLICATION" | Type == "TANDEMDUPLICATION_COMPOUND" ~ paste(Text, insSize, "bp, pos:",delRelativeStart),
      Type == "HDR1MM" ~ paste(Text, insSize, "bp"),
      Type == "Reference" ~ Type,
      TRUE ~ paste(Text,"pos:",delRelativeStart),
    ))
    
    #this needs to be adapted based on user input
    if(input$alleleTopOutcomesChoice == "Total"){
      keepOutcomes = test2 %>% select(Outcome, Subject, fraction) %>% group_by(Subject, Outcome) %>% 
        summarise(totalFraction = sum(fraction)) %>% slice_max(totalFraction, n = input$alleleTopOutcomes + 1)
    } else{
      subject = test2 %>% select(Alias, Subject) %>% distinct() %>% filter(Alias == input$alleleTopOutcomesSample)
      #only take one subject here
      subject = subject$Subject[1]
      #and filter test2 otherwise it still does not work
      test2 = test2 %>% filter(Subject == subject)
      
      keepOutcomes = test2 %>% filter(Alias == input$alleleTopOutcomesSample & Subject == subject) %>% 
        mutate(totalFraction = fraction) %>%
        select(Subject, Outcome, totalFraction) %>% 
        slice_max(totalFraction, n = input$alleleTopOutcomes + 1)
    }
    
    
    
    keepOutcomes = rbind(keepOutcomes, dnaRefStrings %>% ungroup() %>% select(Outcome, Subject, totalFraction) %>% distinct())
    
    test2 = test2 %>% filter(Outcome %in% keepOutcomes$Outcome)
    test2$Outcome = factor(test2$Outcome, levels = unique(keepOutcomes$Outcome))
    test2 = addOutcomeText(test2, dnaRefStrings, leftSize = input$alleleOutcomeTable[1], rightSize = input$alleleOutcomeTable[2])
    
    return(test2)
  })
  
  getDNARefStrings <- function(df){
    return(df %>% filter(countEvents == 0))
  }
  
  addOutcomeText <- function(df, dnaStrings, leftSize, rightSize){
    if(nrow(dnaStrings) == 0 || !"Raw" %in% colnames(df)){
      df$OutcomeText = paste(df$leftFlank, tolower(df$del), df$del, df$insertion)
      return(df)
    }
    
    for(row in 1:nrow(df)){
      left = df$leftFlank[row]
      del = df$del[row]
      right = df$rightFlank[row]
      leftPos = df$delRelativeStart[row]
      rightPos = df$delRelativeEnd[row]
      alias = df$Alias[row]
      subject = df$Subject[row]
      delString = ""
      dnaStringRow = dnaStrings %>% filter(Alias == alias & Subject == subject)
      ##safety to at least select a reference
      if(nrow(dnaStringRow) == 0 | nrow(dnaStringRow) > 1){
        dnaStringRow = dnaStrings %>% ungroup () %>% filter(Subject == subject) %>% filter(row_number() == 1)
      }
      
      #dnaStringRow$delRelativeStart can be >0 which means we have two cut sites!
      startPosLeftDNA = dnaStringRow$delStart-dnaStringRow$delRelativeStart+leftSize
      endPosLeftDNA = dnaStringRow$delStart-dnaStringRow$delRelativeStart+leftPos
      startPosRightDNA = dnaStringRow$delStart-dnaStringRow$delRelativeStart+rightPos+1
      endPosRightDNA = dnaStringRow$delStart+rightSize
      inserted = F
      
      leftDNA = ""
      rightDNA = ""
      if(endPosLeftDNA>startPosLeftDNA){
        leftDNA = substr(dnaStringRow$Raw, start = startPosLeftDNA, stop = endPosLeftDNA)
      }
      if(endPosRightDNA>startPosRightDNA){
        rightDNA = substr(dnaStringRow$Raw, start = startPosRightDNA, stop = endPosRightDNA)
      }
      #delstring depends on size of left and right DNA
      if(df$delSize[row] > 0){
        ##should be adjustable
        #dnaStringRow$delRelativeStart can be >0 which means we have two cut sites!
        totalSize = -leftSize+rightSize+dnaStringRow$delRelativeStart+1
        totalRemains = totalSize - nchar(leftDNA) - nchar(rightDNA)
        if(totalRemains<0) totalRemains = 0
        delStringSizeAdjusted = min(totalRemains, df$delSize[row])
        delString = paste(replicate(delStringSizeAdjusted, "-"), collapse = "")
      } else if(df$insSize[row] >0){
        #to identify the position of the insert, only done if delsize == 0
        delString = "^"
        inserted = T
      }
      
      test = paste0(leftDNA, delString, rightDNA)
      #insert the cut positions
      if(dnaStringRow$delRelativeStart==0){
        leftSizeCalc = -leftSize+1
        if(inserted && nchar(leftDNA)<leftSizeCalc){
          leftSizeCalc = leftSizeCalc + 1
        }
        test = paste0(substr(test, start = 0, stop = leftSizeCalc),"|",substr(test, start = leftSizeCalc+1, stop = nchar(test)))
      } else{
        ##two sites
        leftSizeCalc = -leftSize+1
        middleSizeCalc = leftSizeCalc+dnaStringRow$delRelativeStart
        if(inserted){
          if(nchar(leftDNA)<leftSizeCalc){
            leftSizeCalc = leftSizeCalc + 1
          } else if(nchar(leftDNA)<middleSizeCalc){
            middleSizeCalc = middleSizeCalc+1
          }
        }
        testLeft = paste0(substr(test, start = 0, stop = leftSizeCalc),"|")
        testMiddle = paste0(substr(test, start = leftSizeCalc+1, stop = middleSizeCalc),"|")
        testRight = substr(test, start = middleSizeCalc+1, stop = nchar(test))
        test = paste0(testLeft, testMiddle, testRight)
      }
      df$OutcomeText[row] = test
    }
    
    df
  }
  
  
  output$allelePlot <- renderPlot({
    req(allelePlotData())

    test2 = allelePlotData()
    #kick out the Reference here!
    test2 = test2 %>% filter(Type != "Reference")
    colorType = hardcodedTypesDF()$Color
    colorType = setNames(colorType,hardcodedTypesDF()$Text)
    
    colorType = colorType[names(colorType) %in% unique(test2$Text)]
    
    plots = list()
    
    test2$Alias = factor(test2$Alias, levels = input$multiGroupOrder)
    #colnames = c("Outcome", "insertion", "Alias", "fraction")
    
    addGroup = F
    if(is_grouped()){
      addGroup = T
    }
    
    #normal situation
    if(!addGroup) {
      for(subject in unique(test2$Subject)){
        test2Sub = test2 %>% filter(Subject == subject)
        colorTypeSub = colorType[names(colorType) %in% unique(test2Sub$Text)] 
        plot2 = ggplot(test2Sub, aes(x=fraction, y = Outcome, fill = Text)) + geom_bar(stat="identity") +
          #scale_fill_gradientn(colours = c("white","red")) +
          scale_fill_manual(values = colorTypeSub) +
          scale_y_discrete(limits = rev) +
          facet_wrap(Alias ~ ., nrow = 1) +
          ggtitle(subject) +
          #theme(axis.text.x = element_text(angle = 90, size = 10), axis.text.y = element_text(size=10), panel.grid.major = element_blank(),
          #      panel.grid.minor = element_blank(), panel.background = element_blank(),  legend.title = element_text(size = 8),legend.text = element_text( size = 6)) +
          NULL
        plots[[subject]] = plot2
      }
    }
    ##grouped viewing
    else{
      ##there is a problem here as the list of events might not be complete
      for(subject in unique(test2$Subject)){
        test2Sub = test2 %>% filter(Subject == subject)
        
        ##get the grouped columns as text
        group_column = c(get_group_column(),"Outcome","Text")
        
        ## add zero for samples that do not have that outcome?!!!!
        ##########
        test2Sub = test2Sub %>% select_at(c("fraction","Text","Subject", "Alias",group_column))
        reads = total_reads() %>% select_at(c("Subject", "Alias",get_group_column()))
        ##ensure only the Alias/Subject combinations are added that are selected
        reads = reads %>% filter(Subject %in% input$Subject & Alias %in% input$Aliases )
        
        test2Sub = dplyr::left_join(reads,test2Sub, by = c("Alias","Subject",get_group_column())) %>% 
          group_by_at("Subject") %>% complete(Alias, !!sym(get_group_column()), nesting(Outcome, Text))
        
        
        ##first ensure that the events are combined per Alias
        test2Sub = test2Sub %>% ungroup %>% group_by_at(vars(-fraction)) %>%
          summarise(fraction = sum(fraction))
        

        ##calculate mean to be displayed
        #####still an issue here with the complete function!
        test2Sub = test2Sub %>% ungroup() %>% group_by_at(group_column) %>% 
          summarise(mean = mean(fraction, na.rm = T), sd = sd(fraction, na.rm=T)) %>%
          filter(Outcome != "Reference")
        
        
        
        colorTypeSub = colorType[names(colorType) %in% unique(test2Sub$Text)] 
        plot2 = ggplot(test2Sub, aes(x=mean, y = Outcome, fill = Text)) + 
          geom_bar(stat="identity") +
          geom_errorbar(aes(xmin=mean-sd, xmax=mean+sd), width=.2, stat = "identity") +
          #scale_fill_gradientn(colours = c("white","red")) +
          scale_fill_manual(values = colorTypeSub) +
          scale_y_discrete(limits = rev) +
          #special way to get the formula
          facet_wrap(as.formula(paste(input$GroupColumn,"~",".")), nrow = 1) +
          
          ggtitle(subject) +
          #theme(axis.text.x = element_text(angle = 90, size = 10), axis.text.y = element_text(size=10), panel.grid.major = element_blank(),
          #      panel.grid.minor = element_blank(), panel.background = element_blank(),  legend.title = element_text(size = 8),legend.text = element_text( size = 6)) +
          NULL
        plots[[subject]] = plot2
      }
      
    }
    #forTable = el %>% select(OutcomeText, Category) %>% distinct()
    #table = tableGrob(forTable)
    plotsForDownload$alleles = plots
    grid.arrange(grobs = plots, ncol = 1)
    #plot2
  })
  
  ##get the mutation frequency data
  mutFreqData <- reactive({
    req(filter_in_data())
    req(input$Aliases)
    #speedup multigroup order is lagging behind Aliases
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    df = filter_in_data()
    #Get the totals
    if(is_grouped()){
      groups = c("Subject","Alias", get_group_column())
      dfFraction = df %>% group_by_at(groups) %>% dplyr::count(wt=countEvents)
      dfMutFraction = df[df$Type != "WT",] %>% group_by_at(groups) %>% count (wt = countEvents, name = "wt")
      df = merge(dfFraction, dfMutFraction, by=groups)
      df$mut = df$wt/df$n
      ##set factor
      df[[get_group_column()]] = factor(df[[get_group_column()]], levels = input$multiGroupReplicateOrder)
      return(df)
    } else{
      dfFraction = df %>% group_by(Subject, Alias) %>% dplyr::count(wt=countEvents)
      dfMutFraction = df[df$Type != "WT",] %>% group_by(Subject, Alias) %>% count (wt = countEvents, name = "wt")
      df = merge(dfFraction, dfMutFraction, by=c("Subject","Alias"))
      df$mut = df$wt/df$n
      ##set factor
      df$Alias = factor(df$Alias, levels = input$multiGroupOrder)
      return(df)
    }
  })
  
  output$mutFreqTable <- DT::renderDataTable({
    req(mutFreqData())
    dfMutFreq = mutFreqData() %>% 
      dplyr::rename(total_reads = n, mut_reads = wt, mut_fraction = mut)
    
    if(input$mutFreqTableSummary & is_grouped()){
      groups = c("Subject", get_group_column())
      ##overwrite with summary info
      dfMutFreq = dfMutFreq %>% group_by_at(groups) %>%
        summarise(Samples = n(),
                  Mean = mean(mut_fraction),
                  Median = median(mut_fraction),
                  Min = min(mut_fraction),
                  Max = max(mut_fraction)
        )
      
    }
    dt = DT::datatable(dfMutFreq,rownames = FALSE,extensions = 'Buttons', options = list(
      pageLength = -1,
      dom = 'tB',
      buttons = list("copy", "excel","csv")
    ))
    dt 
  })
  
  output$mutFreq <- renderPlot({
    req(mutFreqData())
    dfMutFreq = mutFreqData()
    #Get the totals
    if(is_grouped()){
      plot <- ggplot(dfMutFreq, aes(x=!!as.symbol(get_group_column()), y = mut, fill = !!as.symbol(get_group_column())))
      
      if(input$mutFreqViolinPlot){
        plot <- plot + geom_violin(trim = FALSE, alpha = 0.5) # Violin plot with transparency
      }
      
      if(input$mutFreqBoxPlot){
        plot <- plot + geom_boxplot(width = 0.1, color = "black", fill = "white", alpha = 0.7)   # Distinct boxplot
      }
      plot <- plot +
        geom_jitter(width = 0.2, size = 1.5, alpha = 0.6) +
        scale_y_continuous(limits = c(0, 1), expand = c(0, 0)) +
        theme(legend.position = "none") +  # Remove legend
        labs(y = "Mutagenic fraction") +
        NULL
      
    }else {
      
      plot <- ggplot(dfMutFreq, aes(x=Alias,y=mut)) + geom_bar(stat = "identity") +
        theme(axis.text.x = element_text(angle = 90, size = 10), axis.text.y = element_text(size=10), panel.grid.major = element_blank(),
              panel.grid.minor = element_blank(), panel.background = element_blank(),  legend.title = element_text(size = 8),legend.text = element_text( size = 6)) +
        xlab("Sample") + ylab("Fraction")
    }
    if(input$facet_wrap == TRUE && "Subject" %in% colnames(dfMutFreq)){
      plot <- plot + facet_grid(~Subject, scales = "free_x", space = "free_x")
    }
    plot
    
  })
  
  output$snvplot <- renderPlot({
    req(filter_in_data())
    req(input$Aliases)
    
    #speedup multigroup order is lagging behind Aliases
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    el = filter_in_data()
    ##ensure the total fraction is set to 1
    el = el %>% 
      group_by(Alias) %>% 
      mutate(fraction = fraction / sum(fraction)) %>%
      ungroup()
    
    
    if(input$snvrange>1){
      el$sizeChange = el$delSize-el$insSize
      el = el %>% filter(Type == "SNV" |
                           (Type == "DELINS" & sizeChange ==0 & delSize<=input$snvrange))
      
      if(input$snvrangesplit){
        ##ok now change the data a bit to ensure >1 DELINS are spread onto the graph
        el = el %>% mutate(rowNr = row_number()) %>%
          slice(rep(1:n(), times = delSize)) %>% 
          group_by(rowNr) %>% 
          mutate(duplicateNr = row_number()) %>%
          mutate(del = substring(del,duplicateNr,duplicateNr)) %>%
          mutate(insertion = substring(insertion, duplicateNr, duplicateNr)) %>%
          mutate(delRelativeStart = delRelativeStart + duplicateNr)
      }
      
      insOption = unique(el$insertion)
      insOptionSorted = insOption[order(nchar(insOption),insOption)]
      el$insertion = factor(el$insertion,levels = insOptionSorted)
              
    } else{
      ##only SNVS
      el = el %>% filter(Type == "SNV")
    }
    
    if(input$sepBySubject == "Combined plot"){
      test = el %>% group_by(Subject, Alias) %>% count (delRelativeStart,wt = fraction, name = "snv") 
      #test = merge(testDF,test,by="Alias")
      #test$snv = test$n/test$total
      if(length(input$Subject) > 1){
        test$Sample = paste(test$Alias, test$Subject)
      } else{
        test$Sample = test$Alias
      }
      
      
      plot = ggplot(test, aes(x=delRelativeStart,y=snv, group=Subject, color=Sample)) + geom_point() +
        theme(axis.text.x = element_text(angle = 90, size = 10), axis.text.y = element_text(size=10), panel.grid.major = element_blank(),
              panel.grid.minor = element_blank(), panel.background = element_blank(), legend.title = element_text(size = 8),legend.text = element_text( size = 6)) +
        xlab("Location")+ylab("Frequency")
      #facet_grid(~Subject, scales = "free_x", space = "free_x") 
      plotsForDownload$snvs = plot
      plot
    }
    else{
      #colours are fixed for now
      #snvColors = c("GC->TA" = "#C1252B", "GC->CG" = "#211C1D", "GC->AT" = "#989797",
      #              "AT->TA" = "#89BAD4", "AT->GC" = "#2574B5", "AT->CG" = "#332B6E")
      #Get the info
      test = el %>% group_by(Subject, Alias,insertion) %>% count (delRelativeStart,wt = fraction, name = "snv")
      plots <- list()
      minX = min(test$delRelativeStart)-1
      maxX = max(test$delRelativeStart)+1
      
      yAllMax = -1
      if(input$yaxisSNV == "max of plots"){
        countMax = test %>% group_by(Subject, Alias,delRelativeStart) %>% count (wt = snv)
        yAllMax = max(countMax$n)
      }
      subjects = intersect(input$Subject, unique(test$Subject))
      for(subject in subjects){
        for(alias in input$multiGroupOrder){
          testPart = test[test$Alias == alias & test$Subject == subject,]
          if(length(input$Subject)>1){
            name = paste(subject, alias, sep = " - ")
          } else{
            name = alias
          }
          plot <- ggplot(testPart, aes(x=delRelativeStart,y=snv, fill=insertion)) + geom_bar(stat = "identity", position = "stack") +
            theme(axis.text.x = element_text(angle = 90, size = 10), axis.text.y = element_text(size=10), panel.grid.major = element_blank(),
                  panel.grid.minor = element_blank(), panel.background = element_blank(), legend.title = element_text(size = 8),legend.text = element_text( size = 6)) +
            ggtitle(name)+
            scale_fill_viridis_d()+
            scale_x_continuous(limits = c(minX,maxX)) +
            xlab("Location")+ylab("Frequency")
          if(yAllMax!=-1){
            plot = plot + scale_y_continuous(limits = c(0,yAllMax))
          }
          plots[[name]] <- plot 
        }
      }
      plotsOut = grid.arrange(grobs = plots, ncol=1)
      plotsForDownload$snvs = plots
      plotsOut
    }
  })
  
  
  sizePlot <- function(el, column = "delSize", fraction = "absolute", ymin, ymax, useylimit){
    
    if(column == "delSize"){
      testData = el %>% group_by(Subject, Alias) %>% dplyr::count(sizeDiff = delSize, wt = fraction)
    }
    else if(column == "insSize"){
      testData = el %>% group_by(Subject, Alias) %>% dplyr::count(sizeDiff = insSize, wt = fraction)
    }
    else{
      testData = el[el$delSize>0,] %>% group_by(Subject, Alias) %>% dplyr::count(sizeDiff = -delSize, wt = fraction)
      testData2 = el[el$insSize>0,] %>% group_by(Subject, Alias) %>% dplyr::count(sizeDiff = insSize, wt = fraction)
      #count the 0 separate
      testData3 = el[el$insSize==0 & el$delSize==0,] %>% group_by(Subject, Alias) %>% dplyr::count(sizeDiff = insSize, wt = fraction)
      testData = rbind(testData,testData2,testData3)
    }
    if(fraction == "relative"){
      testData = testData %>% group_by(Subject, Alias) %>% mutate(n = n / sum(n))
    }
    
    #disabled as change of Alias column might lead to loss of Subject separation
    #get the subject in there
    #AliasSubject = getAliasSubjectDF(in_stat(),keepAliases)
    #if(nrow(AliasSubject)>0){
    #  testData = merge(AliasSubject,testData,by = "Alias", all.x=T)
    #}
    testData$Alias = factor(testData$Alias, levels = input$multiGroupOrder)
    
    if(input$TypePlot == "heatmap"){
      p <- ggplot(testData, aes(x=Alias,y=sizeDiff, fill=n))+geom_tile()+
        scale_fill_gradientn(colours=c("white", "blue"), limits = c(0,max(testData$n)))
    } else if(input$TypePlot == "violin"){ 
      p <- ggplot(testData,aes(x=Alias,y=sizeDiff, weight=n))+geom_violin(lwd = 0.25)+
        geom_boxplot(width=0.1, lwd = 0.25,outlier.shape = NA)
    }  else if(input$TypePlot == "boxplot"){ 
      p <- ggplot(testData,aes(x=Alias,y=sizeDiff, weight=n))+
        geom_boxplot(lwd = 0.25,outlier.shape = NA)
    } else if(input$TypePlot == "median size"){
      #median size
      if("Subject" %in% colnames(testData)){
        medians = testData %>% group_by(Subject, Alias) %>% summarise(median = weighted.median(sizeDiff,n))
      } else{
        medians = testData %>% group_by(Alias) %>% summarise(median = weighted.median(sizeDiff,n))
      }
      p <- ggplot(medians,aes(x=Alias,y=median))+geom_point(color = "red", size = 5)+
        NULL
    } else if(input$TypePlot == "lineplot"){
      p <- ggplot(testData, aes(x = sizeDiff, y = n, color = Alias)) +
        geom_line() +
        geom_point()+
        NULL
    }
    
    p = p + 
      theme(axis.text.x = element_text(angle = 90, size = 10), axis.text.y = element_text(size=10), panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(), panel.background = element_blank(), axis.title=element_blank(), legend.title = element_text(size = 8),legend.text = element_text( size = 6)) 
    if(input$facet_wrap == TRUE  && "Subject" %in% colnames(testData)){
      p = p + facet_grid(~Subject, scales = "free_x", space = "free_x")
    }
    if(useylimit){
      if(column == "delSize" | column == "insSize"){
        ##for line plot the info is on the x-axis
        if(input$TypePlot == "lineplot"){
          p = p+ scale_x_continuous(limits = c(-1,ymax))
          
        }else {
          p = p+ scale_y_continuous(limits = c(-1,ymax))
        }
      } else{
        if(input$TypePlot == "lineplot"){
          p = p+ scale_x_continuous(limits = c(ymin,ymax))
          
        }else {
          p = p+ scale_y_continuous(limits = c(ymin,ymax))
        }
      }
    }
    p
  }
  
  corPlot <- function(el){
    
    testData = el[el$Type=="DELETION",] %>% group_by(Alias) %>% dplyr::count(homologyLength = homologyLength, wt = countEvents, .drop = FALSE)
    means = testData %>% group_by(Alias) %>% summarise(homologyLengthAvg=weighted.mean(homologyLength,n))
    
    el$ins1bpvsrest <- 0
    el$ins1bpvsrest[el$Type=="INSERTION" & el$insSize==1] <- 1
    testData2 = el %>% group_by(Alias) %>% dplyr::count(ins1bp = ins1bpvsrest, wt = countEvents, .drop = FALSE)
    meansIns = testData2 %>% group_by(Alias) %>% summarise(ins1bp=weighted.mean(ins1bp,n))
    
    merged = merge(means,meansIns,by = "Alias",all = T)
    #c("relative","absolute fraction")
    #testData = testData %>% mutate(n = if_else(sizeDiff < 0, -1*n, n))
    #title = paste0(unique(el$Subject)," size ",unique(el$Type))
    merged$Alias = gsub(".fastq","",merged$Alias)
    merged$Alias = gsub("unknown","",merged$Alias)
    p <- ggplot(merged,aes(x=homologyLengthAvg,y=ins1bp,label = Alias))+geom_point(size=4)+
      geom_text_repel()
    theme(axis.text.x = element_text(angle = 90, size = 10), axis.text.y = element_text(size=10), panel.grid.major = element_blank(),
          panel.grid.minor = element_blank(), panel.background = element_blank(), axis.title=element_blank(), legend.title = element_text(size = 8),legend.text = element_text( size = 6)) + 
      #coord_flip() +
      #scale_fill_gradientn(colours=c("blue","lightblue","grey", "lightcoral", "red"), values = scales::rescale(c(min(testData$n),0-.Machine$double.eps,0,0+.Machine$double.eps,max(testData$n)))) +
      #scale_fill_gradientn(colours=c("blue4","lightblue","white", "lightcoral", "red4"), values = scales::rescale(c(min(testData$n),0-.Machine$double.eps,0,0+.Machine$double.eps,max(testData$n)))) +
      #scale_fill_gradientn(colours=c("white", "blue"))+
      ggtitle("homLength vs 1bp Insertions") 
    #scale_y_continuous(limits = c(xmin,xmax))
    return(p)
  }
  
  
  ##tornado plot function
  tornadoplot <- function(newdata, sortType = "Start position", name = "", xmin=-Inf, xmax=Inf, Type = "Regular", ymax= 1)
  {
    if(!is.null(newdata)){
      newdata = newdata %>% filter(typeOrig != "Reference")
    }
    if(is.null(newdata) || nrow(newdata) == 0){
      return()
    }
    
    start_time = Sys.time()
    print("tornadoplot")
    
    ## the number of target sites in our data
    nrSubjects = length(unique(newdata$Subject))
    
    ##ensure we can order the way we want
    if(nrSubjects > 1){
      ##allow for sorting on either Target - Alias or vice versa
      if(input$tornadoSubjectAlias == "Target - Alias") {
        order = newdata %>% select(Subject, Alias) %>% 
          distinct() %>%
          arrange(Subject, Alias) %>%
          mutate(SubjectAlias = paste(Subject, Alias, sep = " - "))
      }
      else{
        order = newdata %>% select(Alias, Subject) %>% 
          distinct() %>%
          arrange(Alias, Subject) %>%
          mutate(SubjectAlias = paste(Subject, Alias, sep = " - "))
      }
      #put that order into a factor to ensure order is conserved in ggplot2
      newdata$SubjectAlias = factor(newdata$SubjectAlias, levels = order$SubjectAlias)
    }
    ##take the ordering from the sample list
    else{
      ##yes but take the correct list, depending on activated grouping
      if(is_grouped()){
        newdata$SubjectAlias = factor(newdata$SubjectAlias, levels = input$multiGroupReplicateOrder)
      } else{
        newdata$SubjectAlias = factor(newdata$SubjectAlias, levels = input$multiGroupOrder)
      }
    }
    
    end_time = Sys.time()-start_time
    print(paste("half: tornadoplot",end_time))
    
    ##pick up the Translocation color
    ##first count if translocation is used
    nrRowsWithTranslocation = newdata %>% filter(Translocation) %>% nrow()
    newdata = newdata %>%
      mutate(TranslocationColor = ifelse(Translocation, TranslocationColorReal, color))
    
    plot = ggplot(newdata)
    
    colourCode = hardcodedTypesDF()$Color
    colourCode = setNames(colourCode,hardcodedTypesDF()$Type)
    
    colourCode = colourCode[names(colourCode) %in% newdata$color | names(colourCode) %in% newdata$tdColor]
    
    ColorText = hardcodedTypesDFnonreactive()
    ColorText = ColorText %>% filter(Type %in% names(colourCode))
    if(TranslocationColorReal %in% newdata$TranslocationColor){
      ColorText = rbind(ColorText, c(TranslocationColorReal,"translocation","#c994c7"))
      colourCode[[TranslocationColorReal]] = "#c994c7"
    }
    
    if(Type=="Regular"){
      ##new style, fewer objects
      if(nrRowsWithTranslocation == 0){
        plot = plot +
          geom_rect(aes(xmin=xmin, xmax=xmax, ymin=y.start, ymax=y.end, fill=color), alpha=1) + 
          ##WT data should only show a block
          geom_rect(data = newdata %>% filter(typeOrig != "WT"), aes(xmin=start.points, xmax=end.points, ymin=y.start, ymax=y.end, fill=tdColor), alpha=1)
      }
      ##old style
      else{
        plot = plot +
          geom_rect(aes(xmin=xmin, xmax=start.points+1, ymin=y.start, ymax=y.end, fill=color), alpha=1) + 
          geom_rect(aes(xmin=end.points, xmax=xmax, ymin=y.start, ymax=y.end, fill=TranslocationColor), alpha=1)+
          ##WT data should only show a block
          geom_rect(data = newdata %>% filter(typeOrig != "WT"), aes(xmin=start.points, xmax=end.points, ymin=y.start, ymax=y.end, fill=tdColor), alpha=1)
      }
    }else if (Type == "Inverted"){
      plot = plot +
        geom_rect(aes(xmin=start.points, xmax=end.points+1, ymin=y.start, ymax=y.end, fill = color), alpha=1)
    }else if (Type == "Inverted - show middle"){
      plot = plot +
        geom_rect(aes(xmin=((start.points+end.points)/2)-1, xmax=((start.points+end.points)/2)+1, ymin=y.start, ymax=y.end, fill = color), alpha=1)  
    }else{
      plot = plot +
        geom_rect(aes(xmin=closestto0-1, xmax=closestto0+1, ymin=y.start, ymax=y.end, fill = color), alpha=1)  
    }
    
    plot = plot + 
      scale_fill_manual(values = colourCode, breaks = names(colourCode), labels = ColorText$Text, na.value = "white") + #no guid is produced
      
      theme(plot.title = element_text(size=10, hjust=0.5),panel.border = element_blank(), panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size = 0.25),axis.text.x = element_text(angle = 90, hjust = 1, vjust=0.5),
      ) + 
      labs(x="Location", y="Fraction") + 
      #ggtitle(ggtitleLabel) + 
      scale_x_continuous(limits = c(xmin, xmax))
    
    #create an scale_y_continous object to adjust
    scale_y_continuous = scale_y_continuous()
    if(is.numeric(ymax)){
      scale_y_continuous$limits = c(0, max(ymax,newdata$y.end)) 
    }
    
    if(!input$expandYaxis){
      scale_y_continuous$expand = c(0,0)
    }
    
    #get the labels
    if(input$YaxisValue == "#Reads"){
      ##reset this for reads
      scale_y_continuous$limits = NULL
    }
    plot = plot + scale_y_continuous
    
    if(input$YaxisValue == "Fraction"){
      aliases = newdata %>% group_by(Subject, SubjectAlias, Alias) %>% summarise(total = sum(countEvents)) 
      ##single target label
      if(nrSubjects == 1){
        aliases = aliases %>% mutate(Text = paste0(Alias," \n(n = ",total,")"))
      }
      ##multi target label
      else{
          aliases = aliases %>% mutate(Text = paste0(Alias," \n(n = ",total,")\n\n", Subject))
      }
      aliasesVec = aliases$Text
      names(aliasesVec) = aliases$SubjectAlias
    }
    
    ##perform facet_wrap and add custom labeller for 'Fraction'
    if(input$YaxisValue == "Fraction"){
      plot = plot + facet_wrap(SubjectAlias ~ . , ncol = input$nrCols, scales='free',
                               labeller = labeller(SubjectAlias = aliasesVec))
    } else{
      plot = plot + facet_wrap(SubjectAlias ~ ., ncol = input$nrCols, scales='free')
    }
    end_time = Sys.time()-start_time
    print(paste("half3: tornadoplot",end_time))
    
    ##add theme
    plot = plot + theme(strip.background = element_blank())
    
    return(plot)
  }
  output$multi_list <- renderUI({
    req(input$Aliases)
    aliases = input$Aliases
    dropdown(
      tags$h3("Sort Samples"),
      bucket_list(
        header = "This list determines the order in the graph",
        add_rank_list(
          input_id= "multiGroupOrder",
          text = "Re-order Samples here",
          labels = aliases
        ),
        group_name = "multiGroup",
        
      ),
      icon = icon("sort"),
      label = "Sort Samples"
    )
  })
  output$multi_list_group <- renderUI({
    req(input$Aliases)
    if(!is_grouped()){
      return()
    }
    groups = NULL
    ##Really make sure this is only done for the tabs that use the GroupColumn
    ##otherwise it might get set, but it breaks other tabs
    ##added tornado plot now as well
    allowedTabs = c("Type","Homology","1bp insertion","Tornado","Efficiency","Target")
    
    if(input$tabs %in% allowedTabs){
      req(filter_in_data())
      el = filter_in_data()
      groups = sort(unique(el[[input$GroupColumn]]))
    } else{
      return()
    }
    dropdown(
      tags$h3("Sort Grouped Samples"),
      bucket_list(
        header = "This list determines the order in the graph if grouping is possible",
        add_rank_list(
          input_id= "multiGroupReplicateOrder",
          text = "Re-order Grouped Samples here",
          labels = groups
        ),
        group_name = "multiGroupReplicate"
        
      ),
      icon = icon("sort"),
      label = "Sort Grouped Samples"
    )
    
    
  })
  output$type_list <- renderUI({
    req(input$Types)
    #print(input$Types)
    labels = hardcodedTypesDF()$Text[hardcodedTypesDF()$Type %in% input$Types]
    dropdown(
      tags$h3("Sort Types"),
      bucket_list(
        header = "This list determines the order in the graph",
        add_rank_list(
          input_id= "order",
          text = "Re-order Types here",
          labels = labels
        ),
        group_name = "multiType",
        
      ),
      icon = icon("sort"),
      label = "Sort Type"
    )
  
    
    
  })
  
  ###### From: https://gitlab.com/snippets/16220 ########
  output$hover_info <- renderUI({
    req(tornadoPlotData())
    
    df = tornadoPlotData()
    
    hover <- input$plot_hover
    if(is.null(hover)){
      return()
    }
    #only on Alias
    if(is.null(hover$panelvar2)){
      point = df %>% filter(SubjectAlias == hover$panelvar1 & hover$y > y.start & hover$y < y.end)
    } else{
      point = df %>% filter(SubjectAlias == hover$panelvar1 & Subject == hover$panelvar2 & hover$y > y.start & hover$y < y.end)
    }
    
    if (nrow(point) == 0) return(NULL)
    
    left_px <- hover$coords_css$x
    top_px <- hover$coords_css$y
    
    style <- paste0("position:absolute;
                  padding: 5px;
                  z-index:100; background-color: rgba(200, 200, 245, 0.85); ",
                    "left:", left_px + 20, "px; top:", top_px + 20 , "px;")
    
    # actual tooltip created as wellPanel
    delSize = NULL
    if(point$size >0){
      if(point$size <10){
        delSize = paste0("<b> delSize: </b>", point$size, "<br/>","<b> del: </b>",point$del,"<br/>")
      } else{
        delSize = paste0("<b> delSize: </b>", point$size, "<br/>")
      }
      if(point$insSize == 0){
        delSize = paste0(delSize,"<b> homology: </b>", point$homology, " bp<br/>")
      }
    }
    insSize = NULL
    if(point$insSize >0){
      if(point$insSize <10){
        insSize = paste0("<b> insSize: </b>", point$insSize, "<br/>","<b> insert: </b>",point$insert,"<br/>")
      } else{
        insSize = paste0("<b> insSize: </b>", point$insSize, "<br/>")
      }
    }
    position = NULL
    if(point$typeOrig != "WT"){
      if(point$size>1){
        position = paste0("<b> position: </b>",point$start.points, " to ",point$end.points,"<br/>")
      } else{
        position = paste0("<b> position: </b>",point$start.points,"<br/>")
      }
      
    }
    #overwrite if SNV or DELINS of equal size
    if(point$size >0 & point$size == point$insSize){
      delSize = paste0("<b> mutation: </b>", point$del, " > ", point$insert, "<br/>")
      insSize = NULL
    }
    wellPanel(
      style = style,
      p(HTML(paste0("<b> Type: </b>", point$typeOrig, "<br/>",
                    delSize,
                    insSize,
                    position,
                    "<b> Fraction: </b>", round(point$yheight,4), "<br/>",
                    NULL
      )
      ))
    )
  })
  
  ##function for aggregation
  combineOutcomes <- function(df){
    groupColumn = get_group_column()
    dfOut = df |> group_by(Subject, Outcome, !!sym(groupColumn), Alias) %>%
      summarise(fraction = sum(fraction))
    return(dfOut)
  }
  
  completeOutcomes <- function(df){
    ##perhaps add a dummy row to ensure always all samples are there?
    ##the nesting offers here only the combinations of Gene and Alias present in the data!!
    groupColumn = get_group_column()
    df_completed <- df %>% data.frame() %>% group_by(Subject) %>% 
      complete(Outcome, nesting(!!sym(groupColumn), Alias),fill = list(fraction = 0))
    return(df_completed)
  }
  
  calculateControls <- function(df, controls, sdValue = 3){
    dfTemp = df %>%
      group_by(Subject, Outcome) %>%
      mutate(meanControls = mean(fraction[Alias %in% controls]),
             sdMean = sd(fraction[Alias %in% controls])) %>%
      mutate(max = meanControls+sdValue*sdMean, min = meanControls-sdValue*sdMean)  
    return(dfTemp)
  }
  
  changeOutcome <- function(df, sigLevel){
    if(sigLevel == 0){
      dfOut = df %>% mutate(Outcome = paste(Type,delRelativeStart,delRelativeEnd,insSize,sep = "|"))
    }
    else if(sigLevel == 1){
      ##alter Outcome
      delSizeLabels = c(0,1,2,5,10,15,20,30,max(df$delSize))
      #homologySteps = c(0,0.9,2.1,5.1,100)
      
      dfOut = df %>%
        filter(deviant == FALSE) %>%
        mutate(Outcome = case_when(
          #Type == "DELETION" ~ paste(Type, "size:",cut(delSize, breaks = delSizeLabels),
          #                           "hom:",cut(homologyLength, breaks = homologySteps,include.lowest = T)),
          Type == "DELETION" ~ paste(Type, "size:",cut(delSize, breaks = delSizeLabels)),
          Type == "INSERTION" ~ paste(Type, "size:",cut(insSize, breaks = delSizeLabels)),
          Type == "DELINS" ~ paste(Type, "size:",cut(delSize, breaks = delSizeLabels)),
          Type == "TINS" ~ paste(Type, isFirstHit),
          Type == "INSERTION_1bp" ~ paste(Type),
          Type == "SNV" ~ paste(Type),
          ##TDs missing for now
          TRUE ~ Outcome
        ))
    } else if(sigLevel == 2){
      dfOut = df %>%
        filter(deviant == FALSE) %>%
        mutate(Outcome = case_when(
          Type == "DELETION" ~ paste(Type,"rest"),
          Type == "INSERTION" ~ paste(Type,"rest"),
          Type == "DELINS" ~ paste(Type,"rest"),
          Type == "TINS" ~ paste(Type),
          Type == "INSERTION_1bp" ~ paste(Type),
          Type == "SNV" ~ paste(Type),
          ##TDs missing for now
          TRUE ~ Outcome
        ))
    } else{
      stop("error")
    }
    dfDev = NULL
    if("deviant" %in% colnames(df)){
      dfDev = df %>% filter(deviant == TRUE)
    }
    dfOut = bind_rows(dfOut, dfDev)
    return(dfOut)
  }
  
  getDeviantOutcomes <- function(df, minValue = 2){
    groupColumn = get_group_column()
    dfOut = df %>%
      filter(fraction > max | fraction < min) %>%
      filter(!Alias %in% input$controls) %>%
      ##also group per Gene
      group_by(Subject, Outcome, !!sym(groupColumn)) %>%
      mutate(total = n()) %>%
      filter(total >= minValue) %>%
      ungroup() %>%
      select(Subject, Outcome) %>%
      distinct()
    return(dfOut)
  }
  
  markOutcomesAsDone <- function(df, deviant){
    ##add a deviant column
    if(!"deviant" %in% colnames(df)){
      df = df %>% mutate(deviant = F)
    }
    deviantDF = deviant %>% mutate(
      Subject_Outcome = paste(Subject, Outcome)
    )
    df = df %>% mutate(
      Subject_Outcome = paste(Subject, Outcome),
      deviant = (deviant == TRUE) | 
        (Subject_Outcome %in% deviantDF$Subject_Outcome)
    )
    return(df)
  }
  
  
  
}

# Run the application 
shinyApp(ui = ui, server = server)

