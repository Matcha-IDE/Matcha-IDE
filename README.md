# Matcha

<!-- Plugin description -->
This tool can help you fill out the Google Play safety section form in a more efficient way, helping you to make sure that your app meets Google Play's requirements.

Matcha is a research project developed by Tianshi Li et al. at Carnegie Mellon University. All the code analysis is conducted on device, and we don't collect any data from your app. For more information, please visit our [website](https://matcha-ide.github.io).

### Key Functionality:
- Automatically detect what data is collected by third-party libraries and your first-party code.
- Generate the Google Play safety section CSV file based on the code analysis results and your input.

<!-- Plugin description end -->

How to get started
------------------

### Prerequisites
For developers who want to install the plugin on their Android Studio, you are recommended to use Android Studio Chipmunk | 2021.2.1 Patch 2 or higher versions.

For developers who want to build and run the plugin from source code, you need to use Java 11 or higher versions.

### How to build and run from source code
Matcha is built using Gradle. To build the plugin, follow IntelliJ's official instructions on [Running a Plugin With the runIde Gradle task﻿](https://plugins.jetbrains.com/docs/intellij/creating-plugin-project.html#running-a-plugin-with-the-runide-gradle-task)

Contributors
------------
* [Tianshi Li](http://tianshili.me) (Carnegie Mellon University)
* Mike Czapik (Carnegie Mellon University)
* Tiffany Yu (Carnegie Mellon University)
* Elijah Neundorfer (Columbus State University)

Cite our paper
--------------
If you find this project helpful to your research, consider citing the following paper:
```
@article{10.1145/3643544,
  author = {Li, Tianshi and Cranor, Lorrie Faith and Agarwal, Yuvraj and Hong, Jason I.},
  title = {Matcha: An IDE Plugin for Creating Accurate Privacy Nutrition Labels},
  year = {2024},
  issue_date = {March 2024},
  publisher = {Association for Computing Machinery},
  address = {New York, NY, USA},
  volume = {8},
  number = {1},
  url = {https://doi.org/10.1145/3643544},
  doi = {10.1145/3643544},
  journal = {Proc. ACM Interact. Mob. Wearable Ubiquitous Technol.},
  month = mar,
  articleno = {33},
  numpages = {38}
}
```
