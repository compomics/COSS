   
        COSS(CompOmics Spectral Library Searching tool)
        ==============================================

    Running COSS(GUI)
   =================
   Download COSS from "http://genesis.ugent.be/maven2/com/compomics/COSS/1.0/COSS-1.0.zip" and unzip it. On windows: your can       run COSS by double clicking on COSS-X.Y.jar file or it can be started on command line using the following command
   $java -jar COSS-X.Y.jar

   X.Y stands for the version number(eg. COSS-1.1.jar).
   On Ubuntu machine: COSS can be run using the command $java -jar COSS-X.Y.jar

   *Make sure java is installed on your machine.

   Parameter Setting: Select and fill all the parameters needed.

   *It is recommonded that your spectral library has a decoy spectra for result validation. 
   If your library file doens contain a decoy spectra, you can start by generating decoy using COSS.
   COSS has two algorithms to generate decoy spectra for the given library. Click GenerateDecoy menue
   and select the algorithm to generate the decoy spectra equeal to the size of your spectra library
   and concatenate to your library.

   Configuring File Reader: Click "Config Spec. Reader", at this time system disables "Configure Reader" 
   and "Start Seach" buttons untill it is finish file configuration.
   
   Searching: click "Start Searching" when it is enabled, now search is started and you can see the status
   on the progress bar. Left window of the system gives information of the query file. It also visualize 
   the spectra.It is automatically displayed after the file is configured.

   Result: To see the result click on the Result tab from the main window. There are tow tables in the
   result tab window: the upper tables lists query spectra that have got a match. They are sorted
   decending order based on the score value. The lower table lists 10 best matches(if exist) for the
   selected query spectrum from the upper table. This also sorted based on the top score values of the
   matched spectrum found. Results also displayed in visual form for a selected query spectrum and 
   matched library spectrum.

    License
   =========
   
   Copyright: Genet Abay Shiferaw, Elien Vandermarliere, Niels Hulstaert, Lennart Martens and Pieter-Jan Volders.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0


   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
   See the License for the specific language governing permissions and
   limitations under the License.
