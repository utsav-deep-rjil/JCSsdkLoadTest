# JCSsdkLoadTest
This project depends on the java sdk : JCSsdk1, which can be found at : https://github.com/utsav-deep-rjil/JCSsdk1.
This project runs all operations of JCSsdk1 in multi threaded environment to perform load testing. It takes 0 to 2 command line arguments:
1. no. of threads to create (default value : 100)
2. volume size (to be used in create volume request) (default value : 10)

# before using it:

After cloning the project, put config.properties file inside src/main/resources. The properties file should contain : 
1. BASE_URL
2. ACCESS_KEY
3. SECRET_KEY

