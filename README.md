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

1. Clone the WebApp e.g. into your Eclipse workspace:

1. Fetch all Maven dependencies for WebApp:

        $ cd WebApp
        $ mvn dependency:copy-dependencies

1. Create Eclipse project files (they are not in Git)

        $ play eclipsify .

1. Create your file `conf/application.conf` from the available `conf/application.conf.dist`
		
1. Import the eclipsified project in Eclipse:
`File -> Import -> Import existing projects` (add the path where WebApp was cloned)

