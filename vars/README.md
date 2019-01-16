DSL Pipelines and Stages in Groovy
==================================

The **vars/** directory stores Jenkins DSL scripts. The **pipeline** scripts are composed of **DSL stages**.

* **Pipeline scripts** provide complete _parameterized_ pipelines.
* **DSL stages** are procedures to solve common stages.

Docker Builds for Python Applications
-------------------------------------

The **pipelines** build, test, and document **Git SCM projects**. These projects have a **Dockerfile** and build images. Some **stages** assume the project is a **Python3** application.

* **Builds**: Build a Docker **image** with the **Dockerfile**
* **Automated Testing**: Run testing in a _temporary_ Docker **container**
* **Documentation**: Run _one or more_ **stages** to document the **code** and **service APIs**

### Building

* TODO: Explain builds

### Automated Testing

* TODO: Explain testing - Unit Tests, QA Testing, Integration Testing

### Documentation

The pipeline can generate **automated documentation** based on the source code.

* **Service REST API**: Read **Open API Specification** _Swagger_
* **Source Code with Comments**: Read source code, e.g. **Python 3 doc-comments**

#### Swagger Documentation

A **REST API** should have a **/swagger.json** endpoint with the **Open API Specification**.
This may be read and documented in a Wiki or other site.

* TODO: Describe **Swagger Wiki**

#### Python Documentation: Sphinx Build

The **Sphinx** project builds Python documentation from structured source code.
Read the **Sphinx Documentation** link and other sites on how to wite these **doc comments**.

* [Sphinx Documentation](http://www.sphinx-doc.org/en/master/)

##### Executing the Sphinx Build

Add the **build_sphinx.sh** script to the /bin/ directory in your project. The Publish Sphinx stage calls it to build documentation for your project.

* Add the code below to your project and name it ./bin/build_sphinx.sh
* Replace _ADD YOUR PROJECT NAME HERE_ with the name of your project
* Create an empty **documentation/** directory in the root python code folder, e.g. **./api** and commit it

``` bash
#! /bin/bash
#
# Build Sphinx Documentation
# ===============================================

echo "Top Level Project Directory: $1"
folderName = $1

ls -al "./$folderName"
sphinx-apidoc --full -H "ADD YOUR PROJECT NAME HERE" \
    -A "Microservices" -V "1.0" \
    -o ."/$folderName/documentation/api" "./$folderName"

ls -al "./$folderName/documentation/api"
echo " 
import os
import sys
sys.path.insert(0, os.path.abspath('../..'))
sys.path.insert(0, os.path.abspath('../../..'))
" >> "./$folderName/documentation/api/conf.py"

cat "./$folderName/documentation/api/conf.py"

cd "./$folderName/documentation/api" && pwd && echo "Inside documentation folder see path above" && make clean && make html
```
