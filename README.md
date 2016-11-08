# ImportNow

Import now, think later. A system that allows importing data files, saving the contents, and adding metadata during and after import.

# Development environment

Install the [Lightbend Activator](https://www.lightbend.com/activator/download) and add the bin/ to your $PATH for convenience.

Run the Activator UI

    activator ui

A browser tab will be opened. 

Press `Code view & Open in IDE` to generate IDE project files for Eclipse or IntelliJ IDEA.

Press the Run button to run the application.

# Configuration

In `conf/application.conf`, the entries on the bottom are application specific, and start with "importnow".

# Remote repository (Optional)

Download and install [GraphDB Free](http://info.ontotext.com/graphdb-free-ontotext) from Ontotext. Connect to is through your browser, and create a new repo, e.g. "importnow".

Set the same name in the `conf/application.conf` file under `importnow.remoterepo.repo`.

Check that `importnow.remoterepo.url` is correct and leads to your GraphDB instance.

Set `importnow.isLocalRepo` to false
