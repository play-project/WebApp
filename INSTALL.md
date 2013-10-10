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

PLAY Project WebApp INSTALLATION
================================
Before you install WebApp as described in [README](README.md) here are some system requirements:

Tested on `CentOS release 6.3 (Final)`:

Requirements
------------

### Runtime Requirements:
#### Java (>=1.6)
#### Postgresql (>=8)

	$ yum install postgresql-server postgresql
	$ chkconfig postgresql on

To use a JDBC connection like `db.url=jdbc:postgresql:webappdb` add this to PostgreSQL's `pg_hba.conf`:

	host    all         all         127.0.0.1/32          md5

Then login to PostgreSQL and add the role:

	$ su - postgres -c psql
	postgres=# CREATE ROLE webapp LOGIN;

#### Python (2.6)
	$ yum install python

For CentOS 5.5 it is `python26`.

#### Play! framework (1.2.x)

### Build Requirements:
#### Maven (3.x)
#### Git

	$ yum install git


Issues
------
For issues and bug reporting, please go to https://github.com/play-project/play/issues?labels=web+portal&page=1&state=open
