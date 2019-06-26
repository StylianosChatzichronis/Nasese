# Nasese software
The Nasese(Neurocognitive Assessment Software for Enrichment Sensorimotor Environments) software with all necessary files Neurocognitive Assessment Software for Enrichment Sensorimotor Environments 


-nasese_lib.rar includes the libraries required to run the software.

Process to install as application:
-Download files
-Create a folder
-Unrar all files in the folder
-Run the NASESE.jar

Publication link: https://www.omicsonline.org/open-access/neurocognitive-assessment-software-for-enrichment-sensory-environments-0974-276X-1000492-107431.html?view=mobile

Process to install and execute manually NASESE from eclipse:

In order to install and execute NASESE, follow the instructions below:

----Intro----

a)Install Eclipse b) Open eclipse c)Save workplace
choose File-> New ->Java project
Project Name -> ASDAT_server
click next, then click finish
In the package explorer there is the ASDAT_server project
left click two times in the ASDAT_server icon 7.Right click in the src folder (New->class)
Package-> asdat_server , Name->ASDAT_server
choose File-> New ->Class 10.Package-> asdat_server , Name->display_client
Copy from files to the corresponding classes. 12.Save

Repeat the steps 3-12 for Project->ASDAT_client, package->asdat_client, classes-> ASDAT_client, display_server

-------------------------Program's required libraries----------

a) Download processing 3.3.1 . Other versions of processing may be also efficient,but there are compatible issues like dissasembling of the brain model.

b)Apart from processing there are some additional libraries. To import the libraries into the eclipse project: Go to package explorer. Right click in the ASDAT_server.java Build path->Configure Build path Select Libraries push Add external jar choose the corresponding libraries

c)In the eclipse workplace there are folders with the projects. Copy & paste the images in the ASDAT_server and ASDAT_client folders. Copy and paste the folder octaves in the ASDAT_server folder.
