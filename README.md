de.foconis.scheduler
====================

Scheduler that scans NSFs and runs periodic tasks.

There is a patrol job that scans periodically all databases.
It recognizes XPage-DBs by the presence of a certain form and then it tries to find jobfactories in 
META-INF/services/de.foconis.core.job.NSFJobGroup

To use it.

1. activate the "de.foconis.core.scheduler.Library" in the properties tab

2. Create a text file:
META-INF/services/de.foconis.core.job.NSFJobGroup
with the name of a java class, e.g.: 
> de.foconis.demo.app.MyJobGroup

3. Create the class:
```java
	public class MyJobGroup extends AbstractNSFJobGroup {
		protected void init() {
			addJob(Every15Seconds.class);
			// you can add more jobs here
		}
		public Schedule getSchedule() {
			return PeriodicScheduleBuilder.newInterval("00:00:15").build();
		}
	}
```

4. Create the jobClass:
```java
	public class Every15Seconds extends NSFJob {
		protected void runCode() throws InterruptedException {
			System.out.println("********* this code will run every 15 seconds *******");
		}
	}
```

5. create a 
> FORM name = de.foconis.form.xsp.default

This is a "marker" to determine quickly if it is worth to search for jobfactories
(TODO: Use a Flag in DB-Icon or sth. similar)

Now look at the server console. The PatrolJob should scan the databases and queue the tasks.
(It may be neccessary to sign the database)

you can take a look at the queue with
> tell http xsp sched list

To stop the scheduler:
> tell http xsp sched stop

You can also do some debugging with
> tell http xsp urlget /schedule-demo.nsf/demo.xsp 'CN=Heinz Fink/O=FOCONIS'

this runs the demo.xsp in the user-context of my boss :-)


Security note
=============

This Implementation honors the Notes-security. (at least it tries to do it - I don't want to open a security hole on the server)

- When loading java classes from an NSF, the must be signed by the SAME user. So sign your NSF after putting it on the server.
=> getSessionAsSigner is checked and this returns null if the code is tainted by someone else and the plugin won't accept the task.

- If you want to run code in the server context (= a session without max. internet access restriction), the code checks
if the signer is allowed to run "unrestricted lotusscript and java agents".

- The code can also "run on behalf of". This is done by creating an XPageSession for the given user. To do this the signer needs the "run-on-behalf"-permission in the server document. This session is restricted by maximum internet access.

- The run-on-behalf-of name must also have the permission to access this server
(to fake any name take a look at FakeNativeContextRequestAdapter.getEnforceAccess())



