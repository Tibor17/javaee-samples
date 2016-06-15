* arquillian deployment
http://csierra.github.io/posts/2013/02/10/organizing-deployments-with-arquillian-and-shrinkwrap/
https://issues.jboss.org/browse/SHRINKRES-238?jql=project%20%3D%20SHRINKRES%20AND%20component%20%3D%20maven-plugin
https://github.com/arquillian/arquillian-examples/blob/master/arquillian-deployment-extension-tutorial/integration-tests/src/test/java/org/arquillian/tutorial/extension/deployment/GreeterTest.java
@Deployment(name = "X") see arquillian.xml deployment name
http://arquillian.org/blog/2015/09/02/arquillian-core-1-1-9-Final/
@ArquillianResource URL url; works only with @Test @RunAsClient
http://stackoverflow.com/questions/16971414/how-do-i-inject-a-url-with-arquillianresource
@Test public void shouldBeAbleToInvokeServletInDeployedWebApp(@ArquillianResource URL contextURL) 
http://www.javased.com/index.php?api=org.jboss.arquillian.test.api.ArquillianResource
@OperateOnDeployment
https://developer.jboss.org/thread/198551?tstart=0


* arquillian deployment entire maven project
http://stackoverflow.com/questions/32457712/how-do-i-setup-arquillian-to-test-a-maven-war-project-by-deploying-the-entire-wa

* arquillian tutorial
https://docs.jboss.org/author/display/ARQ/WildFly+8.1.0+-+Embedded
https://docs.jboss.org/author/display/ARQ/Descriptor+deployment
http://blog.progs.be/585/wildfly-integration-arquillian-maven

* arquillian selenium
http://arquillian.org/guides/functional_testing_using_graphene/

* arquillian jpa
http://arquillian.org/guides/testing_java_persistence_de/

* selenium
http://seleniumtwo-by-arun.blogspot.sk/search/label/aaaf.%206.%20Create%20a%20JUnit%20Selenium%20WebDriver%20Test%20using%20Selenium%20IDE
http://www.softwaretestingclass.com/why-selenium-server-not-required-by-selenium-webdriver/
http://www.softwaretestingclass.com/selenium-training-series-getting-started-with-selenium-ide/
http://www.softwaretestingclass.com/difference-between-selenium-ide-rc-webdriver/

* wildfly-quickstart
https://github.com/wildfly/quickstart

* arquillian examples
https://github.com/tolis-e/arquillian-wildfly-example
https://github.com/arquillian/arquillian-examples
https://docs.jboss.org/author/display/ARQ/Container+configuration
http://arquillian.org/guides/getting_started/
https://github.com/arun-gupta/wildfly-samples
https://github.com/wildfly/boms/tree/master/wildfly-javaee7-with-tools
https://github.com/wildfly-swarm/wildfly-swarm-examples/blob/master/vaadin/pom.xml
http://stackoverflow.com/questions/30479366/arquillian-test-running-in-local-server-instead-of-running-in-remote-wildfly-ser

* selenium
https://github.com/javaee-samples/javaee7-samples/blob/master/pom.xml
https://github.com/zanata/zanata-server/blob/master/functional-test/pom.xml
https://github.com/weld/core/blob/master/examples/pom.xml

* wildfly-maven-plugin
https://examples.javacodegeeks.com/enterprise-java/jboss-wildfly/wildfly-maven-plugin-example/
http://blog.arungupta.me/wildfly-maven-plugin-tech-tip-9/
http://stackoverflow.com/questions/24263196/eclipse-maven-what-goal-should-i-execute-to-actually-deploy-to-wildfly-from
http://www.mastertheboss.com/jboss-frameworks/maven-tutorials/jboss-maven/configuring-maven-wildfly-plugin
http://lauraliparulo.altervista.org/jboss-wildfly-maven-plugin-to-deploy-on-localhostremote-server/
https://docs.jboss.org/wildfly/plugins/maven/latest/execute-commands-mojo.html
https://docs.jboss.org/wildfly/plugins/maven/latest/examples/complex-example.html
http://www.simosh.com/article/dbbegafi-creating-security-domain-using-wildfly-maven-plugin-has-no-effect.html

* H2 database Maven plugin in IT
https://github.com/bmatthews68/inmemdb-maven-plugin

* JBoss CLI
https://docs.jboss.org/author/display/WFLY10/CLI+Recipes
http://www.mastertheboss.com/jboss-server/jboss-configuration/configuring-port-offset-on-jboss-as-wildfly
https://docs.jboss.org/author/display/WFLY8/Admin+Guide
https://docs.jboss.org/author/display/WFLY8/Management+Clients
https://docs.jboss.org/author/display/WFLY10/Interfaces+and+ports
http://developer-should-know.com/post/116298482897/adding-startup-parameters-in-jboss-and-wildfly
http://developer-should-know.com/post/109693982502/useful-jboss-and-wildfly-command-line-parameters
https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6/html/Administration_and_Configuration_Guide/sect-Logging_Configuration_in_the_CLI.html
https://docs.jboss.org/author/display/WFLY8/Messaging+configuration
http://www.mastertheboss.com/jboss-server/jboss-jms/how-to-code-a-remote-jms-client-for-wildfly-8

* other examples
http://www.nailedtothex.org/roller/kyle/entry/articles-wildfly-javamail
http://www.nailedtothex.org/roller/kyle/entry/in-container-jms-consumer-producer
http://acdcjunior.github.io/jboss-cli-recipes-commands-for-wildfly-jboss-as-maven-plugins.html

* wildfly-maven-plugin
https://github.com/radcortez/wow-auctions/blob/master/batch/pom.xml
http://www.radcortez.com/tag/wildfly/
