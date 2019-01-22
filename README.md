# sceptics-tool
The SCEPTICS Tool

A tool for automatic threat/hazard analysis for system architectures (in particular Industrial Control Systems).

This GitHub repository supports the Paper "The SCEPTICS Tool for Threat Analysis in Industrial Control systems"

### Pre-requisites
In order to create and modify the Visio input diagram, you will require:
- Microsoft Windows 7+
- Microsoft Office Visio 2013+

The SCEPTICS tool is OS-agnostic, and may be used on MacOS, Windows and Linux. You will, however, require:
- Oracle JDK 7+ (the tool has not been tested on JDK 10)

### Creating an Input Diagram
#### Visio Input
> We recommend using the reference diagram, 'testModel.vsdx', located in the '`res`' folder as a starting point. The tool expects as its input a file called `testModel.vsdx` in the `res` folder, where your model should be on the Visio Page `Page-1`.

> You may want to use the existing nodes and links, and modify the properties to create your model.

First, you will need to create a minimum of two nodes and connect them using a link. Ensure that the same type of link is added to the legend (we use the link formatting to discriminate between link types).

Nodes and links can have a CVSS Profile or direct probability assigned to them. For CVSS Profiles, right click on the node/link, go to Data > Shape Data. For CVSS profiles, you can add a `cvss` data property directly, otherwise for probability values, you can use a `tweak` property and add your probability.

> To obtain a CVSS profile, visit [the NVD CVSS v3 Calculator](https://nvd.nist.gov/vuln-metrics/cvss/v3-calculator), establish the appropriate CVSS properties for the node/link you are defining and click the link under 'CVSS v3 Vector' (below the graphs) and copy the URL after `?vector=`. An example CVSS v3 Vector which would be added to the tool is `AV:A/AC:H/PR:H/UI:R/S:C/C:L/I:H/A:H/E:H/RL:U/RC:C/CR:H/IR:H/AR:H/MAV:P/MAC:H/MPR:L/MUI:R/MS:C/MC:N/MI:N/MA:N`.

### Adversary and Asset Inputs
> The XML inputs for the asset and adversary model files are validated for correctness against the supplied DTD files in the `res` folder.

The Asset List can contain one or more assets - it is important that the assets are uniquely named, otherwise the tool cannot distinguish between assets when searching for valid paths.

The Adversary Model can contain one or more adversaries, with varing capabilities. It is important to ensure that the entry nodes and adversary `id`s are unique.


#### Adversary Model
> The tool expects the adversary file to be called `adversary.xml`, located in the `res` folder.

An example for two different adversaries is given below.

```xml
<?xml version="1.0"?>
<!DOCTYPE adversaries SYSTEM "adversary.dtd">
<!--Adversary Capabilities/Restriction Mask-->
<adversaries>
  <adversary id="a1">
    <edge_types>
      <type>short-range wireless</type><!-- capabilities of the adversary-->
      <!--OR operation-->
      <type>long-range wireless</type>
    </edge_types>
    <entry_nodes>
    <!--AND operation-->
      <node>SecureBalise</node><!-- the name of the node that is an entry point (from the Visio Graph)-->
    </entry_nodes>
  </adversary>
 
  <adversary id="a2"><!-- Note: the ids must be unique-->
    <edge_types>
      <type>short-range wireless</type>
      <!--OR operation-->
      <type>long-range wireless</type>
    </edge_types>
    <entry_nodes>
    <!--AND operation-->
      <node>Workstation1</node><!--This demonstrates an adversary who has two nodes as entry points-->
      <node>Workstation2</node>
    </entry_nodes>
  </adversary>
</adversaries>
  ```

#### Asset List
> The tool expects the list of assets (that you want to measure exposure against) to be called `assets.xml`, located in the `res` folder.

An example Assets XML file which informs the tool to search for paths to and from the `EVC` node in the Visio Graph.

```xml
<?xml version="1.0"?>
<!DOCTYPE assets SYSTEM "asset.dtd">
<assets>
  <asset>
    <nodes>
      <name>EVC</name> <!-- name corresponding to the Visio Graph-->
    </nodes>
    <data_types>
      <type>authenticated</type><!-- a list of data types that you are interested in (must match what is in the Visio Graph-->
      <type>position</type>
      <type>data</type>
      <type>cvss</type>
    </data_types>
    <data_profiles>
      <profile>cvss</profile><!--the data profile, where CVSS uses the CVSS metrics-->
    </data_profiles>
  </asset> 
</assets>
```

### Running the tool
> For Windows Users, we recommend using PowerShell so that you can access historical commands.
1. Navigate to the top-level folder (i.e. sceptics-tool).
2. Run the appropriate command for your operating system, e.g. for MacOS, run `./misc/run-mac.sh`. For Windows users, it would be `.\misc\run-win.cmd`.

### Example Results
Using the tool with the supplied model graph, the following results will be displayed:

```

     SCEPTICS TOOL
------------------------

INPUT:
Model File: <filename var>
Attack Threshold: <threshold>
Printing the Top <count> possible attack paths per adversary
Key Assets:
        - EVC
Adversaries:
        - a1, Entry Point: SecureBalise
        - a2, Entry Point: Workstation1
        - a2, Entry Point: Workstation2
        - a3, Entry Point: GPS
--------------------
ANALYSIS

Adversary a1:
        Path [SecureBalise]->[Balise transmission module]->[EVC] has a probability of 0.42183
------------
Adversary a2:
        Path [Workstation1]->[Train vendor]->[Internet/VPN]->[WiFi]->[Car BUS]->[EVC] has a probability of 0.0512
        Path [Workstation1]->[Train vendor]->[Internet/VPN]->[GSM]->[Car BUS]->[EVC] has a probability of 0.01246
        Path [Workstation2]->[Train vendor]->[Internet/VPN]->[WiFi]->[Car BUS]->[EVC] has a probability of 0.01191
        Path [Workstation2]->[Train vendor]->[Internet/VPN]->[GSM]->[Car BUS]->[EVC] has a probability of 0.0029
------------------------
Matching * for a1
All Roads Point To Analysis for Asset EVC
        Path [EVC] has a probability of 1
        Path [GSM-R]->[EVC] has a probability of 0.61204
        Path [Balise transmission module]->[EVC] has a probability of 0.61204
        Path [SecureBalise]->[Balise transmission module]->[EVC] has a probability of 0.42183
        Path [WiFi]->[Car BUS]->[EVC] has a probability of 0.3805
        Path [GSM]->[Car BUS]->[EVC] has a probability of 0.3805
        Path [GPS]->[Car BUS]->[EVC] has a probability of 0.3805
        Path [Car BUS]->[EVC] has a probability of 0.3805
        Path [Internet/VPN]->[WiFi]->[Car BUS]->[EVC] has a probability of 0.23288
        Path [GSM-R base]->[GSM-R]->[EVC] has a probability of 0.13841
        Path [Radar doppler]->[EVC] has a probability of 0.07178
        Path [Odometry Sensor]->[EVC] has a probability of 0.07178
        Path [Juridical recording unit]->[EVC] has a probability of 0.07178
        Path [Driver?s display]->[EVC] has a probability of 0.07178
        Path [Internet/VPN]->[GSM]->[Car BUS]->[EVC] has a probability of 0.05668
        Path [Train vendor]->[Internet/VPN]->[WiFi]->[Car BUS]->[EVC] has a probability of 0.05266
        Path [Workstation1]->[Train vendor]->[Internet/VPN]->[WiFi]->[Car BUS]->[EVC] has a probability of 0.0512
------------
Patient-Zero Analysis for Asset EVC
        Path [EVC] has a probability of 1
        Path [EVC]->[GSM-R] has a probability of 0.61204
        Path [EVC]->[Balise transmission module] has a probability of 0.61204
        Path [EVC]->[Balise transmission module]->[SecureBalise] has a probability of 0.42183
        Path [EVC]->[Car BUS]->[WiFi] has a probability of 0.3805
        Path [EVC]->[Car BUS]->[GSM] has a probability of 0.3805
        Path [EVC]->[Car BUS]->[GPS] has a probability of 0.3805
        Path [EVC]->[Car BUS] has a probability of 0.3805
        Path [EVC]->[Car BUS]->[WiFi]->[Internet/VPN] has a probability of 0.23288
        Path [EVC]->[GSM-R]->[GSM-R base] has a probability of 0.13841
        Path [EVC]->[Radar doppler] has a probability of 0.07178
        Path [EVC]->[Odometry Sensor] has a probability of 0.07178
        Path [EVC]->[Juridical recording unit] has a probability of 0.07178
        Path [EVC]->[Driver?s display] has a probability of 0.07178
        Path [EVC]->[Car BUS]->[GSM]->[Internet/VPN] has a probability of 0.05668
        Path [EVC]->[Car BUS]->[WiFi]->[Internet/VPN]->[Train vendor] has a probability of 0.05266
        Path [EVC]->[Car BUS]->[WiFi]->[Internet/VPN]->[Train vendor]->[Workstation1] has a probability of 0.0512
------------
```