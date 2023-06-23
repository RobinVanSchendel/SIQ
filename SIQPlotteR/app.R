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



options(shiny.maxRequestSize=2048*1024^2)

#exampleExcel = "data/20210305_093157_SIQ.xlsx"
#exampleExcel = "data/20220127_103430_SIQ_complete.xlsx"
exampleExcel = "data/20220609_220725_SIQ.xlsx"  ## for testing
#exampleExcel = "Z:\\Datasets - NGS, UV_TMP, MMP\\Targeted Sequencing\\Hartwig\\GenomeScan104596\\Analysis\\20200928_GenomeScan104269_104596_NMS_part_for_siq_testing.xlsx"
exampleData = read_excel(exampleExcel, sheet = "rawData", guess_max = 100000)


# Define UI for application that draws a histogram
ui <- fluidPage(
  
  # Application title
  #titlePanel("SIQ Plotter"),
  
  # Sidebar with a slider input for number of bins 
  sidebarLayout(
    sidebarPanel(
      #h1("SIQPlotteR"),
      img(src="SIQ_title.png",width=200),
      radioButtons(
        "data_input", "",
        choices = 
          list("Load example SIQ paper data" = 1,
               "Upload file (TSV, Text, Excel)" = 2,
               "MUSIC screen" = 3
          )
        ,
        selected =  2),
      conditionalPanel(
        condition = "input.data_input=='2'",
        fileInput("file1",
                  "Select Excel or tab-separated File:",
                  accept=c(".xlsx",".txt")),
      ),
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
      selectInput(
        "AliasColumn",
        "Select Sample Column (default: Alias):", 
        c("Alias"),
        selected = "Alias"
      ),
      uiOutput("subject_selection"),
      radioButtons(
        "keepOverlap",
        "Filter reads by distance from expected cut site:",
        c("disabled","≤10bp", "≤2bp"),
        selected = "disabled",
        inline = T),
      uiOutput("minEvents"),
      radioButtons(
        "minReadsOn",
        "Filter number of reads on:",
        c("all reads","mutagenic reads", "selected types"),
        inline = T
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
      selectInput(
        "GroupColumn",
        "Select Grouping/Replicate Column:", 
        c("-"),
        selected = "-"
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
        checkboxInput(
          "data_labels",
          label = "Show data labels",
          value = FALSE
        )
      ),
      ##type plot only ###
      conditionalPanel(
        condition = "input.tabs == 'Homology'",
        checkboxInput(
          "data_labels_hom",
          label = "Show data labels",
          value = FALSE
        ),
        downloadButton('exportHom',"Export to PDF"),
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
      ##sizeDiff plot only ###
      conditionalPanel(
        condition = "input.tabs == 'Target'",
        #radioButtons(
        #  "SizeFreqType",
        #  "Select Column to plot :",
        #  c("delSize","insSize"),
        #  selected = "delSize",
        #  inline = TRUE),
        radioButtons(
          "overlap",
          "Type of plot",
          c("separate", "overlapping"),
          selected = "separate",
          inline = TRUE),
      ),
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
                    "Number of cols:",
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
          c("heatmap","violin", "median size"),
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
        radioButtons("alleleTopOutcomesChoice",
                     "Set top alleles based on:",
                     c("Total","Sample"),
                     inline = T),
        downloadButton('exportAlleles',"Export to PDF"),
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
          "typePlotOutcome",
          "Select type of plot :",
          c("line","heatmap","pca","XY", "umap"),
          selected = "line",
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
          inputId = "OutcomePartQuartile",
          label = "Set number of SDs",
          value = 3,
          min = 1,
          max = 100
        ),
        checkboxInput(
          "OutcomePCAScale",
          "Scale",
          value = F),
        pickerInput(
          inputId = "genotype", 
          label = "Select genotype column:",
          choices = NULL,
          options = list(
            `actions-box` = TRUE, 
            size = 10,
            `live-search`=TRUE
          ), 
          multiple = F
        ),
        pickerInput(
          inputId = "dose", 
          label = "Select dose column:",
          choices = NULL,
          options = list(
            `actions-box` = TRUE, 
            size = 10,
            `live-search`=TRUE
          ), 
          multiple = F
        ),
        downloadButton('exportOutcome',"Export to PDF"),
      ),
      sliderInput("filterMaxFraction", "Set max fraction of event: ",
                  value = 1,
                  min = 0,
                  max = 1,
                  step = 0.05
      ),
      sliderInput("filterMinTotalFraction", "Set min total fraction of Alias: ",
                  value = 0,
                  min = 0,
                  max = 1,
                  step = 0.05
      ),
      sliderInput("filterMinReads", "Set min reads of event to be included: ",
                  value = 0,
                  min = 0,
                  max = 50,
                  step = 1
      ),
      checkboxInput("facet_wrap",
                    "Separate targets",
                    value=T),
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
                           uiOutput("ui_info")),
                  tabPanel("Type",
                           h3("Mutation types"),
                           p("for each sample the mutation types are shown in this interactive plot. Both relative and absolute fractions can be shown."),
                           uiOutput("ui_typeplot"),
                           DT::dataTableOutput("plot1_data",width = 8)),
                  tabPanel("Homology",
                           h3("Homology plot - Deletions and Tandem duplications only"),
                           p("the homology that was used for repair. NOTE: only deletions and tandem duplications can be included in this plot."),uiOutput("ui_homplot")),
                  #tabPanel("HomologyInsert",uiOutput("ui_hominsplot")),
                  #tabPanel("SizeDiff",uiOutput("ui_sizediffplot")),
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
                           uiOutput("ui_outcome")
                           ),
                  tabPanel("HeatmapEnds",
                           h3("HeatmapEnds"),
                           p("Not sure yet what will be put here"),
                           uiOutput("ui_heatmapend")
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
                  selected = "Tornado"
      )
    )
  ),
  fluidRow(
    tableOutput("your_data")
  )
)

# Define server logic required to draw a histogram
server <- function(input, output, session) {
  
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
    tdColor = "#FF7F00"
    tdCColor = "#F4A460"
    ##
    colourCode <- c("WT" = wtColor, "DELETION" = delColor, "INSERTION" = insColor, "INSERTION_1bp" = insColor1, "DELINS" = delinsColor,
                    "TINS" = tinsColor, "TANDEMDUPLICATION" = tdColor, 
                    "TANDEMDUPLICATION_COMPOUND" = tdCColor,"SNV" = snvColor, "HDR" = hdrColor,"HDR1MM" = hdr1mmColor, "0bp_homology" = bp0Color,
                    "1bp_homology" = bp1Color,"2bp_homology" = bp2Color,"3bp_homology" = bp3Color,
                    "4bp_homology" = bp4Color, "5-15bp_homology" = bp5Color, "15bp_homology" = bp6Color, "white" = "white"
    ) 
    
    hardcodedTypes = c("WT" = "wild-type","INSERTION" = "insertion", "INSERTION_1bp" = "1bp insertion",
                       "DELINS" = "deletion with insert", "TINS" = "deletion with templated insert","TANDEMDUPLICATION" = "tandem duplication (td)",         
                       "TANDEMDUPLICATION_COMPOUND" = "tandem duplication plus (td+)", "SNV" = "snv", "0bp_homology" = "deletion/td 0bp microhomology",              
                       "1bp_homology" = "deletion/td 1bp microhomology", "2bp_homology" = "deletion/td 2bp microhomology",
                       "3bp_homology" = "deletion/td 3bp microhomology", "4bp_homology" = "deletion/td 4bp microhomology",
                       "5-15bp_homology" = "deletion/td 5-15bp microhomology", "DELETION" = "deletion", "HDR" = "homology-directed repair"
                       ,"HDR1MM" = "homology-directed repair mismatch", "15bp_homology" = "deletion >15bp microhomology"
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
  
  d_xminmaxRange <- reactive({
    input$xminmaxRange
  }) %>% debounce(1000)
  
  d_minEvents <- reactive({
    input$minEvents
  }) %>% debounce(500)
  
  in_data <- reactive({
    
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
        if(fileNameXLS$size<200*1024*1024){
          el = read.csv(fileNameXLS$datapath, header=T, stringsAsFactors = FALSE, sep = "\t")
        }
        else{
          start_time = start_time <- Sys.time()
          el = fread(fileNameXLS$datapath, header=T, stringsAsFactors = FALSE, data.table = F)
          end_time <- Sys.time()
          print(paste("fread",end_time-start_time, "seconds"))
        }
      }
    }
    ## MUSIC screen
    else if(input$data_input == 3){
      file = "MB_chunk_manifest.txt"
      el = fread(file, header = T, stringsAsFactors = FALSE, data.table = FALSE)
      
      ##needed to not break down the process below
      el$Type = "dummy"
      el$insSize = 0
    }
    if("Remarks" %in% colnames(el)){
      el = el %>% filter(is.na(Remarks))
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
    ##sometimes people specify the same alias for different subject, which does not work
    #if(nrow(el)>0 && length(unique(el$Subject))>0){
    #  test = el %>%
    #    group_by(Alias) %>%
    #    summarise(Subjects = n_distinct(Subject))
    #  if(max(test$Subjects,rm.na = T)>1){
    #    el$Alias = paste(el$Subject,el$Alias,sep = "_")
    #  }
    #}
    #overwrite insertions of 1bp
    el = el %>% mutate(Type = ifelse(Type == "INSERTION" & insSize == 1,"INSERTION_1bp",Type))
    
    print("endOf in_data")
    return(el)
  })
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
      mutate(fraction = fraction/sum(fraction))
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
    if(input$filterMaxFraction<1){
      el = el %>% filter(fraction<input$filterMaxFraction)
      if(input$filterMinTotalFraction>0){
        keepAliases = el %>% group_by(Alias) %>% count(wt = fraction) %>% filter(n>=input$filterMinTotalFraction)
        el = el %>% filter(Alias %in% keepAliases$Alias)
      }
    }
    if(input$filterMinReads>0){
      el = el %>% filter(countEvents>=input$filterMinReads)
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
  
  filter_in_data <- reactive({
    req(pre_filter_in_data())
    req(input$Aliases)
    
    if(input$data_input == 3 & input$AliasColumn == "Alias"){
      return()
    }
    ##remove aliases not plotted
    el = pre_filter_in_data()
    
    ##so not load in the data
    if(input$data_input == 3){
      ##retrieve the filenames to be read in:
      files = el %>% ungroup() %>% filter(!!as.symbol(input$AliasColumn) %in% input$Aliases) %>% select(fileName) %>% distinct()
      if(nrow(files) == 0){
        return()
      }
      files = files$fileName
      
      read_data <- function(z){
        dat <- fread(file = z, sep = "\t", header = T)
        return(dat)
      }
      
      datalist <- lapply(files, read_data)
      
      el <- rbindlist(datalist, use.names = TRUE)
      ##alter following columns
      ##change this as this is undesired in the end
      el = el %>% 
        mutate(Subject = Alias) %>%
        mutate(Alias = !!as.symbol(input$AliasColumn)) %>%
        mutate(Type = SubType)
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
                                     alleles=NULL)
  
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
      Type=="TANDEMDUPLICATION_COMPOUND" ~ "INSERTION",
      homologyLength<=4 ~ paste0(homologyLength,"bp_homology"),
      homologyLength>=5 & homologyLength<15 ~ "5-15bp_homology",
      homologyLength>=15 ~ "15bp_homology"
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
    ##end of to be removed###
    if("fraction" %in% colnames(el)) {
      plot.data <- data.frame(size = el$delRelativeEndTD-el$delRelativeStartTD, start.points = el$delRelativeStartTD, 
                              end.points = el$delRelativeEndTD, type=el$TypeHom, typeTD=el$TypeTD, color=el$getHomologyColor, code=el$Alias, 
                              yheight = el$fraction, typeOrig = el$Type, left = el$delRelativeStartTD+(el$delRelativeEndTD-el$delRelativeStartTD)/2, 
                              startTD=el$delRelativeStart, countEvents = el$countEvents, Pool = el$Subject, tdColor = el$TDcolor, insSize = el$insSize,
                              Subject = el$Subject, Alias = el$Alias, del = el$del, insert = el$insertion, homology = el$homologyLength)
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
  
  
  
  output$outcomePlot <- renderPlot({
    req(filter_in_data())
    req(input$Aliases)
    if(is.null(input$controls) | length(input$controls) == 0){
      as_ggplot(text_grob("Please select a control sample on the left", size = 15))
    }else{
      data = filter_in_data()
      
      #scale data to fraction
      start_time = Sys.time()
      print("outcomePlot")
      data <- data %>%
        group_by(Alias) %>%
        mutate(fraction = fraction/sum(fraction))
      if(nrow(data)==0){
        return()
      }
      
      ###not for all needed
      if(input$typePlotOutcome!="XY"){
        #data$Outcome <- paste(data$Type)#,data$delRelativeStart,data$delRelativeEnd,data$insSize,sep = "|")
        data$Outcome <- paste(data$Type,data$delRelativeStart,data$delRelativeEnd,data$insSize,sep = "|")
        if(!"insertion" %in% colnames(data)){
          data$insertion = ""
        }
        
        #specify SNV
        data = data %>%
          mutate(Outcome = 
                   ifelse( 
                     Type == "SNV",     paste(Type,delRelativeStart,delRelativeEnd,insertion,sep="|"), 
                     Outcome)) 
        
        #specify insertion (to avoid duplicates)
        data = data %>%
          mutate(Outcome = 
                   ifelse(
                     Type == "INSERTION" | Type == "DELINS" & insSize < 7, paste(Type,delRelativeStart,delRelativeEnd,insertion,sep="|"), 
                     Outcome))
        
        #specify 1BP insertion
        data = data %>%
          mutate(Outcome = 
                   ifelse(
                     Type == "1BP INSERTION", paste(Type,delRelativeStart,delRelativeEnd,insertion,sep="|"), 
                     Outcome))
        
        #specify TDs together
        #as we don't see any differences between them for now
        ##also for TINS
        data = data %>%
          mutate(Outcome = 
                   ifelse(
                     Type == "TANDEMDUPLICATION" | Type == "TANDEMDUPLICATION_COMPOUND" | Type == "TINS", paste(Type,sep="|"), 
                     Outcome))
        
        data = data %>%
          mutate(Outcome = 
                   ifelse(
                     Type == "DELETION" , paste(Type,delRelativeStart,delRelativeEnd,paste0(homologyLength,"bp"),sep="|"), 
                     Outcome))
        
        
        #sometimes we are analysing data where outcomes are found multiple times
        dataUnique <- data %>%
          filter(Alias %in% input$controls) %>%
          group_by(Subject, Outcome) %>%
          count(wt = fraction, name = "fraction")
        #needs to be a data.frame for the slice
        dataUnique = data.frame(dataUnique)
        
        dataUniqueAll <- data %>%
          group_by(Alias, Outcome, Subject) %>%
          count(wt = fraction, name = "fraction")
        #needs to be a data.frame for the slice
        dataUniqueAll = data.frame(dataUniqueAll)
        
        #dataNT1TopOutcomes <- data %>% 
        #  select(c(countEvents, fraction, Barcode, Gene, Outcome)) %>%  
        #  filter(Barcode == "RVsg_mmNT-1") %>%
        #  slice_max(fraction, n = 30, with_ties = TRUE) %>%
        #  arrange(Barcode, desc(fraction))
        
        #Get the top X from the controls
        dataNT1TopOutcomes <- dataUnique %>% group_by(Subject) %>%
          slice_max(fraction, n = input$numberOfOutcomes, with_ties = TRUE) %>%
          arrange(desc(fraction))
        
        dataPlot1 <- dataUniqueAll %>%                                                     # begin with linelist
          # select(c(countEvents, fraction, Barcode, Gene, Outcome)) %>%     # select columns
          ##LATER
          #filter(Gene %in% control | Gene %in% highlight) %>%
          filter(Outcome %in% dataNT1TopOutcomes$Outcome)
        
        # Order top outcomes
        maxSizeString = 50
        dataNT1TopOutcomes = dataNT1TopOutcomes %>%
          mutate(Outcome=stringr::str_trunc(Outcome,maxSizeString))
        dataPlot1 = dataPlot1 %>%
          mutate(Outcome=stringr::str_trunc(Outcome,maxSizeString))
        
        ordered <- unique(dataNT1TopOutcomes$Outcome)
        
      }
      plots = list()
      if(input$typePlotOutcome=="line"){
        ##generate vectors per Outcome
        dfForOutliers = dataPlot1[dataPlot1$Alias %in% input$controls,] %>%
          group_by(Outcome)%>%
          summarise(avg=mean(fraction,na.rm = T), sd=sd(fraction))
          #count(wt = fraction) 
          #mutate(outlier = ifelse(is_outlier(n), Alias, NA))
        dfForOutliers[is.na(dfForOutliers)] <- 0
        dataPlot1 = merge(dataPlot1,dfForOutliers, by = "Outcome")
        
        #dataPlot1 = dataPlot1 %>% group_by(Outcome) %>%
        #  mutate(rank = ntile(fraction,input$OutcomePartQuartile)) %>%
        #  mutate(rank = ifelse(rank<2 | rank >input$OutcomePartQuartile-1,Alias,NA))
        #dataPlot1$Outcome = factor(dataPlot1$Outcome, levels = ordered)
        dataPlot1 = dataPlot1 %>% group_by(Outcome) %>%
          mutate(rank = ifelse(fraction<(avg-input$OutcomePartQuartile*sd) | fraction > (avg+input$OutcomePartQuartile*sd),Alias,NA))
        
        #outliers = unique(dataPlot1$Alias[!is.na(dataPlot1$rank)])
        #dataPlot1$rank[dataPlot1$Alias %in% outliers] = dataPlot1$Alias[dataPlot1$Alias %in% outliers]
        
        dataPlot1$Outcome = factor(dataPlot1$Outcome, levels = ordered)
        
        
        
        plot <- ggplot(dataPlot1,aes(x = Outcome, y = fraction, group = Alias, color = rank))
        ##only draw colors if we have something to color
        if(length(na.omit(dataPlot1$rank))>0){
          plot <- plot + geom_text_repel(aes(label=rank, color = rank), na.rm = T, size=input$OutcomeSize, position = position_dodge(0.5), max.overlaps = Inf)
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
          plots[['line']] <-plot
      }else if(input$typePlotOutcome=="heatmap"){
        
        df = dataPlot1 %>%
          spread(Outcome,fraction)
        rownames(df) = df[,1]
        df = df[,-1]
        df = as.matrix(df)
        df[is.na(df)] <- 0
        scale = "none"
        if(input$OutcomePCAScale){
          #is that useful?
          scale = "row"
        }
        plot = heatmap.2(as.matrix(df), trace = "none",
                  scale = scale,
                  cexRow = input$OutcomeSize/4,
                  cexCol = input$OutcomeSize/4,
                  #cexRow = 0.5,
                  #cexCol = 0.5,
                  #scale = "row"
                  )
        return(plot)
      }else if(input$typePlotOutcome=="pca"){
        if(length(unique(dataPlot1$Subject))>1){
          dataPlot1 = dataPlot1 %>% mutate(Outcome = paste(Outcome, Subject))
        } 
        
        df = dataPlot1 %>% select (-Subject) %>%
          spread(Outcome,fraction)
        rownames(df) = df[,1]
        df = df[,-1]
        df[is.na(df)] <- 0
        pca_res <- prcomp(df, scale. = input$OutcomePCAScale)
        
        dtp <- data.frame('Alias' = rownames(df), pca_res$x[,1:2]) # the first two componets are selected (NB: you can also select 3 for 3D plottings or 3+)
        
        if(!is.null(input$genotype) && !is.null(input$dose)){
          dfPart = data %>% select(Alias, Subject,input$genotype, input$dose) %>% distinct(Alias, .keep_all = T)
          dtp = merge(dtp,dfPart,by = "Alias")
          if(input$genotype!=input$dose){
            dtp$label = paste(dtp[[input$genotype]], dtp[[input$dose]])
          } else {
            dtp$label = dtp[[input$genotype]]
          }
        }
        
        plot1 = ggplot(data = dtp,aes_string(x = "PC1", y = "PC2", col = input$dose, fill = input$genotype)) + 
          geom_point(shape = 21, size = input$OutcomeDotSize, stroke = input$OutcomeStrokeSize) +
          geom_text_repel(size = input$OutcomeSize,aes(label = label), max.overlaps = Inf)+
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
        if(length(unique(dataPlot1$Subject))>1){
          dataPlot1 = dataPlot1 %>% mutate(Outcome = paste(Outcome, Subject))
        } 
        
        df = dataPlot1 %>% select (-Subject) %>%
          spread(Outcome,fraction)
        rownames(df) = df[,1]
        df = df[,-1]
        df[is.na(df)] <- 0
        custom.config = umap.defaults
        custom.config$random_state = 123
        if(nrow(df)<15){
          custom.config$n_neighbors = 2
        }
        test = umap(df, config = custom.config)
        dfTest = data.frame('Alias' = rownames(df),test$layout)
        if(!is.null(input$genotype) && !is.null(input$dose)){
          dfPart = data %>% select(Alias, Subject,input$genotype, input$dose) %>% distinct(Alias, .keep_all = T)
          dfTest = merge(dfTest,dfPart,by = "Alias")
          if(input$genotype!=input$dose){
            dfTest$label = paste(dfTest[[input$genotype]], dfTest[[input$dose]])
          } else {
            dfTest$label = dfTest[[input$genotype]]
          }
        }
        
        plot = ggplot(dfTest,aes_string(x="X2",y="X1", col = input$dose, fill = input$genotype))+
          geom_point(shape=21, size = input$OutcomeDotSize, stroke = input$OutcomeStrokeSize) +
          theme_minimal()
          #theme(legend.position = "none")
        if(input$OutcomeSize>0){
          plot <- plot + geom_text_repel(size = input$OutcomeSize,aes(label = label), max.overlaps = Inf)
        }
        if(!input$OutcomeLegend){
          plot <- plot + theme(legend.position = "none")
        }
        
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
    
    sortType = input$Sort
    
    rownames(newdata) <- 1:nrow(newdata)
    newdata$y.start <- 0
    newdata$y.end <- 0
    yoffset = 0
    #newdata$closestto0 = pmin(abs(newdata$start.points),abs(newdata$end.points))
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
    #newdata$y.end = cumsum(newdata$yheight)
    #Subject = el$Subject, Alias = el$Alias
    #newdata$y.start = newdata$y.end - newdata$yheight
    
    newdata = newdata %>% group_by(Alias, Subject) %>% mutate(y.end = cumsum(yheight), y.start = y.end-yheight)
    
    newdata
  })
  
  output$subjectPlot <- renderPlot({
    req(tornadoPlotData())
    req(input$yaxis)
    req(input$Aliases)
    req(d_xminmaxRange())
    #speedup multigroup order is lagging behind Aliases
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    plot.data = tornadoPlotData()
    
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
    
        
    newdata <- plot.data #%>% 
    if(nrow(newdata)>0){
      newdata$Alias = factor(newdata$Alias, levels = input$multiGroupOrder)
        plot = tornadoplot(newdata, ymax = ymaxInput, xmin = d_xminmaxRange()[1],
                             xmax = d_xminmaxRange()[2],
                             Type = input$Type)
    }
    plotsForDownload$tornados <- plot
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
        if(input$overlap =="separate"){
          plots = length(input$multiGroupOrder)
          ggsave(file, arrangeGrob(grobs=plotsForDownload$target, ncol=1, nrow = plots),height=(plots*input$plotHeight)/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")  
        } else{
          plots = 1
          ggsave(file, plotsForDownload$target,height=input$plotHeight/72, width=input$plotWidth/72,limitsize = FALSE, device = "pdf")  
        }
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
        ggsave(file, plotsForDownload$homs[["hom"]],height=input$plotHeight/72, width=input$plotWidth/72, limitsize = FALSE)
      }
    }
  )
  
  output$export = downloadHandler(
    filename = function() {"plotsTornado.pdf"},
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
    if(length(unique(el$Subject))>1){
      el = el %>% select(Subject, OutcomeText, insertion, Outcome, Alias, fraction)
      #target = 2
    } else {
      el = el %>% select(OutcomeText,insertion, Outcome, Alias, fraction) 
      #target = 1
    }
    el = el %>% mutate(insertion = ifelse(nchar(insertion)<60, insertion, paste(nchar(insertion),"bp")))
    
    ##summarise same events for spread
    el = el %>% group_by_at(vars(-fraction)) %>% summarise(fraction = sum(fraction)) %>% ungroup()
    
    el = el %>%
      mutate(fraction = round(fraction,4)) %>%
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
  
  ##type data table
  output$plot1_data <- DT::renderDataTable({
    req(filter_in_data())
    el = filter_in_data()
    
    if(input$datatableFraction == "relative"){
      countDF = el %>% group_by(Alias, Subject) %>%  
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
        group_by(Alias, Subject) %>%  
        dplyr::count(Type = Type, wt = countEvents, .drop = FALSE) 
    }
    ##remove the Reference type (used for alleles)
    countDF = countDF %>% 
      filter(Type != "Reference")
    
    ##spread for viewing
    countDFSpread = spread(countDF,"Type","n")
    columns = ncol(countDFSpread)
    
    dt = DT::datatable(countDFSpread,rownames = FALSE,extensions = 'Buttons', options = list(
      pageLength = -1,
      dom = 'tB',
      buttons = c('copy', 'excelHtml5')
    ))
    if(input$datatableFraction == "relative"){
      dt <- dt %>% formatRound(columns = c(3:columns), digits = 4)
    }
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
  
  #homology plot call
  output$homPlot <- renderPlot({
    req(input$Aliases)
    #speedup multigroup order is lagging behind Aliases
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    el = filter_in_data()
    el$Alias = factor(el$Alias, levels = input$multiGroupOrder)
    types = intersect(input$Types,c("DELETION","TANDEMDUPLICATION"))
    plot = homplot(el, types = types)
    plots=list()
    plots[["hom"]] <- plot
    plotsForDownload$homs <- plots
    plot
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
  
  output$subject_selection <- renderUI({
    choices = sort(unique(in_data()$Subject))
    #data_input == 1 = example data
    if(input$data_input == 1){
      selected = choices
    } else{
      selected = choices[1]
    }
    pickerInput(
      inputId = "Subject", 
      label = "Select Target(s):",
      choices = choices,
      selected = selected,
      options = list(
        `actions-box` = TRUE, 
        size = 10,
        `live-search`=TRUE,
        `selected-text-format` = "count > 3"
      ), 
      multiple = TRUE
    )
  })
  observe({
    if(input$data_input == 3){
      updatePickerInput(session, inputId = "AliasColumn", selected = "Gene")
    }
  })
  
  
  observe({
    req(in_data())
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
  
  observe({
    req(in_data())
    req(hardcodedTypesDF())
    ##need to set the types for the screen
    if(input$data_input == 3){
      types = c("HDR","DELETION","DELINS","INSERTION","TINS","WT","INSERTION_1bp")
      choices = hardcodedTypesDF()[hardcodedTypesDF()$Type %in% types,]
    } else{
      choices = hardcodedTypesDF()[hardcodedTypesDF()$Type %in% unique(in_data()$Type),]
    }
    choices = choices[order(choices$Text), ]
    choicesInv = setNames(choices$Type, choices$Text)
    updatePickerInput(session, "Types", choices = choicesInv, selected = choicesInv)
  })
  
  observe({
    req(filter_in_data())
    el = filter_in_data() %>% select(Alias) %>% distinct()
    updatePickerInput(session, "alleleTopOutcomesSample", choices = el$Alias )
    
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
  
  observe({
    df = in_data()
    testDF = df %>% group_by(Alias) %>% dplyr::count(wt=countEvents)
    if(max(testDF$n)>20000){
      step = 1000
    }
    else{
      step = 100
    }
    updateSliderInput(session, "minEvents", min = 0, max = max(testDF$n), step = step)
  })
  
  
  
  observe({
    req(pre_filter_in_data())
    req(d_minEvents())
    print("ObserveAliasesStart")
    totalAliases = in_stat()
    if("Alias" %in% colnames(totalAliases) & "Subject" %in% colnames(totalAliases)){
      aliases = totalAliases$Alias[totalAliases$Subject %in% input$Subject]
      aliases = sort(unique(aliases))
    } else {
      subAliases = in_data()[in_data()$Subject %in% input$Subject,]
      aliases = sort(unique(subAliases$Alias))
    }
    print("ObserveAliasesStart2")
    #print(aliases)
    pre_filter_DF = pre_filter_in_data()
    ##additionalFilter for the max fraction to be included
    #remove Aliases that don't have enough counts
    print("ObserveAliasesStart3")
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
    print("ObserveAliasesStart3.5")
    if(input$AliasColumn == "Alias"){
      aliases = aliases[aliases %in% plotAliases]
    } else{
      aliases = plotAliases
    }
    intersect = intersect(aliases, input$Aliases)
    if(length(intersect) > 0 & length(intersect)<100){
      selected = intersect
    }
    else 
    if(length(aliases) >100){
      selected = NULL
    } 
    ##if no overlap between selected and these aliases, select them all
    else{
      selected = aliases
    }
    print("ObserveAliasesStart4")
    ##this does not contain the samples that have 0 reads anymore
    ##which may be an issue for the SampleInfo tab
    updatePickerInput(session, "Aliases", choices = aliases, selected = selected)
    print("ObserveAliasesEnd")
    print(paste("selected",selected))
  })
  
  observe({
    req(input$Aliases)
    updatePickerInput(session, "controls", choices = input$Aliases)
  })
  
  observe({
    req(input$Types)
    choices = c(input$Types,"mean_homology_deletion","mean_homology_TD")
    updatePickerInput(session, "controlsX", choices = choices)
    updatePickerInput(session, "controlsY", choices = choices, selected = choices[1])
  })
  
  observe({
    req(filter_in_data())
    df = filter_in_data()
    updatePickerInput(session, "genotype", choices = colnames(df), selected = "Alias")
    updatePickerInput(session, "dose", choices = colnames(df), selected = "Alias")
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
    print(paste("color_test",end_time-start_time, "seconds"))
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
    df = filter_in_data() %>%
      group_by(Alias) %>%
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
    req(plot_rows)
    #print(paste("plot rows",plot_rows()*200))
    plotOutput("subjectPlot", height = plot_rows()*input$plotHeight, width = input$plotWidth,
               hover = hoverOpts("plot_hover", delay = 10, delayType = "debounce"))
  })
  
  output$ui_tornadplot_flankinsertions <- renderUI({
    #print(paste("plot rows",plot_rows()*200))
    plotOutput("tornadoPlotFI", height = plot_rows_no_col()*input$plotHeight, width = input$plotWidth)
  })
  
  output$ui_outcome <- renderUI({
    #print(paste("plot rows",plot_rows()*200))
    if(input$typePlotOutcome=="XY"){
      height = input$plotHeight*length(input$controlsY)
    } else if(input$typePlotOutcome=="pca"){
      height = input$plotHeight*2
    } else{
      height = input$plotHeight
    }
      plotOutput("outcomePlot", height = height, width = input$plotWidth)
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
    #print(paste("plot rows",plot_rows()*200))
    plotOutput("sizeDiffPlot", height = input$plotHeight, width = input$plotWidth)
  })
  
  output$ui_sizeplot <- renderUI({
    #print(paste("plot rows",plot_rows()*200))
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
      plotOutput("snvplot", height = plot_rows_no_col()*input$plotHeight, width = input$plotWidth)
    }
  })
  output$ui_SizeFreq <- renderUI({
    if(input$overlap =="separate"){
      plots = length(input$multiGroupOrder)
    } else{
      plots = 1
    }
    plotOutput("SizeFreq", height = plots*input$plotHeight, width = input$plotWidth)
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
    for(subject in input$Subject){
      for(alias in input$multiGroupOrder){
        tempDF = subset(el, Alias == alias & Subject == subject)
        if(length(input$Subject) > 1){
          name = paste(subject, alias)
        } else{
          name = paste(alias)
        }
        
        #-1 to exlude last position
        if(nrow(tempDF)==0){
          p = text_grob(paste(name,"\nNO DATA"), size = 10)
        }else{
          testDF = do.call("rbind", mapply(function(x, y, z, a, b) cbind.data.frame(x:y, z, a, b),
                                           tempDF$delRelativeStart, 
                                           tempDF$delRelativeEnd-1,
                                           tempDF$fraction, alias, subject, SIMPLIFY = FALSE)) %>%
            as.data.frame(stringsAsFactors = FALSE)                 %>%
            setNames(c("Locus", "Value","Alias","Subject"))   
          testDFSum = testDF %>%
            group_by(Locus)                 %>%
            summarise(sum = sum(Value)) 
          
          
          p = ggplot(testDFSum,aes(x = Locus, y = sum)) + 
            geom_bar(stat = "identity") + labs(x = "Locus")+ggtitle(name) + xlim(c(xmin,xmax))+
            theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
                  panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
                  legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title=element_blank(), legend.title = element_text(size = 10)) +
            xlab("location")+ylab("fraction of total")
        }
        if(input$overlap == "separate"){
          plots[[name]] = p
        }else{   
          dfs[[name]] = testDF
        }
      }
    }
    if(input$overlap =="separate"){
      plotsForDownload$target = plots
      grid.arrange(grobs = plots, ncol=1)
    }else{
      testDFtotal = do.call("rbind",dfs)
      testDFtotal = testDFtotal %>% group_by(Subject, Alias,Locus) %>% summarise(sum = sum(Value))
      p = ggplot(testDFtotal,aes(x = Locus, y = sum, fill=Alias)) + 
        geom_bar(stat = "identity", position="identity", alpha=0.4) +
        xlim(c(xmin,xmax))+
        xlab("location")+ylab("fraction of total")+
        theme(plot.title = element_text(size=10),panel.border = element_blank(), panel.grid.major = element_blank(),
              panel.grid.minor = element_blank(), panel.background = element_blank(), axis.line = element_line(colour = "black", size =0.25),axis.text.x = element_text(angle = 90, hjust = 1 ,vjust = 0.5, size = 10),
              legend.text = element_text( size = 10), legend.key.size = unit(8, "mm"), axis.title=element_blank(), legend.title = element_text(size = 10)) +
        facet_grid(~Subject)
      plotsForDownload$target = p
      p
    }
  })
  
  
  observe({
    req(filter_in_data())
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
    print(paste(minValueX, maxValueX))
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
      if(input$data_labels==TRUE){
        plot <- plot + geom_text(aes(label = n),
                                 position=position_stack(0.5))  
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
    
    if("INSERTION" %in% types){
      el$homologyLength <- factor(el$homologyLength, levels = c("INSERTION",0:max(el$homologyLength)))
      el$homologyLength[el$Type=="INSERTION"] <- "INSERTION"
    }
    else{
      el = el[el$Type %in% types,]
      el$homologyLength <- factor(el$homologyLength, levels = unique(sort(el$homologyLength)))
    }
    if(fraction=="total"){
      test = el[el$Type %in% types,] %>% group_by(Alias, Subject) %>% dplyr::count(homologyLength = homologyLength, wt = fraction) %>%   mutate(fraction = n / sum(n))
    }
    else{
      test = el[el$Type %in% types,] %>% group_by(Alias, Subject) %>% dplyr::count(homologyLength = homologyLength, wt = fraction) %>%   mutate(totalFraction = sum(n))
      #test = test[test$Type %in% types,]
      #fractions = rowsum(test$n, test$Alias)
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
      ##browser()
    }
    
    dnaRefStrings = getDNARefStrings(el) %>% mutate(Outcome = "Reference", totalFraction = Inf, fraction = Inf)
    #bug if Alias is a merge of more samples, we should select more events, so File was added to grouping
    if("File" %in% colnames(el)){
      elSub = el %>% ungroup() %>% group_by(Subject, Alias, File) %>% slice_max(fraction, n = input$alleleTopOutcomes)
    } else{
      elSub = el %>% ungroup() %>% group_by(Subject, Alias) %>% slice_max(fraction, n = input$alleleTopOutcomes)
    }
    el = rbind(elSub, dnaRefStrings)
    
    el = el %>% ungroup() %>% mutate(Category = paste(Type,delSize))
    
    colorType = hardcodedTypesDF()$Color
    colorType = setNames(colorType,hardcodedTypesDF()$Text)
    
    ###only keep the important ones
    test2 = merge(el,hardcodedTypesDF(),by = "Type", all.x=T)
    
    test2 = test2 %>% mutate(Outcome = ifelse(Type == "DELETION",paste(Text, delSize,"bp, hom:", paste0(homologyLength,"bp"), homology, ", pos:",delRelativeStart),Outcome))
    test2 = test2 %>% mutate(Outcome = ifelse(Type == "DELETION" & homologyLength == 0,paste(Text, delSize, "bp, pos:",delRelativeStart),Outcome))
    test2 = test2 %>% mutate(Outcome = ifelse(Type == "WT",paste(Text),Outcome))
    test2 = test2 %>% mutate(Outcome = ifelse(Type == "INSERTION_1bp",paste(Text, insertion),Outcome))
    test2 = test2 %>% mutate(Outcome = ifelse(Type == "INSERTION",paste(Text, insSize),Outcome))
    test2 = test2 %>% mutate(Outcome = ifelse(Type == "SNV",paste(Text, del,">", insertion,"pos:",delRelativeStart),Outcome))
    test2 = test2 %>% mutate(Outcome = ifelse(Type == "TINS" | Type == "DELINS",paste(Text, "del:" ,delSize,", ins:", insSize, ", pos:",delRelativeStart),Outcome))
    test2 = test2 %>% mutate(Outcome = ifelse(Type == "TANDEMDUPLICATION" | Type == "TANDEMDUPLICATION_COMPOUND",paste(Text, insSize, "bp, pos:",delRelativeStart),Outcome))
    
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
    
    addGroup = F
    if(input$GroupColumn != "-" & input$GroupColumn %in% colnames(test2)){
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
      for(subject in unique(test2$Subject)){
        test2Sub = test2 %>% filter(Subject == subject)
        
        ##get the grouped columns as text
        group_column = c(input$GroupColumn,"Outcome","Text")

        ##calculate mean to be displayed
        test2Sub = test2Sub %>% ungroup() %>% group_by_at(group_column) %>% 
          summarise(mean = mean(fraction, na.rm = T), sd = sd(fraction, na.rm=T))
        
        ## add zero for samples that do not have that outcome?!!!!
        ##########
        
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
  
  
  output$mutFreq <- renderPlot({
    req(filter_in_data())
    req(input$Aliases)
    #speedup multigroup order is lagging behind Aliases
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    el = filter_in_data()
    #Get the totals
    testDF = el %>% group_by(Subject, Alias) %>% dplyr::count(wt=countEvents)
    test = el[el$Type != "WT",] %>% group_by(Subject, Alias) %>% count (wt = countEvents, name = "wt")
    test = merge(testDF, test, by=c("Subject","Alias"))
    
    test$mut = test$wt/test$n
    
    plot <- ggplot(test, aes(x=Alias,y=mut)) + geom_bar(stat = "identity") +
      theme(axis.text.x = element_text(angle = 90, size = 10), axis.text.y = element_text(size=10), panel.grid.major = element_blank(),
            panel.grid.minor = element_blank(), panel.background = element_blank(),  legend.title = element_text(size = 8),legend.text = element_text( size = 6)) +
      xlab("Sample") + ylab("Fraction")
    
    if(input$facet_wrap == TRUE && "Subject" %in% colnames(test)){
      plot <- plot + facet_grid(~Subject, scales = "free_x", space = "free_x")
    }
    plot  
    
  })
  
  output$snvplot <- renderPlot({
    req(in_data())
    req(input$Aliases)
    
    #speedup multigroup order is lagging behind Aliases
    if(length(input$Aliases) != length(input$multiGroupOrder)){
      return()
    }
    el = filter_in_data()
    if(input$snvrange>1){
      el$sizeChange = el$delSize-el$insSize
      el = el %>% filter(Type == "SNV" |
                           (Type == "DELINS" & sizeChange ==0 & delSize<=input$snvrange))
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
      for(subject in input$Subject){
        for(alias in input$multiGroupOrder){
          testPart = test[test$Alias == alias & test$Subject == subject,]
          if(length(input$Subject)>1){
            name = paste(subject, alias)
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
      "violin"
      p <- ggplot(testData,aes(x=Alias,y=sizeDiff, weight=n))+geom_violin(lwd = 0.25)+
        geom_boxplot(width=0.1, lwd = 0.25,outlier.shape = NA)
    } else{
      #median size
      if("Subject" %in% colnames(testData)){
        medians = testData %>% group_by(Subject, Alias) %>% summarise(median = weighted.median(sizeDiff,n))
      } else{
        medians = testData %>% group_by(Alias) %>% summarise(median = weighted.median(sizeDiff,n))
      }
      p <- ggplot(medians,aes(x=Alias,y=median))+geom_point(color = "red", size = 5)+
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
        p = p+ scale_y_continuous(limits = c(-1,ymax))
      } else{
        p = p+ scale_y_continuous(limits = c(ymin,ymax))
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
    if(is.null(newdata) || nrow(newdata) == 0){
      return()
    }
    
    plot = ggplot(newdata)
    
    colourCode = hardcodedTypesDF()$Color
    colourCode = setNames(colourCode,hardcodedTypesDF()$Type)
    
    colourCode = colourCode[names(colourCode) %in% newdata$color | names(colourCode) %in% newdata$tdColor]
    #add white
    colourCode["white"] = "white"
    
    ColorText = hardcodedTypesDFnonreactive()
    ColorText = ColorText %>% filter(Type %in% names(colourCode))
    ColorText = rbind(ColorText, c("white","","white"))
    

    if(Type=="Regular"){
      plot = plot +
        geom_rect(aes(xmin=xmin, xmax=start.points+1, ymin=y.start, ymax=y.end, fill=color), alpha=1) + 
        geom_rect(aes(xmin=end.points, xmax=xmax, ymin=y.start, ymax=y.end, fill=color), alpha=1)+
        geom_rect(aes(xmin=start.points, xmax=end.points, ymin=y.start, ymax=y.end, fill=tdColor), alpha=1)
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
    
    #if(input$YaxisValue == "Fraction"){
    #  ggtitleLabel = paste0(name,"\n(n=",sum(newdata$countEvents),")")
    #} else{
    #  ggtitleLabel = name
    #}
    
    plot = plot + 
      scale_fill_manual(values = colourCode, labels = ColorText$Text) + #no guid is produced
      
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
      ##setting per facet does not work yet
      
      #number = formatC(sum(newdata$countEvents), format = "e", digits = 1)
      #breaks = c(max(newdata$y.end))
      #scale_y_continuous$labels = c(number)
      #scale_y_continuous$breaks = c(breaks)
      
    }
    plot = plot + scale_y_continuous
    
    if(input$YaxisValue == "Fraction"){
      aliases = newdata %>% group_by(Alias, Subject) %>% summarise(Text = paste0(Alias," \n(n = ",sum(countEvents),")")) %>% distinct()
      aliasesVec = aliases$Text
      names(aliasesVec) = aliases$Alias
    }
    
    
    if(length(unique(newdata$Subject))==1){
      if(input$YaxisValue == "Fraction"){
        plot = plot + facet_wrap(Alias ~ . , ncol = input$nrCols, scales='free',
                                 labeller = labeller(Alias = aliasesVec))
      } else{
        plot = plot + facet_wrap(Alias ~ ., ncol = input$nrCols, scales='free')
      }
    } else{
      if(input$YaxisValue == "Fraction"){
        plot = plot + facet_wrap(Subject ~ Alias, ncol = input$nrCols, scales='free',
                                 labeller = labeller(Alias = aliasesVec))
      } else{
        plot = plot + facet_wrap(Subject ~ Alias, ncol = input$nrCols, scales='free')
      }
    }
    plot = plot + theme(strip.background = element_blank())
    #grid.arrange(grobs=plots, ncol=input$nrCols)
    
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
    aliases = NULL
    ##Really make sure this is only done for the tabs that use the GroupColumn
    ##otherwise it might get set, but it breaks other tabs
    if(input$tabs == "Type" & !is.null(input$GroupColumn) & input$GroupColumn != "-"){
      req(filter_in_data())
      el = filter_in_data()
      aliases = sort(unique(el[[input$GroupColumn]]))
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
          labels = aliases
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
      point = df %>% filter(Alias == hover$panelvar1 & hover$y > y.start & hover$y < y.end)
    } else{
      point = df %>% filter(Alias == hover$panelvar1 & Subject == hover$panelvar2 & hover$y > y.start & hover$y < y.end)
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
  
  
  
}

# Run the application 
shinyApp(ui = ui, server = server)

