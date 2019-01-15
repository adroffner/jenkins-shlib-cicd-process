# Sphinx Build Stage README

## The below script needs to be included in the /bin/ directory in your project for the Publish Sphinx phase that builds out documentation for your project based on your directories, modules, methods, and doc comments.

### Steps to integrate sphinx into your project: 

- Add the below code to your project and name it build_sphinx.sh
- Replace the 'ADD YOUR PROJECT NAME HERE' with the name of your project.
- Copy to the /bin folder at the top level of the project
- Make an empty documentation directory in your 'api' folder and be sure to commit it along with the rest of your code.

#! /bin/bash
#### ### ####
#### This script builds out the sphinx documentation
#### ### ####

echo "This represents the top level project directory: $1"
folderName = $1

ls -al "./$folderName"
sphinx-apidoc --full -H "ADD YOUR PROJECT NAME HERE" -A "Microservices" -V "1.0" -o ."/$folderName/documentation/api" "./$folderName"

ls -al "./$folderName/documentation/api"
echo " 
import os
import sys
sys.path.insert(0, os.path.abspath('../..'))
sys.path.insert(0, os.path.abspath('../../..'))
" >> "./$folderName/documentation/api/conf.py"

cat "./$folderName/documentation/api/conf.py"

cd "./$folderName/documentation/api" && pwd && echo "Inside documentation folder see path above" && make clean && make html