<html>
		<head>
		<title>Java Parallel Processing Framework Home Page</title>
		<meta name="description" content="An open-source, Java-based, framework for parallel computing.">
		<meta name="keywords" content="JPPF, Java, Parallel Computing, Distributed Computing, Grid Computing, Cluster, Grid">
		<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8">
		<link rel="shortcut icon" href="images/jppf-icon.ico" type="image/x-icon">
		<link rel="stylesheet" type="text/css" href="./jppf.css" title="Style">
	</head>
	<body>
		<div align="center">
				<table align="center" width="80%" cellspacing="0" cellpadding="5"
			class="table_" style="background: url('images/grid.gif'); background-repeat: repeat; background-attachment: fixed">
			<tr><td height="5"></td></tr>
			<tr>
				<td width="30%" align="left" valign="center">
					<h3>Java Parallel Processing Framework</h3>
				</td>
				<td width="40%" align="center">
					<img src="images/logo.gif" border="0" alt="Java Parallel Processing Framework"/>
				</td>
				<td width="30%" align="right">
					<a href="http://sourceforge.net" target="_top">
						<img src="http://sourceforge.net/sflogo.php?group_id=135654&amp;type=4"
							width="125" height="37" border="0" alt="SourceForge.net Logo" />
					</a>
				</td>
			</tr>
			<tr><td height="5"></td></tr>
		</table>
		<!--<table border="0" style="background-color: #8080FF" cellspacing="0" cellpadding="0" width="80%">-->
		<table style="background: url('images/bkg-menu.gif'); background-repeat: repeat; background-attachment: fixed"
			cellspacing="0" cellpadding="0" width="80%">
			<tr>
				<td>
					<table border="0" cellspacing="0" cellpadding="5">
						<tr>
							<td class="menu_first"><a href="index.html">Home</a></td>
							<td class="menu"><a href="http://sourceforge.net/project/showfiles.php?group_id=135654">Files</a></td>
							<td class="menu"><a href="faq.php">Faqs</a></td>
							<td class="menu"><a href="news.php">News</a></td>
							<td class="menu"><a href="http://sourceforge.net/projects/jppf-project">Project</a></td>
							<td class="menu"><a href="readme.html">Readme</a></td >
							<td class="menu"><a href="api/index.html">API Doc</a></td >
							<td class="menu"><a href="archi.html">Architecture</a></td >
							<td class="menu"><a href="todos.html">Todos</a></td >
							<td class="menu"><a href="screenshots.html">Screenshots</a></td >
							<td class="menu"><a href="links.html">Links</a></td>
							<td class="menu"></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		<table border="0" cellspacing="0" cellpadding="0" width="80%">
			<tr>
				<td width="50%" valign="top" rowspan="2">
					<table class="table_" cellspacing="0" cellpadding="5" width="100%">
						<tr><td>
							<h3>Project Description</h3>
							Java Parallel Processing Framework is a set of tools and APIs to facilitate the parallelization of CPU intensive applications, and distribute their execution over a network of heterogenous nodes.<br>
							It is intended to run in clusters and grids.
						</td></tr>
						<tr><td height="1px" colspan="0" style="background-color: #8080FF"/></tr>
						<tr><td>
							<h4>Features</h4>
							<ul>
								<li>an API to delegate the processing of parallelized tasks to local and remote execution services</li>
								<li>a set of APIs and user interface tools to administrate and monitor execution services</li>
								<li>real-time adaptive load balancing capabilities</li>
								<li>scalability up to an arbitrary number of processing nodes</li>
								<li>support for failover and recovery</li>
								<li>limited intrusiveness for existing or legacy code</li>
								<li>a dynamic deployment mechanism, that enables the execution of new, or updated, code without having to deploy onto the grid</li>
								<li>fully documented APIs, administration guide and developer guide</li>
								<li>runs on any platform supporting Java 2 Platform Standard Edition 5.0 (J2SE 1.5)</li>
							</ul>
						</td></tr>
						<tr><td height="1px" colspan="0" style="background-color: #8080FF"/></tr>
						<tr><td>
							<h4>Current&nbsp;status: <span style="color: black; font-weight: normal; font-size: 10pt">Version 0.10.2 - beta</span></h4>
						</td></tr>
						<tr><td height="1px" colspan="0" style="background-color: #8080FF"/></tr>
						<tr><td>
							<h4>Licensing: <span style="color: black; font-weight: normal; font-size: 10pt">This project is licensed under the GNU General Public License (GPL).<br>
							A copy of the licensing terms can be obtained at the <a href="http://www.opensource.org/licenses/gpl-license.php">
							<b>Open Source Initiative</b></a> web site.</span></h4>
						</td></tr>
						<tr><td height="1px" colspan="0" style="background-color: #8080FF"/></tr>
					</table>
				</td>
				<td width="50%" valign="top">
					<table class="table_" cellspacing="0" cellpadding="5" width="100%">
						<tr><td>
						<?php
							$link = mysql_connect('mysql4-j', 'j135654admin', 'tri75den')
								 or die('Could not connect: ' . mysql_error());
							mysql_select_db('j135654_web') or die('Could not select database');
							$query = 'SELECT * FROM news ORDER BY date DESC';
							$result = mysql_query($query) or die('Query failed: ' . mysql_error());
							$line = mysql_fetch_array($result, MYSQL_ASSOC);
							printf("<h3>Latest news: <span style='color: black'>%s %s</span></h3>", date("n/j/Y", strtotime($line["date"])), $line["title"]);
							printf("%s", $line["desc"]);
						?>
						<p><u style="color: #8080FF"><strong style="color: #8080FF">Summary of changes:</strong></u>
						<?php
							printf("%s", $line["content"]);
							mysql_free_result($result);
							mysql_close($link);
						?>
						</td></tr>
						<tr><td height="1px" colspan="0" style="background-color: #8080FF"/></tr>
						<tr><td>
							<h3>JPPF in the World</h3>
							<b>
							<a href="http://lwn.net/Articles/156109" target=_top>At LWN.net</a><br>
							<a href="http://www.clustermonkey.net//component/option,com_weblinks/catid,40/Itemid,23" target=_top>At Cluster Monkey</a><br>
							</b>
							&nbsp;<br>
						</td></tr>
						<tr><td height="1px" colspan="0" style="background-color: #8080FF"/></tr>
						<tr><td>
							<h3>Feedback Wanted: <span style="color: #000060">help making JPPF a better open source product</span></h3>
							Suggestions, bug reports, criticism and ideas are most welcome. I will do my best to answer promptly.<br>
							<a href="http://sourceforge.net/forum/forum.php?forum_id=458548" target=_top>An open discussion forum is available here</a><br>
							<a href="http://sourceforge.net/forum/forum.php?forum_id=458549" target=_top>A help forum is available here</a><br>
							<a href="http://sourceforge.net/tracker/?atid=733518&group_id=135654&func=browse" target=_top>The bugs tracking system is here</a><br>
							<a href="http://sourceforge.net/tracker/?atid=733521&group_id=135654&func=browse" target=_top>The feature request tracking system is here</a><br>
						</td></tr>
						<tr><td height="1px" colspan="0" style="background-color: #8080FF"/></tr>
					</table>
				</td>
			</tr>
		</div>
	</body>
</html>
