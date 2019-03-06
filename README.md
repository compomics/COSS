# coss

 * [Project Description](#project-description)
 * [Input Data](#input-data)
 * [Output Data](#output-data)
 * [Downloads](#downloads)
 * [Usage](#usage)
 
 ---
## Project Description

COSS is user-friendly spectral library search tool capable of processing large databases and supporting different spectral file formats. COSS is developed in java and hence it is platform independent. COSS uses external sub-system for file reading and writing and in addition to file input-output subsystem there are three main processes: preprocessing, feature extraction and matching. All the processes are organized and implemented in a modular fation so that future upgrade can be made easy. 



[Go to top of page](#coss)

----
## Input Data
COSS supports the following spectral file format  for query(experimental) spectra.
 - Mascot Generic Format: mgf
 - NIST spectral library format: msp
 - Format from institute of system biology(ISB): mzXML
 - HUPO Proteomics Standards Initiative file format(PSI): mzData
 - ISB and PSI joint format: mzML
 - Sequest: dta
 - pkl
 - ms2
 
 Library spectra file formats should either be mgf or msp file formats. 
 


## Output Data
Users can export the result in excel. The output table contains 15 columns.

| Parameter  | Description        |
|------------|--------------------|
| Title |Title of the spectrum (for msp file, Name is used as title)|
| Library  | describes weather the library is true library or decoy |
| Scan num.  | Scan number  |
| Sequence    |Peptide sequence from the matched library spectrum.   |
| Prec. Mass (M/Z)  | Precursor mass of query spectrum |
| Charge | Charge of query specrum   |
| Score | Search socre   |
| Validation  | validation either <1% FDR or <%5 FDR  |
| #filteredQuerypeaks   |query peaks after filtering the spectrum under 100Da mass window   |
| #filteredLibraryPeaks  | library peaks after filtering spectrum under 100Da mass window  |
| SumIntQuery  | sum of peak intensities in filtered query spectrum  |
| SumIntLib  | sum of peak intensities in filtered library spectrum  |
| #MatchedPeaks  | Number of matched peaks found in query and library spectrua being matched|
| MatchedIntQuery | sum of peak intensities of query spectrum that have a match in library spectrum|
| MatchedIntLib | sum of peak intensities of library spectrum that have a match in query spectrum |

It is also possible to save result in .cos format so that users can import it to visualize.



[Go to top of page](#coss)


----
## Downloads

Download the latest version of COSS  <a href="http://genesis.ugent.be/maven2/com/compomics/COSS/1.0/COSS-1.0.zip" onclick="trackOutboundLink('usage','download','coss','http://genesis.ugent.be/maven2/com/compomics/COSS/1.0/.zip'); return false;">here</a>.  

You can run COSS as CLI or our user-friendly GUI. 

----

## Usage
- Running COSS(GUI): download COSS from the link provided and unzip it.
  On windows: your can run COSS by double clicking on COSS-X.Y.jar file or it can be started on command line using the following command  
  
                $java -jar COSS-X.Y.jar
				
  * X.Y   stands for the version number(eg. COSS-1.1.jar).

	*Make sure java is installed on your machine.
	
- Parameter Setting: Select and fill all the parameters needed.

*It is recommonded that your spectral library has a decoy spectra for result validation. If your library file doens contain a decoy spectra, you can start by generating decoy using COSS. COSS has two algorithms to generate decoy spectra for the given library. Click  GenerateDecoy menue and select the algorithm to generate the decoy spectra equeal to the size of your spectra library and concatenate to your library.

- Configuring File Reader: Click "Config Spec. Reader", at this time system disables "Configure Reader" and "Start Seach" buttons untill   it  is finish file configuration. 
- Searching: click "Start Searching" when it is enabled, now search is started and you can see the status on the progress bar.

 Left window of the system gives information of the query file. It also visualize the spectra.It is automatically displayed after the   
 file is configured.

- Result: To see the result click on the Result tab from the main window. There are tow tables in the result tab window: the upper      
  tables lists query spectra that have got a match. They are sorted decending order based on the score value. The lower table lists 10  
  best matches(if exist) for the selected query spectrum from the upper table. This also sorted based on the top score values of the  
  matched spectrum found. Results also displayed in visual form for a selected query spectrum and matched library spectrum. 


- Running COSS(CLI):
    Command line searching also possible in COSS with the following commands:
	

        java -jar COSS-X.Y.jar targetSpectraFile librarySpectraFile    
	 
        or                    
							  
        java -jar COSS-X.Y.jar targetSpectraFile librarySpectraFile precursorMassTolerance(PPM) fragmentTolerance(Da.) 
			   
        or                     
							  
        java -jar COSS-X.Y.jar targetSpectraFile librarySpectraFile precursorMassTolerance(PPM) fragmentTolerance(Da.) maxNumberofCharge

Decoy spectra can be generated and appended with the following command

        java -jar COSS-X.Y.jar -d librarySpectraFile
	 

  * X.Y   stands for the version number(eg. COSS-1.1.jar).
  
  *Make sure java is installed on your machine.


----

| Java | Maven | Netbeans | 
|:--:|:--:|:--:|
|[![java](http://genesis.ugent.be/public_data/image/java.png)](http://java.com/en/) | [![maven](http://genesis.ugent.be/public_data/image/maven.png)](http://maven.apache.org/) | [![netbeans](https://netbeans.org/images_www/visual-guidelines/NB-logo-single.jpg)](https://netbeans.org/)


[Go to top of page](#coss)



