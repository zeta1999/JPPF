$template{name="sample-readme-html-header" title="Grid Failover"}$
<div style="padding-right: 5px">
<h3>What does the sample do?</h3>
This demo illustrates a JPPF grid failover mechanism which ensures that a JPPF driver is not a single point of failure.

<h3>How does it work?</h3>
The failover mechanism relies on two main components:
<ul class="samplesList">
  <li>the nodes have a specific <a href="https://www.jppf.org/doc/6.3/index.php?title=Defining_the_node_connection_strategy">node connection strategy</a>
  which handles an ordered list of drivers to connect to, as follows:
    <ul class="samplesList">
      <li>the first driver in the list is considered the primary driver, all others are backups in case the primary goes down</li>
      <li>when initially connecting or reconnecting due to a mangement request, the node attemps to connect
      to the first (highest priority) driver in the list, then goes down the list if the driver is not online</li>
      <li>when reconnecting due to a disconnection from the driver, the node goes down the list of drivers,
      rolling back to the top when the end of the list is reached</li>
    </ul>
  </li>
  <li>a JPPF client is used as a controller, to detect when a driver is back up after going down, and decide whether it is the new primary and force the nodes to reconnect to it.
  This client is used as follows:
    <ul class="samplesList">
      <li>it is <a href="https://www.jppf.org/doc/6.3/index.php?title=Client_and_administration_console_configuration#Manual_network_configuration">configured</a>
      to connect to the same drivers as provided to the nodes. Each configured driver has a <a href="https://www.jppf.org/doc/6.3/index.php?title=Client_and_administration_console_configuration#Priority">priority</a>
      in descending order of the list given to the nodes</li>
      <li>for each driver connection, a <a href="https://www.jppf.org/doc/6.3/index.php?title=Connection_pools#Status_notifications_for_existing_connections">connection status listener</a> is registered </li>
      <li>the listener detects situations when the primary driver goes down or gets back up and determines which dfriver is the current primary</li>
      <li>when a new primary driver goes back up, the listener issues a management request to <a href="http://localhost:8880/doc/6.3/index.php?title=Node_management_and_monitoring#Shutting_down.2C_restarting_and_reconnecting_the_node">force the nodes to reconnect</a>, once they complete their remaining tasks</li>
    </ul>
  </li>
</ul>

<h3>How do I run it?</h3>
<ul class="samplesList">
  <li>Before running this sample application, you must have at least two JPPF servers running.<br>
  For information on how to set up a node and server, please refer to the <a href="https://www.jppf.org/doc/6.3/index.php?title=Introduction">JPPF documentation</a>.</li>
  <li>Update the <a href="drivers.yaml.html">drivers definition file</a> to reflect the information on your JPPF servers</li>
  <li>build the sample with this command: <b><tt class="samples">ant zip</tt></b>. This will create a file named <b>jppf-grid-failover.zip</b></li>
  <li>configure a node to use the driver discovery:
    <ul class="samplesList">
      <li>unzip the <b>jppf-grid-failover.zip</b> file into the root installation directory of a node. It will copy the required libraries into the node's <b>/lib</b> directory,
      and the <b>drivers.yaml</b> file in the node's root installation directory.</li>
      <li>in a text editor, open the node's configuration file located at <b>&lt;node_install_root&gt;/config/jppf-node.properties</b> and add this line:<br>
        <tt class="samples">jppf.server.connection.strategy = org.jppf.example.gridfailover.NodeSideDiscovery</tt></li>
      <li>start the node: <tt class="samples">./startNode.sh</tt> or <tt class="samples">startNode.bat</tt></li>
    </ul>
  </li>
  <li>configure an administration console to use the driver discovery:
    <ul class="samplesList">
      <li>unzip the <b>jppf-grid-failover.zip</b> file into the root installation directory of an adminsitration console. It will copy the required libraries into the console's <b>/lib</b> directory,
      and the <b>drivers.yaml</b> file in the console's root installation directory.</li>
      <li>in a text editor, open the admin console's configuration file located at <b>&lt;console_install_root&gt;/config/jppf-gui.properties</b> and add this line:
        <tt class="samples">jppf.remote.execution.enabled = false</tt><br>
        this will disable the default built-in discovery mechanism, such that only our custom one is used</li>
      <li>the client-side discovery auto-installs through the Service Provider Interface (SPI), thus no configuration update is needed for this</li>
      <li>start the admin console: <tt class="samples">./startConsole.sh</tt> or <tt class="samples">startConsole.bat</tt></li>
      <li>once started, the admin console should show the drivers defined in the drivers.yaml file, along with a node connected to the first driver in the list</li>
    </ul>
  </li>
  <li>from this sample's root directory, start the sample grid controller: <tt class="samples">./run.sh</tt> or <tt class="samples">run.bat</tt>. The console output will show something similar to this:
<pre class="samples" style="text-align: left; padding-left: 5px; font-size: 0.95em; color: #A0FFA0; background-color: black">
client process id: 37556, uuid: A9619789-6E4B-4DA9-B267-FC014A811275
press [Enter] to exit
discovered new driver: ClientConnectionPoolInfo[name=primary, secure=false, host=localhost,
 port=11111, priority=2, poolSize=1, jmxPoolSize=1, heartbeatEnabled=false, maxJobs=2147483647]
discovered new driver: ClientConnectionPoolInfo[name=backup, secure=false, host=localhost,
 port=11112, priority=1, poolSize=1, jmxPoolSize=1, heartbeatEnabled=false, maxJobs=2147483647]
[client: primary-1 - ClassServer] Attempting connection to the class server at localhost:11111
[client: backup-1 - ClassServer] Attempting connection to the class server at localhost:11112
[client: backup-1 - ClassServer] Reconnected to the class server
[client: primary-1 - ClassServer] Reconnected to the class server
[client: primary-1 - TasksServer] Attempting connection to the task server at localhost:11111
[client: backup-1 - TasksServer] Attempting connection to the task server at localhost:11112
[client: primary-1 - TasksServer] Reconnected to the JPPF task server
[client: backup-1 - TasksServer] Reconnected to the JPPF task server
</pre>
  <li>shutdown the primary server:
    <ul class="samplesList">
      <li>in the administration console's "Tree view", select the driver to which a node is connected</li>
      <li>click on the "Server restart or shutdown" button (<img src="data/server_restart.gif"/>)</li>
      <li>in the displayed dialog, enter "<b>0</b>" for the "Shutdown delay" and uncheck the "Restart" checkbox:<br>
      <img src="data/server_shutdown_dialog.png"/></li>
      <li>click the "Ok" button to terminate the server</li>
    </ul>
  </li>
  <li>you will notice that the node reconnects to the "backup" server</li>
  <li>now start the terminated server again</li>
  <li>observe how the node disconnects from the backup server and reconnects to the one that was just restarted</li>
</ul>

<h3>Related source files</h3>
<ul class="samplesList">
  <li><a href="src/org/jppf/example/gridfailover/NodeSideDiscovery.java.html">NodeSideDiscovery.java</a> : the node connection strategy</li>
  <li><a href="src/org/jppf/example/gridfailover/ClientSideDiscovery.java.html">ClientSideDiscovery.java</a> : the client-side driver discovery plugin</li>
  <li><a href="src/org/jppf/example/gridfailover/ConnectionListener.java.html">ConnectionListener.java</a> : the grid topology monitor and controller, which detects when
    a primary driver comes back online and forces the node to reconnect to this driver</li>
  <li><a href="src/org/jppf/example/gridfailover/Utils.java.html">Utils.java</a> : utilities to parse the YAML drivers definition file</li>
  <li><a href="drivers.yaml.html">drivers.yaml</a> : the jppf drivers defintion file, in YAML format</li>
  <li><a href="config/jppf.properties.html">jppf.properties</a> : the jppf client configuration file</li>
</ul>

<h3>I have additional questions and comments, where can I go?</h3>
<p>There are 2 privileged places you can go to:
<ul class="samplesList">
  <li><a href="https://www.jppf.org/forums">The JPPF Forums</a></li>
  <li><a href="https://www.jppf.org/doc/6.3/">The JPPF documentation</a></li>
</ul>
</div>
$template{name="sample-readme-html-footer"}$
