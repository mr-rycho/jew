# jew
Jew (Java log viEW) is a log file viewer. Besides standard functions like tail and highlighting it offers a few cool features like collapsing of stacktraces, quick by-thread filter, superfast scrolling etc.

To build:
mvn clean install assembly:single

To run:
javaw -jar ....jew...-with-dependencies.jar your_log_file.log
