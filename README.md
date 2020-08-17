<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links

<!-- PROJECT LOGO -->
<p align="center">
  <h3 align="center">Jorm</h3>
  <p align="center">
  An Java object-relational mapper
 </p>
  </p>
  <p align="center">
  <br />
  <br />
  <a href="https://github.com/MrRaptorious/JORM"><strong>Explore the docs »</strong></a>
  <br />
  <a href="https://github.com/MrRaptorious/JORM">View Demo</a>
  ·
  <a href="https://github.com/MrRaptorious/JORM/issues">Report Bug</a>
  ·
  <a href="https://github.com/MrRaptorious/JORM/issues">Request Feature</a>
</p>



<!-- TABLE OF CONTENTS -->
## Table of Contents

* [About the Project](#about-the-project)
* [Usage](#usage)
  * [Configure](#configure)
  * [StartUp](#startup)
  * [Getting Data](#getting-data)
  * [Creating Objects](#creating-objects)
* [Roadmap](#roadmap)
* [License](#license)
* [Acknowledgements](#acknowledgements)



<!-- ABOUT THE PROJECT -->
## About The Project
Jorm stands for Java object-relational mapper. So ist a mapper for Relational databases (like SQLite or MySQL) and java objects.
It was created because I had to create software with database access for school and hate writing DAO objects (who likes that?).

<!-- USAGE EXAMPLES -->
## Usage

### Configure
1. All objects that need to be saved in the database must inherit from "PersistentObject"

2. All properies in that should be saved in the database have to have the "Persistent" attribute. You can also only decorate the class with the "Persistent" attribute and exclude properties with the "NonPersistent" attribute. Persistent Properties of List types have to be changed to the "JormList<T extends PersistentObject>" type.

### StartUp
1. You have to create a new Application by calling "JormApplication.getApplication()"

2. You have to Create a new ApplicationSubmanager with a connectionString and a corresponding DependencyConfiguration as arguments.

3. Now you can register types (extending PersistentObject) at the ApplicationSubmanager. Registerd Types will be saved in the database.

4. You can now register your Submanager at the Application and make it available everywhere in your program

5. Call start() on your Application-Object.

### Getting Data
In general data is accessed and handeled by an ObjectSpace.
You can create ObjectSpaces by calling createObjectSpace() on an ApplicationSubmanager-Object.
With methods like "getObjects" you can query your database and receive objects loaded from the database.

### Creating Objects
You can create objects by using the provided ObjecSpace constructor or by calling createObject() on an ObjectSpace-Instance.
If you want wo safe your newly created objects to the database you have to call commit() on the ObjectSpace-Instance. Alternatively you can revert your changes by calling rollback() on the instance.


<!-- ROADMAP -->
## Roadmap

See the [open issues](https://github.com/othneildrew/Best-README-Template/issues) for a list of proposed features (and known issues).


<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.


<!-- ACKNOWLEDGEMENTS -->
## Acknowledgements
* [Choose an Open Source License](https://choosealicense.com)

<!-- MARKDOWN LINKS & IMAGES -->

