# COSS

 * [Project Description](#project-description)
 * [Input Data](#input-data)
 * [Output Data](#output-data)
 * [Downloads](#downloads)
 * [Usage](#usage)
 
---
## Project Description

COSS is a user-friendly spectral library search tool capable of processing large spectral libraries and supporting multiple file formats. COSS is developed in Java and hence it is platform independent
Incase of COSS V2, Percolator, a semi-supervised machine learning tool is integrated to inhance the identification rate and the installation path should be given, if Percolator option is checked, to COSS inorder to run COSS in different platform.
COSS uses external subsystems for file reading and writing. Next to file io subsystems there are three main processes: preprocessing, feature extraction and matching. All processes are organized and implemented in a modular fashion to facilitate future upgrades. 

[Go to top of page](#coss)

---
## Input Data
COSS supports the following spectral file format for query (experimental) spectra.
 - Mascot Generic Format: mgf
 - NIST spectral library format: msp
 - Institute of System Biology (ISB): mzXML
 - HUPO Proteomics Standards Initiative file format (PSI): mzData
 - ISB and PSI joint format: mzML
 - SEQUEST: dta
 - pkl
 - ms2
 
Accepted spectral library file formats are mgf and msp.

## Search parameters
 - precursor mass: precursor mass error tolerance (in ppm/Da.)
 - fragment tolerance: fragment mass error tolerance (in ppm/Da.)
 - Max. precursor charge: maximum charge of precursor
 
## Preprocessing
 - This section of the implementation is designed to have multiple options for best search results. Research is still going on and at this time COSS is set to default values.

[Go to top of page](#coss)

---
## Output Data
Users can export the result in excel. The output table contains 15 columns.

| Parameter  | Description        |
|------------|--------------------|
| Title | Title of the spectrum (for msp file, Name is used as title) |
| Library | Describes weather the library is true library or decoy |
| Scan num. | Scan number |
| Sequence | Peptide sequence from the matched library spectrum |
| Prec. Mass (M/Z) | Precursor mass of query spectrum |
| Charge | Charge of query specrum |
| Score | Search score |
| Validation | Validation, either 1% FDR or %5 FDR |
| #filteredQuerypeaks | Query peaks after filtering the spectrum under 100Da mass window |
| #filteredLibraryPeaks | Library peaks after filtering spectrum under 100Da mass window |
| SumIntQuery | Sum of peak intensities in filtered query spectrum |
| SumIntLib | Sum of peak intensities in filtered library spectrum |
| #MatchedPeaks | Number of matched peaks found in query and library spectrua being matched |
| MatchedIntQuery | Sum of peak intensities of query spectrum that have a match in library spectrum |
| MatchedIntLib | Sum of peak intensities of library spectrum that have a match in query spectrum |

It is also possible to save results in .cos format. This allows users to re-import previous results for visualization.
When Percolator is selected, the output is stored in tab delimited file in the same directory of the input file.

[Go to top of page](#coss)

---
## Downloads

Download the latest version of COSS  <a href="http://genesis.ugent.be/maven2/com/compomics/COSS/" onclick="trackOutboundLink('usage','download','coss','http://genesis.ugent.be/maven2/com/compomics/COSS/2.0/.zip'); return false;">here</a>.  

COSS can be run with the user-friendly GUI or through the CLI. 

[Go to top of page](#coss)

---
## Usage
### GUI
- Download COSS from the provided link and unzip it.
- On Windows you can run COSS by double clicking the COSS-X.Y.jar file. COSS can also be started from the command line using the following command:
```
$java -jar COSS-X.Y.jar
```
*X.Y stands for the version number (eg. COSS-1.1.jar)  
Make sure Java is installed on your machine.*
	
- Parameter Setting: Select and fill all required parameters.
- If rescoring with Percolator is needed, make sure to check the option and give the full executable path pf percolator in the setting menu
- Decoy generation: It is recommended to add decoy spectra to your spectral library for result validation. You can generate decoy library spectra using COSS builtin decoy generation. COSS has two algorithms to generate decoy spectra, reverse sequence and random sequence techniques. Click the GenerateDecoy menu and select the algorithm to generate the decoy spectra (which will be equal in size to your spectra library) and concatenate the decoys to your library. 

*Note: make sure the spectral library is annotated before generating decoy library, if not you can use spectrum annotator provided in COSS.

- Configuring File Reader: Click "Config Spec. Reader". At this time, the system disables the "Configure Reader" and "Start Seach" buttons until it is finished with the configuration. 
- Searching: Click "Start Searching", COSS starts searching and displays the status on the progress bar. The left-hand side window shows information of the query file. It also visualizes the spectra.
- Result: To see the results, click on the Result tab from the main window. The upper table lists the experimental spectra while the lower table lists the top 10 matched spectra for the selected experimental spectrum. An interactive spectrum comparison view is presented at the bottom with the selected experimental spectrum (red) mirrored with the selected matched library spectrum (blue).

### CLI
Command line searching is possible in COSS with the following commands:
```
java -jar COSS-X.Y.jar targetSpectraFile librarySpectraFile algorithm(0=MSROBIN, 1=Cosine similarity)   
```
or                    
```
java -jar COSS-X.Y.jar targetSpectraFile librarySpectraFile algorithm(0=MSROBIN, 1=Cosine similarity) precursorMassTolerance(PPM) fragmentTolerance(Da.) 
```
or
```
java -jar COSS-X.Y.jar targetSpectraFile librarySpectraFile algorithm(0=MSROBIN, 1=Cosine similarity) precursorMassTolerance(PPM) fragmentTolerance(Da.) maxNumberofCharge
Note: In COSS V2, Percolator is set on by default and can be switched off by adding "-nP" option at the end of the command
```
Decoy spectra can be generated and appended with the following command: dV- reverse, dR- random
```
java -jar COSS-X.Y.jar -dV librarySpectraFile
```

Spectrum annotation can be done using the following command:

```
java -jar COSS-X.Y.jar -a librarySpectraFile fragment_tolerance
```
*X.Y stands for the version number (eg. COSS-1.1.jar)  
Make sure Java is installed on your machine.*

[Go to top of page](#coss)

---
| Java | Maven | Netbeans | 
|:--:|:--:|:--:|
|[![java](http://genesis.ugent.be/uvpublicdata/image/java.png)](http://java.com/en/) | [![maven](http://genesis.ugent.be/uvpublicdata/image/maven.png)](http://maven.apache.org/) | [![netbeans](https://netbeans.org/images_www/visual-guidelines/NB-logo-single.jpg)](https://netbeans.org/)

[Go to top of page](#coss)
