The files in this directory are working examples on how to use the PMonitor 
infrastructure on the JASMIN/CEMS LOTUS cluster. This is an IBM LSF based 
infrastructure.

Description on the LSF can be found here:<p>
https://www.ibm.com/support/knowledgecenter/SSETD4/product_welcome_platform_lsf.html

Documentation on the recommended use of the JASMIN/LOTUS infrastructure is 
here:<p>
https://help.jasmin.ac.uk/category/107-batch-computing-on-lotus

# Software structure

@todo



## environment

The script "envirnonment.sh" creates the required software structure used for 
the communication between PMonitor and the LSF. To activate, it needs to be 
sourced in the shell used to submit commands:

<em>>. ./environment.sh</em>

It defines a method to submit jobs to the LOTUS cluster and defines a number 
of environment variables.

The method <em>submit_job</em> - as the name says - submits a single job to 
the cluster. It takes two cmd-line arguments:
<ol>
    <li>jobname: the unique name of the job to be executes</li>
    <li>command: the command (with all cmd-line parameters) that is to be 
    executed in the directory <b>PM_EXE_DIR</b></li>
</ol>

You most likely do not need to adapt this definition.

To adapt this example file to your environment, you need to adapt four 
variables that are listed in the top of the environment file:

* <b>PROJECT</b>: defines the project name which can be used to identify the 
jobs on the cluster
* <b>WORKING_DIR</b>: defines the working directory, i.e. the directory where 
the job is executed
* <b>PM_LOG_DIR</b>: defines the directory where log files are written to. 
This directory must be existing, the code will not generate it. 
<em><b>IMPORTANT</b>: If not existing, no log files are written and the job 
status can not be traced!</em>
* <b>PM_EXE_DIR</b>: defines the directory where the job executable resides
 

## minimal_job

This file comprises a very stupid task to be executed on the cluster. It prints two messages 
and waits between the messages for 10 seconds.
