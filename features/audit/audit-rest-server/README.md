



SessionResource.java


<?xml version="1.0" encoding="UTF-8"?>
<web-app version="3.1" xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
  http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">

    <security-constraint>
        <web-resource-collection>
            <web-resource-name>REST API</web-resource-name>
            <url-pattern>/restapi/*</url-pattern>
        </web-resource-collection>

        <auth-constraint>
            <role-name>*</role-name>
        </auth-constraint>

        <!--<user-data-constraint>-->
            <!--<transport-guarantee>CONFIDENTIAL</transport-guarantee>-->
        <!--</user-data-constraint>-->

    </security-constraint>

    <security-role>
        <role-name>*</role-name>
    </security-role>

    <login-config>
        <auth-method>FORM</auth-method>
        <realm-name>secureDomain</realm-name>
        <form-login-config>
            <form-login-page>/restapi/v1/session/loginForm</form-login-page>
            <form-error-page>/restapi/v1/session/loginError</form-error-page>
        </form-login-config>
    </login-config>

    <session-config>
        <session-timeout>30</session-timeout>
    </session-config>

</web-app>