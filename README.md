de.foconis.scheduler
====================

Scheduler that scans NSFs and runs periodic tasks.

There is a patrol job that scans periodically all databases.
It recognizes XPage-DBs by the presence of a certain form and then it tries to find jobfactories in 
META-INF/services/de.foconis.core.job.NSFJobGroup

To use it.
1. activate the "de.foconis.core.scheduler.Library" in the properties tab

2. create a 
	FORM name = de.foconis.form.xsp.default
This is a "marker" to determine quickly if it is worth to search for jobfactories
(TODO: Use a Flag in DB-Icon or sth. similar)


3. Create a text file:
META-INF/services/de.foconis.core.job.NSFJobGroup
with the name of a java class
