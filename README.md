GestureRecognizer
=================

## User Manual

Last updated: 09/16/2014

Authors: Baptiste Angles, Thomas Pellegrini

Contact: pellegri@irit.fr

Introduction
------------

GestureRecognizer is a Java program (and a library) able to:

- Play and cut a Kinect stream to generate a gesture file containing joint coordinates and labels.
- Train a model able to be used for template-based or HMM-based recognition.
- Perform online or offline gesture recognition

Installation
------------

### Requirements and dependencies:

- Linux x64
- g++
- python
- JDK >= 6
- libusb-1.0-0-dev
- freeglut3-dev
- ant
- subversion
- git
- doxygen

Command line:
``` sh
sudo apt-get install g++ python openjdk-7-jdk libusb-1.0-0-dev freeglut3-dev ant subversion git doxygen
```

### With the installer

``` sh
./install.sh [installation directory]
```

Only Gesture Recognizer will be installed in the directory.

``` sh
./install.sh ~/software
```

### Without the installer

Additionnal dependencies:
- OpenNI (+ org.openni.jar)
- NITE (+ com.primesense.NITE.jar)
- SensorKinect
- JavaOSC (+ javaosc.jar)

``` sh
git clone https://github.com/0xg/GestureRecognizer.git
```

The 3 .jar files must then be copied into the GestureRecognizer/lib/ directory.

Data types
----------

The program uses 3 different types of files

### Gesture Files

#### .gst format:

Binary format. It can contain both the joint coordinates and the frame labels.

#### .txt format:

Each line represents a frame (capture) and is formatted like this:

timestamp joint_1_x joint_1_y joint_1_z joint_2_x joint_2_y joint_2_z […]

The joints must respect this order:

1. HEAD	
2. NECK 
3. TORSO 
4. WAIST
5. LEFT_COLLAR
6. LEFT_SHOULDER
7. LEFT_ELBOW
8. LEFT_WRIST
9. LEFT_HAND
10. LEFT_FINGERTIP
11. RIGHT_COLLAR
12. RIGHT_SHOULDER
13. RIGHT_ELBOW
14. RIGHT_WRIST
15. RIGHT_HAND
16. RIGHT_FINGERTIP
17. LEFT_HIP
18. LEFT_KNEE
19. LEFT_ANKLE
20. LEFT_FOOT
21. RIGHT_HIP
22. RIGHT_KNEE 
23. RIGHT_ANKLE
24. RIGHT_FOOT

#### .csv format:

Format used by the Chalearn challenge. It is very similar to the .txt format but with a few differences. More information on: http://gesture.chalearn.org/mmdata#Track3

#### .oni format:

Ce format ne contient pas directement de geste mais plutot des frames de camera RGB et Depth. NITE permet d'en extraire un geste.

### Label files

#### .csv format:

Format used by the Chalearn challenge. Each line represents a gesture and is formatted like this:
```
name start_frame end_frame
```

### Model files

The only format is binary and is produced by the ant train command. There is no particuliar file extension.

Usage
-----

### Configuration

The configuration of the program is made in config.xml. The different parameters are:

- detectionThreshold: This is the detection threshold used by the online recognizer. [min: 0, max: 1, default: 0.97]
- windows: the list of the analysis window sizes used by both the offline and online recognizers.
- sampleCount: the capture count after the resampling step.
- joints: the list of joints kept after the normalization step.

### Record a gesture

``` sh
ant record -Dinput=... -Dlabels=... -Doutput=... -Dui=...
```

- input: a gesture file (txt, oni, gst, csv) or “kinect”
- labels: (optionnal) a label file ou or the label (ie. "Hello")
- output: the path of the output gesture file (.gst or .txt)
- ui: (optionnal) yes/no to enable/disable the graphical interface that plays the input stream

#### Examples:

With a .oni file as the input, one must precise the gesture label. Here, some gesture files are extracted from the .oni source, with 2 instances of the WG gesture and 1 of the PLAY gesture:

``` sh
ant record -Dinput=p1.oni -Dlabels=wg -Doutput=”wg1.gst” -Dui=yes
ant record -Dinput=p1.oni -Dlabels=wg -Doutput=”wg2.gst” -Dui=yes
ant record -Dinput=p1.oni -Dlabels=play -Doutput=”play.gst” -Dui=yes
```

The gesture division is made by pressing ENTER to start and stop recording.

Same thing when using the kinect as the input:

``` sh
ant record -Dinput=kinect -Dlabels=Hello -Doutput=”gesteHello.gst” -Dui=yes
```
A .txt gesture file can be converted to a .gst file:

``` sh
ant record -Dinput=geste1.txt -Dlabels=labels.csv -Doutput=”geste1.gst”
```
A .txt gesture file and a .csv label file can be merged into a .gst file:

``` sh
ant record -Dinput=geste1.txt -Dlabels=labels1.csv -Doutput=”geste1.txt”
```

### Train a model

``` sh
ant train -Dinput=... -Dtype=... -Doutput=...
```

- input: the .gst files (labeled) or directories used to train the model
- type: the type of the model (“hmm” or “template”)
- output: the path to the ouput model file

#### Examples

To train a model with the gesture files we just recorded:

``` sh
ant train -Dinput=”wg1.gst wg2.gst play.gst” -Dtype=template -Doutput=model_wg_play
```

To create a HMM model with a directory containing some .gst files:

``` sh
ant train -Dinput=directory1/ -Dtype=hmm -output=model_hmm1
```

or even:

``` sh
ant train -Dinput=”directory1/ play.gst“ -Dtype=hmm -output=model_hmm1
```

### Recognize a gesture

``` sh
ant recognize -Dinput=... -Dmodel=... -Dtype=... -Doutput=... -Dui=...
```

- input: a gesture file or “kinect”
- model: a model file
- type:
	- offline: distance à des gestes template ou HMM selon la nature du modèle spécifié
	- online: distance à des gestes template uniquement 
- output: stdout (default) or a path or “ip port osc_address”
- ui: (optionnal) yes/no to enable/disable the graphical interface that plays the input stream

#### Examples

```
ant recognize -Dinput=p1.oni -Dmodel=model_play_wg -Dtype=online -Doutput=stdout -Dui=yes
ant recognize -Dinput=kinect -Dmodel=model_play_wg -Dtype=online -Doutput=stdout -Dui=yes
ant recognize -Dinput=geste1.gst -Dmodel=model1 -Dtype=offline -Doutput=”192.168.0.1 9999 /drums”
```

License
-------

GestureRecognizer is released under the [MIT license.](https://github.com/0xg/GestureRecognizer/blob/master/LICENSE)


