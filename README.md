# JCSsdkTest
This project depends on the java sdk : jcs-java-sdk-sbs, which can be found at : https://github.com/utsav-deep-rjil/jcs-sdk.
This project runs all operations of jcs-java-sdk-sbs in multi threaded environment to perform load testing. It takes 0 to 2 command line arguments:
1. no. of threads to create (default value : 100)
2. volume size (to be used in create volume request) (default value : 10)

# before using it:

After cloning the project, put config.properties file inside src/main/resources. The properties file should contain : 
1. BASE_URL
2. ACCESS_KEY
3. SECRET_KEY


