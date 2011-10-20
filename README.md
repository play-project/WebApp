    ...........................................................................
    ........:$$$7..............................................................
    .....==7$$$I~~...........MMMMMMMMM....DMM..........MM,........MM7......MM..
    ...,?+Z$$$?=~,,:.........MMM,,,?MMM+..MMM.........,MMMM,......7MM,....MMM..
    ..:+?$ZZZ$+==:,:~........MMM.....MMM..MMM.........,MMDMMM:.....,MMI..MMM...
    ..++7ZZZZ?+++====,.......MMM....~MMM..MMM.........,MM??DMMM:....?MM,MMM....
    ..?+OZZZ7~~~~OOI=:.......MMMMMMMMMM...MMM.........,MM?II?MMM~....DMMMM.....
    ..+7OOOZ?+==+7Z$Z:.......MMM$$$I,.....MMM.........,MM??8MMM~......NMM......
    ..:OOOOO==~~~+OZ+........MMM..........MMM.........,MMDMMM~........NMM......
    ..,8OOOO+===+$$?,........MMM..........MMM.,,,,,...,MMMM:..........NMM......
    ,,+8OOOZIIIIII=,,,,,,,,,,MMM,,,,,,,,,,NMMMMMMMMM=,,MM:,,,,........8MM......
    ,,,:O8OO~+~:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
    ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
    ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
                                                      ASCII Art: GlassGiant.com

PLAY Project WebApp
===================
This is the Web frontend for the PLAY Research Project. The PLAY Project will
develop and validate an elastic and reliable architecture for dynamic and
complex, event-driven interaction in large highly distributed and heterogeneous
service systems. Such an architecture will enable ubiquitous exchange of
information between heterogeneous services, providing the possibilities to adapt
and personalize their execution, resulting in the so-called situational-driven
process adaptivity.																 

The WebApp uses the [Play! framework](www.playframework.org) (no affiliated with
the research project of same name). See the installation section on how to get
everything running including Eclipse integration.

Installation
-------------																 
1. Install the Play! framework from www.playframework.org. We tested v. 1.2.x

2. Install the Play! Maven plugin:

        $ play install maven

3. Clone the WebApp e.g. into your Eclipse workspace:

4. Fetch all Maven dependencies for WebApp:

        $ cd WebApp
        $ play mvn:update --with maven

5. Create Eclipse project files (they are not in Git)

        $ play eclipsify .

6. Import the eclipsified project in Eclipse:
File -> Import -> Projects from Git (add the path where WebApp was cloned)

