................................................................................
.........:$$$7..................................................................
......==7$$$I~~...........MMMMMMMMM....DMM..........MM,........MM7......MM......
....,?+Z$$$?=~,,:.........MMM,,,?MMM+..MMM.........,MMMM,......7MM,....MMM......
...:+?$ZZZ$+==:,:~........MMM.....MMM..MMM.........,MMDMMM:.....,MMI..MMM.......
...++7ZZZZ?+++====,.......MMM....~MMM..MMM.........,MM??DMMM:....?MM,MMM........
...?+OZZZ7~~~~OOI=:.......MMMMMMMMMM...MMM.........,MM?II?MMM~....DMMMM.........
...+7OOOZ?+==+7Z$Z:.......MMM$$$I,.....MMM.........,MM??8MMM~......NMM..........
...:OOOOO==~~~+OZ+........MMM..........MMM.........,MMDMMM~........NMM..........
...,8OOOO+===+$$?,........MMM..........MMM.,,,,,...,MMMM:..........NMM..........
,,,+8OOOZIIIIII=,,,,,,,,,,MMM,,,,,,,,,,NMMMMMMMMM=,,MM:,,,,........8MM..........
,,,,:O8OO~+~:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
                                                                 GlassGiant.com

PLAY Project WebApp
===================
This is the Web frontend for the PLAY Research Project. The PLAY Project will
develop and validate an elastic and reliable architecture for dynamic and
complex, event-driven interaction in large highly distributed and heterogeneous
service systems. Such an architecture will enable ubiquitous exchange of
information between heterogeneous services, providing the possibilities to adapt
and personalize their execution, resulting in the so-called situational-driven
process adaptivity.																 

The WebApp uses the [Play framework](www.playframework.org) (no affiliated with
the research project of same name). See the installation section on how to get
everything running including Eclipse integration.

Installation
-------------																 
install playframework (1.2.3)
play install maven

cd WebApp
play mvn:update --with maven

play eclipsify .

Eclipse: File -> Import -> Projects from Git (add the path where WebApp was cloned)

