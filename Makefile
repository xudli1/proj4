#
# Makefile for Minibase Projects
# CS 5443, Fall 2006, UTSA
#
# This Makefile was designed for Linux.
# For Windows (cygwin), change each : to \; in the variables below.
#

SRCPATH = src
BINPATH = bin
SOLJARS = lib/diskmgr.jar:lib/bufmgr.jar:lib/heap.jar:lib/index.jar:lib/relop.jar:lib/query.jar

JAVAC = javac -source 1.5 -target 1.5 -d $(BINPATH) -sourcepath $(SRCPATH):$(SOLJARS)  -classpath $(BINPATH):$(SOLJARS) 
JAVA  = java -classpath $(BINPATH):$(SOLJARS)

#
# Change the following line for each project
PROJ = proj1

PROJFILES = REPORT.txt $($(PROJ)_files)
proj1_files = $(SRCPATH)/bufmgr/*.java $(SRCPATH)/heap/*.java
proj2_files = $(SRCPATH)/index/*.java 
proj3_files = $(SRCPATH)/relop/*.java 
proj4_files = $(SRCPATH)/query/*.java 

.PHONY: global diskmgr bufmgr heap index relop parser query tests \
        clean dmtest bmtest hftest ixtest rotest test

## for using your own packages
## edit the following line. uncomment the next line, comment the line below it 
#all: global diskmgr bufmgr heap index relop parser query tests
all: $(PROJ)

# packages of the projects
proj1: global bufmgr heap tests

proj2: global index tests

proj3: global relop tests

proj4: global parser query tests


global diskmgr bufmgr heap index relop parser query tests :
	$(JAVAC) $(SRCPATH)/$@/*.java

dmtest:
	$(JAVA) tests.DMTest

bmtest: 
	$(JAVA) tests.BMTest

hftest: 
	$(JAVA) tests.HFTest

ixtest: 
	$(JAVA) tests.IXTest

rotest: 
	$(JAVA) tests.ROTest

test:
	$(JAVA) global.Msql < bin/tests/TestDB.sql


clean: clean_classes clean_backups clean_temps
	rm -rf *.minibase $(BINPATH)/*

clean_classes:
	\find . -name \*.class -exec rm -f {} \;

clean_backups:
	\find . -name \*~ -exec rm -f {} \;

clean_temps:
	\find . -name \#* -exec rm -f {} \;


turnin:
	-turnin -v -c cs5443 -p $(PROJ) $(PROJFILES)

verify:
	-turnin -v -c cs5443 -p $(PROJ)

zipfile:
	-zip -r $(USER).zip $(PROJFILES)
